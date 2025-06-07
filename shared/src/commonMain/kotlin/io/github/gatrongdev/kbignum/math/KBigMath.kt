package io.github.gatrongdev.kbignum.math

/**
 * Mathematical utility functions for KBigDecimal and KBigInteger
 */
object KBigMath {
    /**
     * Calculate square root of a KBigDecimal using Newton's method
     */
    fun sqrt(
        value: KBigDecimal,
        scale: Int = 10,
    ): KBigDecimal {
        if (value.signum() < 0) {
            throw IllegalArgumentException("Cannot calculate square root of negative number")
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

        return x.setScale(scale, 4)
    }

    /**
     * Calculate factorial of a KBigInteger
     */
    fun factorial(n: KBigInteger): KBigInteger {
        if (n.signum() < 0) {
            throw IllegalArgumentException("Factorial is not defined for negative numbers")
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
     * Calculate greatest common divisor (GCD) of two KBigIntegers
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
     * Calculate least common multiple (LCM) of two KBigIntegers
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
     * Check if a KBigInteger is prime (basic implementation)
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
     * Calculate power with KBigInteger base and exponent
     */
    fun pow(
        base: KBigInteger,
        exponent: KBigInteger,
    ): KBigInteger {
        if (exponent.signum() < 0) {
            throw IllegalArgumentException("Negative exponent not supported for integer power")
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

    /**
     * Calculate absolute value
     */
    fun abs(value: KBigDecimal): KBigDecimal = value.abs()

    fun abs(value: KBigInteger): KBigInteger = value.abs()

    /**
     * Calculate maximum of two values
     */
    fun max(
        a: KBigDecimal,
        b: KBigDecimal,
    ): KBigDecimal = a.max(b)

    fun max(
        a: KBigInteger,
        b: KBigInteger,
    ): KBigInteger = a.max(b)

    /**
     * Calculate minimum of two values
     */
    fun min(
        a: KBigDecimal,
        b: KBigDecimal,
    ): KBigDecimal = a.min(b)

    fun min(
        a: KBigInteger,
        b: KBigInteger,
    ): KBigInteger = a.min(b)
}
