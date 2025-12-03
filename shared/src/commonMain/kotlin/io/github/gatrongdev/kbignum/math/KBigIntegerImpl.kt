package io.github.gatrongdev.kbignum.math

expect internal class KBigIntegerImpl(value: String) : KBigInteger {
    companion object {
        fun fromLong(value: Long): KBigIntegerImpl

        fun fromInt(value: Int): KBigIntegerImpl

        fun fromString(value: String): KBigIntegerImpl

        val ZERO: KBigIntegerImpl
    }

    override fun toLong(): Long

    override fun toString(): String

    override fun toPreciseNumber(): KBigDecimal

    // getString() removed - use toString() instead

    override fun add(other: KBigInteger): KBigInteger

    override fun subtract(other: KBigInteger): KBigInteger

    override fun multiply(other: KBigInteger): KBigInteger

    override fun divide(other: KBigInteger): KBigInteger

    override fun mod(other: KBigInteger): KBigInteger

    override fun abs(): KBigInteger

    override fun signum(): Int

    override fun compareTo(other: KBigInteger): Int
}
