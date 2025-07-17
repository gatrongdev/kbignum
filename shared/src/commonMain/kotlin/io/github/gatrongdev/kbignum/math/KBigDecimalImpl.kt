package io.github.gatrongdev.kbignum.math

expect class KBigDecimalImpl(value: String) : KBigDecimal {
    companion object {
        fun fromLong(value: Long): KBigDecimalImpl

        fun fromInt(value: Int): KBigDecimalImpl

        fun fromString(value: String): KBigDecimalImpl

        val ZERO: KBigDecimalImpl
    }

    override fun add(other: KBigDecimal): KBigDecimal

    override fun subtract(other: KBigDecimal): KBigDecimal

    override fun multiply(other: KBigDecimal): KBigDecimal

    override fun divide(other: KBigDecimal): KBigDecimal

    override fun divide(
        other: KBigDecimal,
        scale: Int,
    ): KBigDecimal

    override fun divide(
        other: KBigDecimal,
        scale: Int,
        mode: Int,
    ): KBigDecimal

    override fun abs(): KBigDecimal

    override fun signum(): Int

    override fun setScale(
        scale: Int,
        roundingMode: Int,
    ): KBigDecimal

    override fun toBigInteger(): KBigInteger

    override fun compareTo(other: KBigDecimal): Int

    override fun toString(): String

    // getString() removed - use toString() instead
}
