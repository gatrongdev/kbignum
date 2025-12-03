package io.github.gatrongdev.kbignum.math

import io.github.gatrongdev.kbignum.ffi.*
import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
actual class KBigDecimalImpl actual constructor(value: String) : KBigDecimal {
    private val stringValue: String
    private var cachedScale: Int? = null

    init {
        if (!isValidDecimalString(value)) {
            throw NumberFormatException("Invalid number format: $value")
        }
        // Remove leading '+' sign for normalization
        val trimmed = value.trim()
        stringValue = if (trimmed.startsWith("+")) trimmed.substring(1) else trimmed
    }

    private fun isValidDecimalString(str: String): Boolean {
        if (str.isEmpty()) return false
        if (str != str.trim()) return false
        if (str.any { it.isWhitespace() }) return false

        val validPattern = Regex("^[+-]?(\\d+\\.?\\d*|\\.\\d+)([eE][+-]?\\d+)?$")
        if (!validPattern.matches(str)) return false

        if (str == "." || str == "+" || str == "-" || str == "+." || str == "-.") return false
        if (str.contains("..")) return false
        if (str.count { it == '.' } > 1) return false
        if (str.count { it == 'e' || it == 'E' } > 1) return false
        if (str.any { it.isLetter() && it != 'e' && it != 'E' }) return false

        return true
    }

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
        val resultScale = maxOf(scale(), otherImpl.scale())
        val resultPtr = bigdecimal_add(stringValue, otherImpl.stringValue, resultScale)
        if (resultPtr == null) {
            throw ArithmeticException("Addition failed")
        }
        val result = resultPtr.toKString()
        bigint_free_string(resultPtr)
        return KBigDecimalImpl(result)
    }

    actual override fun subtract(other: KBigDecimal): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        val resultScale = maxOf(scale(), otherImpl.scale())
        val resultPtr = bigdecimal_subtract(stringValue, otherImpl.stringValue, resultScale)
        if (resultPtr == null) {
            throw ArithmeticException("Subtraction failed")
        }
        val result = resultPtr.toKString()
        bigint_free_string(resultPtr)
        return KBigDecimalImpl(result)
    }

    actual override fun multiply(other: KBigDecimal): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        val resultScale = scale() + otherImpl.scale()
        val resultPtr = bigdecimal_multiply(stringValue, otherImpl.stringValue, resultScale)
        if (resultPtr == null) {
            throw ArithmeticException("Multiplication failed")
        }
        val result = resultPtr.toKString()
        bigint_free_string(resultPtr)
        return KBigDecimalImpl(result)
    }

    actual override fun divide(other: KBigDecimal): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        val thisScale = scale()
        val otherScale = otherImpl.scale()
        val preferredScale = maxOf(thisScale, otherScale)

        // Try division at preferred scale first
        val resultPtr = bigdecimal_divide(stringValue, otherImpl.stringValue, preferredScale)
        if (resultPtr == null) {
            throw ArithmeticException("Division by zero")
        }
        val result = resultPtr.toKString()
        bigint_free_string(resultPtr)

        return KBigDecimalImpl(result)
    }

    actual override fun divide(other: KBigDecimal, scale: Int): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        val resultPtr = bigdecimal_divide(stringValue, otherImpl.stringValue, scale)
        if (resultPtr == null) {
            throw ArithmeticException("Division by zero")
        }
        val result = resultPtr.toKString()
        bigint_free_string(resultPtr)
        return KBigDecimalImpl(result)
    }

    actual override fun divide(other: KBigDecimal, scale: Int, mode: Int): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl

        // For UNNECESSARY mode (7), we need to check if rounding is required
        if (mode == 7) {
            val highScalePtr = bigdecimal_divide(stringValue, otherImpl.stringValue, scale + 10)
            if (highScalePtr != null) {
                val highScale = highScalePtr.toKString()
                bigint_free_string(highScalePtr)

                val truncatedPtr = bigdecimal_set_scale(highScale, scale, mode)
                if (truncatedPtr == null) {
                    throw ArithmeticException("Rounding necessary")
                }
                val result = truncatedPtr.toKString()
                bigint_free_string(truncatedPtr)
                return KBigDecimalImpl(result)
            }
        }

        val resultPtr = bigdecimal_divide(stringValue, otherImpl.stringValue, scale)
        if (resultPtr == null) {
            throw ArithmeticException("Division by zero")
        }
        val result = resultPtr.toKString()
        bigint_free_string(resultPtr)

        // Apply rounding mode
        val roundedPtr = bigdecimal_set_scale(result, scale, mode)
        if (roundedPtr == null) {
            throw ArithmeticException("Rounding failed")
        }
        val rounded = roundedPtr.toKString()
        bigint_free_string(roundedPtr)
        return KBigDecimalImpl(rounded)
    }

    actual override fun abs(): KBigDecimal {
        val resultPtr = bigdecimal_abs(stringValue)
        if (resultPtr == null) {
            throw ArithmeticException("Absolute value failed")
        }
        val result = resultPtr.toKString()
        bigint_free_string(resultPtr)
        return KBigDecimalImpl(result)
    }

    actual override fun signum(): Int {
        return bigdecimal_signum(stringValue)
    }

    actual override fun setScale(scale: Int, roundingMode: Int): KBigDecimal {
        val currentScale = scale()

        // If same scale, return as is
        if (currentScale == scale) {
            return this
        }

        val resultPtr = bigdecimal_set_scale(stringValue, scale, roundingMode)
        if (resultPtr == null) {
            throw ArithmeticException("Rounding necessary")
        }
        val result = resultPtr.toKString()
        bigint_free_string(resultPtr)
        return KBigDecimalImpl(result)
    }

    actual override fun toBigInteger(): KBigInteger {
        val resultPtr = bigdecimal_to_biginteger(stringValue)
        if (resultPtr == null) {
            throw ArithmeticException("Conversion failed")
        }
        val result = resultPtr.toKString()
        bigint_free_string(resultPtr)
        return KBigIntegerImpl(result)
    }

    actual override fun compareTo(other: KBigDecimal): Int {
        val otherImpl = other as KBigDecimalImpl
        return bigdecimal_compare(stringValue, otherImpl.stringValue)
    }

    actual override fun toString(): String {
        return stringValue
    }

    override fun scale(): Int {
        if (cachedScale != null) {
            return cachedScale!!
        }

        val str = stringValue

        // Handle scientific notation
        if (str.contains("E") || str.contains("e")) {
            val parts = str.uppercase().split("E")
            val mantissa = parts[0]
            val exponent = parts[1].toInt()
            val decimalIndex = mantissa.indexOf('.')

            cachedScale = if (decimalIndex == -1) {
                maxOf(0, -exponent)
            } else {
                val mantissaScale = mantissa.length - decimalIndex - 1
                maxOf(0, mantissaScale - exponent)
            }
        } else {
            val decimalIndex = str.indexOf('.')
            cachedScale = if (decimalIndex == -1) 0 else str.length - decimalIndex - 1
        }

        return cachedScale!!
    }

    override fun precision(): Int {
        val str = stringValue.trim()

        if (str.contains("E") || str.contains("e")) {
            val parts = str.uppercase().split("E")
            val mantissa = parts[0].replace(".", "").replace("-", "").replace("+", "")
            return mantissa.trimStart('0').let { if (it.isEmpty()) 1 else it.length }
        }

        val digits = str.replace(".", "").replace("-", "").replace("+", "")
        val trimmed = digits.trimStart('0')
        return if (trimmed.isEmpty()) 1 else trimmed.length
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KBigDecimalImpl) return false

        // Compare using Rust FFI comparison
        return bigdecimal_compare(stringValue, other.stringValue) == 0
    }

    override fun hashCode(): Int {
        return stringValue.hashCode()
    }
}
