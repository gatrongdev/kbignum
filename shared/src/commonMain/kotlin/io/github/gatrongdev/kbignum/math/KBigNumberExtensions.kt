package io.github.gatrongdev.kbignum.math

// Extension functions for converting common types to KBigDecimal

/**
 * Converts a String to a KBigDecimal.
 * @receiver The String representation of a decimal number
 * @return A KBigDecimal representing the value of this string
 * @throws NumberFormatException if the string is not a valid decimal representation
 */
fun String.toKBigDecimal(): KBigDecimal = KBigDecimal.fromString(this)

/**
 * Converts an Int to a KBigDecimal.
 * @receiver The Int value to convert
 * @return A KBigDecimal representing this integer value
 */
fun Int.toKBigDecimal(): KBigDecimal = KBigDecimal.fromInt(this)

/**
 * Converts a Long to a KBigDecimal.
 * @receiver The Long value to convert
 * @return A KBigDecimal representing this long value
 */
fun Long.toKBigDecimal(): KBigDecimal = KBigDecimal.fromLong(this)

/**
 * Converts a Double to a KBigDecimal.
 * @receiver The Double value to convert
 * @return A KBigDecimal representing this double value
 */
fun Double.toKBigDecimal(): KBigDecimal = KBigDecimal.fromString(this.toString())

/**
 * Converts a Float to a KBigDecimal.
 * @receiver The Float value to convert
 * @return A KBigDecimal representing this float value
 */
fun Float.toKBigDecimal(): KBigDecimal = KBigDecimal.fromString(this.toString())

// Extension functions for converting common types to KBigInteger

/**
 * Converts a String to a KBigInteger.
 * @receiver The String representation of an integer
 * @return A KBigInteger representing the value of this string
 * @throws NumberFormatException if the string is not a valid integer representation
 */
fun String.toKBigInteger(): KBigInteger = KBigInteger.fromString(this)

/**
 * Converts an Int to a KBigInteger.
 * @receiver The Int value to convert
 * @return A KBigInteger representing this integer value
 */
fun Int.toKBigInteger(): KBigInteger = KBigInteger.fromInt(this)

/**
 * Converts a Long to a KBigInteger.
 * @receiver The Long value to convert
 * @return A KBigInteger representing this long value
 */
fun Long.toKBigInteger(): KBigInteger = KBigInteger.fromLong(this)

// Operator overloads for KBigDecimal

/**
 * Addition operator for KBigDecimal.
 */
operator fun KBigDecimal.plus(other: KBigDecimal): KBigDecimal = this.add(other)

/**
 * Subtraction operator for KBigDecimal.
 */
operator fun KBigDecimal.minus(other: KBigDecimal): KBigDecimal = this.subtract(other)

/**
 * Multiplication operator for KBigDecimal.
 */
operator fun KBigDecimal.times(other: KBigDecimal): KBigDecimal = this.multiply(other)

/**
 * Division operator for KBigDecimal with default scale and rounding.
 * WARNING: This currently defaults to a fixed scale of 10 and HALF_UP rounding.
 * For precise control, use the [divide] method directly.
 */
operator fun KBigDecimal.div(other: KBigDecimal): KBigDecimal = this.divide(other, 10, KBRoundingMode.HalfUp)

/**
 * Unary minus operator for KBigDecimal.
 */
operator fun KBigDecimal.unaryMinus(): KBigDecimal = this.negate()

/**
 * Unary plus operator for KBigDecimal (identity operation).
 */
operator fun KBigDecimal.unaryPlus(): KBigDecimal = this

// Operator overloads for KBigInteger

/**
 * Addition operator for KBigInteger.
 */
operator fun KBigInteger.plus(other: KBigInteger): KBigInteger = this.add(other)

/**
 * Subtraction operator for KBigInteger.
 */
operator fun KBigInteger.minus(other: KBigInteger): KBigInteger = this.subtract(other)

/**
 * Multiplication operator for KBigInteger.
 */
operator fun KBigInteger.times(other: KBigInteger): KBigInteger = this.multiply(other)

/**
 * Division operator for KBigInteger.
 */
operator fun KBigInteger.div(other: KBigInteger): KBigInteger = this.divide(other)

/**
 * Remainder operator for KBigInteger.
 */
operator fun KBigInteger.rem(other: KBigInteger): KBigInteger = this.mod(other)

/**
 * Unary minus operator for KBigInteger.
 */
operator fun KBigInteger.unaryMinus(): KBigInteger = this.negate()

/**
 * Unary plus operator for KBigInteger (identity operation).
 */
operator fun KBigInteger.unaryPlus(): KBigInteger = this

// Bitwise Infix Operations for KBigInteger

/**
 * Shifts this KBigInteger left by [n] bits.
 */
infix fun KBigInteger.shl(n: Int): KBigInteger = throw NotImplementedError("Bitwise ops pending implementation in Core")

/**
 * Shifts this KBigInteger right by [n] bits.
 */
infix fun KBigInteger.shr(n: Int): KBigInteger = throw NotImplementedError("Bitwise ops pending implementation in Core")

// Since 'and', 'or', 'xor' are likely not in KBigInteger yet, we temporarily comment/stub them or implement fallback.
// Assuming KBigInteger doesn't have public bitwise methods yet based on previous file reads.
// We will revisit adding 'and', 'or', 'xor' to KBigInteger kernel first if needed.

// Math functions for KBigDecimal

/**
 * Calculates this KBigDecimal raised to the power of the specified integer exponent.
 */
fun KBigDecimal.pow(exponent: Int): KBigDecimal {
    if (exponent < 0) throw ArithmeticException("Negative exponent not supported")
    var result = KBigDecimal.ONE
    var base = this
    var exp = exponent

    while (exp > 0) {
        if (exp % 2 == 1) result = result.multiply(base)
        base = base.multiply(base)
        exp /= 2
    }
    return result
}

// Math functions for KBigInteger

/**
 * Calculates this KBigInteger raised to the power of the specified integer exponent.
 */
fun KBigInteger.pow(exponent: Int): KBigInteger {
    if (exponent < 0) throw ArithmeticException("Negative exponent not supported")
    var result = KBigInteger.ONE
    var base = this
    var exp = exponent

    while (exp > 0) {
        if (exp % 2 == 1) result = result.multiply(base)
        base = base.multiply(base)
        exp /= 2
    }
    return result
}

// Utility functions

fun KBigDecimal.max(other: KBigDecimal): KBigDecimal = if (this >= other) this else other
fun KBigDecimal.min(other: KBigDecimal): KBigDecimal = if (this <= other) this else other
fun KBigInteger.max(other: KBigInteger): KBigInteger = if (this >= other) this else other
fun KBigInteger.min(other: KBigInteger): KBigInteger = if (this <= other) this else other
