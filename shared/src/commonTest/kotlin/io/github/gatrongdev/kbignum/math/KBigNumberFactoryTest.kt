package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

class KBigNumberFactoryTest {

    // KBIGDECIMAL FACTORY TESTS
    @Test
    fun factoryFromString_withValidInput_returnsCorrectKBigDecimal() {
        // Arrange
        val validInput = "123.456"
        val expected = "123.456"

        // Act
        val actual = KBigDecimalFactory.fromString(validInput)

        // Assert
        assertEquals(expected, actual.toString())
    }
    
    @Test
    fun kBigDecimalFactoryFromString_withInvalidInput_throwsNumberFormatException() {
        // Arrange
        val invalidInput = "abc"

        // Act & Assert
        assertFailsWith<NumberFormatException> {
            KBigDecimalFactory.fromString(invalidInput)
        }
    }
    
    @Test
    fun factoryFromInt_returnsCorrectKBigDecimal() {
        // Arrange
        val intValue = 123
        val expected = "123"

        // Act
        val actual = KBigDecimalFactory.fromInt(intValue)

        // Assert
        assertEquals(expected, actual.toString())
    }
    
    @Test
    fun factoryFromLong_returnsCorrectKBigDecimal() {
        // Arrange
        val longValue = 123456789L
        val expected = "123456789"

        // Act
        val actual = KBigDecimalFactory.fromLong(longValue)

        // Assert
        assertEquals(expected, actual.toString())
    }
    
    @Test
    fun kBigDecimalFactoryConstants_ZeroOneTen_haveCorrectValues() {
        // Arrange & Act & Assert
        assertEquals("0", KBigDecimalFactory.ZERO.toString())
        assertEquals("1", KBigDecimalFactory.ONE.toString())
        assertEquals("10", KBigDecimalFactory.TEN.toString())
        
        assertTrue(KBigDecimalFactory.ZERO.isZero())
        assertTrue(KBigDecimalFactory.ONE.isPositive())
        assertTrue(KBigDecimalFactory.TEN.isPositive())
    }
    
    // KBIGINTEGER FACTORY TESTS
    @Test
    fun factoryFromString_withValidInteger_returnsCorrectKBigInteger() {
        // Arrange
        val validInteger = "123456789"
        val expected = "123456789"

        // Act
        val actual = KBigIntegerFactory.fromString(validInteger)

        // Assert
        assertEquals(expected, actual.toString())
    }
    
    @Test
    fun kBigIntegerFactoryFromString_withInvalidInput_throwsNumberFormatException() {
        // Arrange
        val invalidInput = "123.456"

        // Act & Assert
        assertFailsWith<NumberFormatException> {
            KBigIntegerFactory.fromString(invalidInput)
        }
    }
    
    @Test
    fun factoryFromInt_returnsCorrectKBigInteger() {
        // Arrange
        val intValue = 123
        val expected = "123"

        // Act
        val actual = KBigIntegerFactory.fromInt(intValue)

        // Assert
        assertEquals(expected, actual.toString())
    }
    
    @Test
    fun factoryFromLong_returnsCorrectKBigInteger() {
        // Arrange
        val longValue = 123456789L
        val expected = "123456789"

        // Act
        val actual = KBigIntegerFactory.fromLong(longValue)

        // Assert
        assertEquals(expected, actual.toString())
    }
    
    @Test
    fun kBigIntegerFactoryConstants_ZeroOneTen_haveCorrectValues() {
        // Arrange & Act & Assert
        assertEquals("0", KBigIntegerFactory.ZERO.toString())
        assertEquals("1", KBigIntegerFactory.ONE.toString())
        assertEquals("10", KBigIntegerFactory.TEN.toString())
        
        assertTrue(KBigIntegerFactory.ZERO.isZero())
        assertTrue(KBigIntegerFactory.ONE.isPositive())
        assertTrue(KBigIntegerFactory.TEN.isPositive())
    }
}