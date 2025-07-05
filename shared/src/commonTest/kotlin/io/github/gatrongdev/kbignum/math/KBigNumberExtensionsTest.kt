package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

class KBigNumberExtensionsTest {

    // STRING TO BIGNUMBER CONVERSION TESTS
    @Test
    fun stringToKBigDecimal_withValidDecimalString_succeeds() {
        // Arrange
        val validDecimal = "123.456"
        val expected = "123.456"

        // Act
        val actual = validDecimal.toKBigDecimal()

        // Assert
        assertEquals(expected, actual.toString())
    }
    
    @Test
    fun stringToKBigDecimal_withValidIntegerString_succeeds() {
        // Arrange
        val validInteger = "123"
        val expected = "123"

        // Act
        val actual = validInteger.toKBigDecimal()

        // Assert
        assertEquals(expected, actual.toString())
    }
    
    @Test
    fun stringToKBigDecimal_withLeadingPlusSign_isParsedCorrectly() {
        // Arrange
        val positiveString = "+123.456"
        val expected = "123.456"

        // Act
        val actual = positiveString.toKBigDecimal()

        // Assert
        assertEquals(expected, actual.toString())
    }
    
    @Test
    fun stringToKBigDecimal_withWhitespace_isTrimmedAndParsedCorrectly() {
        // Arrange
        val whitespaceString = "  123.456  "
        val expected = "123.456"

        // Act
        val actual = whitespaceString.trim().toKBigDecimal()

        // Assert
        assertEquals(expected, actual.toString())
    }
    
    @Test
    fun stringToKBigDecimal_withInvalidString_throwsNumberFormatException() {
        // Arrange
        val invalidString = "abc"

        // Act & Assert
        assertFailsWith<NumberFormatException> {
            invalidString.toKBigDecimal()
        }
    }
    
    @Test
    fun stringToKBigInteger_withValidIntegerString_succeeds() {
        // Arrange
        val validInteger = "123456789"
        val expected = "123456789"

        // Act
        val actual = validInteger.toKBigInteger()

        // Assert
        assertEquals(expected, actual.toString())
    }
    
    @Test
    fun stringToKBigInteger_withLeadingPlusSign_isParsedCorrectly() {
        // Arrange
        val positiveString = "+123456789"
        val expected = "123456789"

        // Act
        val actual = positiveString.toKBigInteger()

        // Assert
        assertEquals(expected, actual.toString())
    }
    
    @Test
    fun stringToKBigInteger_withWhitespace_isTrimmedAndParsedCorrectly() {
        // Arrange
        val whitespaceString = "  123456789  "
        val expected = "123456789"

        // Act
        val actual = whitespaceString.trim().toKBigInteger()

        // Assert
        assertEquals(expected, actual.toString())
    }
    
    @Test
    fun stringToKBigInteger_withDecimalString_throwsNumberFormatException() {
        // Arrange
        val decimalString = "123.456"

        // Act & Assert
        assertFailsWith<NumberFormatException> {
            decimalString.toKBigInteger()
        }
    }
    
    @Test
    fun stringToKBigInteger_withInvalidString_throwsNumberFormatException() {
        // Arrange
        val invalidString = "abc"

        // Act & Assert
        assertFailsWith<NumberFormatException> {
            invalidString.toKBigInteger()
        }
    }
    
    // PRIMITIVE TO BIGNUMBER CONVERSION TESTS
    @Test
    fun intToKBigDecimal_convertsCorrectly() {
        // Arrange
        val intValue = 123
        val expected = "123"

        // Act
        val actual = intValue.toKBigDecimal()

        // Assert
        assertEquals(expected, actual.toString())
    }
    
    @Test
    fun intToKBigInteger_convertsCorrectly() {
        // Arrange
        val intValue = 123
        val expected = "123"

        // Act
        val actual = intValue.toKBigInteger()

        // Assert
        assertEquals(expected, actual.toString())
    }
    
    @Test
    fun longToKBigDecimal_convertsCorrectly() {
        // Arrange
        val longValue = 123456789L
        val expected = "123456789"

        // Act
        val actual = longValue.toKBigDecimal()

        // Assert
        assertEquals(expected, actual.toString())
    }
    
    @Test
    fun longToKBigInteger_convertsCorrectly() {
        // Arrange
        val longValue = 123456789L
        val expected = "123456789"

        // Act
        val actual = longValue.toKBigInteger()

        // Assert
        assertEquals(expected, actual.toString())
    }
    
    @Test
    fun doubleToKBigDecimal_withStandardValue_convertsCorrectly() {
        // Arrange
        val doubleValue = 123.456
        val expected = "123.456"

        // Act
        val actual = doubleValue.toString().toKBigDecimal()

        // Assert
        assertEquals(expected, actual.toString())
    }
    
    @Test
    fun doubleToKBigDecimal_withSpecialValues_handlesCorrectly() {
        // Arrange
        val nanValue = Double.NaN
        val infinityValue = Double.POSITIVE_INFINITY

        // Act & Assert
        assertFailsWith<NumberFormatException> {
            nanValue.toString().toKBigDecimal()
        }
        assertFailsWith<NumberFormatException> {
            infinityValue.toString().toKBigDecimal()
        }
    }
    
    @Test
    fun floatToKBigDecimal_convertsCorrectly() {
        // Arrange
        val floatValue = 123.456f
        val expected = "123.456"

        // Act
        val actual = floatValue.toString().toKBigDecimal()

        // Assert
        assertEquals(expected, actual.toString())
    }
    
    // OPERATORS TESTS
    @Test
    fun plusOperator_onTwoKBigIntegers_matchesAddMethod() {
        // Arrange
        val a = "123".toKBigInteger()
        val b = "456".toKBigInteger()

        // Act
        val operatorResult = a + b
        val methodResult = a.add(b)

        // Assert
        assertEquals(methodResult.toString(), operatorResult.toString())
    }
    
    @Test
    fun minusOperator_onTwoKBigDecimals_matchesSubtractMethod() {
        // Arrange
        val a = "123.456".toKBigDecimal()
        val b = "67.89".toKBigDecimal()

        // Act
        val operatorResult = a - b
        val methodResult = a.subtract(b)

        // Assert
        assertEquals(methodResult.toString(), operatorResult.toString())
    }
    
    @Test
    fun timesOperator_onTwoKBigIntegers_matchesMultiplyMethod() {
        // Arrange
        val a = "123".toKBigInteger()
        val b = "456".toKBigInteger()

        // Act
        val operatorResult = a * b
        val methodResult = a.multiply(b)

        // Assert
        assertEquals(methodResult.toString(), operatorResult.toString())
    }
    
    @Test
    fun divOperator_onTwoKBigIntegers_matchesDivideMethod() {
        // Arrange
        val a = "456".toKBigInteger()
        val b = "123".toKBigInteger()

        // Act
        val operatorResult = a / b
        val methodResult = a.divide(b)

        // Assert
        assertEquals(methodResult.toString(), operatorResult.toString())
    }
    
    @Test
    fun remOperator_onTwoKBigIntegers_matchesModMethod() {
        // Arrange
        val a = "456".toKBigInteger()
        val b = "123".toKBigInteger()

        // Act
        val operatorResult = a % b
        val methodResult = a.mod(b)

        // Assert
        assertEquals(methodResult.toString(), operatorResult.toString())
    }
    
    @Test
    fun plusOperator_onTwoKBigDecimals_matchesAddMethod() {
        // Arrange
        val a = "123.45".toKBigDecimal()
        val b = "67.89".toKBigDecimal()

        // Act
        val operatorResult = a + b
        val methodResult = a.add(b)

        // Assert
        assertEquals(methodResult.toString(), operatorResult.toString())
    }
    
    @Test
    fun timesOperator_onTwoKBigDecimals_matchesMultiplyMethod() {
        // Arrange
        val a = "123.45".toKBigDecimal()
        val b = "67.89".toKBigDecimal()

        // Act
        val operatorResult = a * b
        val methodResult = a.multiply(b)

        // Assert
        assertEquals(methodResult.toString(), operatorResult.toString())
    }
    
    @Test
    fun divOperator_onTwoKBigDecimals_matchesDivideMethod() {
        // Arrange
        val a = "123.45".toKBigDecimal()
        val b = "67.89".toKBigDecimal()

        // Act
        val operatorResult = a / b
        val methodResult = a.divide(b, 10, RoundingMode.HALF_UP)

        // Assert
        assertEquals(methodResult.toString(), operatorResult.toString())
    }
    
    @Test
    fun minusOperator_onTwoKBigIntegers_matchesSubtractMethod() {
        // Arrange
        val a = "456".toKBigInteger()
        val b = "123".toKBigInteger()

        // Act
        val operatorResult = a - b
        val methodResult = a.subtract(b)

        // Assert
        assertEquals(methodResult.toString(), operatorResult.toString())
    }
    
    // UTILITY FUNCTIONS TESTS
    @Test
    fun max_returnsTheLargerOfTwoNumbers() {
        // Arrange
        val smaller = "10".toKBigDecimal()
        val larger = "20".toKBigDecimal()

        // Act
        val actual = smaller.max(larger)

        // Assert
        assertEquals(larger, actual)
    }
    
    @Test
    fun min_returnsTheSmallerOfTwoNumbers() {
        // Arrange
        val smaller = "10".toKBigDecimal()
        val larger = "20".toKBigDecimal()

        // Act
        val actual = larger.min(smaller)

        // Assert
        assertEquals(smaller, actual)
    }
}