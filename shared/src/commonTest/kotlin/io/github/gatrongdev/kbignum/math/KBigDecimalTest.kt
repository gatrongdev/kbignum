package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KBigDecimalTest {
    // ADD FUNCTION TESTS
    @Test
    fun add_twoPositiveNumbers_returnsCorrectSum() {
        // Arrange
        val a = "123.45".toKBigDecimal()
        val b = "67.89".toKBigDecimal()
        val expected = "191.34".toKBigDecimal()

        // Act
        val actual = a.add(b)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun add_positiveAndNegativeNumbers_returnsCorrectSum() {
        // Arrange
        val a = "123.45".toKBigDecimal()
        val b = "-67.89".toKBigDecimal()
        val expected = "55.56".toKBigDecimal()

        // Act
        val actual = a.add(b)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun add_twoNegativeNumbers_returnsCorrectSum() {
        // Arrange
        val a = "-123.45".toKBigDecimal()
        val b = "-67.89".toKBigDecimal()
        val expected = "-191.34".toKBigDecimal()

        // Act
        val actual = a.add(b)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun add_numberToZero_returnsTheNumberItself() {
        // Arrange
        val number = "123.45".toKBigDecimal()
        val zero = KBigDecimalFactory.ZERO
        val expected = number

        // Act
        val actual = number.add(zero)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun add_zeroToNumber_returnsTheNumberItself() {
        // Arrange
        val number = "123.45".toKBigDecimal()
        val zero = KBigDecimalFactory.ZERO
        val expected = number

        // Act
        val actual = zero.add(number)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun add_numbersWithDifferentScales_returnsCorrectSumAndScale() {
        // Arrange
        val a = "123.4".toKBigDecimal()
        val b = "67.895".toKBigDecimal()
        val expected = "191.295".toKBigDecimal()

        // Act
        val actual = a.add(b)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun add_largeNumbers_handlesCorrectlyWithoutOverflow() {
        // Arrange
        val a = "1234567890.1234567890".toKBigDecimal()
        val b = "9876543210.9876543210".toKBigDecimal()

        val expected = "11111111101.1111111100".toKBigDecimal()

        // Act
        val actual = a.add(b)

        // Assert
        assertEquals(expected, actual)
    }

    // SUBTRACT FUNCTION TESTS
    @Test
    fun subtract_positiveNumbers_returnsCorrectDifference() {
        // Arrange
        val a = "123.45".toKBigDecimal()
        val b = "67.89".toKBigDecimal()
        val expected = "55.56".toKBigDecimal()

        // Act
        val actual = a.subtract(b)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun subtract_positiveAndNegativeNumbers_returnsCorrectDifference() {
        // Arrange
        val a = "123.45".toKBigDecimal()
        val b = "-67.89".toKBigDecimal()
        val expected = "191.34".toKBigDecimal()

        // Act
        val actual = a.subtract(b)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun subtract_twoNegativeNumbers_returnsCorrectDifference() {
        // Arrange
        val a = "-123.45".toKBigDecimal()
        val b = "-67.89".toKBigDecimal()
        val expected = "-55.56".toKBigDecimal()

        // Act
        val actual = a.subtract(b)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun subtract_numberFromItself_returnsZero() {
        // Arrange
        val number = "123.45".toKBigDecimal()
        val expected = KBigDecimalFactory.ZERO

        // Act
        val actual = number.subtract(number)

        // Assert
        assertTrue(actual.isZero())
    }

    @Test
    fun subtract_zeroFromNumber_returnsTheNumberItself() {
        // Arrange
        val number = "123.45".toKBigDecimal()
        val zero = KBigDecimalFactory.ZERO
        val expected = number

        // Act
        val actual = number.subtract(zero)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun subtract_numberFromZero_returnsTheNegatedNumber() {
        // Arrange
        val number = "123.45".toKBigDecimal()
        val zero = KBigDecimalFactory.ZERO
        val expected = "-123.45".toKBigDecimal()

        // Act
        val actual = zero.subtract(number)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun subtract_numbersWithDifferentScales_returnsCorrectDifferenceAndScale() {
        // Arrange
        val a = "123.4".toKBigDecimal()
        val b = "67.895".toKBigDecimal()
        val expected = "55.505".toKBigDecimal()

        // Act
        val actual = a.subtract(b)

        // Assert
        assertEquals(expected, actual)
    }

    // MULTIPLY FUNCTION TESTS
    @Test
    fun multiply_twoPositiveNumbers_returnsCorrectProduct() {
        // Arrange
        val a = "123.45".toKBigDecimal()
        val b = "67.89".toKBigDecimal()
        val expected = "8381.0205".toKBigDecimal()

        // Act
        val actual = a.multiply(b)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun multiply_positiveAndNegativeNumbers_returnsCorrectProduct() {
        // Arrange
        val a = "123.45".toKBigDecimal()
        val b = "-67.89".toKBigDecimal()
        val expected = "-8381.0205".toKBigDecimal()

        // Act
        val actual = a.multiply(b)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun multiply_twoNegativeNumbers_returnsCorrectProduct() {
        // Arrange
        val a = "-123.45".toKBigDecimal()
        val b = "-67.89".toKBigDecimal()
        val expected = "8381.0205".toKBigDecimal()

        // Act
        val actual = a.multiply(b)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun multiply_byZero_returnsZero() {
        // Arrange
        val number = "123.45".toKBigDecimal()
        val zero = KBigDecimalFactory.ZERO

        // Act
        val actual = number.multiply(zero)

        // Assert
        assertTrue(actual.isZero())
    }

    @Test
    fun multiply_byOne_returnsTheNumberItself() {
        // Arrange
        val number = "123.45".toKBigDecimal()
        val one = KBigDecimalFactory.ONE
        val expected = number

        // Act
        val actual = number.multiply(one)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun multiply_byNegativeOne_returnsTheNegatedNumber() {
        // Arrange
        val number = "123.45".toKBigDecimal()
        val negativeOne = "-1".toKBigDecimal()
        val expected = "-123.45".toKBigDecimal()

        // Act
        val actual = number.multiply(negativeOne)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun multiply_numbersWithDifferentScales_returnsCorrectProductAndScale() {
        // Arrange
        val a = "123.4".toKBigDecimal()
        val b = "67.895".toKBigDecimal()
        val expected = "8378.2430".toKBigDecimal()

        // Act
        val actual = a.multiply(b)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun multiply_largeNumbers_handlesCorrectlyWithoutOverflow() {
        // Arrange
        val a = "123456789012345678901234567890.123456789012345678901234567890".toKBigDecimal()
        val b = "2".toKBigDecimal()

        // Act
        val actual = a.multiply(b)

        // Assert
        val actualStr = actual.toString()
        assertTrue(actualStr.startsWith("246913578024691357802469135780"))
    }

    // DIVIDE FUNCTION TESTS
    @Test
    fun divide_byIntegerDivisor_returnsCorrectQuotient() {
        // Arrange
        val dividend = "123.45".toKBigDecimal()
        val divisor = "5".toKBigDecimal()
        val expected = "24.69".toKBigDecimal()

        // Act
        val actual = dividend.divide(divisor, 2, RoundingMode.HALF_UP)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun divide_numberByItself_returnsOne() {
        // Arrange
        val number = "123.45".toKBigDecimal()
        val expected = "1.00".toKBigDecimal()

        // Act
        val actual = number.divide(number, 2, RoundingMode.HALF_UP)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun divide_numberByOne_returnsItself() {
        // Arrange
        val number = "123.45".toKBigDecimal()
        val one = KBigDecimalFactory.ONE
        val expected = "123.45".toKBigDecimal()

        // Act
        val actual = number.divide(one, 2, RoundingMode.HALF_UP)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun divide_zeroByNumber_returnsZero() {
        // Arrange
        val zero = KBigDecimalFactory.ZERO
        val number = "123.45".toKBigDecimal()

        // Act
        val actual = zero.divide(number, 2, RoundingMode.HALF_UP)

        // Assert
        assertTrue(actual.isZero())
    }

    @Test
    fun divide_byZero_throwsArithmeticException() {
        // Arrange
        val number = "123.45".toKBigDecimal()
        val zero = KBigDecimalFactory.ZERO

        // Act & Assert
        assertFailsWith<ArithmeticException> {
            number.divide(zero, 2, RoundingMode.HALF_UP)
        }
    }

    @Test
    fun divide_positiveAndNegativeNumbers_returnsCorrectlySignedQuotient() {
        // Arrange
        val positive = "123.45".toKBigDecimal()
        val negative = "-5".toKBigDecimal()
        val expected = "-24.69".toKBigDecimal()

        // Act
        val actual = positive.divide(negative, 2, RoundingMode.HALF_UP)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun divide_twoNegativeNumbers_returnsCorrectlySignedQuotient() {
        // Arrange
        val negative1 = "-123.45".toKBigDecimal()
        val negative2 = "-5".toKBigDecimal()
        val expected = "24.69".toKBigDecimal()

        // Act
        val actual = negative1.divide(negative2, 2, RoundingMode.HALF_UP)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun divide_withRoundingNeeded_appliesUpCorrectly() {
        // Arrange
        val dividend = "22".toKBigDecimal()
        val divisor = "7".toKBigDecimal()
        val expected = "3.15".toKBigDecimal()

        // Act
        val actual = dividend.divide(divisor, 2, RoundingMode.UP)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun divide_withRoundingNeeded_appliesDownCorrectly() {
        // Arrange
        val dividend = "22".toKBigDecimal()
        val divisor = "7".toKBigDecimal()
        val expected = "3.14".toKBigDecimal()

        // Act
        val actual = dividend.divide(divisor, 2, RoundingMode.DOWN)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun divide_withRoundingNeeded_appliesCeilingCorrectly() {
        // Arrange
        val dividend = "22".toKBigDecimal()
        val divisor = "7".toKBigDecimal()
        val expected = "3.15".toKBigDecimal()

        // Act
        val actual = dividend.divide(divisor, 2, RoundingMode.CEILING)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun divide_withRoundingNeeded_appliesFloorCorrectly() {
        // Arrange
        val dividend = "22".toKBigDecimal()
        val divisor = "7".toKBigDecimal()
        val expected = "3.14".toKBigDecimal()

        // Act
        val actual = dividend.divide(divisor, 2, RoundingMode.FLOOR)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun divide_withRoundingNeeded_appliesHalfUpCorrectly() {
        // Arrange
        val dividend = "22".toKBigDecimal()
        val divisor = "7".toKBigDecimal()
        val expected = "3.14".toKBigDecimal()

        // Act
        val actual = dividend.divide(divisor, 2, RoundingMode.HALF_UP)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun divide_withRoundingNeeded_appliesHalfDownCorrectly() {
        // Arrange
        val dividend = "22".toKBigDecimal()
        val divisor = "7".toKBigDecimal()
        val expected = "3.14".toKBigDecimal()

        // Act
        val actual = dividend.divide(divisor, 2, RoundingMode.HALF_DOWN)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun divide_withRoundingNeeded_appliesHalfEvenCorrectly() {
        // Arrange
        val dividend = "22".toKBigDecimal()
        val divisor = "7".toKBigDecimal()
        val expected = "3.14".toKBigDecimal()

        // Act
        val actual = dividend.divide(divisor, 2, RoundingMode.HALF_EVEN)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun divide_whenRoundingIsUnnecessaryButModeIsSet_doesNotThrowException() {
        // Arrange
        val dividend = "10".toKBigDecimal()
        val divisor = "2".toKBigDecimal()
        val expected = "5.00".toKBigDecimal()

        // Act
        val actual = dividend.divide(divisor, 2, RoundingMode.UNNECESSARY)

        // Assert
        assertEquals(expected, actual)
    }

    // SINGLE-PARAMETER DIVIDE FUNCTION TESTS
    @Test
    fun divide_singleParameter_byIntegerDivisor_returnsCorrectQuotient() {
        // Arrange
        val dividend = "123.45".toKBigDecimal()
        val divisor = "2".toKBigDecimal()
        val expected = "61.725".toKBigDecimal()

        // Act
        val actual = dividend.divide(divisor)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun divide_singleParameter_numberByItself_returnsOne() {
        // Arrange
        val number = "123.45".toKBigDecimal()
        val expected = "1.00".toKBigDecimal()

        // Act
        val actual = number.divide(number)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun divide_singleParameter_numberByOne_returnsItself() {
        // Arrange
        val number = "123.45".toKBigDecimal()
        val one = "1".toKBigDecimal()

        // Act
        val actual = number.divide(one)

        // Assert
        assertEquals(number, actual)
    }

    @Test
    fun divide_singleParameter_zeroByNumber_returnsZero() {
        // Arrange
        val zero = KBigDecimalFactory.ZERO
        val number = "123.45".toKBigDecimal()

        // Act
        val actual = zero.divide(number)

        // Assert
        assertTrue(actual.isZero())
    }

    @Test
    fun divide_singleParameter_byZero_throwsArithmeticException() {
        // Arrange
        val number = "123.45".toKBigDecimal()
        val zero = KBigDecimalFactory.ZERO

        // Act & Assert
        assertFailsWith<ArithmeticException> {
            number.divide(zero)
        }
    }

    @Test
    fun divide_singleParameter_positiveAndNegativeNumbers_returnsCorrectlySignedQuotient() {
        // Arrange
        val positive = "123.45".toKBigDecimal() // scale 2
        val negative = "-67.89".toKBigDecimal() // scale 2
        val expected = "-1.82".toKBigDecimal() // scale 2 (consistent with same scales rule)

        // Act
        val actual = positive.divide(negative)

        // Assert
        assertEquals(expected.toString(), actual.toString())
    }

    @Test
    fun divide_singleParameter_twoNegativeNumbers_returnsPositiveQuotient() {
        // Arrange
        val negative1 = "-123.45".toKBigDecimal() // scale 2
        val negative2 = "-67.89".toKBigDecimal() // scale 2
        val expected = "1.82".toKBigDecimal() // scale 2 (consistent with same scales rule)

        // Act
        val actual = negative1.divide(negative2)

        // Assert
        assertEquals(expected.toString(), actual.toString())
    }

    @Test
    fun divide_singleParameter_withDifferentScales_usesMaxScale() {
        // Arrange
        val dividend = "123.4567".toKBigDecimal() // scale 4
        val divisor = "12.34".toKBigDecimal() // scale 2
        // Expected scale should be max(4, 2) = 4

        // Act
        val actual = dividend.divide(divisor)

        // Assert
        assertEquals(4, actual.scale())
        assertTrue(actual.toString().startsWith("10.00"))
    }

    @Test
    fun divide_singleParameter_withSameScales_maintainsScale() {
        // Arrange
        val dividend = "123.45".toKBigDecimal() // scale 2
        val divisor = "12.34".toKBigDecimal() // scale 2
        // Expected scale should be max(2, 2) = 2

        // Act
        val actual = dividend.divide(divisor)

        // Assert
        assertEquals(2, actual.scale())
        assertTrue(actual.toString().startsWith("10.00"))
    }

    @Test
    fun divide_singleParameter_veryLargeNumbers_handlesCorrectly() {
        // Arrange
        val dividend = "999999999999999999999999.123456789".toKBigDecimal()
        val divisor = "999999999999999999999999".toKBigDecimal()

        // Act
        val actual = dividend.divide(divisor)

        // Assert
        assertTrue(actual.toString().startsWith("1.000000000000000000000000123"))
    }

    // SETSCALE FUNCTION TESTS
    @Test
    fun setScale_toIncreaseScale_padsWithZeros() {
        // Arrange
        val number = "123.45".toKBigDecimal()
        val expected = "123.45000".toKBigDecimal()

        // Act
        val actual = number.setScale(5, RoundingMode.UNNECESSARY)

        // Assert
        assertEquals(expected, actual)
        assertEquals(5, actual.scale())
    }

    @Test
    fun setScale_toDecreaseScaleWithRoundingUp_roundsCorrectly() {
        // Arrange
        val number = "123.456".toKBigDecimal()
        val expected = "123.46".toKBigDecimal()

        // Act
        val actual = number.setScale(2, RoundingMode.UP)

        // Assert
        assertEquals(expected, actual)
        assertEquals(2, actual.scale())
    }

    @Test
    fun setScale_toDecreaseScaleWithRoundingDown_roundsCorrectly() {
        // Arrange
        val number = "123.456".toKBigDecimal()
        val expected = "123.45".toKBigDecimal()

        // Act
        val actual = number.setScale(2, RoundingMode.DOWN)

        // Assert
        assertEquals(expected, actual)
        assertEquals(2, actual.scale())
    }

    @Test
    fun setScale_toDecreaseScaleWithRoundingCeiling_roundsCorrectly() {
        // Arrange
        val number = "123.451".toKBigDecimal()
        val expected = "123.46".toKBigDecimal()

        // Act
        val actual = number.setScale(2, RoundingMode.CEILING)

        // Assert
        assertEquals(expected, actual)
        assertEquals(2, actual.scale())
    }

    @Test
    fun setScale_toDecreaseScaleWithRoundingFloor_roundsCorrectly() {
        // Arrange
        val number = "123.459".toKBigDecimal()
        val expected = "123.45".toKBigDecimal()

        // Act
        val actual = number.setScale(2, RoundingMode.FLOOR)

        // Assert
        assertEquals(expected, actual)
        assertEquals(2, actual.scale())
    }

    @Test
    fun setScale_toDecreaseScaleWithRoundingHalfUp_roundsCorrectly() {
        // Arrange
        val number = "123.455".toKBigDecimal()
        val expected = "123.46".toKBigDecimal()

        // Act
        val actual = number.setScale(2, RoundingMode.HALF_UP)

        // Assert
        assertEquals(expected, actual)
        assertEquals(2, actual.scale())
    }

    @Test
    fun setScale_toDecreaseScaleWithRoundingHalfDown_roundsCorrectly() {
        // Arrange
        val number = "123.455".toKBigDecimal()
        val expected = "123.45".toKBigDecimal()

        // Act
        val actual = number.setScale(2, RoundingMode.HALF_DOWN)

        // Assert
        assertEquals(expected, actual)
        assertEquals(2, actual.scale())
    }

    @Test
    fun setScale_toDecreaseScaleWithRoundingHalfEven_roundsToEvenNeighbor() {
        // Arrange
        val number = "123.455".toKBigDecimal()
        val expected = "123.46".toKBigDecimal()

        // Act
        val actual = number.setScale(2, RoundingMode.HALF_EVEN)

        // Assert
        assertEquals(expected, actual)
        assertEquals(2, actual.scale())
    }

    @Test
    fun setScale_whenRoundingIsNecessaryButModeIsUnnecessary_throwsArithmeticException() {
        // Arrange
        val number = "123.456".toKBigDecimal()

        // Act & Assert
        assertFailsWith<ArithmeticException> {
            number.setScale(2, RoundingMode.UNNECESSARY)
        }
    }

    @Test
    fun setScale_whenNoRoundingIsNeededAndModeIsUnnecessary_returnsSameValue() {
        // Arrange
        val number = "123.45".toKBigDecimal()
        val expected = "123.45".toKBigDecimal()

        // Act
        val actual = number.setScale(2, RoundingMode.UNNECESSARY)

        // Assert
        assertEquals(expected, actual)
        assertEquals(2, actual.scale())
    }

    // ABS FUNCTION TESTS
    @Test
    fun abs_onPositiveNumber_returnsItself() {
        // Arrange
        val positive = "123.45".toKBigDecimal()
        val expected = positive

        // Act
        val actual = positive.abs()

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun abs_onNegativeNumber_returnsPositiveCounterpart() {
        // Arrange
        val negative = "-123.45".toKBigDecimal()
        val expected = "123.45".toKBigDecimal()

        // Act
        val actual = negative.abs()

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun abs_onZero_returnsZero() {
        // Arrange
        val zero = KBigDecimalFactory.ZERO
        val expected = zero

        // Act
        val actual = zero.abs()

        // Assert
        assertEquals(expected, actual)
        assertTrue(actual.isZero())
    }

    // NEGATE FUNCTION TESTS
    @Test
    fun negate_onPositiveNumber_returnsNegativeCounterpart() {
        // Arrange
        val positive = "123.45".toKBigDecimal()
        val expected = "-123.45".toKBigDecimal()

        // Act
        val actual = positive.negate()

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun negate_onNegativeNumber_returnsPositiveCounterpart() {
        // Arrange
        val negative = "-123.45".toKBigDecimal()
        val expected = "123.45".toKBigDecimal()

        // Act
        val actual = negative.negate()

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun negate_onZero_returnsZero() {
        // Arrange
        val zero = KBigDecimalFactory.ZERO
        val expected = zero

        // Act
        val actual = zero.negate()

        // Assert
        assertEquals(expected, actual)
        assertTrue(actual.isZero())
    }

    // COMPARISON TESTS
    @Test
    fun compareTo_aGreaterThanB_returnsPositive() {
        // Arrange
        val a = "123.45".toKBigDecimal()
        val b = "67.89".toKBigDecimal()

        // Act
        val result = a.compareTo(b)

        // Assert
        assertTrue(result > 0)
    }

    @Test
    fun compareTo_aLessThanB_returnsNegative() {
        // Arrange
        val a = "67.89".toKBigDecimal()
        val b = "123.45".toKBigDecimal()

        // Act
        val result = a.compareTo(b)

        // Assert
        assertTrue(result < 0)
    }

    @Test
    fun compareTo_aEqualToB_returnsZero() {
        // Arrange
        val a = "123.45".toKBigDecimal()
        val b = "123.45".toKBigDecimal()

        // Act
        val result = a.compareTo(b)

        // Assert
        assertEquals(0, result)
    }

    @Test
    fun compareTo_numbersWithDifferentScalesButSameValue_returnsZero() {
        // Arrange
        val a = "1.2".toKBigDecimal()
        val b = "1.20".toKBigDecimal()

        // Act
        val result = a.compareTo(b)

        // Assert
        assertEquals(0, result)
    }

    @Test
    fun compareTo_withZero_worksCorrectly() {
        // Arrange
        val positive = "123.45".toKBigDecimal()
        val negative = "-123.45".toKBigDecimal()
        val zero = KBigDecimalFactory.ZERO

        // Act & Assert
        assertTrue(positive.compareTo(zero) > 0)
        assertTrue(negative.compareTo(zero) < 0)
        assertEquals(0, zero.compareTo(zero))
    }

    // UTILITY FUNCTION TESTS
    @Test
    fun signum_onPositiveNumber_returnsOne() {
        // Arrange
        val positive = "123.45".toKBigDecimal()
        val expected = 1

        // Act
        val actual = positive.signum()

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun signum_onNegativeNumber_returnsNegativeOne() {
        // Arrange
        val negative = "-123.45".toKBigDecimal()
        val expected = -1

        // Act
        val actual = negative.signum()

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun signum_onZero_returnsZero() {
        // Arrange
        val zero = KBigDecimalFactory.ZERO
        val expected = 0

        // Act
        val actual = zero.signum()

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun isZero_onZero_returnsTrue() {
        // Arrange
        val zero = KBigDecimalFactory.ZERO

        // Act
        val actual = zero.isZero()

        // Assert
        assertTrue(actual)
    }

    @Test
    fun isZero_onNonZero_returnsFalse() {
        // Arrange
        val nonZero = "123.45".toKBigDecimal()

        // Act
        val actual = nonZero.isZero()

        // Assert
        assertFalse(actual)
    }

    @Test
    fun isPositive_onPositive_returnsTrue() {
        // Arrange
        val positive = "123.45".toKBigDecimal()

        // Act
        val actual = positive.isPositive()

        // Assert
        assertTrue(actual)
    }

    @Test
    fun isPositive_onZeroOrNegative_returnsFalse() {
        // Arrange
        val zero = KBigDecimalFactory.ZERO
        val negative = "-123.45".toKBigDecimal()

        // Act & Assert
        assertFalse(zero.isPositive())
        assertFalse(negative.isPositive())
    }

    @Test
    fun isNegative_onNegative_returnsTrue() {
        // Arrange
        val negative = "-123.45".toKBigDecimal()

        // Act
        val actual = negative.isNegative()

        // Assert
        assertTrue(actual)
    }

    @Test
    fun isNegative_onZeroOrPositive_returnsFalse() {
        // Arrange
        val zero = KBigDecimalFactory.ZERO
        val positive = "123.45".toKBigDecimal()

        // Act & Assert
        assertFalse(zero.isNegative())
        assertFalse(positive.isNegative())
    }

    // CONVERSION TESTS
    @Test
    fun toBigInteger_onNumberWithFraction_truncatesCorrectly() {
        // Arrange
        val decimal = "123.456".toKBigDecimal()
        val expected = "123".toKBigInteger()

        // Act
        val actual = decimal.toBigInteger()

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun toBigInteger_onNegativeNumber_truncatesCorrectly() {
        // Arrange
        val decimal = "-123.456".toKBigDecimal()
        val expected = "-123".toKBigInteger()

        // Act
        val actual = decimal.toBigInteger()

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun toString_preservesExactRepresentation() {
        // Arrange
        val originalString = "123.456789"
        val decimal = originalString.toKBigDecimal()

        // Act
        val actual = decimal.toString()

        // Assert
        assertEquals(originalString, actual)
    }

    @Test
    fun toString_onNumberWithTrailingZeros_includesTrailingZeros() {
        // Arrange
        val decimal = "123.450000".toKBigDecimal()
        val expected = "123.450000"

        // Act
        val actual = decimal.toString()

        // Assert
        assertEquals(expected, actual)
    }
}
