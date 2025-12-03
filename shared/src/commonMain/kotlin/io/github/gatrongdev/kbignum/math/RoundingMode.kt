package io.github.gatrongdev.kbignum.math

/**
 * Rounding mode enum for KBigDecimal operations.
 * These constants define how rounding should be performed when precision must be reduced.
 */
enum class RoundingMode(val value: Int) {
    /** Rounding mode to round away from zero */
    UP(0),

    /** Rounding mode to round towards zero */
    DOWN(1),

    /** Rounding mode to round towards positive infinity */
    CEILING(2),

    /** Rounding mode to round towards negative infinity */
    FLOOR(3),

    /** Rounding mode to round towards the nearest neighbor, with ties rounded up */
    HALF_UP(4),

    /** Rounding mode to round towards the nearest neighbor, with ties rounded down */
    HALF_DOWN(5),

    /** Rounding mode to round towards the nearest neighbor, with ties rounded to the even neighbor */
    HALF_EVEN(6),

    /** Rounding mode to assert that the requested operation has an exact result */
    UNNECESSARY(7);

    companion object {
        fun fromInt(value: Int): RoundingMode {
            return entries.find { it.value == value } ?: HALF_UP
        }
    }
}
