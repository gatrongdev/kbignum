package io.github.gatrongdev.kbignum.math

import platform.Foundation.NSNumber
import platform.Foundation.numberWithLongLong

actual class KBigIntegerImpl actual constructor(value: String) : KBigInteger {
    private val nsNumber: NSNumber = NSNumber.numberWithLongLong(value.toLong())

    actual companion object {
        actual fun fromLong(value: Long): KBigIntegerImpl {
            return KBigIntegerImpl(value.toString())
        }

        actual fun fromInt(value: Int): KBigIntegerImpl {
            return KBigIntegerImpl(value.toString())
        }

        actual fun fromString(value: String): KBigIntegerImpl {
            return KBigIntegerImpl(value)
        }

        actual val ZERO: KBigIntegerImpl = KBigIntegerImpl("0")
    }

    actual override fun toLong(): Long {
        return nsNumber.longLongValue
    }

    actual override fun toString(): String {
        return nsNumber.stringValue
    }

    actual override fun toPreciseNumber(): KBigDecimal {
        return KBigDecimalImpl(nsNumber.stringValue)
    }

    actual override fun getString(): String {
        return nsNumber.stringValue
    }

    actual override fun add(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        val result = this.toLong() + otherImpl.toLong()
        return KBigIntegerImpl(result.toString())
    }

    actual override fun subtract(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        val result = this.toLong() - otherImpl.toLong()
        return KBigIntegerImpl(result.toString())
    }

    actual override fun multiply(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        val result = this.toLong() * otherImpl.toLong()
        return KBigIntegerImpl(result.toString())
    }

    actual override fun divide(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        val result = this.toLong() / otherImpl.toLong()
        return KBigIntegerImpl(result.toString())
    }

    actual override fun mod(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        val result = this.toLong() % otherImpl.toLong()
        return KBigIntegerImpl(result.toString())
    }

    actual override fun abs(): KBigInteger {
        val result = if (toLong() < 0) -toLong() else toLong()
        return KBigIntegerImpl(result.toString())
    }

    actual override fun signum(): Int {
        val value = toLong()
        return when {
            value > 0 -> 1
            value < 0 -> -1
            else -> 0
        }
    }

    actual override fun compareTo(other: KBigInteger): Int {
        val otherImpl = other as KBigIntegerImpl
        val thisValue = this.toLong()
        val otherValue = otherImpl.toLong()
        return when {
            thisValue > otherValue -> 1
            thisValue < otherValue -> -1
            else -> 0
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KBigIntegerImpl) return false
        return nsNumber.isEqualToNumber(other.nsNumber)
    }

    override fun hashCode(): Int {
        return nsNumber.hash.toInt()
    }
}
