package io.github.gatrongdev.kbignum.math

/**
 * Cross-platform representation of rounding behaviors supported by KBigDecimal operations.
 * The enum mirrors the semantics of `java.math.RoundingMode` while providing type-safety
 * and shared documentation across Kotlin Multiplatform targets.
 */
enum class KBRoundingMode(
    /** Stable identifier matching legacy integer constants (see [RoundingMode]). */
    val legacyCode: Int,
) {
    /** Rounds away from zero. */
    Up(0),

    /** Rounds towards zero. */
    Down(1),

    /** Rounds towards positive infinity. */
    Ceiling(2),

    /** Rounds towards negative infinity. */
    Floor(3),

    /** Rounds towards the nearest neighbour; ties are rounded away from zero. */
    HalfUp(4),

    /** Rounds towards the nearest neighbour; ties are rounded towards zero. */
    HalfDown(5),

    /** Rounds towards the nearest neighbour; ties are rounded to the even neighbour. */
    HalfEven(6),

    /** Asserts that rounding is unnecessary; throws if precision would be lost. */
    Unnecessary(7),
    ;

    companion object {
        /** Looks up the [KBRoundingMode] associated with a legacy integer code. */
        fun fromLegacyCode(code: Int): KBRoundingMode =
            values().firstOrNull { it.legacyCode == code }
                ?: error("Unknown rounding mode code: $code")
    }
}

/** Converts a [KBRoundingMode] to its legacy integer constant counterpart. */
fun KBRoundingMode.toLegacyCode(): Int = legacyCode

/** Converts a legacy integer rounding constant into [KBRoundingMode]. */
fun Int.toKBRoundingMode(): KBRoundingMode = KBRoundingMode.fromLegacyCode(this)
