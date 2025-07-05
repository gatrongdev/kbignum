package io.github.gatrongdev.kbignum.math

import java.math.BigInteger

actual class KBigIntegerImpl actual constructor(value: String) : KBigInteger {
    private val bigInteger: BigInteger = BigInteger(value)

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
        // Check if the value is within Long range
        if (bigInteger < BigInteger.valueOf(Long.MIN_VALUE) || bigInteger > BigInteger.valueOf(Long.MAX_VALUE)) {
            throw ArithmeticException("BigInteger out of long range")
        }
        return bigInteger.toLong()
    }

    actual override fun toString(): String {
        return bigInteger.toString()
    }

    actual override fun toPreciseNumber(): KBigDecimal {
        return KBigDecimalImpl(bigInteger.toString())
    }

    // getString() removed - use toString() instead

    actual override fun add(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        return KBigIntegerImpl(bigInteger.add(otherImpl.bigInteger).toString())
    }

    actual override fun subtract(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        return KBigIntegerImpl(bigInteger.subtract(otherImpl.bigInteger).toString())
    }

    actual override fun multiply(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        return KBigIntegerImpl(bigInteger.multiply(otherImpl.bigInteger).toString())
    }

    actual override fun divide(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        return KBigIntegerImpl(bigInteger.divide(otherImpl.bigInteger).toString())
    }

    actual override fun mod(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl
        return KBigIntegerImpl(bigInteger.mod(otherImpl.bigInteger).toString())
    }

    actual override fun abs(): KBigInteger {
        return KBigIntegerImpl(bigInteger.abs().toString())
    }

    actual override fun signum(): Int {
        return bigInteger.signum()
    }

    actual override fun compareTo(other: KBigInteger): Int {
        val otherImpl = other as KBigIntegerImpl
        return bigInteger.compareTo(otherImpl.bigInteger)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KBigIntegerImpl) return false
        return bigInteger == other.bigInteger
    }

    override fun hashCode(): Int {
        return bigInteger.hashCode()
    }
}