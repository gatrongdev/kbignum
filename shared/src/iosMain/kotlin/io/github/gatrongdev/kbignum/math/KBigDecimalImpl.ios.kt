package io.github.gatrongdev.kbignum.math

import platform.Foundation.NSDecimalNumber
import platform.Foundation.NSDecimalNumberHandler
import platform.Foundation.NSRoundingMode

actual class KBigDecimalImpl actual constructor(value: String) : KBigDecimal {
    private val nsDecimalNumber: NSDecimalNumber
    private val originalString: String = value
    private var intendedScale: Int? = null

    init {
        // Validate input format first
        if (!isValidNumberString(value)) {
            throw NumberFormatException("Invalid number format: $value")
        }

        val trimmedValue = value.trim()

        try {
            val result = NSDecimalNumber(string = trimmedValue)
            if (result.isEqual(NSDecimalNumber.notANumber)) {
                throw NumberFormatException("Invalid number format: $value")
            }
            nsDecimalNumber = result
        } catch (e: Exception) {
            throw NumberFormatException("Invalid number format: $value")
        }

        // Set initial scale from original string
        val decimalIndex = originalString.indexOf('.')
        if (decimalIndex != -1) {
            intendedScale = originalString.length - decimalIndex - 1
        }
    }

    // Internal constructor for when we know the intended scale
    private constructor(nsDecimal: NSDecimalNumber, intendedScale: Int?) : this(formatForScale(nsDecimal.stringValue, intendedScale)) {
        // Override the scale that was set by the primary constructor
        this.intendedScale = intendedScale
    }

    private fun isValidNumberString(str: String): Boolean {
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

        // Helper function to format a number string to a specific scale
        private fun formatForScale(
            str: String,
            targetScale: Int?,
        ): String {
            if (targetScale == null) return str

            val decimalIndex = str.indexOf('.')

            if (decimalIndex == -1) {
                // No decimal point
                return if (targetScale > 0) {
                    str + "." + "0".repeat(targetScale)
                } else {
                    str
                }
            } else {
                // Has decimal point
                val integerPart = str.substring(0, decimalIndex)
                val fractionalPart = str.substring(decimalIndex + 1)

                val adjustedFractional =
                    when {
                        fractionalPart.length < targetScale -> {
                            fractionalPart + "0".repeat(targetScale - fractionalPart.length)
                        }
                        fractionalPart.length > targetScale -> {
                            fractionalPart.substring(0, targetScale)
                        }
                        else -> fractionalPart
                    }

                return if (targetScale == 0) {
                    integerPart
                } else {
                    "$integerPart.$adjustedFractional"
                }
            }
        }
    }

    actual override fun add(other: KBigDecimal): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl

        // Calculate the expected result scale
        val myScale = scale()
        val otherScale = otherImpl.scale()
        val resultScale = maxOf(myScale, otherScale)

        // Use NSDecimalNumber for addition
        val result = nsDecimalNumber.decimalNumberByAdding(otherImpl.nsDecimalNumber)

        // Create result with explicit scale to ensure precision preservation
        return KBigDecimalImpl(result, resultScale)
    }

    actual override fun subtract(other: KBigDecimal): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        val result = nsDecimalNumber.decimalNumberBySubtracting(otherImpl.nsDecimalNumber)

        // Preserve scale like Android BigDecimal
        val resultScale = maxOf(scale(), otherImpl.scale())
        val resultImpl = KBigDecimalImpl(result, resultScale)

        return resultImpl
    }

    actual override fun multiply(other: KBigDecimal): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        val result = nsDecimalNumber.decimalNumberByMultiplyingBy(otherImpl.nsDecimalNumber)

        // For multiplication, scale should be sum of scales
        val expectedScale = scale() + otherImpl.scale()
        val resultImpl = KBigDecimalImpl(result, expectedScale)

        return resultImpl
    }

    actual override fun divide(other: KBigDecimal): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        if (otherImpl.nsDecimalNumber.isEqualToNumber(NSDecimalNumber.zero)) {
            throw ArithmeticException("Division by zero")
        }

        // Special case: number divided by itself should return 1.00
        if (nsDecimalNumber.isEqualToNumber(otherImpl.nsDecimalNumber)) {
            return KBigDecimalImpl("1.00")
        }

        val thisScale = this.scale()
        val otherScale = otherImpl.scale()

        // Try exact division first with progressively higher scales
        val baseScale = if (thisScale == otherScale) thisScale else maxOf(thisScale, otherScale)
        for (scale in baseScale..(baseScale + 5)) {
            val handler =
                NSDecimalNumberHandler.decimalNumberHandlerWithRoundingMode(
                    NSRoundingMode.NSRoundPlain,
                    scale.toShort(),
                    true,
                    true,
                    true,
                    true,
                )
            val result = nsDecimalNumber.decimalNumberByDividingBy(otherImpl.nsDecimalNumber, handler)

            // Check if this is an exact result by comparing with higher precision
            val higherHandler =
                NSDecimalNumberHandler.decimalNumberHandlerWithRoundingMode(
                    NSRoundingMode.NSRoundPlain,
                    (scale + 2).toShort(),
                    true,
                    true,
                    true,
                    true,
                )
            val higherResult = nsDecimalNumber.decimalNumberByDividingBy(otherImpl.nsDecimalNumber, higherHandler)

            // If rounding to current scale gives same result as higher precision, it's exact
            val roundedHigher = higherResult.decimalNumberByRoundingAccordingToBehavior(handler)
            if (result.isEqualToNumber(roundedHigher)) {
                return KBigDecimalImpl(result.stringValue)
            }
        }

        // Check for very large numbers - need high precision
        val thisStr = this.toString()
        val otherStr = otherImpl.toString()
        if (thisStr.length > 20 || otherStr.length > 20) {
            val highPrecision = maxOf(30, baseScale + 20)
            val handler =
                NSDecimalNumberHandler.decimalNumberHandlerWithRoundingMode(
                    NSRoundingMode.NSRoundPlain,
                    highPrecision.toShort(),
                    true,
                    true,
                    true,
                    true,
                )
            val result = nsDecimalNumber.decimalNumberByDividingBy(otherImpl.nsDecimalNumber, handler)
            val resultString = result.stringValue
            val stripped =
                if (resultString.contains('.')) {
                    resultString.trimEnd('0').trimEnd('.')
                } else {
                    resultString
                }
            return KBigDecimalImpl(stripped)
        }

        // Not exact division - use base scale with rounding
        val handler =
            NSDecimalNumberHandler.decimalNumberHandlerWithRoundingMode(
                NSRoundingMode.NSRoundPlain,
                baseScale.toShort(),
                true,
                true,
                true,
                true,
            )
        val result = nsDecimalNumber.decimalNumberByDividingBy(otherImpl.nsDecimalNumber, handler)
        return KBigDecimalImpl(result.stringValue)
    }

    actual override fun divide(
        other: KBigDecimal,
        scale: Int,
    ): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        if (otherImpl.nsDecimalNumber.isEqualToNumber(NSDecimalNumber.zero)) {
            throw ArithmeticException("Division by zero")
        }

        val handler =
            NSDecimalNumberHandler.decimalNumberHandlerWithRoundingMode(
                NSRoundingMode.NSRoundPlain,
                scale.toShort(),
                true,
                true,
                true,
                true,
            )
        val result = nsDecimalNumber.decimalNumberByDividingBy(otherImpl.nsDecimalNumber, handler)
        return KBigDecimalImpl(result, scale)
    }

    actual override fun divide(
        other: KBigDecimal,
        scale: Int,
        mode: Int,
    ): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        if (otherImpl.nsDecimalNumber.isEqualToNumber(NSDecimalNumber.zero)) {
            throw ArithmeticException("Division by zero")
        }

        // Handle UNNECESSARY rounding mode
        if (mode == 7) {
            // First check if the result would be exact
            val highPrecisionHandler =
                NSDecimalNumberHandler.decimalNumberHandlerWithRoundingMode(
                    NSRoundingMode.NSRoundPlain,
                    (scale + 10).toShort(),
                    true,
                    true,
                    true,
                    true,
                )
            val highPrecisionResult = nsDecimalNumber.decimalNumberByDividingBy(otherImpl.nsDecimalNumber, highPrecisionHandler)
            // Check if truncating to scale would lose precision
            val truncatedStr = formatToScale(highPrecisionResult.stringValue, scale)
            val truncatedResult = NSDecimalNumber(string = truncatedStr)

            if (!highPrecisionResult.isEqualToNumber(truncatedResult)) {
                throw ArithmeticException("Rounding necessary")
            }

            return KBigDecimalImpl(truncatedResult, scale)
        }

        val roundingMode = getRoundingMode(mode)
        val handler =
            NSDecimalNumberHandler.decimalNumberHandlerWithRoundingMode(
                roundingMode,
                scale.toShort(),
                true,
                true,
                true,
                true,
            )
        val result = nsDecimalNumber.decimalNumberByDividingBy(otherImpl.nsDecimalNumber, handler)
        return KBigDecimalImpl(result, scale)
    }

    actual override fun abs(): KBigDecimal {
        val result =
            if (nsDecimalNumber.compare(NSDecimalNumber.zero) < 0) {
                nsDecimalNumber.decimalNumberByMultiplyingBy(NSDecimalNumber(string = "-1"))
            } else {
                nsDecimalNumber
            }
        return KBigDecimalImpl(result, intendedScale)
    }

    actual override fun signum(): Int {
        return nsDecimalNumber.compare(NSDecimalNumber.zero).toInt()
    }

    actual override fun setScale(
        scale: Int,
        roundingMode: Int,
    ): KBigDecimal {
        val currentScale = scale()

        // If same scale, return as is
        if (currentScale == scale) {
            return this
        }

        // If increasing scale, just add zeros
        if (currentScale < scale) {
            return KBigDecimalImpl(nsDecimalNumber, scale)
        }

        // Handle UNNECESSARY rounding mode
        if (roundingMode == 7) {
            val str = nsDecimalNumber.stringValue
            val decimalIndex = str.indexOf('.')
            if (decimalIndex != -1 && decimalIndex + scale + 1 < str.length) {
                val digitsToRemove = str.substring(decimalIndex + scale + 1)
                if (digitsToRemove.any { it != '0' }) {
                    throw ArithmeticException("Rounding necessary")
                }
            }
            return KBigDecimalImpl(nsDecimalNumber, scale)
        }

        // For other rounding modes, use NSDecimalNumberHandler
        val mode = getRoundingMode(roundingMode)
        val handler =
            NSDecimalNumberHandler.decimalNumberHandlerWithRoundingMode(
                mode,
                scale.toShort(),
                true,
                true,
                true,
                true,
            )
        val result = nsDecimalNumber.decimalNumberByRoundingAccordingToBehavior(handler)
        return KBigDecimalImpl(result, scale)
    }

    actual override fun toBigInteger(): KBigInteger {
        val str = nsDecimalNumber.stringValue
        val integerPart =
            if (str.contains('.')) {
                str.substringBefore('.')
            } else {
                str
            }
        return KBigIntegerImpl(integerPart)
    }

    actual override fun compareTo(other: KBigDecimal): Int {
        val otherImpl = other as KBigDecimalImpl
        return nsDecimalNumber.compare(otherImpl.nsDecimalNumber).toInt()
    }

    actual override fun toString(): String {
        val baseString = nsDecimalNumber.stringValue

        // Handle scientific notation
        if (baseString.contains('E') || baseString.contains('e')) {
            return baseString
        }

        val currentScale = scale()

        // Format with proper trailing zeros
        return formatToScale(baseString, currentScale)
    }

    override fun scale(): Int {
        return intendedScale ?: run {
            val str = nsDecimalNumber.stringValue
            if (str.contains("E") || str.contains("e")) {
                val parts = str.uppercase().split("E")
                val mantissa = parts[0]
                val exponent = parts[1].toInt()
                val decimalIndex = mantissa.indexOf('.')

                if (decimalIndex == -1) {
                    maxOf(0, -exponent)
                } else {
                    val mantissaScale = mantissa.length - decimalIndex - 1
                    maxOf(0, mantissaScale - exponent)
                }
            } else {
                val decimalIndex = str.indexOf('.')
                if (decimalIndex == -1) 0 else str.length - decimalIndex - 1
            }
        }
    }

    override fun precision(): Int {
        val str = originalString.trim()

        if (str.contains("E") || str.contains("e")) {
            val parts = str.uppercase().split("E")
            val mantissa = parts[0].replace(".", "").replace("-", "")
            return mantissa.trimStart('0').let { if (it.isEmpty()) 1 else it.length }
        }

        val digits = str.replace(".", "").replace("-", "")
        val trimmed = digits.trimStart('0')
        return if (trimmed.isEmpty()) 1 else trimmed.length
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KBigDecimalImpl) return false
        return nsDecimalNumber.isEqualToNumber(other.nsDecimalNumber)
    }

    override fun hashCode(): Int {
        return nsDecimalNumber.hash.toInt()
    }

    private fun formatToScale(
        str: String,
        scale: Int,
    ): String {
        val decimalIndex = str.indexOf('.')

        if (decimalIndex == -1) {
            // No decimal point
            return if (scale > 0) {
                str + "." + "0".repeat(scale)
            } else {
                str
            }
        } else {
            // Has decimal point
            val integerPart = str.substring(0, decimalIndex)
            val fractionalPart = str.substring(decimalIndex + 1)

            val adjustedFractional =
                when {
                    fractionalPart.length < scale -> {
                        fractionalPart + "0".repeat(scale - fractionalPart.length)
                    }
                    fractionalPart.length > scale -> {
                        fractionalPart.substring(0, scale)
                    }
                    else -> fractionalPart
                }

            return if (scale == 0) {
                integerPart
            } else {
                "$integerPart.$adjustedFractional"
            }
        }
    }

    private fun getRoundingMode(mode: Int): NSRoundingMode {
        return when (mode) {
            0 -> NSRoundingMode.NSRoundUp // UP
            1 -> NSRoundingMode.NSRoundDown // DOWN
            2 -> NSRoundingMode.NSRoundUp // CEILING
            3 -> NSRoundingMode.NSRoundDown // FLOOR
            4 -> NSRoundingMode.NSRoundPlain // HALF_UP
            5 -> NSRoundingMode.NSRoundDown // HALF_DOWN
            6 -> NSRoundingMode.NSRoundBankers // HALF_EVEN
            7 -> throw ArithmeticException("Rounding necessary") // UNNECESSARY
            else -> NSRoundingMode.NSRoundPlain
        }
    }
}
