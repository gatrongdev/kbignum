package io.github.gatrongdev.kbignum.math

import platform.Foundation.NSDecimalNumber
import platform.Foundation.NSDecimalNumberHandler
import platform.Foundation.NSRoundingMode

actual class KBigDecimalImpl actual constructor(value: String) : KBigDecimal {
    private val nsDecimalNumber: NSDecimalNumber
    private val originalString: String = value
    private var intendedScale: Int? = null
    
    init {
        // Validate input format first - do NOT trim, whitespace should be invalid
        if (!isValidNumberString(value)) {
            throw NumberFormatException("Invalid number format: $value")
        }
        
        // Use trimmed value for NSDecimalNumber creation
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
    }
    
    // Internal constructor for when we know the intended scale
    private constructor(nsDecimal: NSDecimalNumber, intendedScale: Int?) : this(nsDecimal.stringValue) {
        this.intendedScale = intendedScale
    }
    
    private fun isValidNumberString(str: String): Boolean {
        if (str.isEmpty()) return false
        
        // Check for leading/trailing whitespace - should be invalid
        if (str != str.trim()) return false
        
        // Additional strict check for any whitespace characters anywhere in the string
        if (str.any { it.isWhitespace() }) return false
        
        // Basic regex for valid decimal numbers - must have at least one digit
        val validPattern = Regex("^[+-]?(\\d+\\.?\\d*|\\.\\d+)([eE][+-]?\\d+)?$")
        if (!validPattern.matches(str)) return false
        
        // Additional checks for common invalid patterns
        if (str == "." || str == "+" || str == "-" || str == "+." || str == "-.") return false
        if (str.contains("..")) return false
        if (str.count { it == '.' } > 1) return false
        if (str.count { it == 'e' || it == 'E' } > 1) return false
        
        // Check for letters mixed with numbers
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
    }

    actual override fun add(other: KBigDecimal): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        val result = nsDecimalNumber.decimalNumberByAdding(otherImpl.nsDecimalNumber)
        return KBigDecimalImpl(result.stringValue)
    }

    actual override fun subtract(other: KBigDecimal): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        val result = nsDecimalNumber.decimalNumberBySubtracting(otherImpl.nsDecimalNumber)
        return KBigDecimalImpl(result.stringValue)
    }

    actual override fun multiply(other: KBigDecimal): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        val result = nsDecimalNumber.decimalNumberByMultiplyingBy(otherImpl.nsDecimalNumber)
        val resultImpl = KBigDecimalImpl(result.stringValue)
        
        // For multiplication, preserve trailing zeros to maintain expected scale
        val expectedScale = scale() + otherImpl.scale()
        if (resultImpl.scale() < expectedScale) {
            return resultImpl.setScale(expectedScale, 0)
        }
        
        return resultImpl
    }

    actual override fun divide(
        other: KBigDecimal,
        scale: Int,
    ): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        // Check for division by zero
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
        return KBigDecimalImpl(result.stringValue)
    }

    actual override fun divide(
        other: KBigDecimal,
        scale: Int,
        mode: Int,
    ): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        // Check for division by zero
        if (otherImpl.nsDecimalNumber.isEqualToNumber(NSDecimalNumber.zero)) {
            throw ArithmeticException("Division by zero")
        }
        
        // Handle UNNECESSARY rounding mode
        if (mode == 7) {
            // Do division with high precision first
            val handler = NSDecimalNumberHandler.decimalNumberHandlerWithRoundingMode(
                NSRoundingMode.NSRoundPlain,
                (scale + 10).toShort(),
                true, true, true, true
            )
            val result = nsDecimalNumber.decimalNumberByDividingBy(otherImpl.nsDecimalNumber, handler)
            val resultImpl = KBigDecimalImpl(result.stringValue)
            
            // Check if result has the exact scale
            if (resultImpl.scale() != scale) {
                throw ArithmeticException("Rounding necessary")
            }
            return resultImpl.setScale(scale, 7)
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
        val resultImpl = KBigDecimalImpl(result.stringValue)
        
        // Apply proper rounding for CEILING/FLOOR if needed
        if (mode == 2 || mode == 3) {
            return resultImpl.setScale(scale, mode)
        }
        
        // Ensure the result has the correct scale
        return resultImpl.setScale(scale, mode)
    }

    actual override fun abs(): KBigDecimal {
        val result =
            if (nsDecimalNumber.compare(NSDecimalNumber.zero) < 0) {
                nsDecimalNumber.decimalNumberByMultiplyingBy(NSDecimalNumber(string = "-1"))
            } else {
                nsDecimalNumber
            }
        return KBigDecimalImpl(result.stringValue)
    }

    actual override fun signum(): Int {
        return nsDecimalNumber.compare(NSDecimalNumber.zero).toInt()
    }

    actual override fun setScale(
        scale: Int,
        roundingMode: Int,
    ): KBigDecimal {
        // Handle UNNECESSARY rounding mode first
        if (roundingMode == 7) {
            val currentScale = scale()
            if (currentScale != scale) {
                throw ArithmeticException("Rounding necessary")
            }
            return this
        }
        
        // For CEILING and FLOOR, we need special handling
        if (roundingMode == 2 || roundingMode == 3) {
            return setScaleWithDirectionalRounding(scale, roundingMode)
        }
        
        // For UP, DOWN, and HALF_DOWN we need special handling too
        if (roundingMode == 0 || roundingMode == 1 || roundingMode == 5) {
            return setScaleWithCustomRounding(scale, roundingMode)
        }
        
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
        val resultImpl = KBigDecimalImpl(result.stringValue)
        resultImpl.intendedScale = scale
        return resultImpl
    }
    
    private fun setScaleWithDirectionalRounding(scale: Int, roundingMode: Int): KBigDecimal {
        val currentScale = scale()
        if (currentScale == scale) return this
        if (currentScale < scale) {
            // Need to add trailing zeros
            val zeros = "0".repeat(scale - currentScale)
            val newValue = if (nsDecimalNumber.stringValue.contains('.')) {
                nsDecimalNumber.stringValue + zeros
            } else {
                nsDecimalNumber.stringValue + "." + zeros
            }
            val resultImpl = KBigDecimalImpl(newValue)
            resultImpl.intendedScale = scale
            return resultImpl
        }
        
        // Need to round - check if actual rounding is needed
        val str = nsDecimalNumber.stringValue
        val decimalIndex = str.indexOf('.')
        
        if (decimalIndex == -1 || decimalIndex + scale + 1 >= str.length) {
            // No rounding needed, just truncate or extend
            return this
        }
        
        val digitToRound = str[decimalIndex + scale + 1].digitToInt()
        val isNegative = nsDecimalNumber.compare(NSDecimalNumber.zero) < 0
        
        // For CEILING: round towards positive infinity
        // For FLOOR: round towards negative infinity
        val shouldRoundUp = when (roundingMode) {
            2 -> {  // CEILING: round toward positive infinity
                if (digitToRound > 0) {
                    if (isNegative) false else true  // For positive: round up, for negative: truncate
                } else {
                    false
                }
            }
            3 -> {  // FLOOR: round toward negative infinity  
                if (digitToRound > 0) {
                    if (isNegative) true else false  // For positive: truncate, for negative: round down (make more negative)
                } else {
                    false
                }
            }
            else -> false
        }
        
        // Apply manual rounding like in setScaleWithCustomRounding
        if (shouldRoundUp) {
            // Parse the string and manually increment the digit at the scale position
            val integerPart = str.substring(0, decimalIndex)
            val fractionalPart = str.substring(decimalIndex + 1)
            
            // Get the part we want to keep
            val keepFractional = if (scale == 0) "" else fractionalPart.substring(0, scale)
            
            // Create the truncated number first
            val truncatedValue = if (scale == 0) {
                integerPart
            } else {
                "$integerPart.$keepFractional"
            }
            
            // Convert to NSDecimalNumber and add the increment
            val truncated = NSDecimalNumber(string = truncatedValue)
            val increment = NSDecimalNumber(string = if (scale == 0) "1" else "0.${"0".repeat(scale - 1)}1")
            
            val result = if (isNegative) {
                // For negative numbers, we subtract the increment to make it more negative
                truncated.decimalNumberBySubtracting(increment)
            } else {
                // For positive numbers, we add the increment
                truncated.decimalNumberByAdding(increment)
            }
            
            val resultImpl = KBigDecimalImpl(result.stringValue)
            resultImpl.intendedScale = scale
            return resultImpl
        } else {
            // Just truncate
            val integerPart = str.substring(0, decimalIndex)
            val fractionalPart = str.substring(decimalIndex + 1)
            
            val truncatedFractional = if (fractionalPart.length <= scale) {
                fractionalPart.padEnd(scale, '0')
            } else {
                fractionalPart.substring(0, scale)
            }
            
            val result = if (scale == 0) integerPart else "$integerPart.$truncatedFractional"
            val resultImpl = KBigDecimalImpl(result)
            resultImpl.intendedScale = scale
            return resultImpl
        }
    }
    
    private fun setScaleWithCustomRounding(scale: Int, roundingMode: Int): KBigDecimal {
        val currentScale = scale()
        if (currentScale == scale) return this
        if (currentScale < scale) {
            // Need to add trailing zeros
            val zeros = "0".repeat(scale - currentScale)
            val newValue = if (nsDecimalNumber.stringValue.contains('.')) {
                nsDecimalNumber.stringValue + zeros
            } else {
                nsDecimalNumber.stringValue + "." + zeros
            }
            val resultImpl = KBigDecimalImpl(newValue)
            resultImpl.intendedScale = scale
            return resultImpl
        }
        
        // Need to round - check if actual rounding is needed
        val str = nsDecimalNumber.stringValue
        val decimalIndex = str.indexOf('.')
        
        if (decimalIndex == -1 || decimalIndex + scale + 1 >= str.length) {
            // No rounding needed, just truncate or extend
            return this
        }
        
        val digitToRound = str[decimalIndex + scale + 1].digitToInt()
        val isNegative = nsDecimalNumber.compare(NSDecimalNumber.zero) < 0
        
        val shouldRoundUp = when (roundingMode) {
            0 -> {  // UP - round away from zero
                if (digitToRound > 0) {
                    // For negative numbers, rounding away from zero means making the absolute value larger
                    // For positive numbers, rounding away from zero means making the value larger
                    true
                } else {
                    false
                }
            }
            1 -> false  // DOWN - truncate towards zero (never round up, just truncate)
            5 -> {  // HALF_DOWN
                if (digitToRound > 5) true
                else if (digitToRound < 5) false
                else false  // exactly 5 rounds down
            }
            else -> false
        }
        
        // Apply manual rounding since NSDecimalNumber's rounding modes don't behave correctly for our needs
        if (shouldRoundUp) {
            // Parse the string and manually increment the digit at the scale position
            val integerPart = str.substring(0, decimalIndex)
            val fractionalPart = str.substring(decimalIndex + 1)
            
            // Get the part we want to keep
            val keepFractional = if (scale == 0) "" else fractionalPart.substring(0, scale)
            
            // Create the truncated number first
            val truncatedValue = if (scale == 0) {
                integerPart
            } else {
                "$integerPart.$keepFractional"
            }
            
            // Convert to NSDecimalNumber and add the increment
            val truncated = NSDecimalNumber(string = truncatedValue)
            val increment = NSDecimalNumber(string = if (scale == 0) "1" else "0.${"0".repeat(scale - 1)}1")
            
            val result = if (isNegative) {
                // For negative numbers, we subtract the increment to make it more negative (away from zero)
                truncated.decimalNumberBySubtracting(increment)
            } else {
                // For positive numbers, we add the increment
                truncated.decimalNumberByAdding(increment)
            }
            
            return KBigDecimalImpl(result.stringValue)
        } else {
            // For DOWN mode, just truncate the decimal places manually
            val str = nsDecimalNumber.stringValue
            val decimalIndex = str.indexOf('.')
            
            if (decimalIndex == -1 || scale == 0) {
                // No decimal point or scale is 0, return integer part
                val integerPart = if (decimalIndex == -1) str else str.substring(0, decimalIndex)
                val resultImpl = if (scale == 0) {
                    KBigDecimalImpl(integerPart)
                } else {
                    KBigDecimalImpl("$integerPart.${"0".repeat(scale)}")
                }
                resultImpl.intendedScale = scale
                return resultImpl
            } else {
                // Has decimal point, truncate to required scale
                val integerPart = str.substring(0, decimalIndex)
                val fractionalPart = str.substring(decimalIndex + 1)
                
                val truncatedFractional = if (fractionalPart.length <= scale) {
                    fractionalPart.padEnd(scale, '0')
                } else {
                    fractionalPart.substring(0, scale)
                }
                
                val result = if (scale == 0) integerPart else "$integerPart.$truncatedFractional"
                val resultImpl = KBigDecimalImpl(result)
                resultImpl.intendedScale = scale
                return resultImpl
            }
        }
    }

    actual override fun toBigInteger(): KBigInteger {
        // Get the string representation and remove decimal part
        val str = nsDecimalNumber.stringValue
        val integerPart = if (str.contains('.')) {
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
        
        // Special handling for scientific notation - return as is
        if (baseString.contains('E') || baseString.contains('e')) {
            return baseString
        }
        
        val currentScale = scale()
        
        // Handle decimal places
        if (currentScale > 0) {
            val decimalIndex = baseString.indexOf('.')
            if (decimalIndex == -1) {
                // No decimal point, add it with the required zeros
                return baseString + "." + "0".repeat(currentScale)
            } else {
                // Has decimal point, check if we need trailing zeros
                val actualDecimalPlaces = baseString.length - decimalIndex - 1
                if (actualDecimalPlaces < currentScale) {
                    val zerosToAdd = currentScale - actualDecimalPlaces
                    return baseString + "0".repeat(zerosToAdd)
                }
            }
        }
        
        return baseString
    }

    override fun scale(): Int {
        // Use intended scale if it was explicitly set
        intendedScale?.let { return it }
        
        val str = nsDecimalNumber.stringValue
        
        // Handle scientific notation  
        if (str.contains("E") || str.contains("e")) {
            val parts = str.uppercase().split("E")
            val mantissa = parts[0]
            val exponent = parts[1].toInt()
            val decimalIndex = mantissa.indexOf('.')
            
            if (decimalIndex == -1) {
                return maxOf(0, -exponent)
            } else {
                val mantissaScale = mantissa.length - decimalIndex - 1
                return maxOf(0, mantissaScale - exponent)
            }
        }
        
        // Handle regular decimal numbers
        val decimalIndex = str.indexOf('.')
        return if (decimalIndex == -1) 0 else str.length - decimalIndex - 1
    }

    override fun precision(): Int {
        // Use the original string for precision calculation to preserve trailing zeros
        val str = originalString.trim()
        
        // Handle scientific notation
        if (str.contains("E") || str.contains("e")) {
            val parts = str.uppercase().split("E")
            val mantissa = parts[0].replace(".", "").replace("-", "")
            return mantissa.trimStart('0').let { if (it.isEmpty()) 1 else it.length }
        }
        
        // Handle regular decimal numbers using original string to preserve trailing zeros
        val digits = str.replace(".", "").replace("-", "")
        val trimmed = digits.trimStart('0')
        return if (trimmed.isEmpty()) 1 else trimmed.length
    }

    // getString() removed - use toString() instead

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KBigDecimalImpl) return false
        return nsDecimalNumber.isEqualToNumber(other.nsDecimalNumber)
    }

    override fun hashCode(): Int {
        return nsDecimalNumber.hash.toInt()
    }

    private fun getRoundingMode(mode: Int): NSRoundingMode {
        return when (mode) {
            0 -> NSRoundingMode.NSRoundUp           // UP
            1 -> NSRoundingMode.NSRoundDown         // DOWN
            2 -> NSRoundingMode.NSRoundUp           // CEILING (round towards positive infinity)
            3 -> NSRoundingMode.NSRoundDown         // FLOOR (round towards negative infinity)  
            4 -> NSRoundingMode.NSRoundPlain        // HALF_UP
            5 -> NSRoundingMode.NSRoundDown         // HALF_DOWN - TODO: need custom implementation
            6 -> NSRoundingMode.NSRoundBankers      // HALF_EVEN
            7 -> throw ArithmeticException("Rounding necessary") // UNNECESSARY
            else -> NSRoundingMode.NSRoundPlain
        }
    }
}
