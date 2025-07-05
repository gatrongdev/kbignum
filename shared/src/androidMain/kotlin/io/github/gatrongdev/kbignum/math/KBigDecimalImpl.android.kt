package io.github.gatrongdev.kbignum.math

import java.math.BigDecimal
import java.math.RoundingMode

actual class KBigDecimalImpl actual constructor(value: String) : KBigDecimal {
    private val bigDecimal: BigDecimal = BigDecimal(value)

    actual companion object {
        actual fun fromLong(value: Long): KBigDecimalImpl {
            return KBigDecimalImpl(value.toString())
        }

        actual fun fromInt(value: Int): KBigDecimalImpl {
            return KBigDecimalImpl(value.toString())
        }

        actual fun fromString(value: String): KBigDecimalImpl {
            return KBigDecimalImpl(value)
        }

        actual val ZERO: KBigDecimalImpl = KBigDecimalImpl("0")
    }

    actual override fun add(other: KBigDecimal): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        return KBigDecimalImpl(bigDecimal.add(otherImpl.bigDecimal).toString())
    }

    actual override fun subtract(other: KBigDecimal): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        return KBigDecimalImpl(bigDecimal.subtract(otherImpl.bigDecimal).toString())
    }

    actual override fun multiply(other: KBigDecimal): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        return KBigDecimalImpl(bigDecimal.multiply(otherImpl.bigDecimal).toString())
    }

    actual override fun divide(
        other: KBigDecimal,
        scale: Int,
    ): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        return KBigDecimalImpl(
            bigDecimal.divide(
                otherImpl.bigDecimal,
                scale,
                RoundingMode.HALF_UP,
            ).toString(),
        )
    }

    actual override fun divide(
        other: KBigDecimal,
        scale: Int,
        mode: Int,
    ): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        val roundingMode = getRoundingMode(mode)
        return KBigDecimalImpl(
            bigDecimal.divide(
                otherImpl.bigDecimal,
                scale,
                roundingMode,
            ).toString(),
        )
    }

    actual override fun abs(): KBigDecimal {
        return KBigDecimalImpl(bigDecimal.abs().toString())
    }

    actual override fun signum(): Int {
        return bigDecimal.signum()
    }

    actual override fun setScale(
        scale: Int,
        roundingMode: Int,
    ): KBigDecimal {
        val mode = getRoundingMode(roundingMode)
        return KBigDecimalImpl(bigDecimal.setScale(scale, mode).toString())
    }

    actual override fun toBigInteger(): KBigInteger {
        return KBigIntegerImpl(bigDecimal.toBigInteger().toString())
    }

    actual override fun compareTo(other: KBigDecimal): Int {
        val otherImpl = other as KBigDecimalImpl
        return bigDecimal.compareTo(otherImpl.bigDecimal)
    }

    actual override fun toString(): String {
        return bigDecimal.toString()
    }

    override fun scale(): Int {
        return bigDecimal.scale()
    }

    override fun precision(): Int {
        return bigDecimal.precision()
    }

    // getString() removed - use toString() instead

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KBigDecimalImpl) return false
        return bigDecimal == other.bigDecimal
    }

    override fun hashCode(): Int {
        return bigDecimal.hashCode()
    }

    private fun getRoundingMode(mode: Int): RoundingMode {
        return when (mode) {
            0 -> RoundingMode.UP
            1 -> RoundingMode.DOWN
            2 -> RoundingMode.CEILING
            3 -> RoundingMode.FLOOR
            4 -> RoundingMode.HALF_UP
            5 -> RoundingMode.HALF_DOWN
            6 -> RoundingMode.HALF_EVEN
            7 -> RoundingMode.UNNECESSARY
            else -> RoundingMode.HALF_UP
        }
    }
}
