package io.github.gatrongdev.kbignum.math

/**
 * Factory object for creating KBigDecimal instances.
 * Provides convenient methods for creating KBigDecimal objects from various types.
 */
object KBigDecimalFactory {
    /** Constant representing zero as a KBigDecimal */
    val ZERO: KBigDecimal = KBigDecimalImpl.ZERO

    /** Constant representing one as a KBigDecimal */
    val ONE: KBigDecimal = KBigDecimalImpl("1")

    /** Constant representing ten as a KBigDecimal */
    val TEN: KBigDecimal = KBigDecimalImpl("10")

    /**
     * Creates a KBigDecimal from a string representation.
     * @param value The string representation of the decimal number
     * @return A KBigDecimal representing the specified value
     * @throws NumberFormatException if the string is not a valid decimal representation
     */
    fun fromString(value: String): KBigDecimal {
        return KBigDecimalImpl(value)
    }

    /**
     * Creates a KBigDecimal from a Long value.
     * @param value The Long value to convert
     * @return A KBigDecimal representing the specified Long value
     */
    fun fromLong(value: Long): KBigDecimal {
        return KBigDecimalImpl.fromLong(value)
    }

    /**
     * Creates a KBigDecimal from an Int value.
     * @param value The Int value to convert
     * @return A KBigDecimal representing the specified Int value
     */
    fun fromInt(value: Int): KBigDecimal {
        return KBigDecimalImpl.fromInt(value)
    }

    // fromDouble and fromFloat removed - use extension functions Double.toKBigDecimal() and Float.toKBigDecimal() instead
}

/**
 * Factory object for creating KBigInteger instances.
 * Provides convenient methods for creating KBigInteger objects from various types.
 */
object KBigIntegerFactory {
    /** Constant representing zero as a KBigInteger */
    val ZERO: KBigInteger = KBigIntegerImpl.ZERO

    /** Constant representing one as a KBigInteger */
    val ONE: KBigInteger = KBigIntegerImpl("1")

    /** Constant representing ten as a KBigInteger */
    val TEN: KBigInteger = KBigIntegerImpl("10")

    /**
     * Creates a KBigInteger from a string representation.
     * @param value The string representation of the integer
     * @return A KBigInteger representing the specified value
     * @throws NumberFormatException if the string is not a valid integer representation
     */
    fun fromString(value: String): KBigInteger {
        return KBigIntegerImpl(value)
    }

    /**
     * Creates a KBigInteger from a Long value.
     * @param value The Long value to convert
     * @return A KBigInteger representing the specified Long value
     */
    fun fromLong(value: Long): KBigInteger {
        return KBigIntegerImpl.fromLong(value)
    }

    /**
     * Creates a KBigInteger from an Int value.
     * @param value The Int value to convert
     * @return A KBigInteger representing the specified Int value
     */
    fun fromInt(value: Int): KBigInteger {
        return KBigIntegerImpl.fromInt(value)
    }
}

