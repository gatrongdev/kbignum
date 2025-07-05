package io.github.gatrongdev.kbignum.math

import platform.Foundation.NSDecimalNumber

actual class KBigIntegerImpl actual constructor(value: String) : KBigInteger {
    private val nsDecimalNumber: NSDecimalNumber?
    private val originalString: String = value.trim()
    private val stringValue: String
    private val isLargeNumber: Boolean

    init {
        // Validate input format first - integers shouldn't have decimal points
        if (!isValidIntegerString(originalString)) {
            throw NumberFormatException("Invalid number format: $originalString")
        }

        // Determine if this is a large number that exceeds NSDecimalNumber precision
        isLargeNumber = originalString.replace("-", "").length > 34 // NSDecimalNumber max precision

        if (isLargeNumber) {
            // For very large numbers, store as string and use string arithmetic
            nsDecimalNumber = null
            stringValue = originalString
        } else {
            try {
                val result = NSDecimalNumber(string = originalString)
                if (result.isEqual(NSDecimalNumber.notANumber)) {
                    throw NumberFormatException("Invalid number format: $originalString")
                }
                nsDecimalNumber = result
                stringValue = result.stringValue
            } catch (e: Exception) {
                throw NumberFormatException("Invalid number format: $originalString")
            }
        }
    }

    private fun isValidIntegerString(str: String): Boolean {
        if (str.isEmpty()) return false

        // Check for leading/trailing whitespace - should be invalid
        if (str != str.trim()) return false

        // Basic regex for valid integer numbers - no decimal points allowed
        val validPattern = Regex("^[+-]?\\d+$")
        if (!validPattern.matches(str)) return false

        // Additional checks for common invalid patterns
        if (str == "+" || str == "-") return false

        // Check for letters
        if (str.any { it.isLetter() }) return false

        return true
    }

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
        return if (isLargeNumber) {
            stringValue.toLongOrNull() ?: throw ArithmeticException("Number too large for Long: $stringValue")
        } else {
            nsDecimalNumber!!.longLongValue
        }
    }

    actual override fun toString(): String {
        return stringValue
    }

    actual override fun toPreciseNumber(): KBigDecimal {
        return KBigDecimalImpl(stringValue)
    }

    // getString() removed - use toString() instead

    actual override fun add(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl

        // If either number is large, fall back to string arithmetic via KBigDecimal
        if (isLargeNumber || otherImpl.isLargeNumber) {
            val thisDecimal = KBigDecimalImpl(stringValue)
            val otherDecimal = KBigDecimalImpl(otherImpl.stringValue)
            val result = thisDecimal.add(otherDecimal)
            return KBigIntegerImpl(result.toString())
        }

        val result = nsDecimalNumber!!.decimalNumberByAdding(otherImpl.nsDecimalNumber!!)
        return KBigIntegerImpl(result.stringValue)
    }

    actual override fun subtract(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl

        if (isLargeNumber || otherImpl.isLargeNumber) {
            val thisDecimal = KBigDecimalImpl(stringValue)
            val otherDecimal = KBigDecimalImpl(otherImpl.stringValue)
            val result = thisDecimal.subtract(otherDecimal)
            return KBigIntegerImpl(result.toString())
        }

        val result = nsDecimalNumber!!.decimalNumberBySubtracting(otherImpl.nsDecimalNumber!!)
        return KBigIntegerImpl(result.stringValue)
    }

    actual override fun multiply(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl

        if (isLargeNumber || otherImpl.isLargeNumber) {
            val thisDecimal = KBigDecimalImpl(stringValue)
            val otherDecimal = KBigDecimalImpl(otherImpl.stringValue)
            val result = thisDecimal.multiply(otherDecimal)
            return KBigIntegerImpl(result.toString())
        }

        val result = nsDecimalNumber!!.decimalNumberByMultiplyingBy(otherImpl.nsDecimalNumber!!)
        return KBigIntegerImpl(result.stringValue)
    }

    actual override fun divide(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl

        // Check for division by zero
        if (otherImpl.stringValue == "0") {
            throw ArithmeticException("Division by zero")
        }

        if (isLargeNumber || otherImpl.isLargeNumber) {
            val thisDecimal = KBigDecimalImpl(stringValue)
            val otherDecimal = KBigDecimalImpl(otherImpl.stringValue)
            val result = thisDecimal.divide(otherDecimal, 0)
            return KBigIntegerImpl(result.toString().split(".")[0])
        }

        val result = nsDecimalNumber!!.decimalNumberByDividingBy(otherImpl.nsDecimalNumber!!)
        val integerPart = result.stringValue.split(".")[0]
        return KBigIntegerImpl(integerPart)
    }

    actual override fun mod(other: KBigInteger): KBigInteger {
        val otherImpl = other as KBigIntegerImpl

        // Check for modulo by zero
        if (otherImpl.stringValue == "0") {
            throw ArithmeticException("Division by zero")
        }

        if (isLargeNumber || otherImpl.isLargeNumber) {
            // For large numbers, use: a % b = a - (a / b) * b
            val division = divide(other)
            val product = division.multiply(other)
            return subtract(product)
        }

        val division = nsDecimalNumber!!.decimalNumberByDividingBy(otherImpl.nsDecimalNumber!!)
        val integerPart = division.stringValue.split(".")[0]
        val integerDivision = NSDecimalNumber(string = integerPart)
        val product = integerDivision.decimalNumberByMultiplyingBy(otherImpl.nsDecimalNumber!!)
        val result = nsDecimalNumber!!.decimalNumberBySubtracting(product)
        return KBigIntegerImpl(result.stringValue)
    }

    actual override fun abs(): KBigInteger {
        return if (stringValue.startsWith("-")) {
            KBigIntegerImpl(stringValue.substring(1))
        } else {
            this
        }
    }

    actual override fun signum(): Int {
        return when {
            stringValue == "0" -> 0
            stringValue.startsWith("-") -> -1
            else -> 1
        }
    }

    actual override fun compareTo(other: KBigInteger): Int {
        val otherImpl = other as KBigIntegerImpl

        if (isLargeNumber || otherImpl.isLargeNumber) {
            // For large numbers, use string comparison with proper sign handling
            val thisVal = stringValue
            val otherVal = otherImpl.stringValue

            // Handle signs first
            if (thisVal.startsWith("-") && !otherVal.startsWith("-")) return -1
            if (!thisVal.startsWith("-") && otherVal.startsWith("-")) return 1

            val thisAbs = thisVal.removePrefix("-")
            val otherAbs = otherVal.removePrefix("-")
            val isNegative = thisVal.startsWith("-")

            return when {
                thisAbs.length > otherAbs.length -> if (isNegative) -1 else 1
                thisAbs.length < otherAbs.length -> if (isNegative) 1 else -1
                else -> {
                    val comparison = thisAbs.compareTo(otherAbs)
                    if (isNegative) -comparison else comparison
                }
            }
        }

        return nsDecimalNumber!!.compare(otherImpl.nsDecimalNumber!!).toInt()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KBigIntegerImpl) return false
        return stringValue == other.stringValue
    }

    override fun hashCode(): Int {
        return stringValue.hashCode()
    }
}
