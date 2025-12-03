package io.github.gatrongdev.kbignum.math

/**
 * Interface for arbitrary-precision decimal arithmetic.
 * Provides operations for mathematical calculations with decimal numbers of unlimited precision.
 */
interface KBigDecimal : Comparable<KBigDecimal> {
    /**
     * Returns a KBigDecimal that is the sum of this and the specified value.
     * @param other The value to add to this number
     * @return The result of the addition
     */
    fun add(other: KBigDecimal): KBigDecimal

    /**
     * Returns a KBigDecimal that is the difference of this and the specified value.
     * @param other The value to subtract from this number
     * @return The result of the subtraction
     */
    fun subtract(other: KBigDecimal): KBigDecimal

    /**
     * Returns a KBigDecimal that is the product of this and the specified value.
     * @param other The value to multiply with this number
     * @return The result of the multiplication
     */
    fun multiply(other: KBigDecimal): KBigDecimal

    /**
     * Returns a KBigDecimal that is the quotient of this divided by the specified value.
     * @param other The divisor
     * @return The result of the division with the maximum scale of the two operands
     * @throws ArithmeticException if other is zero
     */
    fun divide(other: KBigDecimal): KBigDecimal

    /**
     * Returns a KBigDecimal that is the quotient of this divided by the specified value.
     * @param other The divisor
     * @param scale The number of digits to the right of the decimal point in the result
     * @return The result of the division with the specified scale
     * @throws ArithmeticException if other is zero
     */
    fun divide(
        other: KBigDecimal,
        scale: Int,
    ): KBigDecimal

    /**
     * Returns a KBigDecimal that is the quotient of this divided by the specified value.
     * @param other The divisor
     * @param scale The number of digits to the right of the decimal point in the result
     * @param mode The rounding mode to apply (see RoundingMode constants)
     * @return The result of the division with the specified scale and rounding mode
     * @throws ArithmeticException if other is zero or if rounding is necessary but the rounding mode is UNNECESSARY
     */
    fun divide(
        other: KBigDecimal,
        scale: Int,
        mode: RoundingMode,
    ): KBigDecimal

    /**
     * Returns the absolute value of this KBigDecimal.
     * @return The absolute value (always non-negative)
     */
    fun abs(): KBigDecimal

    /**
     * Returns the signum function of this KBigDecimal.
     * @return -1 if negative, 0 if zero, 1 if positive
     */
    fun signum(): Int

    /**
     * Returns a KBigDecimal with the specified scale and rounding mode.
     * @param scale The number of digits to the right of the decimal point
     * @param roundingMode The rounding mode to apply (see RoundingMode constants)
     * @return A KBigDecimal with the specified scale
     * @throws ArithmeticException if rounding is necessary but the rounding mode is UNNECESSARY
     */
    fun setScale(
        scale: Int,
        roundingMode: RoundingMode,
    ): KBigDecimal


    /**
     * Converts this KBigDecimal to a KBigInteger by discarding the fractional part.
     * @return KBigInteger representation of this decimal (truncated towards zero)
     */
    fun toBigInteger(): KBigInteger

    /**
     * Returns the string representation of this KBigDecimal.
     * @return String representation of the number
     */
    override fun toString(): String

    // getString() removed - use toString() instead

    // Additional utility functions

    /**
     * Returns the negation of this KBigDecimal.
     * @return The negative value of this number
     */
    fun negate(): KBigDecimal = KBigDecimalFactory.ZERO.subtract(this)

    /**
     * Returns the scale of this KBigDecimal (number of digits to the right of the decimal point).
     * @return The scale of this decimal number
     */
    fun scale(): Int = 0 // Default implementation, can be overridden

    /**
     * Returns the precision of this KBigDecimal (total number of significant digits).
     * @return The number of significant digits in this decimal number
     */
    fun precision(): Int = toString().replace(".", "").replace("-", "").length

    /**
     * Checks if this KBigDecimal is equal to zero.
     * @return true if this number equals zero, false otherwise
     */
    fun isZero(): Boolean = signum() == 0

    /**
     * Checks if this KBigDecimal is positive (greater than zero).
     * @return true if this number is positive, false otherwise
     */
    fun isPositive(): Boolean = signum() > 0

    /**
     * Checks if this KBigDecimal is negative (less than zero).
     * @return true if this number is negative, false otherwise
     */
    fun isNegative(): Boolean = signum() < 0
}
