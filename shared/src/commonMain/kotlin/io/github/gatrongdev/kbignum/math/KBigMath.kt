package io.github.gatrongdev.kbignum.math

/**
 * Mathematical utility functions for KBigDecimal and KBigInteger.
 * Provides advanced mathematical operations not available in the basic interfaces.
 */
object KBigMath {
    /**
     * Calculates the square root of a KBigDecimal using Newton-Raphson iteration.
     * The initial estimate is derived from the decimal exponent so inputs outside
     * the `Double` range remain in the right order of magnitude.
     *
     * @param value The KBigDecimal to calculate the square root of
     * @param scale The number of decimal places in the result (default: 10)
     * @return The square root of the input value with the specified precision
     * @throws ArithmeticException if the value is negative or the iteration does not converge
     * @throws IllegalArgumentException if [scale] is negative or too large
     */
    fun sqrt(
        value: KBigDecimal,
        scale: Int = 10,
    ): KBigDecimal {
        if (value.signum() < 0) {
            throw ArithmeticException("Cannot calculate square root of negative number")
        }
        require(scale >= 0) { "Scale must be non-negative" }

        if (value.isZero()) {
            return KBigDecimal.ZERO
        }

        // Derive a scale-aware seed from the decimal exponent. A Double-based seed
        // saturates for values outside Double's range and can be hundreds of orders
        // of magnitude away from the true root.
        val digits = value.unscaledValue.toString().removePrefix("-").length.toLong()
        val decimalExponent = digits - 1 - value.scale.toLong()
        val rootExponent =
            if (decimalExponent >= 0) {
                decimalExponent / 2
            } else {
                (decimalExponent - 1) / 2
            }

        val requiredScale =
            (if (rootExponent < 0) -rootExponent else 0L) + scale.toLong() + 4L
        require(requiredScale <= Int.MAX_VALUE) { "Requested scale is too large" }
        val workingScale = requiredScale.toInt()

        var x =
            if (rootExponent >= 0) {
                KBigDecimal(KBigInteger.TEN.pow(rootExponent.toInt()), 0)
            } else {
                KBigDecimal(KBigInteger.ONE, (-rootExponent).toInt())
            }
        if (decimalExponent % 2L != 0L) {
            x = x.multiply(KBigDecimal.fromInt(3))
        }

        val two = KBigDecimal.fromInt(2)
        var iterations = 0
        var converged = false
        while (iterations < 100) {
            val term = value.divide(x, workingScale, KBRoundingMode.HalfUp)
            val next = x.add(term).divide(two, workingScale, KBRoundingMode.HalfUp)
            if (next == x) {
                converged = true
                break
            }
            x = next
            iterations++
        }
        if (!converged) throw ArithmeticException("Square root did not converge")

        val result = x.divide(KBigDecimal.ONE, scale, KBRoundingMode.HalfUp)

        // Strip trailing zeros for cleaner output (e.g., "4" instead of "4.0000000000")
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
