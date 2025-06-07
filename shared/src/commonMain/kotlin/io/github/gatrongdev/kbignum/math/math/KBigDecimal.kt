package io.github.gatrongdev.kbignum.math.math

interface KBigDecimal : Comparable<KBigDecimal> {
    fun add(other: KBigDecimal): KBigDecimal

    fun subtract(other: KBigDecimal): KBigDecimal

    fun multiply(other: KBigDecimal): KBigDecimal

    fun divide(
        other: KBigDecimal,
        scale: Int,
    ): KBigDecimal

    fun divide(
        other: KBigDecimal,
        scale: Int,
        mode: Int,
    ): KBigDecimal

    fun abs(): KBigDecimal

    fun signum(): Int

    fun setScale(
        scale: Int,
        roundingMode: Int,
    ): KBigDecimal

    fun toBigInteger(): KBigInteger

    override fun toString(): String

    fun getString(): String

    // Additional utility functions

    fun negate(): KBigDecimal = KBigDecimalFactory.ZERO.subtract(this)

    fun scale(): Int = 0 // Default implementation, can be overridden

    fun precision(): Int = toString().replace(".", "").replace("-", "").length

    fun isZero(): Boolean = signum() == 0

    fun isPositive(): Boolean = signum() > 0

    fun isNegative(): Boolean = signum() < 0
}
