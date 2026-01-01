package io.github.gatrongdev.kbignum.math

import kotlin.math.pow

/**
 * Mathematical utility functions for KBigDecimal and KBigInteger.
 * Provides advanced mathematical operations not available in the basic interfaces.
 */
object KBigMath {

    /**
     * Calculates the square root of a KBigDecimal using Adaptive Newton-Raphson method.
     *
     * Optimization Strategy:
     * 1. Smart Initial Guess: Uses Double approximation to start with ~15 correct digits immediately.
     * 2. Adaptive Precision: Starts with low precision and doubles it in each Newton step.
     *    Doubling precision matches the quadratic convergence of Newton's method.
     *    This avoids expensive high-precision division in early steps.
     *
     * @param value The KBigDecimal to calculate the square root of
     * @param scale The number of decimal places in the result (default: 10)
     * @return The square root of the input value with the specified precision
     * @throws IllegalArgumentException if the value is negative
     */
    fun sqrt(
        value: KBigDecimal,
        scale: Int = 10,
    ): KBigDecimal {
        if (value.signum() < 0) {
            throw ArithmeticException("Cannot calculate square root of negative number")
        }

        if (value.isZero()) {
            return KBigDecimal.ZERO
        }

        // Target scale is the requested scale.
        // We add a small buffer (+4) to ensure the last digit is rounded correctly.
        val targetScale = scale
        val workingScale = targetScale + 4

        // --- 1. Smart Initial Guess using Double ---
        // Fast path for numbers that fit in Double standard range
        // Or extract approximate Double magnitude for super huge numbers.

        val doubleVal = value.toDoubleApproximation()
        val initialGuessDouble = kotlin.math.sqrt(doubleVal)

        // If the result fits in double reasonably (finite and not too large for simple string parse), use it
        var x = try {
             if (initialGuessDouble.isFinite() && !initialGuessDouble.isNaN()
                 && initialGuessDouble > 1e-300 && initialGuessDouble < 1e300) {
                 // Use simple string conversion only when the double is in a "normal" range
                 // that doesn't produce scientific notation or overly long decimals.
                 doubleToKBigDecimal(initialGuessDouble)
             } else {
                 // Fallback: use value / 3 as a rough starting approximation
                 value.divide(KBigDecimal.fromInt(3), 18, KBRoundingMode.HalfUp)
             }
        } catch (e: Exception) {
             // Safety fallback
             value.divide(KBigDecimal.fromInt(3), 18, KBRoundingMode.HalfUp)
        }

        // Ensure x is not zero (would cause division by zero in Newton step)
        if (x.isZero()) {
            x = KBigDecimal.ONE
        }

        // Initial precision: Double gives matches up to ~15-17 digits.
        // We can start our adaptive loop at precision ~18 to be safe or just start doubling from there.
        var currentScale = 18

        // Align x to currentScale
        // x = x.setScale(currentScale) - simulated
        // We don't have setScale cleanly exposed as mutating, so we just use it in the first op.

        val two = KBigDecimal.fromInt(2)

        // --- 2. Adaptive Precision Newton-Raphson ---
        // Iterate while current precision < required precision

        while (currentScale < workingScale) {
            // Next precision target: Double the current precision
            // Newton method roughly doubles correct digits per iteration.
            currentScale *= 2

            // Cap at working scale
            if (currentScale > workingScale) {
                currentScale = workingScale
            }

            // One Newton Step: x = (x + value/x) / 2
            // Perform division at 'currentScale' precision.
            // Using HalfUp rounding is standard.

            // term = value / x
            val term = value.divide(x, currentScale, KBRoundingMode.HalfUp)

            // x = (x + term) / 2
            x = x.add(term).divide(two, currentScale, KBRoundingMode.HalfUp)
        }

        // --- 3. Final Polish ---
        // One last step at full working precision to ensure stability and correct rounding at the edge.
        // (The loop ends when currentScale == workingScale, so the last op was already at workingScale)
        // Check if we need one more verify? Usually not if we doubled carefully.
        // But to be absolutely robust against the last bit of jitter, we can do one final check or step.
        // For performance, we trust the quadratic convergence. With 15 digits start,
        // 1 step -> 30 digits, 2 steps -> 60, 3 -> 120... 7 steps -> 1920 digits.
        // Very fast.

        // Return result scaled to targetScale with trailing zeros stripped
        val result = x.divide(KBigDecimal.ONE, targetScale, KBRoundingMode.HalfUp)

        // Strip trailing zeros for cleaner output (e.g., "4" instead of "4.0000000000")
        val resultStr = result.toString()
        val cleanStr = if (resultStr.contains('.')) {
            val trimmed = resultStr.trimEnd('0')
            if (trimmed.endsWith('.')) trimmed.dropLast(1) else trimmed
        } else {
            resultStr
        }

        return if (cleanStr.isEmpty() || cleanStr == "-") {
            KBigDecimal.ZERO
        } else {
            KBigDecimal.fromString(cleanStr)
        }
    }

    /**
     * Safely converts a Double to KBigDecimal.
     * Manually parses Double.toString() to handle scientific notation without Java formatters.
     */
    private fun doubleToKBigDecimal(d: Double): KBigDecimal {
        if (d == 0.0) return KBigDecimal.ZERO
        val s = d.toString()
        
        // Check for scientific notation (E or e)
        val eIndex = s.indexOf('E').takeIf { it >= 0 } ?: s.indexOf('e')
        if (eIndex == -1) {
            return KBigDecimal.fromString(s)
        }
        
        // Parse scientific notation: Mantissa * 10^Exponent
        val mantissaStr = s.substring(0, eIndex)
        val exponentStr = s.substring(eIndex + 1)
        
        val mantissa = KBigDecimal.fromString(mantissaStr)
        val exponent = exponentStr.toInt() 
        
        // Result = unscaled * 10^(-scale) * 10^exponent
        //        = unscaled * 10^-(scale - exponent)
        val rawScale = mantissa.scale - exponent
        
        return if (rawScale >= 0) {
            KBigDecimal(mantissa.unscaledValue, rawScale)
        } else {
            // Negative scale means the number is an integer with trailing zeros.
            // Normalize to scale 0 by multiplying unscaled value by 10^(-rawScale).
            val shift = -rawScale
            // KBigInteger.TEN is 10. pow(shift) calculates 10^shift.
            val factor = KBigInteger.TEN.pow(shift)
            KBigDecimal(mantissa.unscaledValue.multiply(factor), 0)
        }
    }

    /**
     * Helper to convert KBigDecimal to Double for estimation.
     * Doesn't need to be perfect, just standard Double precision.
     */
    private fun KBigDecimal.toDoubleApproximation(): Double {
        try {
            // For most numbers, string conversion is simplest and most accurate
            // It's O(n^2) for huge numbers but acceptable for initial guess purpose
            val str = this.toString()

            // Try direct parsing first
            val directParse = str.toDoubleOrNull()
            if (directParse != null && directParse.isFinite() && directParse > 0.0) {
                return directParse
            }

            // For very large or very small numbers that overflow/underflow Double,
            // use magnitude-based estimation
            val unscaledLen = this.unscaledValue.magnitude.size
            if (unscaledLen == 0) return 0.0

            // value = unscaledValue * 10^(-scale)
            // log10(value) = log10(unscaledValue) - scale
            // log10(unscaledValue) ≈ bitLength * log10(2)

            val bitLen = this.unscaledValue.bitLength()
            val log10Unscaled = bitLen * 0.30102999566 // log10(2)
            val log10Value = log10Unscaled - this.scale

            // value ≈ 10^log10Value
            // We want a rough mantissa, so extract leading bits
            val mag = this.unscaledValue.magnitude
            val topWord = mag[unscaledLen - 1].toLong() and 0xFFFFFFFFL
            val secondWord = if (unscaledLen > 1) mag[unscaledLen - 2].toLong() and 0xFFFFFFFFL else 0L

            // Normalize to get a mantissa in range [1, 10)
            val combinedBits = (topWord shl 32) or secondWord
            val leadingZeros = countLeadingZerosLong(combinedBits)
            val normalizedBits = if (leadingZeros < 64) combinedBits shl leadingZeros else combinedBits

            // Convert to a mantissa roughly in [1, 2)
            val mantissa = normalizedBits.toDouble() / Long.MAX_VALUE.toDouble() + 1.0

            // Result = mantissa * 10^(log10Value)
            // But we need to be careful about overflow
            return if (log10Value > 308) {
                Double.MAX_VALUE // Would overflow
            } else if (log10Value < -308) {
                Double.MIN_VALUE // Would underflow
            } else {
                mantissa * (10.0).pow(log10Value)
            }

        } catch (e: Exception) {
            return 1.0 // Safety fallback
        }
    }

    // Internal helper to get top 63 bits signed (Long) from KBigInteger for estimation
    private fun KBigInteger.toLongHighBits(): Long {
        if (this.signum == 0) return 0L
        val mag = this.magnitude
        val len = mag.size
        if (len == 0) return 0L

        val top = mag[len - 1].toLong() and 0xFFFFFFFFL
        // Kotlin stdlib countLeadingZeroBits is experimental/1.4?
        // Let's use manual check or simple shift.

        // Actually, assuming we have at least 1 word.
        // We want to normalize to ~63 bits.
        // If len=1: return mag[0]
        if (len == 1) return top

        // If len >= 2: combine top two words?
        val second = mag[len - 2].toLong() and 0xFFFFFFFFL

        // Combine: (top << 32) | second
        // But we want the MOST SIGNIFICANT bits, not just the top words aligned to Long.
        // We want '1.xxxxx' type mantissa or large integer?
        // Let's just return the top 64 bits as a Long (ignoring that it represents a much larger number).
        // This 'Long' is just the mantissa M. The total value is M * 2^K.

        // If top word uses K bits, we take top word and (63-K) bits of next word.
        // But to simplify Logic:
        // Just take (top << 32) | second.
        // This gives us the top 64 bits of the number (roughly).
        // The real magnitude is adjusted by 2^( (len-2)*32 ).
        // Yes.

        return (top shl 32) or second
    }

    // Quick bitLength estimation
    private fun KBigInteger.bitLength(): Long {
        if (this.magnitude.isEmpty()) return 0
        val lastIdx = this.magnitude.size - 1
        val last = this.magnitude[lastIdx]
        // 32 - leadingZeros
        val wBits = 32 - countLeadingZeros(last)
        return (lastIdx * 32L) + wBits
    }

    private fun countLeadingZeros(i: Int): Int {
        // Simple manual implementation if needed, or Java Integer.numberOfLeadingZeros
        // Since we are in commonMain (KMP), we can't use Java Integer easily without expect/actual.
        // But this file seems to be used in AndroidUnitTest which is JVM.
        // Wait, package is commonMain. We must be pure Kotlin.

        if (i == 0) return 32
        var n = 1
        var x = i
        if (x ushr 16 == 0) { n += 16; x = x shl 16 }
        if (x ushr 24 == 0) { n += 8; x = x shl 8 }
        if (x ushr 28 == 0) { n += 4; x = x shl 4 }
        if (x ushr 30 == 0) { n += 2; x = x shl 2 }
        n -= x ushr 31
        return n
    }

    private fun countLeadingZerosLong(l: Long): Int {
        if (l == 0L) return 64
        val high = (l ushr 32).toInt()
        return if (high != 0) {
            countLeadingZeros(high)
        } else {
            32 + countLeadingZeros(l.toInt())
        }
    }

    /**
     * Calculates the factorial of a KBigInteger (n!).
     * The factorial of n is the product of all positive integers less than or equal to n.
     * @param n The KBigInteger to calculate the factorial of
     * @return The factorial of n (n!)
     * @throws IllegalArgumentException if n is negative
     */
    fun factorial(n: KBigInteger): KBigInteger {
        if (n.signum() < 0) {
            throw ArithmeticException("Factorial is not defined for negative numbers")
        }

        var result = KBigInteger.ONE
        var current = KBigInteger.ONE
        val target = n

        while (current.compareTo(target) <= 0) {
            result = result.multiply(current)
            current = current.add(KBigInteger.ONE)
        }

        return result
    }

    /**
     * Calculates the greatest common divisor (GCD) of two KBigIntegers using Euclid's algorithm.
     * The GCD is the largest positive integer that divides both numbers without remainder.
     * @param a The first KBigInteger
     * @param b The second KBigInteger
     * @return The greatest common divisor of a and b
     */
    fun gcd(
        a: KBigInteger,
        b: KBigInteger,
    ): KBigInteger {
        var u = a.abs()
        var v = b.abs()

        if (u.isZero()) return v
        if (v.isZero()) return u

        // Stein's Algorithm (Binary GCD)
        // 1. GCD(0, v) = v, GCD(u, 0) = u (handled above)
        // 2. GCD(2u, 2v) = 2 * GCD(u, v)
        // 3. GCD(2u, v) = GCD(u, v) if v is odd
        // 4. GCD(u, v) = GCD(u, v - u) if u, v odd and u <= v

        val uZeros = u.getLowestSetBit()
        if (uZeros > 0) u = u shr uZeros

        val vZeros = v.getLowestSetBit()
        if (vZeros > 0) v = v shr vZeros

        val commonZeros = minOf(uZeros, vZeros)

        // Loop while u != v
        while (u.signum() != 0 && v.signum() != 0 && u != v) {
             val cmp = u.compareTo(v)
             if (cmp == 0) break

             if (cmp > 0) {
                 // u > v
                 u = u.subtract(v)
                 val z = u.getLowestSetBit()
                 if (z > 0) u = u shr z
             } else {
                 // v > u
                 v = v.subtract(u)
                 val z = v.getLowestSetBit()
                 if (z > 0) v = v shr z
             }
        }

        return if (commonZeros > 0) u shl commonZeros else u
    }

    /**
     * Calculates the least common multiple (LCM) of two KBigIntegers.
     * The LCM is the smallest positive integer that is divisible by both numbers.
     * @param a The first KBigInteger
     * @param b The second KBigInteger
     * @return The least common multiple of a and b, or zero if either input is zero
     */
    fun lcm(
        a: KBigInteger,
        b: KBigInteger,
    ): KBigInteger {
        if (a.isZero() || b.isZero()) {
            return KBigInteger.ZERO
        }

        return a.abs().multiply(b.abs()).divide(gcd(a, b))
    }

    /**
     * Checks if a KBigInteger is a prime number using trial division.
     * A prime number is a natural number greater than 1 that has no positive divisors other than 1 and itself.
     * @param n The KBigInteger to test for primality
     * @return true if n is prime, false otherwise
     */
    fun isPrime(n: KBigInteger): Boolean {
        if (n.compareTo(KBigInteger.fromInt(2)) < 0) {
            return false
        }

        if (n.compareTo(KBigInteger.fromInt(2)) == 0) {
            return true
        }

        if (n.mod(KBigInteger.fromInt(2)).isZero()) {
            return false
        }

        var i = KBigInteger.fromInt(3)
        // Optimization: Use the new sqrt function for limit
        // n is KBigInteger. convert to KBigDecimal for sqrt
        val nDecimal = KBigDecimal(n, 0)

        // We only really need integer sqrt.
        // sqrt(n)
        val sqrtVal = sqrt(nDecimal, 0)
        val sqrt = sqrtVal.toBigInteger()

        while (i.compareTo(sqrt) <= 0) {
            if (n.mod(i).isZero()) {
                return false
            }
            i = i.add(KBigInteger.fromInt(2))
        }

        return true
    }

    /**
     * Calculates the power of a KBigInteger raised to another KBigInteger exponent using binary exponentiation.
     * This method efficiently computes base^exponent.
     * @param base The base KBigInteger
     * @param exponent The exponent KBigInteger (must be non-negative)
     * @return The result of base raised to the power of exponent
     * @throws IllegalArgumentException if the exponent is negative
     */
    fun pow(
        base: KBigInteger,
        exponent: KBigInteger,
    ): KBigInteger {
        if (exponent.signum() < 0) {
            throw ArithmeticException("Negative exponent not supported for integer power")
        }

        if (exponent.isZero()) {
            return KBigInteger.ONE
        }

        var result = KBigInteger.ONE
        var b = base
        var exp = exponent

        while (!exp.isZero()) {
            if (exp.testBit(0)) {
                result = result.multiply(b)
            }
            b = b.multiply(b)
            exp = exp shr 1
        }

        return result
    }
}
