package io.github.gatrongdev.kbignum.math

/** Precision and rounding rules for a decimal division. */
data class DivisionConfig(
    val scale: Int,
    val roundingMode: KBRoundingMode = KBRoundingMode.HalfUp,
) {
    init {
        require(scale >= 0) { "Scale must be non-negative" }
    }
}

/** Common decimal scales for application-specific calculations. */
object PrecisionScale {
    const val CURRENCY = 2
    const val EXCHANGE_RATE = 4
    const val PERCENTAGE = 2
    const val SCIENTIFIC = 10
    const val HIGH_PRECISION = 20
    const val INTEREST_RATE = 6
    const val CRYPTOCURRENCY = 8
}

/** Ready-to-use division rules for common use cases. */
object DivisionStrategy {
    val DEFAULT = DivisionConfig(scale = 10)
    val CURRENCY = DivisionConfig(PrecisionScale.CURRENCY)
    val FINANCIAL = DivisionConfig(PrecisionScale.CURRENCY, KBRoundingMode.HalfEven)
    val EXCHANGE_RATE = DivisionConfig(PrecisionScale.EXCHANGE_RATE)
    val PERCENTAGE = DivisionConfig(PrecisionScale.PERCENTAGE)
    val SCIENTIFIC = DivisionConfig(PrecisionScale.SCIENTIFIC)
    val HIGH_PRECISION = DivisionConfig(PrecisionScale.HIGH_PRECISION)
    val INTEREST_RATE = DivisionConfig(PrecisionScale.INTEREST_RATE)
    val CRYPTOCURRENCY = DivisionConfig(PrecisionScale.CRYPTOCURRENCY)
    val EXACT = DivisionConfig(PrecisionScale.SCIENTIFIC, KBRoundingMode.Unnecessary)
}
