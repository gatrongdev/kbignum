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

    actual override fun divide(other: KBigDecimal): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        if (otherImpl.bigDecimal.signum() == 0) {
            throw ArithmeticException("Division by zero")
        }

        // Special case: number divided by itself should return 1.00
        if (this.bigDecimal.compareTo(otherImpl.bigDecimal) == 0) {
            return KBigDecimalImpl("1.00")
        }

        val thisScale = this.scale()
        val otherScale = otherImpl.scale()

        // Try exact division first with progressively higher scales
        val baseScale = if (thisScale == otherScale) thisScale else maxOf(thisScale, otherScale)
        for (scale in baseScale..(baseScale + 5)) {
            try {
                val exactResult = bigDecimal.divide(otherImpl.bigDecimal, scale, RoundingMode.UNNECESSARY)
                return KBigDecimalImpl(exactResult.toString())
            } catch (e: ArithmeticException) {
                // Not exact at this scale, continue
            }
        }

        // Check for very large numbers - need high precision
        val thisStr = this.toString()
        val otherStr = otherImpl.toString()
        if (thisStr.length > 20 || otherStr.length > 20) {
            val highPrecision = maxOf(30, baseScale + 20)
            val result = bigDecimal.divide(otherImpl.bigDecimal, highPrecision, RoundingMode.HALF_UP)
            return KBigDecimalImpl(result.stripTrailingZeros().toString())
        }

        // Not exact division - use base scale with rounding
        val result = bigDecimal.divide(otherImpl.bigDecimal, baseScale, RoundingMode.HALF_UP)
        return KBigDecimalImpl(result.toString())
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
        val roundingMode = getRoundingMode(mode.toKBRoundingMode())
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
        val mode = getRoundingMode(roundingMode.toKBRoundingMode())
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

    private fun getRoundingMode(mode: KBRoundingMode): RoundingMode =
        when (mode) {
            KBRoundingMode.Up -> RoundingMode.UP
            KBRoundingMode.Down -> RoundingMode.DOWN
            KBRoundingMode.Ceiling -> RoundingMode.CEILING
            KBRoundingMode.Floor -> RoundingMode.FLOOR
            KBRoundingMode.HalfUp -> RoundingMode.HALF_UP
            KBRoundingMode.HalfDown -> RoundingMode.HALF_DOWN
            KBRoundingMode.HalfEven -> RoundingMode.HALF_EVEN
            KBRoundingMode.Unnecessary -> RoundingMode.UNNECESSARY
        }
}
