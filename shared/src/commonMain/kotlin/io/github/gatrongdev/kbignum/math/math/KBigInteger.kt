package io.github.gatrongdev.kbignum.math.math

interface KBigInteger {
    fun toLong(): Long

    override fun toString(): String

    fun toPreciseNumber(): KBigDecimal

    fun getString(): String

    // Additional utility functions

    fun add(other: KBigInteger): KBigInteger

    fun subtract(other: KBigInteger): KBigInteger

    fun multiply(other: KBigInteger): KBigInteger

    fun divide(other: KBigInteger): KBigInteger

    fun mod(other: KBigInteger): KBigInteger

    fun abs(): KBigInteger

    fun signum(): Int

    fun negate(): KBigInteger = KBigIntegerFactory.ZERO.subtract(this)

    fun compareTo(other: KBigInteger): Int

    fun isZero(): Boolean = toString() == "0"

    fun isPositive(): Boolean = signum() > 0

    fun isNegative(): Boolean = signum() < 0
}
