package io.github.gatrongdev.kbignum.math

// Extension functions for converting common types to KBigDecimal

/**
 * Converts a String to a KBigDecimal.
 * @receiver The String representation of a decimal number
 * @return A KBigDecimal representing the value of this string
 * @throws NumberFormatException if the string is not a valid decimal representation
 */
fun String.toKBigDecimal(): KBigDecimal = KBigDecimalFactory.fromString(this)

/**
 * Converts an Int to a KBigDecimal.
 * @receiver The Int value to convert
 * @return A KBigDecimal representing this integer value
 */
fun Int.toKBigDecimal(): KBigDecimal = KBigDecimalFactory.fromInt(this)

/**
 * Converts a Long to a KBigDecimal.
 * @receiver The Long value to convert
 * @return A KBigDecimal representing this long value
 */
fun Long.toKBigDecimal(): KBigDecimal = KBigDecimalFactory.fromLong(this)

/**
 * Converts a Double to a KBigDecimal.
 * @receiver The Double value to convert
 * @return A KBigDecimal representing this double value
 */
fun Double.toKBigDecimal(): KBigDecimal = KBigDecimalFactory.fromString(this.toString())

/**
 * Converts a Float to a KBigDecimal.
 * @receiver The Float value to convert
 * @return A KBigDecimal representing this float value
 */
fun Float.toKBigDecimal(): KBigDecimal = KBigDecimalFactory.fromString(this.toString())

// Extension functions for converting common types to KBigInteger

/**
 * Converts a String to a KBigInteger.
 * @receiver The String representation of an integer
 * @return A KBigInteger representing the value of this string
 * @throws NumberFormatException if the string is not a valid integer representation
 */
fun String.toKBigInteger(): KBigInteger = KBigIntegerFactory.fromString(this)

/**
 * Converts an Int to a KBigInteger.
 * @receiver The Int value to convert
 * @return A KBigInteger representing this integer value
 */
fun Int.toKBigInteger(): KBigInteger = KBigIntegerFactory.fromInt(this)

/**
 * Converts a Long to a KBigInteger.
 * @receiver The Long value to convert
 * @return A KBigInteger representing this long value
 */
fun Long.toKBigInteger(): KBigInteger = KBigIntegerFactory.fromLong(this)

// Operator overloads for KBigDecimal

/**
 * Addition operator for KBigDecimal.
 * @receiver The first operand
 * @param other The second operand to add
 * @return The sum of this and other
 */
operator fun KBigDecimal.plus(other: KBigDecimal): KBigDecimal = this.add(other)

/**
 * Subtraction operator for KBigDecimal.
 * @receiver The minuend
 * @param other The subtrahend to subtract
 * @return The difference of this minus other
 */
operator fun KBigDecimal.minus(other: KBigDecimal): KBigDecimal = this.subtract(other)

/**
 * Multiplication operator for KBigDecimal.
 * @receiver The first factor
 * @param other The second factor to multiply
 * @return The product of this and other
 */
operator fun KBigDecimal.times(other: KBigDecimal): KBigDecimal = this.multiply(other)

/**
 * Division operator for KBigDecimal with default scale and rounding.
 * @receiver The dividend
 * @param other The divisor
 * @return The quotient with scale 10 and HALF_UP rounding
 * @throws ArithmeticException if other is zero
 */
operator fun KBigDecimal.div(other: KBigDecimal): KBigDecimal = this.divide(other, 10, RoundingMode.HALF_UP)


/**
 * Unary minus operator for KBigDecimal.
 * @receiver The KBigDecimal to negate
 * @return The negation of this number
 */
operator fun KBigDecimal.unaryMinus(): KBigDecimal = KBigDecimalFactory.ZERO.subtract(this)

/**
 * Unary plus operator for KBigDecimal (identity operation).
 * @receiver The KBigDecimal
 * @return This number unchanged
 */
operator fun KBigDecimal.unaryPlus(): KBigDecimal = this

// Operator overloads for KBigInteger

/**
 * Addition operator for KBigInteger.
 * @receiver The first operand
 * @param other The second operand to add
 * @return The sum of this and other
 */
operator fun KBigInteger.plus(other: KBigInteger): KBigInteger = this.add(other)

/**
 * Subtraction operator for KBigInteger.
 * @receiver The minuend
 * @param other The subtrahend to subtract
 * @return The difference of this minus other
 */
operator fun KBigInteger.minus(other: KBigInteger): KBigInteger = this.subtract(other)

/**
 * Multiplication operator for KBigInteger.
 * @receiver The first factor
 * @param other The second factor to multiply
 * @return The product of this and other
 */
operator fun KBigInteger.times(other: KBigInteger): KBigInteger = this.multiply(other)

/**
 * Division operator for KBigInteger.
 * @receiver The dividend
 * @param other The divisor
 * @return The quotient of this divided by other
 * @throws ArithmeticException if other is zero
 */
operator fun KBigInteger.div(other: KBigInteger): KBigInteger = this.divide(other)

/**
 * Remainder operator for KBigInteger.
 * @receiver The dividend
 * @param other The divisor
 * @return The remainder of this divided by other
 * @throws ArithmeticException if other is zero
 */
operator fun KBigInteger.rem(other: KBigInteger): KBigInteger = this.mod(other)

/**
 * Unary minus operator for KBigInteger.
 * @receiver The KBigInteger to negate
 * @return The negation of this number
 */
operator fun KBigInteger.unaryMinus(): KBigInteger = KBigIntegerFactory.ZERO.subtract(this)

/**
 * Unary plus operator for KBigInteger (identity operation).
 * @receiver The KBigInteger
 * @return This number unchanged
 */
operator fun KBigInteger.unaryPlus(): KBigInteger = this

// Comparison operators are inherited from Comparable interface and implemented in the interfaces

// Math functions for KBigDecimal

/**
 * Calculates this KBigDecimal raised to the power of the specified integer exponent.
 * @receiver The base KBigDecimal
 * @param exponent The integer exponent (must be non-negative)
 * @return This KBigDecimal raised to the power of exponent
 * @throws IllegalArgumentException if the exponent is negative
 */
fun KBigDecimal.pow(exponent: Int): KBigDecimal {
    var result = KBigDecimalFactory.ONE
    var base = this
    var exp = exponent

    if (exp < 0) {
        throw ArithmeticException("Negative exponent not supported")
    }

    while (exp > 0) {
        if (exp % 2 == 1) {
            result = result.multiply(base)
        }
        base = base.multiply(base)
        exp /= 2
    }

    return result
}

// Math functions for KBigInteger

/**
 * Calculates this KBigInteger raised to the power of the specified integer exponent.
 * @receiver The base KBigInteger
 * @param exponent The integer exponent (must be non-negative)
 * @return This KBigInteger raised to the power of exponent
 * @throws IllegalArgumentException if the exponent is negative
 */
fun KBigInteger.pow(exponent: Int): KBigInteger {
    var result = KBigIntegerFactory.ONE
    var base = this
    var exp = exponent

    if (exp < 0) {
        throw ArithmeticException("Negative exponent not supported")
    }

    while (exp > 0) {
        if (exp % 2 == 1) {
            result = result.multiply(base)
        }
        base = base.multiply(base)
        exp /= 2
    }

    return result
}

// isZero(), isPositive(), isNegative() functions are already implemented in the interfaces

/**
 * Returns the maximum of this KBigDecimal and the specified value.
 * @receiver The first KBigDecimal to compare
 * @param other The second KBigDecimal to compare
 * @return The larger of the two values
 */
fun KBigDecimal.max(other: KBigDecimal): KBigDecimal = if (this >= other) this else other

/**
 * Returns the minimum of this KBigDecimal and the specified value.
 * @receiver The first KBigDecimal to compare
 * @param other The second KBigDecimal to compare
 * @return The smaller of the two values
 */
fun KBigDecimal.min(other: KBigDecimal): KBigDecimal = if (this <= other) this else other

/**
 * Returns the maximum of this KBigInteger and the specified value.
 * @receiver The first KBigInteger to compare
 * @param other The second KBigInteger to compare
 * @return The larger of the two values
 */
fun KBigInteger.max(other: KBigInteger): KBigInteger = if (this >= other) this else other

/**
 * Returns the minimum of this KBigInteger and the specified value.
 * @receiver The first KBigInteger to compare
 * @param other The second KBigInteger to compare
 * @return The smaller of the two values
 */
fun KBigInteger.min(other: KBigInteger): KBigInteger = if (this <= other) this else other
