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
            return KBigDecimalFactory.ZERO
        }

        var x = value
        var previous: KBigDecimal
        val two = KBigDecimalFactory.fromInt(2)

        do {
            previous = x
            x = x.add(value.divide(x, scale + 2, 4)).divide(two, scale + 2, 4)
        } while (x.subtract(previous).abs().compareTo(KBigDecimalFactory.fromString("1E-${scale + 1}")) > 0)

        val result = x.setScale(scale, 4)
        // Remove trailing zeros for cleaner output
        val resultStr = result.toString()
        val cleanStr =
            if (resultStr.contains('.')) {
                resultStr.trimEnd('0').trimEnd('.')
            } else {
                resultStr
            }
        return if (cleanStr.isEmpty() || cleanStr == "-") {
            KBigDecimalFactory.ZERO
        } else {
            KBigDecimalFactory.fromString(cleanStr)
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

        var result = KBigIntegerFactory.ONE
        var current = KBigIntegerFactory.ONE
        val target = n

        while (current.compareTo(target) <= 0) {
            result = result.multiply(current)
            current = current.add(KBigIntegerFactory.ONE)
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
        var x = a.abs()
        var y = b.abs()

        while (!y.isZero()) {
            val temp = y
            y = x.mod(y)
            x = temp
        }

        return x
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
            return KBigIntegerFactory.ZERO
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
        if (n.compareTo(KBigIntegerFactory.fromInt(2)) < 0) {
            return false
        }

        if (n.compareTo(KBigIntegerFactory.fromInt(2)) == 0) {
            return true
        }

        if (n.mod(KBigIntegerFactory.fromInt(2)).isZero()) {
            return false
        }

        var i = KBigIntegerFactory.fromInt(3)
        val sqrt = sqrt(n.toPreciseNumber(), 0).toBigInteger()

        while (i.compareTo(sqrt) <= 0) {
            if (n.mod(i).isZero()) {
                return false
            }
            i = i.add(KBigIntegerFactory.fromInt(2))
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
            return KBigIntegerFactory.ONE
        }

        var result = KBigIntegerFactory.ONE
        var b = base
        var exp = exponent

        while (!exp.isZero()) {
            if (exp.mod(KBigIntegerFactory.fromInt(2)).compareTo(KBigIntegerFactory.ONE) == 0) {
                result = result.multiply(b)
            }
            b = b.multiply(b)
            exp = exp.divide(KBigIntegerFactory.fromInt(2))
        }

        return result
    }

    // abs(), max(), min() functions are available as methods on the objects themselves
    // Use value.abs(), a.max(b), a.min(b) instead of KBigMath.abs(value), KBigMath.max(a, b), KBigMath.min(a, b)
}
