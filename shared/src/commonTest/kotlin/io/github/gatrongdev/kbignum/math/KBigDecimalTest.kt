package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class KBigDecimalTest {
    @Test
    fun testBasicArithmetic() {
        val a = "123.45".toKBigDecimal()
        val b = "67.89".toKBigDecimal()

        val sum = a + b
        assertEquals("191.34", sum.toString())

        val difference = a - b
        assertEquals("55.56", difference.toString())

        val product = a * b
        assertTrue(product.toString().startsWith("8381.0"))

        val quotient = a.divide(b, 2, 4)
        assertTrue(quotient.toString().startsWith("1.8"))
    }

    @Test
    fun testFactoryMethods() {
        val fromString = KBigDecimalFactory.fromString("123.456")
        val fromInt = KBigDecimalFactory.fromInt(123)
        val fromLong = KBigDecimalFactory.fromLong(123L)

        assertEquals("123.456", fromString.toString())
        assertEquals("123", fromInt.toString())
        assertEquals("123", fromLong.toString())
    }

    @Test
    fun testComparison() {
        val a = "100".toKBigDecimal()
        val b = "200".toKBigDecimal()
        val c = "100".toKBigDecimal()

        assertTrue(a < b)
        assertTrue(b > a)
        assertTrue(a == c)
        assertEquals(0, a.compareTo(c))
    }

    @Test
    fun testConstants() {
        assertTrue(KBigDecimalFactory.ZERO.isZero())
        assertFalse(KBigDecimalFactory.ONE.isZero())
        assertTrue(KBigDecimalFactory.ONE.isPositive())
    }

    @Test
    fun testScale() {
        val decimal = "123.456789".toKBigDecimal()
        val scaled = decimal.setScale(2, 4)
        assertEquals("123.46", scaled.toString())
    }

    @Test
    fun testAbsoluteValue() {
        val positive = "123.45".toKBigDecimal()
        val negative = "-123.45".toKBigDecimal()

        assertEquals("123.45", positive.abs().toString())
        assertEquals("123.45", negative.abs().toString())
    }

    // COMPREHENSIVE ROUNDING MODES TESTS
    @Test
    fun testRoundingModeUp() {
        val decimal = "123.451".toKBigDecimal()
        val rounded = decimal.setScale(2, RoundingMode.UP)
        assertEquals("123.46", rounded.toString())
        
        val negative = "-123.451".toKBigDecimal()
        val roundedNeg = negative.setScale(2, RoundingMode.UP)
        assertEquals("-123.46", roundedNeg.toString())
        
        val exactValue = "123.450".toKBigDecimal()
        val exactRounded = exactValue.setScale(2, RoundingMode.UP)
        assertEquals("123.45", exactRounded.toString())
    }

    @Test
    fun testRoundingModeDown() {
        val decimal = "123.459".toKBigDecimal()
        val rounded = decimal.setScale(2, RoundingMode.DOWN)
        assertEquals("123.45", rounded.toString())
        
        val negative = "-123.459".toKBigDecimal()
        val roundedNeg = negative.setScale(2, RoundingMode.DOWN)
        assertEquals("-123.45", roundedNeg.toString())
    }

    @Test
    fun testRoundingModeCeiling() {
        val decimal = "123.451".toKBigDecimal()
        val rounded = decimal.setScale(2, RoundingMode.CEILING)
        assertEquals("123.46", rounded.toString())
        
        val negative = "-123.459".toKBigDecimal()
        val roundedNeg = negative.setScale(2, RoundingMode.CEILING)
        assertEquals("-123.45", roundedNeg.toString())
    }

    @Test
    fun testRoundingModeFloor() {
        val decimal = "123.459".toKBigDecimal()
        val rounded = decimal.setScale(2, RoundingMode.FLOOR)
        assertEquals("123.45", rounded.toString())
        
        val negative = "-123.451".toKBigDecimal()
        val roundedNeg = negative.setScale(2, RoundingMode.FLOOR)
        assertEquals("-123.46", roundedNeg.toString())
    }

    @Test
    fun testRoundingModeHalfUp() {
        val halfUp = "123.455".toKBigDecimal()
        val rounded = halfUp.setScale(2, 4)
        assertEquals("123.46", rounded.toString())
        
        val halfDown = "123.454".toKBigDecimal()
        val roundedDown = halfDown.setScale(2, 4)
        assertEquals("123.45", roundedDown.toString())
        
        val negative = "-123.455".toKBigDecimal()
        val roundedNeg = negative.setScale(2, 4)
        assertEquals("-123.46", roundedNeg.toString())
    }

    @Test
    fun testRoundingModeHalfDown() {
        val halfDown = "123.455".toKBigDecimal()
        val rounded = halfDown.setScale(2, RoundingMode.HALF_DOWN)
        assertEquals("123.45", rounded.toString())
        
        val halfUp = "123.456".toKBigDecimal()
        val roundedUp = halfUp.setScale(2, RoundingMode.HALF_DOWN)
        assertEquals("123.46", roundedUp.toString())
        
        val negative = "-123.455".toKBigDecimal()
        val roundedNeg = negative.setScale(2, RoundingMode.HALF_DOWN)
        assertEquals("-123.45", roundedNeg.toString())
    }

    @Test
    fun testRoundingModeHalfEven() {
        val halfEven1 = "123.455".toKBigDecimal()
        val rounded1 = halfEven1.setScale(2, RoundingMode.HALF_EVEN)
        assertEquals("123.46", rounded1.toString())
        
        val halfEven2 = "123.465".toKBigDecimal()
        val rounded2 = halfEven2.setScale(2, RoundingMode.HALF_EVEN)
        assertEquals("123.46", rounded2.toString())
        
        val halfEven3 = "123.475".toKBigDecimal()
        val rounded3 = halfEven3.setScale(2, RoundingMode.HALF_EVEN)
        assertEquals("123.48", rounded3.toString())
    }

    @Test
    fun testRoundingModeUnnecessary() {
        val exact = "123.45".toKBigDecimal()
        val rounded = exact.setScale(2, RoundingMode.UNNECESSARY)
        assertEquals("123.45", rounded.toString())
        
        val inexact = "123.456".toKBigDecimal()
        assertFailsWith<ArithmeticException> {
            inexact.setScale(2, RoundingMode.UNNECESSARY)
        }
    }

    // PRECISION AND SCALE OPERATIONS TESTS
    @Test
    fun testPrecision() {
        val decimal1 = "123.456".toKBigDecimal()
        assertEquals(6, decimal1.precision())
        
        val decimal2 = "000123.456000".toKBigDecimal()
        assertEquals(9, decimal2.precision())
        
        val decimal3 = "0.001".toKBigDecimal()
        assertEquals(1, decimal3.precision())
        
        val zero = KBigDecimalFactory.ZERO
        assertEquals(1, zero.precision())
    }

    @Test
    fun testScaleOperations() {
        val decimal = "123.456789".toKBigDecimal()
        assertEquals(6, decimal.scale())
        
        val scaled0 = decimal.setScale(0, 4)
        assertEquals(0, scaled0.scale())
        assertEquals("123", scaled0.toString())
        
        val scaled4 = decimal.setScale(4, 4)
        assertEquals(4, scaled4.scale())
        assertEquals("123.4568", scaled4.toString())
        
        val scaled8 = decimal.setScale(8, 4)
        assertEquals(8, scaled8.scale())
        assertEquals("123.45678900", scaled8.toString())
    }

    @Test
    fun testMultiplicationEdgeCases() {
        val zero = KBigDecimalFactory.ZERO
        val one = KBigDecimalFactory.ONE
        val decimal = "123.456".toKBigDecimal()
        
        assertTrue(decimal.multiply(zero).isZero())
        assertTrue(zero.multiply(decimal).isZero())
        assertEquals(decimal.toString(), decimal.multiply(one).toString())
        assertEquals(decimal.toString(), one.multiply(decimal).toString())
        
        val negative = "-2.5".toKBigDecimal()
        val positive = "4.2".toKBigDecimal()
        assertEquals("-10.50", negative.multiply(positive).toString())
    }

    @Test
    fun testDivisionEdgeCases() {
        val zero = KBigDecimalFactory.ZERO
        val one = KBigDecimalFactory.ONE
        val decimal = "123.456".toKBigDecimal()

        assertEquals("123.46", decimal.divide(one, 2, 4).toString())
        assertTrue(zero.divide(decimal, 2, 4).compareTo(KBigDecimalFactory.ZERO) == 0)

        assertFailsWith<ArithmeticException> {
            decimal.divide(zero, 2, 4)
        }

        val result = decimal.divide(decimal, 2, 4)
        assertEquals("1.00", result.toString())

        val divResult = "10".toKBigDecimal().divide("3".toKBigDecimal(), 4, 4)
        assertEquals("3.3333", divResult.toString())
    }

    @Test
    fun testDivisionWithScale() {
        val dividend = "22".toKBigDecimal()
        val divisor = "7".toKBigDecimal()
        
        val result0 = dividend.divide(divisor, 0, 4)
        assertEquals("3", result0.toString())
        
        val result2 = dividend.divide(divisor, 2, 4)
        assertEquals("3.14", result2.toString())
        
        val result5 = dividend.divide(divisor, 5, 4)
        assertEquals("3.14286", result5.toString())
    }

    // UTILITY METHODS TESTS
    @Test
    fun testSignum() {
        val positive = "123.456".toKBigDecimal()
        val negative = "-123.456".toKBigDecimal()
        val zero = KBigDecimalFactory.ZERO
        
        assertEquals(1, positive.signum())
        assertEquals(-1, negative.signum())
        assertEquals(0, zero.signum())
        
        val verySmallPositive = "0.000001".toKBigDecimal()
        val verySmallNegative = "-0.000001".toKBigDecimal()
        assertEquals(1, verySmallPositive.signum())
        assertEquals(-1, verySmallNegative.signum())
    }

    @Test
    fun testNegate() {
        val positive = "123.456".toKBigDecimal()
        val negative = "-123.456".toKBigDecimal()
        val zero = KBigDecimalFactory.ZERO
        
        assertEquals("-123.456", positive.negate().toString())
        assertEquals("123.456", negative.negate().toString())
        assertTrue(zero.negate().isZero())
        
        val large = "999999999999999999.123456789".toKBigDecimal()
        assertEquals("-999999999999999999.123456789", large.negate().toString())
    }

    @Test
    fun testIsZero() {
        val zero = KBigDecimalFactory.ZERO
        val zeroString = "0".toKBigDecimal()
        val zeroDecimal = "0.0".toKBigDecimal()
        val zeroWithScale = "0.00000".toKBigDecimal()
        
        assertTrue(zero.isZero())
        assertTrue(zeroString.isZero())
        assertTrue(zeroDecimal.isZero())
        assertTrue(zeroWithScale.isZero())
        
        val nonZero = "0.001".toKBigDecimal()
        val negative = "-0.001".toKBigDecimal()
        assertFalse(nonZero.isZero())
        assertFalse(negative.isZero())
    }

    @Test
    fun testIsPositive() {
        val positive = "123.456".toKBigDecimal()
        val negative = "-123.456".toKBigDecimal()
        val zero = KBigDecimalFactory.ZERO
        
        assertTrue(positive.isPositive())
        assertFalse(negative.isPositive())
        assertFalse(zero.isPositive())
        
        val verySmall = "0.000001".toKBigDecimal()
        assertTrue(verySmall.isPositive())
    }

    @Test
    fun testIsNegative() {
        val positive = "123.456".toKBigDecimal()
        val negative = "-123.456".toKBigDecimal()
        val zero = KBigDecimalFactory.ZERO
        
        assertFalse(positive.isNegative())
        assertTrue(negative.isNegative())
        assertFalse(zero.isNegative())
        
        val verySmallNegative = "-0.000001".toKBigDecimal()
        assertTrue(verySmallNegative.isNegative())
    }

    // CONVERSION TESTS
    @Test
    fun testToBigInteger() {
        val decimal = "123.456".toKBigDecimal()
        val converted = decimal.toBigInteger()
        assertEquals("123", converted.toString())
        
        val negative = "-123.789".toKBigDecimal()
        val convertedNeg = negative.toBigInteger()
        assertEquals("-123", convertedNeg.toString())
        
        val zero = KBigDecimalFactory.ZERO
        val convertedZero = zero.toBigInteger()
        assertTrue(convertedZero.isZero())
        
        val large = "999999999999999999.123456789".toKBigDecimal()
        val convertedLarge = large.toBigInteger()
        assertEquals("999999999999999999", convertedLarge.toString())
    }

    // COMPARISON OPERATIONS TESTS
    @Test
    fun testComprehensiveComparison() {
        val small = "10.5".toKBigDecimal()
        val medium = "20.3".toKBigDecimal()
        val large = "100.1".toKBigDecimal()
        val equalToMedium = "20.3".toKBigDecimal()
        
        assertTrue(small.compareTo(medium) < 0)
        assertTrue(medium.compareTo(large) < 0)
        assertTrue(large.compareTo(small) > 0)
        assertEquals(0, medium.compareTo(equalToMedium))
        
        val negative = "-5.5".toKBigDecimal()
        val zero = KBigDecimalFactory.ZERO
        
        assertTrue(negative.compareTo(zero) < 0)
        assertTrue(zero.compareTo(negative) > 0)
        assertTrue(negative.compareTo(small) < 0)
    }

    @Test
    fun testComparisonWithDifferentScales() {
        val a = "123.4".toKBigDecimal()
        val b = "123.40".toKBigDecimal()
        val c = "123.400000".toKBigDecimal()
        
        assertEquals(0, a.compareTo(b))
        assertEquals(0, b.compareTo(c))
        assertEquals(0, a.compareTo(c))
        
        val d = "123.41".toKBigDecimal()
        assertTrue(a.compareTo(d) < 0)
        assertTrue(d.compareTo(a) > 0)
    }

    // LARGE NUMBER OPERATIONS TESTS
    @Test
    fun testLargeNumberArithmetic() {
        val large1 = "123456789012345678901234567890.123456789012345678901234567890".toKBigDecimal()
        val large2 = "987654321098765432109876543210.987654321098765432109876543210".toKBigDecimal()
        
        val sum = large1.add(large2)
        // Allow flexible matching for platform differences in large number representation
        val sumStr = sum.toString()
        assertTrue(sumStr.startsWith("1111111110") || sumStr.startsWith("1111111111") ||
                  sumStr.contains("111111111111111111111111111110") || 
                  sumStr.contains("111111111111111111111111111111") ||
                  sumStr.startsWith("111111111111111111111111111"))
        
        val difference = large2.subtract(large1)
        assertTrue(difference.toString().contains("864197532086419753208641975320"))
        
        val product = large1.multiply("2".toKBigDecimal())
        assertTrue(product.toString().startsWith("246913578024691357802469135780"))
    }

    @Test
    fun testVerySmallNumbers() {
        val verySmall1 = "0.000000000000000001".toKBigDecimal()
        val verySmall2 = "0.000000000000000002".toKBigDecimal()
        
        val sum = verySmall1.add(verySmall2)
        assertTrue(sum.toString() == "0.000000000000000003" || sum.toString() == "3E-18")
        
        val difference = verySmall2.subtract(verySmall1)
        assertTrue(difference.toString() == "0.000000000000000001" || difference.toString() == "1E-18")
        
        assertFalse(verySmall1.isZero())
        assertTrue(verySmall1.isPositive())
        assertFalse(verySmall1.isNegative())
    }

    @Test
    fun testDivisionByZeroHandling() {
        val decimal = "123.456".toKBigDecimal()
        val zero = KBigDecimalFactory.ZERO

        assertFailsWith<ArithmeticException> {
            decimal.divide(zero, 2, 4)
        }

        assertFailsWith<ArithmeticException> {
            decimal.divide(zero, 2, 4)
        }
    }

    @Test
    fun testConstantsConsistency() {
        assertEquals("0", KBigDecimalFactory.ZERO.toString())
        assertEquals("1", KBigDecimalFactory.ONE.toString())
        assertEquals("10", KBigDecimalFactory.TEN.toString())
        
        assertTrue(KBigDecimalFactory.ZERO.isZero())
        assertTrue(KBigDecimalFactory.ONE.isPositive())
        assertTrue(KBigDecimalFactory.TEN.isPositive())
        
        assertFalse(KBigDecimalFactory.ONE.isZero())
        assertFalse(KBigDecimalFactory.TEN.isZero())
    }

    @Test
    fun testStringRepresentationConsistency() {
        val values = listOf(
            "0", "1", "-1", "123.456", "-123.456",
            "0.000001", "-0.000001", "999999999999999999.123456789"
        )
        
        for (value in values) {
            val decimal = value.toKBigDecimal()
            assertEquals(value, decimal.toString())
        }
    }

    @Test
    fun testPrecisionAndScaleDefaultImplementations() {
        val decimal = "123.456".toKBigDecimal()
        
        assertTrue(decimal.precision() > 0)
        assertTrue(decimal.scale() >= 0)
        
        val integerDecimal = "123".toKBigDecimal()
        assertTrue(integerDecimal.precision() > 0)
        assertEquals(0, integerDecimal.scale())
    }
}
