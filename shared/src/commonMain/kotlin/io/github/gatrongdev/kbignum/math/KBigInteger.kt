package io.github.gatrongdev.kbignum.math

/**
 * Interface for arbitrary-precision integer arithmetic.
 * Provides operations for mathematical calculations with integers of unlimited size.
 */
interface KBigInteger {
    /**
     * Converts this KBigInteger to a Long value.
     * @return The Long representation of this number
     * @throws ArithmeticException if the value is too large to fit in a Long
     */
    fun toLong(): Long

    /**
     * Returns the string representation of this KBigInteger.
     * @return String representation of the number
     */
    override fun toString(): String

    /**
     * Converts this KBigInteger to a KBigDecimal for precise decimal operations.
     * @return KBigDecimal representation of this integer
     */
    fun toPreciseNumber(): KBigDecimal

    // getString() removed - use toString() instead

    // Additional utility functions

    /**
     * Returns a KBigInteger that is the sum of this and the specified value.
     * @param other The value to add to this number
     * @return The result of the addition
     */
    fun add(other: KBigInteger): KBigInteger

    /**
     * Returns a KBigInteger that is the difference of this and the specified value.
     * @param other The value to subtract from this number
     * @return The result of the subtraction
     */
    fun subtract(other: KBigInteger): KBigInteger

    /**
     * Returns a KBigInteger that is the product of this and the specified value.
     * @param other The value to multiply with this number
     * @return The result of the multiplication
     */
    fun multiply(other: KBigInteger): KBigInteger

    /**
     * Returns a KBigInteger that is the quotient of this divided by the specified value.
     * @param other The divisor
     * @return The result of the division (quotient only)
     * @throws ArithmeticException if other is zero
     */
    fun divide(other: KBigInteger): KBigInteger

    /**
     * Returns a KBigInteger that is the remainder of this divided by the specified value.
     * @param other The divisor
     * @return The remainder of the division
     * @throws ArithmeticException if other is zero
     */
    fun mod(other: KBigInteger): KBigInteger

    /**
     * Returns the absolute value of this KBigInteger.
     * @return The absolute value (always non-negative)
     */
    fun abs(): KBigInteger

    /**
     * Returns the signum function of this KBigInteger.
     * @return -1 if negative, 0 if zero, 1 if positive
     */
    fun signum(): Int

    /**
     * Returns the negation of this KBigInteger.
     * @return The negative value of this number
     */
    fun negate(): KBigInteger = KBigIntegerFactory.ZERO.subtract(this)

    /**
     * Compares this KBigInteger with the specified KBigInteger.
     * @param other The KBigInteger to compare with
     * @return -1 if this < other, 0 if this == other, 1 if this > other
     */
    operator fun compareTo(other: KBigInteger): Int

    /**
     * Checks if this KBigInteger is equal to zero.
     * @return true if this number equals zero, false otherwise
     */
    fun isZero(): Boolean = toString() == "0"

    /**
     * Checks if this KBigInteger is positive (greater than zero).
     * @return true if this number is positive, false otherwise
     */
    fun isPositive(): Boolean = signum() > 0

    /**
     * Checks if this KBigInteger is negative (less than zero).
     * @return true if this number is negative, false otherwise
     */
    fun isNegative(): Boolean = signum() < 0
}
