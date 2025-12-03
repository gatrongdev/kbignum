package io.github.gatrongdev.kbignum.math

import io.github.gatrongdev.kbignum.ffi.*
import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
actual class KBigIntegerImpl : KBigInteger {
    private val bytes: ByteArray

    actual constructor(value: String) {
        if (!isValidIntegerString(value)) {
            throw NumberFormatException("Invalid number format: $value")
        }
        // Remove leading '+' sign for normalization
        val trimmed = value.trim()
        val cleanValue = if (trimmed.startsWith("+")) trimmed.substring(1) else trimmed
        
        bytes = memScoped {
            val resultPtr = bigint_from_string_bytes(cleanValue.cstr)
                ?: throw NumberFormatException("Failed to parse BigInteger from string")
            
            val result = resultPtr.pointed
            val data = result.data ?: throw IllegalStateException("Null data pointer")
            val len = result.len.toInt()
            
            val byteArray = data.readBytes(len)
            bigint_free_byte_result(resultPtr)
            byteArray
        }
    }

    private constructor(bytes: ByteArray) {
        this.bytes = bytes
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
        return bytes.usePinned { pinned ->
            bigint_to_long_bytes(pinned.addressOf(0).reinterpret(), bytes.size.toULong())
        }
    }

    actual override fun toString(): String {
        return bytes.usePinned { pinned ->
            val resultPtr = bigint_to_string_bytes(pinned.addressOf(0).reinterpret(), bytes.size.toULong())
                ?: return "0"
            val result = resultPtr.toKString()
            bigint_free_string(resultPtr)
            result
        }
    }

    actual override fun toPreciseNumber(): KBigDecimal {
        return KBigDecimalImpl(toString())
    }

    actual override fun add(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        return performOp(otherImpl) { aPtr, aLen, bPtr, bLen ->
            bigint_add_bytes(aPtr, aLen, bPtr, bLen)
        }
    }

    actual override fun subtract(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        return performOp(otherImpl) { aPtr, aLen, bPtr, bLen ->
            bigint_subtract_bytes(aPtr, aLen, bPtr, bLen)
        }
    }

    actual override fun multiply(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        return performOp(otherImpl) { aPtr, aLen, bPtr, bLen ->
            bigint_multiply_bytes(aPtr, aLen, bPtr, bLen)
        }
    }

    actual override fun divide(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        return performOp(otherImpl) { aPtr, aLen, bPtr, bLen ->
            bigint_divide_bytes(aPtr, aLen, bPtr, bLen)
        }
    }

    actual override fun mod(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        return performOp(otherImpl) { aPtr, aLen, bPtr, bLen ->
            bigint_mod_bytes(aPtr, aLen, bPtr, bLen)
        }
    }

    actual override fun abs(): KBigInteger {
        return bytes.usePinned { pinned ->
            val resultPtr = bigint_abs_bytes(pinned.addressOf(0).reinterpret(), bytes.size.toULong())
                ?: throw ArithmeticException("Absolute value failed")
            
            val result = resultPtr.pointed
            val data = result.data ?: throw IllegalStateException("Null data pointer")
            val len = result.len.toInt()
            
            val byteArray = data.readBytes(len)
            bigint_free_byte_result(resultPtr)
            KBigIntegerImpl(byteArray)
        }
    }

    actual override fun signum(): Int {
        return bytes.usePinned { pinned ->
            bigint_signum_bytes(pinned.addressOf(0).reinterpret(), bytes.size.toULong())
        }
    }

    actual override fun compareTo(other: KBigInteger): Int {
        val otherImpl = other as KBigIntegerImpl
        return bytes.usePinned { pinnedA ->
            otherImpl.bytes.usePinned { pinnedB ->
                bigint_compare_bytes(
                    pinnedA.addressOf(0).reinterpret(), bytes.size.toULong(),
                    pinnedB.addressOf(0).reinterpret(), otherImpl.bytes.size.toULong()
                )
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KBigIntegerImpl) return false
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }

    private inline fun performOp(
        other: KBigIntegerImpl,
        op: (CPointer<ByteVar>, ULong, CPointer<ByteVar>, ULong) -> CPointer<ByteArrayResult>?
    ): KBigInteger {
        return bytes.usePinned { pinnedA ->
            other.bytes.usePinned { pinnedB ->
                val resultPtr = op(
                    pinnedA.addressOf(0).reinterpret(), bytes.size.toULong(),
                    pinnedB.addressOf(0).reinterpret(), other.bytes.size.toULong()
                ) ?: throw ArithmeticException("Operation failed")

                val result = resultPtr.pointed
                val data = result.data ?: throw IllegalStateException("Null data pointer")
                val len = result.len.toInt()

                val byteArray = data.readBytes(len)
                bigint_free_byte_result(resultPtr)
                KBigIntegerImpl(byteArray)
            }
        }
    }
}

