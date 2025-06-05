package vn.com.gatrong.kbignum.math

import platform.Foundation.NSDecimalNumber
import platform.Foundation.NSDecimalNumberHandler
import platform.Foundation.NSRoundingMode

actual class KBigDecimalImpl actual constructor(value: String) : KBigDecimal {
    private val nsDecimalNumber: NSDecimalNumber = NSDecimalNumber(string = value)

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
        val result = nsDecimalNumber.decimalNumberByAdding(otherImpl.nsDecimalNumber)
        return KBigDecimalImpl(result.stringValue)
    }

    actual override fun subtract(other: KBigDecimal): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        val result = nsDecimalNumber.decimalNumberBySubtracting(otherImpl.nsDecimalNumber)
        return KBigDecimalImpl(result.stringValue)
    }

    actual override fun multiply(other: KBigDecimal): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        val result = nsDecimalNumber.decimalNumberByMultiplyingBy(otherImpl.nsDecimalNumber)
        return KBigDecimalImpl(result.stringValue)
    }

    actual override fun divide(other: KBigDecimal, scale: Int): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        val handler = NSDecimalNumberHandler.decimalNumberHandlerWithRoundingMode(
            NSRoundingMode.NSRoundPlain,
            scale.toShort(),
            true,
            true,
            true,
            true
        )
        val result = nsDecimalNumber.decimalNumberByDividingBy(otherImpl.nsDecimalNumber, handler)
        return KBigDecimalImpl(result.stringValue)
    }

    actual override fun divide(other: KBigDecimal, scale: Int, mode: Int): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        val roundingMode = getRoundingMode(mode)
        val handler = NSDecimalNumberHandler.decimalNumberHandlerWithRoundingMode(
            roundingMode,
            scale.toShort(),
            true,
            true,
            true,
            true
        )
        val result = nsDecimalNumber.decimalNumberByDividingBy(otherImpl.nsDecimalNumber, handler)
        return KBigDecimalImpl(result.stringValue)
    }

    actual override fun abs(): KBigDecimal {
        val result = if (nsDecimalNumber.compare(NSDecimalNumber.zero) < 0) {
            nsDecimalNumber.decimalNumberByMultiplyingBy(NSDecimalNumber(string = "-1"))
        } else {
            nsDecimalNumber
        }
        return KBigDecimalImpl(result.stringValue)
    }

    actual override fun signum(): Int {
        return nsDecimalNumber.compare(NSDecimalNumber.zero).toInt()
    }

    actual override fun setScale(scale: Int, roundingMode: Int): KBigDecimal {
        val mode = getRoundingMode(roundingMode)
        val handler = NSDecimalNumberHandler.decimalNumberHandlerWithRoundingMode(
            mode,
            scale.toShort(),
            true,
            true,
            true,
            true
        )
        val result = nsDecimalNumber.decimalNumberByRoundingAccordingToBehavior(handler)
        return KBigDecimalImpl(result.stringValue)
    }

    actual override fun toBigInteger(): KBigInteger {
        return KBigIntegerImpl(nsDecimalNumber.longLongValue.toString())
    }

    actual override fun compareTo(other: KBigDecimal): Int {
        val otherImpl = other as KBigDecimalImpl
        return nsDecimalNumber.compare(otherImpl.nsDecimalNumber).toInt()
    }

    actual override fun toString(): String {
        return nsDecimalNumber.stringValue
    }

    actual override fun getString(): String {
        return nsDecimalNumber.stringValue
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KBigDecimalImpl) return false
        return nsDecimalNumber.isEqualToNumber(other.nsDecimalNumber)
    }

    override fun hashCode(): Int {
        return nsDecimalNumber.hash.toInt()
    }

    private fun getRoundingMode(mode: Int): NSRoundingMode {
        return when (mode) {
            0 -> NSRoundingMode.NSRoundUp
            1 -> NSRoundingMode.NSRoundDown
            2 -> NSRoundingMode.NSRoundUp
            3 -> NSRoundingMode.NSRoundDown
            4 -> NSRoundingMode.NSRoundPlain
            5 -> NSRoundingMode.NSRoundPlain
            6 -> NSRoundingMode.NSRoundBankers
            7 -> NSRoundingMode.NSRoundPlain
            else -> NSRoundingMode.NSRoundPlain
        }
    }
}
