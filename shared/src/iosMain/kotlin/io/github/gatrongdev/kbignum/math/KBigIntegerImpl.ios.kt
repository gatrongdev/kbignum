package io.github.gatrongdev.kbignum.math

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign

actual internal class KBigIntegerImpl actual constructor(value: String) : KBigInteger {
    private val bigInteger: BigInteger = BigInteger.parseString(value)

    actual companion object {
        actual fun fromLong(value: Long): KBigIntegerImpl {
            return KBigIntegerImpl(BigInteger.fromLong(value).toString())
        }

        actual fun fromInt(value: Int): KBigIntegerImpl {
            return KBigIntegerImpl(BigInteger.fromInt(value).toString())
        }

        actual fun fromString(value: String): KBigIntegerImpl {
            return KBigIntegerImpl(value)
        }

        actual val ZERO: KBigIntegerImpl = KBigIntegerImpl(BigInteger.ZERO.toString())
    }

    actual override fun toLong(): Long {
        return bigInteger.longValue()
    }

    actual override fun toString(): String {
        return bigInteger.toString()
    }

    actual override fun toPreciseNumber(): KBigDecimal {
        return KBigDecimalImpl(toString())
    }

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
        return KBigIntegerImpl(bigInteger.remainder(otherImpl.bigInteger).toString())
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

