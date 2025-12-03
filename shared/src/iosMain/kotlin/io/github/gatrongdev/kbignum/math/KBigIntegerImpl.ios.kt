package io.github.gatrongdev.kbignum.math

import io.github.gatrongdev.kbignum.ffi.*
import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
actual class KBigIntegerImpl actual constructor(value: String) : KBigInteger {
    private val stringValue: String

    init {
        if (!isValidIntegerString(value)) {
            throw NumberFormatException("Invalid number format: $value")
        }
        // Remove leading '+' sign for normalization
        val trimmed = value.trim()
        stringValue = if (trimmed.startsWith("+")) trimmed.substring(1) else trimmed
    }

    private fun isValidIntegerString(str: String): Boolean {
        if (str.isEmpty()) return false
        if (str != str.trim()) return false

        val validPattern = Regex("^[+-]?\\d+$")
        if (!validPattern.matches(str)) return false

        if (str == "+" || str == "-") return false
        if (str.any { it.isLetter() }) return false

        return true
    }

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
        // Check if value is within Long range
        val longMax = KBigIntegerImpl(Long.MAX_VALUE.toString())
        val longMin = KBigIntegerImpl(Long.MIN_VALUE.toString())

        if (this.compareTo(longMax) > 0) {
            throw ArithmeticException("BigInteger out of long range")
        }
        if (this.compareTo(longMin) < 0) {
            throw ArithmeticException("BigInteger out of long range")
        }

        return bigint_to_long(stringValue)
    }

    actual override fun toString(): String {
        return stringValue
    }

    actual override fun toPreciseNumber(): KBigDecimal {
        return KBigDecimalImpl(stringValue)
    }

    actual override fun add(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        val resultPtr = bigint_add(stringValue, otherImpl.stringValue)
        if (resultPtr == null) {
            throw ArithmeticException("Addition failed")
        }
        val result = resultPtr.toKString()
        bigint_free_string(resultPtr)
        return KBigIntegerImpl(result)
    }

    actual override fun subtract(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        val resultPtr = bigint_subtract(stringValue, otherImpl.stringValue)
        if (resultPtr == null) {
            throw ArithmeticException("Subtraction failed")
        }
        val result = resultPtr.toKString()
        bigint_free_string(resultPtr)
        return KBigIntegerImpl(result)
    }

    actual override fun multiply(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        val resultPtr = bigint_multiply(stringValue, otherImpl.stringValue)
        if (resultPtr == null) {
            throw ArithmeticException("Multiplication failed")
        }
        val result = resultPtr.toKString()
        bigint_free_string(resultPtr)
        return KBigIntegerImpl(result)
    }

    actual override fun divide(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        val resultPtr = bigint_divide(stringValue, otherImpl.stringValue)
        if (resultPtr == null) {
            throw ArithmeticException("Division by zero")
        }
        val result = resultPtr.toKString()
        bigint_free_string(resultPtr)
        return KBigIntegerImpl(result)
    }

    actual override fun mod(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        val resultPtr = bigint_mod(stringValue, otherImpl.stringValue)
        if (resultPtr == null) {
            throw ArithmeticException("Division by zero")
        }
        val result = resultPtr.toKString()
        bigint_free_string(resultPtr)
        return KBigIntegerImpl(result)
    }

    actual override fun abs(): KBigInteger {
        val resultPtr = bigint_abs(stringValue)
        if (resultPtr == null) {
            throw ArithmeticException("Absolute value failed")
        }
        val result = resultPtr.toKString()
        bigint_free_string(resultPtr)
        return KBigIntegerImpl(result)
    }

    actual override fun signum(): Int {
        return bigint_signum(stringValue)
    }

    actual override fun compareTo(other: KBigInteger): Int {
        val otherImpl = other as KBigIntegerImpl
        return bigint_compare(stringValue, otherImpl.stringValue)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KBigIntegerImpl) return false
        return stringValue == other.stringValue
    }

    override fun hashCode(): Int {
        return stringValue.hashCode()
    }
}
