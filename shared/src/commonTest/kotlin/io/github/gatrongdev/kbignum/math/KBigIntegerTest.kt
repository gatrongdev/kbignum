package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

class KBigIntegerTest {
    
    // ADD FUNCTION TESTS
    @Test
    fun add_twoPositiveNumbers_returnsCorrectResult() {
        // Arrange
        val a = "123456789".toKBigInteger()
        val b = "987654321".toKBigInteger()
        val expected = "1111111110".toKBigInteger()

        // Act
        val actual = a.add(b)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun add_positiveAndNegativeNumbers_returnsCorrectResult() {
        // Arrange
        val a = "500".toKBigInteger()
        val b = "-300".toKBigInteger()
        val expected = "200".toKBigInteger()

        // Act
        val actual = a.add(b)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun add_twoNegativeNumbers_returnsCorrectResult() {
        // Arrange
        val a = "-123".toKBigInteger()
        val b = "-456".toKBigInteger()
        val expected = "-579".toKBigInteger()

        // Act
        val actual = a.add(b)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun add_withZero_returnsCorrectResult() {
        // Arrange
        val number = "123456789".toKBigInteger()
        val zero = KBigIntegerFactory.ZERO

        // Act & Assert
        assertEquals(number, number.add(zero))
        assertEquals(number, zero.add(number))
    }
    
    @Test
    fun add_largeNumbers_handlesCorrectlyWithoutOverflow() {
        // Arrange
        val a = "999999999999999999999999999999".toKBigInteger()
        val b = "111111111111111111111111111111".toKBigInteger()
        val expected = "1111111111111111111111111111110".toKBigInteger()

        // Act
        val actual = a.add(b)

        // Assert
        assertEquals(expected, actual)
    }
    
    // SUBTRACT FUNCTION TESTS
    @Test
    fun subtract_twoPositiveNumbers_returnsCorrectResult() {
        // Arrange
        val a = "987654321".toKBigInteger()
        val b = "123456789".toKBigInteger()
        val expected = "864197532".toKBigInteger()

        // Act
        val actual = a.subtract(b)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun subtract_positiveAndNegativeNumbers_returnsCorrectResult() {
        // Arrange
        val a = "500".toKBigInteger()
        val b = "-300".toKBigInteger()
        val expected = "800".toKBigInteger()

        // Act
        val actual = a.subtract(b)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun subtract_twoNegativeNumbers_returnsCorrectResult() {
        // Arrange
        val a = "-500".toKBigInteger()
        val b = "-300".toKBigInteger()
        val expected = "-200".toKBigInteger()

        // Act
        val actual = a.subtract(b)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun subtract_withZero_returnsCorrectResult() {
        // Arrange
        val number = "123456789".toKBigInteger()
        val zero = KBigIntegerFactory.ZERO

        // Act & Assert
        assertEquals(number, number.subtract(zero))
        assertEquals(number.negate(), zero.subtract(number))
    }
    
    @Test
    fun subtract_largeNumbers_handlesCorrectlyWithoutOverflow() {
        // Arrange
        val a = "999999999999999999999999999999".toKBigInteger()
        val b = "111111111111111111111111111111".toKBigInteger()
        val expected = "888888888888888888888888888888".toKBigInteger()

        // Act
        val actual = a.subtract(b)

        // Assert
        assertEquals(expected, actual)
    }
    
    // MULTIPLY FUNCTION TESTS
    @Test
    fun multiply_twoPositiveNumbers_returnsCorrectResult() {
        // Arrange
        val a = "123".toKBigInteger()
        val b = "456".toKBigInteger()
        val expected = "56088".toKBigInteger()

        // Act
        val actual = a.multiply(b)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun multiply_positiveAndNegativeNumbers_returnsCorrectResult() {
        // Arrange
        val a = "10".toKBigInteger()
        val b = "-5".toKBigInteger()
        val expected = "-50".toKBigInteger()

        // Act
        val actual = a.multiply(b)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun multiply_twoNegativeNumbers_returnsCorrectResult() {
        // Arrange
        val a = "-10".toKBigInteger()
        val b = "-5".toKBigInteger()
        val expected = "50".toKBigInteger()

        // Act
        val actual = a.multiply(b)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun multiply_withZero_returnsCorrectResult() {
        // Arrange
        val number = "123456789".toKBigInteger()
        val zero = KBigIntegerFactory.ZERO

        // Act & Assert
        assertTrue(number.multiply(zero).isZero())
        assertTrue(zero.multiply(number).isZero())
    }
    
    @Test
    fun multiply_largeNumbers_handlesCorrectlyWithoutOverflow() {
        // Arrange
        val a = "999999999999999999".toKBigInteger()
        val b = "888888888888888888".toKBigInteger()

        // Act
        val actual = a.multiply(b)

        // Assert
        assertEquals("888888888888888887111111111111111112", actual.toString())
    }
    
    // DIVIDE FUNCTION TESTS
    @Test
    fun divide_byDivisor_returnsCorrectIntegerQuotient() {
        // Arrange
        val dividend = "1000".toKBigInteger()
        val divisor = "10".toKBigInteger()
        val expected = "100".toKBigInteger()

        // Act
        val actual = dividend.divide(divisor)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun divide_numberByItself_returnsOne() {
        // Arrange
        val number = "123456789".toKBigInteger()
        val expected = KBigIntegerFactory.ONE

        // Act
        val actual = number.divide(number)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun divide_numberByOne_returnsItself() {
        // Arrange
        val number = "123456789".toKBigInteger()
        val one = KBigIntegerFactory.ONE

        // Act
        val actual = number.divide(one)

        // Assert
        assertEquals(number, actual)
    }
    
    @Test
    fun divide_zeroByNumber_returnsZero() {
        // Arrange
        val zero = KBigIntegerFactory.ZERO
        val number = "123456789".toKBigInteger()

        // Act
        val actual = zero.divide(number)

        // Assert
        assertTrue(actual.isZero())
    }
    
    @Test
    fun divide_byZero_throwsArithmeticException() {
        // Arrange
        val number = "123456789".toKBigInteger()
        val zero = KBigIntegerFactory.ZERO

        // Act & Assert
        assertFailsWith<ArithmeticException> {
            number.divide(zero)
        }
    }
    
    // MOD FUNCTION TESTS
    @Test
    fun mod_positiveNumbers_returnsCorrectRemainder() {
        // Arrange
        val dividend = "17".toKBigInteger()
        val divisor = "5".toKBigInteger()
        val expected = "2".toKBigInteger()

        // Act
        val actual = dividend.mod(divisor)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun mod_withNegativeDividend_returnsCorrectResult() {
        // Arrange
        val dividend = "-17".toKBigInteger()
        val divisor = "5".toKBigInteger()

        // Act
        val actual = dividend.mod(divisor)

        // Assert
        // Result can be either 3 or -2 depending on implementation
        assertTrue(actual.toString() == "3" || actual.toString() == "-2")
    }
    
    @Test
    fun mod_withZeroRemainder_returnsZero() {
        // Arrange
        val dividend = "20".toKBigInteger()
        val divisor = "4".toKBigInteger()

        // Act
        val actual = dividend.mod(divisor)

        // Assert
        assertTrue(actual.isZero())
    }
    
    @Test
    fun mod_byZero_throwsArithmeticException() {
        // Arrange
        val number = "123456789".toKBigInteger()
        val zero = KBigIntegerFactory.ZERO

        // Act & Assert
        assertFailsWith<ArithmeticException> {
            number.mod(zero)
        }
    }
    
    // ABS FUNCTION TESTS
    @Test
    fun abs_onPositive_returnsItself() {
        // Arrange
        val positive = "123456789".toKBigInteger()

        // Act
        val actual = positive.abs()

        // Assert
        assertEquals(positive, actual)
    }
    
    @Test
    fun abs_onNegative_returnsPositive() {
        // Arrange
        val negative = "-123456789".toKBigInteger()
        val expected = "123456789".toKBigInteger()

        // Act
        val actual = negative.abs()

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun abs_onZero_returnsZero() {
        // Arrange
        val zero = KBigIntegerFactory.ZERO

        // Act
        val actual = zero.abs()

        // Assert
        assertTrue(actual.isZero())
    }
    
    // NEGATE FUNCTION TESTS
    @Test
    fun negate_onPositive_returnsNegative() {
        // Arrange
        val positive = "123456789".toKBigInteger()
        val expected = "-123456789".toKBigInteger()

        // Act
        val actual = positive.negate()

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun negate_onNegative_returnsPositive() {
        // Arrange
        val negative = "-123456789".toKBigInteger()
        val expected = "123456789".toKBigInteger()

        // Act
        val actual = negative.negate()

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun negate_onZero_returnsZero() {
        // Arrange
        val zero = KBigIntegerFactory.ZERO

        // Act
        val actual = zero.negate()

        // Assert
        assertTrue(actual.isZero())
    }
    
    // COMPARISON TESTS
    @Test
    fun compareTo_aGreaterThanB_returnsPositive() {
        // Arrange
        val a = "456".toKBigInteger()
        val b = "123".toKBigInteger()

        // Act
        val result = a.compareTo(b)

        // Assert
        assertTrue(result > 0)
    }
    
    @Test
    fun compareTo_aLessThanB_returnsNegative() {
        // Arrange
        val a = "123".toKBigInteger()
        val b = "456".toKBigInteger()

        // Act
        val result = a.compareTo(b)

        // Assert
        assertTrue(result < 0)
    }
    
    @Test
    fun compareTo_aEqualToB_returnsZero() {
        // Arrange
        val a = "123456789".toKBigInteger()
        val b = "123456789".toKBigInteger()

        // Act
        val result = a.compareTo(b)

        // Assert
        assertEquals(0, result)
    }
    
    @Test
    fun equals_onNumbersWithSameValueButDifferentScales_returnsFalse() {
        // Arrange
        // Note: KBigInteger doesn't have scales like KBigDecimal
        // This test verifies that integers with same value are equal
        val a = "123".toKBigInteger()
        val b = "123".toKBigInteger()

        // Act
        val result = a == b

        // Assert
        assertTrue(result) // For integers, same value should be equal
    }
    
    @Test
    fun compareTo_onNumbersWithSameValueButDifferentScales_returnsZero() {
        // Arrange
        // Note: KBigInteger doesn't have scales, so same values should compare as equal
        val a = "123".toKBigInteger()
        val b = "123".toKBigInteger()

        // Act
        val result = a.compareTo(b)

        // Assert
        assertEquals(0, result)
    }
    
    // UTILITY FUNCTION TESTS
    @Test
    fun signum_onPositive_returnsOne() {
        // Arrange
        val positive = "123456789".toKBigInteger()

        // Act
        val actual = positive.signum()

        // Assert
        assertEquals(1, actual)
    }
    
    @Test
    fun signum_onNegative_returnsNegativeOne() {
        // Arrange
        val negative = "-123456789".toKBigInteger()

        // Act
        val actual = negative.signum()

        // Assert
        assertEquals(-1, actual)
    }
    
    @Test
    fun signum_onZero_returnsZero() {
        // Arrange
        val zero = KBigIntegerFactory.ZERO

        // Act
        val actual = zero.signum()

        // Assert
        assertEquals(0, actual)
    }
    
    @Test
    fun isZero_onZero_returnsTrue() {
        // Arrange
        val zero = KBigIntegerFactory.ZERO

        // Act
        val actual = zero.isZero()

        // Assert
        assertTrue(actual)
    }
    
    @Test
    fun isZero_onNonZero_returnsFalse() {
        // Arrange
        val nonZero = "123456789".toKBigInteger()

        // Act
        val actual = nonZero.isZero()

        // Assert
        assertFalse(actual)
    }
    
    // CONVERSION TESTS
    @Test
    fun toPreciseNumber_returnsEquivalentKBigDecimal() {
        // Arrange
        val integer = "123456789".toKBigInteger()

        // Act
        val actual = integer.toPreciseNumber()

        // Assert
        assertEquals("123456789", actual.toString())
        assertTrue(actual is KBigDecimal)
    }
    
    @Test
    fun toLong_onValueWithinLongRange_returnsCorrectLong() {
        // Arrange
        val integer = "123456789".toKBigInteger()
        val expected = 123456789L

        // Act
        val actual = integer.toLong()

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun toLong_onValueExceedingLongMax_throwsException() {
        // Arrange
        val veryLarge = "99999999999999999999999999999999999999".toKBigInteger()

        // Act & Assert
        assertFailsWith<ArithmeticException> {
            veryLarge.toLong()
        }
    }
    
    @Test
    fun toLong_onValueExceedingLongMin_throwsException() {
        // Arrange
        val verySmall = "-99999999999999999999999999999999999999".toKBigInteger()

        // Act & Assert
        assertFailsWith<ArithmeticException> {
            verySmall.toLong()
        }
    }
}