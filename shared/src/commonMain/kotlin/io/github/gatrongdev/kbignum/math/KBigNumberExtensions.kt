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

// Bitwise infix functions for KBigInteger

/**
 * Infix function for bitwise AND.
 */
infix fun KBigInteger.and(other: KBigInteger): KBigInteger = this.and(other)

/**
 * Infix function for bitwise OR.
 */
infix fun KBigInteger.or(other: KBigInteger): KBigInteger = this.or(other)

/**
 * Infix function for bitwise XOR.
 */
infix fun KBigInteger.xor(other: KBigInteger): KBigInteger = this.xor(other)


// Math functions for KBigDecimal



// Utility functions

fun KBigDecimal.max(other: KBigDecimal): KBigDecimal = if (this >= other) this else other
fun KBigDecimal.min(other: KBigDecimal): KBigDecimal = if (this <= other) this else other
fun KBigInteger.max(other: KBigInteger): KBigInteger = if (this >= other) this else other
fun KBigInteger.min(other: KBigInteger): KBigInteger = if (this <= other) this else other
