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

/**
 * Rounding mode constants for KBigDecimal operations.
 * These constants define how rounding should be performed when precision must be reduced.
 */
object RoundingMode {
    /** Rounding mode to round away from zero */
    const val UP = 0

    /** Rounding mode to round towards zero */
    const val DOWN = 1

    /** Rounding mode to round towards positive infinity */
    const val CEILING = 2

    /** Rounding mode to round towards negative infinity */
    const val FLOOR = 3

    /** Rounding mode to round towards the nearest neighbor, with ties rounded up */
    const val HALF_UP = 4

    /** Rounding mode to round towards the nearest neighbor, with ties rounded down */
    const val HALF_DOWN = 5

    /** Rounding mode to round towards the nearest neighbor, with ties rounded to the even neighbor */
    const val HALF_EVEN = 6

    /** Rounding mode to assert that the requested operation has an exact result */
    const val UNNECESSARY = 7
}

/**
 * Precision scale constants for common use cases.
 * These constants define standard decimal places for various types of calculations.
 */
object PrecisionScale {
    /** Scale for currency calculations (2 decimal places) - e.g., $10.99 */
    const val CURRENCY = 2

    /** Scale for exchange rates (4 decimal places) - e.g., 1.2345 USD/EUR */
    const val EXCHANGE_RATE = 4

    /** Scale for percentage calculations (2 decimal places) - e.g., 12.34% */
    const val PERCENTAGE = 2

    /** Scale for high precision scientific calculations (10 decimal places) */
    const val SCIENTIFIC = 10

    /** Scale for very high precision calculations (20 decimal places) */
    const val HIGH_PRECISION = 20

    /** Scale for interest rate calculations (6 decimal places) */
    const val INTEREST_RATE = 6

    /** Scale for cryptocurrency calculations (8 decimal places) - e.g., BTC */
    const val CRYPTOCURRENCY = 8
}

/**
 * Configuration for division operations combining scale and rounding mode.
 * @property scale Number of digits to the right of the decimal point
 * @property roundingMode Rounding mode to apply (see RoundingMode constants)
 */
data class DivisionConfig(
    val scale: Int,
    val roundingMode: Int = RoundingMode.HALF_UP,
)

/**
 * Predefined division strategies for common use cases.
 * Each strategy combines a scale and rounding mode optimized for specific scenarios.
 */
object DivisionStrategy {
    /**
     * Currency division (2 decimal places, HALF_UP rounding).
     * Suitable for: money calculations, prices, financial transactions.
     * Example: $100 / 3 = $33.33
     */
    val CURRENCY =
        DivisionConfig(
            scale = PrecisionScale.CURRENCY,
            roundingMode = RoundingMode.HALF_UP,
        )

    /**
     * Financial division (2 decimal places, HALF_EVEN rounding).
     * Suitable for: accounting, banking (banker's rounding for fairness).
     * Example: reduces cumulative rounding bias in repeated calculations
     */
    val FINANCIAL =
        DivisionConfig(
            scale = PrecisionScale.CURRENCY,
            roundingMode = RoundingMode.HALF_EVEN,
        )

    /**
     * Exchange rate division (4 decimal places, HALF_UP rounding).
     * Suitable for: currency conversion, forex calculations.
     * Example: 100 USD / 1.2345 = 81.0037 EUR
     */
    val EXCHANGE_RATE =
        DivisionConfig(
            scale = PrecisionScale.EXCHANGE_RATE,
            roundingMode = RoundingMode.HALF_UP,
        )

    /**
     * Percentage division (2 decimal places, HALF_UP rounding).
     * Suitable for: percentage calculations, ratios.
     * Example: 50 / 3 = 16.67%
     */
    val PERCENTAGE =
        DivisionConfig(
            scale = PrecisionScale.PERCENTAGE,
            roundingMode = RoundingMode.HALF_UP,
        )

    /**
     * Scientific division (10 decimal places, HALF_UP rounding).
     * Suitable for: scientific computations, engineering calculations.
     * Example: high precision results for research
     */
    val SCIENTIFIC =
        DivisionConfig(
            scale = PrecisionScale.SCIENTIFIC,
            roundingMode = RoundingMode.HALF_UP,
        )

    /**
     * High precision division (20 decimal places, HALF_UP rounding).
     * Suitable for: very precise calculations requiring minimal rounding error.
     * Example: complex mathematical computations
     */
    val HIGH_PRECISION =
        DivisionConfig(
            scale = PrecisionScale.HIGH_PRECISION,
            roundingMode = RoundingMode.HALF_UP,
        )

    /**
     * Interest rate division (6 decimal places, HALF_UP rounding).
     * Suitable for: interest calculations, APR/APY computations.
     * Example: 5% / 12 months = 0.416667% per month
     */
    val INTEREST_RATE =
        DivisionConfig(
            scale = PrecisionScale.INTEREST_RATE,
            roundingMode = RoundingMode.HALF_UP,
        )

    /**
     * Cryptocurrency division (8 decimal places, HALF_UP rounding).
     * Suitable for: Bitcoin and other crypto calculations.
     * Example: 1 BTC / 3 = 0.33333333 BTC
     */
    val CRYPTOCURRENCY =
        DivisionConfig(
            scale = PrecisionScale.CRYPTOCURRENCY,
            roundingMode = RoundingMode.HALF_UP,
        )

    /**
     * Exact division (requires exact result, no rounding).
     * Suitable for: divisions that must be exact or throw exception.
     * Example: 10 / 2 = 5 (exact), but 10 / 3 throws ArithmeticException
     */
    val EXACT =
        DivisionConfig(
            scale = 0,
            roundingMode = RoundingMode.UNNECESSARY,
        )
}
