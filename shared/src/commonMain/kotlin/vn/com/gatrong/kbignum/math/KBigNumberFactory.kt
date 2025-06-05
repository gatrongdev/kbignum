package vn.com.gatrong.kbignum.math

/**
 * Factory object for creating KBigDecimal instances
 */
object KBigDecimalFactory {
    val ZERO: KBigDecimal = KBigDecimalImpl.ZERO
    val ONE: KBigDecimal = KBigDecimalImpl("1")
    val TEN: KBigDecimal = KBigDecimalImpl("10")

    fun fromString(value: String): KBigDecimal {
        return KBigDecimalImpl(value)
    }

    fun fromLong(value: Long): KBigDecimal {
        return KBigDecimalImpl.fromLong(value)
    }

    fun fromInt(value: Int): KBigDecimal {
        return KBigDecimalImpl.fromInt(value)
    }

    fun fromDouble(value: Double): KBigDecimal {
        return KBigDecimalImpl(value.toString())
    }

    fun fromFloat(value: Float): KBigDecimal {
        return KBigDecimalImpl(value.toString())
    }
}

/**
 * Factory object for creating KBigInteger instances
 */
object KBigIntegerFactory {
    val ZERO: KBigInteger = KBigIntegerImpl.ZERO
    val ONE: KBigInteger = KBigIntegerImpl("1")
    val TEN: KBigInteger = KBigIntegerImpl("10")

    fun fromString(value: String): KBigInteger {
        return KBigIntegerImpl(value)
    }

    fun fromLong(value: Long): KBigInteger {
        return KBigIntegerImpl.fromLong(value)
    }

    fun fromInt(value: Int): KBigInteger {
        return KBigIntegerImpl.fromInt(value)
    }
}

/**
 * Rounding mode constants
 */
object RoundingMode {
    const val UP = 0
    const val DOWN = 1
    const val CEILING = 2
    const val FLOOR = 3
    const val HALF_UP = 4
    const val HALF_DOWN = 5
    const val HALF_EVEN = 6
    const val UNNECESSARY = 7
}
