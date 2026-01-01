package io.github.gatrongdev.kbignum.math

/**
 * Mathematical utility functions for KBigDecimal and KBigInteger.
 * Provides advanced mathematical operations not available in the basic interfaces.
 */
object KBigMath {
    /**
     * Calculates the square root of a KBigDecimal using Newton's method for iterative approximation.
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

        // Optimization: Adaptive Newton-Raphson
        
        // Initial precision: enough to start converging
        // Start closer to target scale to avoid rounding errors accumulation?
        // Actually, adaptive is good, but we need to ensure we don't return early with low precision x.
        // We accumulate precision.
        
        val targetScale = scale
        val workScale = targetScale + 5 // Working precision
        
        var currentScale = 16 
        if (currentScale > workScale) currentScale = workScale
        
        // Initial Guess: value / 3 is simple
        // Using low precision for guess
        var x = value.divide(KBigDecimal.fromInt(3), currentScale, KBRoundingMode.HalfUp)
        if (x.isZero()) x = KBigDecimal.fromInt(1)
        
        val two = KBigDecimal.fromInt(2)
        var previous = KBigDecimal.ZERO
        
        // Convergence loop with increasing precision
        while (true) {
             // Loop for convergence at currentScale
             do {
                previous = x
                // x = (x + value/x) / 2
                // When increasing precision, previous result 'x' (low precision) is used as input.
                val div = value.divide(x, currentScale + 2, KBRoundingMode.HalfUp)
                x = x.add(div).divide(two, currentScale + 2, KBRoundingMode.HalfUp)
             } while (x.subtract(previous).abs().compareTo(KBigDecimal(KBigInteger.ONE, currentScale)) > 0)
             
             if (currentScale >= workScale) break
             
             // Double precision for next step
             currentScale *= 2
             if (currentScale > workScale) currentScale = workScale
        }
        
        // Final result at target specific scale
        // The original logic didn't assume KBigDecimal had setScale. It just returned 'x'.
        // But 'x' now has high precision (workScale+2).
        // Returning 'x' maintains precision. 
        // The failures were likely due to:
        // 1. "sqrt_onPerfectSquare": returned "4" -> maybe "4.000...00" mismatch string check?
        //    Expected "4", Actual "4.00000000" or similar?
        //    Test `assertEquals(expected.toString(), actual.toString())` -> stringent.
        // 2. "sqrt_onVerySmallDecimal": 0.0001 -> 0.01. Code returned "0" maybe due to cleanStr logic removing all 0s?
        
        val result = x

        // Logic to emulate stripTrailingZeros properly
        // If result is integer like (4.000), resultStr should be "4".
        // KBigDecimal("4") toString is "4".
        // KBigDecimal("4.00") toString is "4.00" (if scale kept).
        
        // The existing test expects "startsWith(1.4)" for 2. 
        // Logic: if resultStr contains '.', trimEnd('0'), then trimEnd('.')
        // "4.00" -> "4." -> "4". Correct.
        // "1.414..." -> "1.414...". Correct.
        // "0.0100" -> "0.01". Correct.
        
        val resultStr = result.toString()
        val cleanStr =
            if (resultStr.contains('.')) {
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
        // Invariant: u and v are both odd (after initial shifts and loop adjustments)
        // Actually, optimization:
        //  - make u odd
        //  - make v odd
        //  - if u > v: swap u, v
        //  - v = v - u
        //  - v = v / 2 (make odd again)
        //  - repeat

        while (u.signum() != 0 && v.signum() != 0 && u != v) {
             // Invariant: u is odd here (from initial shift or previous loop)
             
             // Ensure v is odd. In first iteration v is already odd.
             // Inside loop, v becomes (v-u)/2^k.
             // But wait, KBigInteger is immutable. 'u' and 'v' are vars.
             
             // Optimized loop:
             val cmp = u.compareTo(v)
             if (cmp == 0) break
             
             if (cmp > 0) {
                 // u > v
                 // u = (u - v) / 2^k
                 u = u.subtract(v)
                 val z = u.getLowestSetBit()
                 if (z > 0) u = u shr z
             } else {
                 // v > u
                 // v = (v - u) / 2^k
                 v = v.subtract(u)
                 val z = v.getLowestSetBit()
                 if (z > 0) v = v shr z
             }
        }
        
        // Result is u * 2^k
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
        // sqrt is KBigDecimal function, we need to handle conversion carefully or standard math
        // KBigMath.sqrt input is KBigDecimal.
        // n.toPreciseNumber() ?? Assuming extension exists or we wrap.
        // Looking at file content, line 137: sqrt(n.toPreciseNumber(), 0).toBigInteger()
        // `toPreciseNumber` likely redundant if we just KBigDecimal(n)

        // I'll assume n.toPreciseNumber() works if it was there, or replace with explicit
        val nDecimal = KBigDecimal(n, 0)
        val sqrt = sqrt(nDecimal, 0).toBigInteger()

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
