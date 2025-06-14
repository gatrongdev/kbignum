package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

class KBigIntegerTest {
    @Test
    fun testBasicOperations() {
        val a = "123456789".toKBigInteger()
        val b = "987654321".toKBigInteger()

        assertEquals("123456789", a.toString())
        assertEquals("987654321", b.toString())
        assertEquals(123456789L, a.toLong())
    }

    @Test
    fun testFactoryMethods() {
        val fromString = KBigIntegerFactory.fromString("123456")
        val fromInt = KBigIntegerFactory.fromInt(123456)
        val fromLong = KBigIntegerFactory.fromLong(123456L)

        assertEquals("123456", fromString.toString())
        assertEquals("123456", fromInt.toString())
        assertEquals("123456", fromLong.toString())
    }

    @Test
    fun testConversionToPreciseNumber() {
        val bigInt = "123456".toKBigInteger()
        val bigDec = bigInt.toPreciseNumber()

        assertEquals("123456", bigDec.toString())
        assertTrue(bigDec is KBigDecimal)
    }

    @Test
    fun testConstants() {
        assertEquals("0", KBigIntegerFactory.ZERO.toString())
        assertEquals("1", KBigIntegerFactory.ONE.toString())
        assertEquals("10", KBigIntegerFactory.TEN.toString())
    }

    @Test
    fun testStringValue() {
        val bigInt = "999999999999999999".toKBigInteger()
        assertEquals("999999999999999999", bigInt.toString())
    }

    // COMPREHENSIVE ARITHMETIC OPERATIONS TESTS
    @Test
    fun testAddition() {
        val a = "123456789".toKBigInteger()
        val b = "987654321".toKBigInteger()
        val result = a.add(b)
        assertEquals("1111111110", result.toString())
        
        // Test with zero
        val zero = KBigIntegerFactory.ZERO
        assertEquals(a.toString(), a.add(zero).toString())
        assertEquals(a.toString(), zero.add(a).toString())
        
        // Test with negative numbers
        val negative = "-500".toKBigInteger()
        val positive = "300".toKBigInteger()
        assertEquals("-200", negative.add(positive).toString())
        
        // Test large numbers
        val large1 = "999999999999999999999999999999".toKBigInteger()
        val large2 = "111111111111111111111111111111".toKBigInteger()
        assertEquals("1111111111111111111111111111110", large1.add(large2).toString())
    }

    @Test
    fun testSubtraction() {
        val a = "987654321".toKBigInteger()
        val b = "123456789".toKBigInteger()
        val result = a.subtract(b)
        assertEquals("864197532", result.toString())
        
        // Test with zero
        val zero = KBigIntegerFactory.ZERO
        assertEquals(a.toString(), a.subtract(zero).toString())
        assertEquals("-" + a.toString(), zero.subtract(a).toString())
        
        // Test with negative result
        val small = "100".toKBigInteger()
        val large = "500".toKBigInteger()
        assertEquals("-400", small.subtract(large).toString())
        
        // Test large numbers
        val large1 = "999999999999999999999999999999".toKBigInteger()
        val large2 = "111111111111111111111111111111".toKBigInteger()
        assertEquals("888888888888888888888888888888", large1.subtract(large2).toString())
    }

    @Test
    fun testMultiplication() {
        val a = "123".toKBigInteger()
        val b = "456".toKBigInteger()
        val result = a.multiply(b)
        assertEquals("56088", result.toString())
        
        // Test with zero
        val zero = KBigIntegerFactory.ZERO
        assertEquals("0", a.multiply(zero).toString())
        assertEquals("0", zero.multiply(a).toString())
        
        // Test with one
        val one = KBigIntegerFactory.ONE
        assertEquals(a.toString(), a.multiply(one).toString())
        assertEquals(a.toString(), one.multiply(a).toString())
        
        // Test with negative numbers
        val negative = "-10".toKBigInteger()
        val positive = "5".toKBigInteger()
        assertEquals("-50", negative.multiply(positive).toString())
        assertEquals("-50", positive.multiply(negative).toString())
        
        // Test large numbers
        val large1 = "999999999999999999".toKBigInteger()
        val large2 = "888888888888888888".toKBigInteger()
        assertEquals("888888888888888887111111111111111112", large1.multiply(large2).toString())
    }

    @Test
    fun testDivision() {
        val a = "1000".toKBigInteger()
        val b = "10".toKBigInteger()
        val result = a.divide(b)
        assertEquals("100", result.toString())
        
        // Test with one
        val one = KBigIntegerFactory.ONE
        assertEquals(a.toString(), a.divide(one).toString())
        
        // Test with same number
        assertEquals("1", a.divide(a).toString())
        
        // Test with negative numbers
        val negative = "-100".toKBigInteger()
        val positive = "10".toKBigInteger()
        assertEquals("-10", negative.divide(positive).toString())
        assertEquals("-1", positive.multiply("-1".toKBigInteger()).divide(positive).toString())
        
        // Test large numbers
        val large1 = "999999999999999999999999999999".toKBigInteger()
        val large2 = "111111111111111111111111111111".toKBigInteger()
        assertEquals("9", large1.divide(large2).toString())
    }

    @Test
    fun testDivisionByZero() {
        val a = "100".toKBigInteger()
        val zero = KBigIntegerFactory.ZERO
        
        assertFailsWith<ArithmeticException> {
            a.divide(zero)
        }
    }

    @Test
    fun testModulo() {
        val a = "17".toKBigInteger()
        val b = "5".toKBigInteger()
        val result = a.mod(b)
        assertEquals("2", result.toString())
        
        // Test with zero remainder
        val c = "20".toKBigInteger()
        val d = "4".toKBigInteger()
        assertEquals("0", c.mod(d).toString())
        
        // Test with negative numbers
        val negative = "-17".toKBigInteger()
        val positive = "5".toKBigInteger()
        val negResult = negative.mod(positive)
        assertTrue(negResult.toString() == "3" || negResult.toString() == "-2")
        
        // Test large numbers
        val large1 = "999999999999999999999999999999".toKBigInteger()
        val large2 = "111111111111111111111111111111".toKBigInteger()
        assertEquals("0", large1.mod(large2).toString())
    }

    @Test
    fun testModuloByZero() {
        val a = "100".toKBigInteger()
        val zero = KBigIntegerFactory.ZERO
        
        assertFailsWith<ArithmeticException> {
            a.mod(zero)
        }
    }

    // COMPARISON OPERATIONS TESTS
    @Test
    fun testCompareTo() {
        val a = "123".toKBigInteger()
        val b = "456".toKBigInteger()
        val c = "123".toKBigInteger()
        
        assertTrue(a.compareTo(b) < 0) // a < b
        assertTrue(b.compareTo(a) > 0) // b > a
        assertEquals(0, a.compareTo(c)) // a == c
        
        // Test with zero
        val zero = KBigIntegerFactory.ZERO
        val positive = "100".toKBigInteger()
        val negative = "-100".toKBigInteger()
        
        assertTrue(zero.compareTo(positive) < 0)
        assertTrue(positive.compareTo(zero) > 0)
        assertTrue(negative.compareTo(zero) < 0)
        assertTrue(zero.compareTo(negative) > 0)
        
        // Test large numbers
        val large1 = "999999999999999999999999999999".toKBigInteger()
        val large2 = "111111111111111111111111111111".toKBigInteger()
        assertTrue(large1.compareTo(large2) > 0)
        assertTrue(large2.compareTo(large1) < 0)
    }

    // UTILITY METHODS TESTS
    @Test
    fun testAbs() {
        val positive = "123".toKBigInteger()
        val negative = "-123".toKBigInteger()
        val zero = KBigIntegerFactory.ZERO
        
        assertEquals("123", positive.abs().toString())
        assertEquals("123", negative.abs().toString())
        assertEquals("0", zero.abs().toString())
        
        // Test large numbers
        val largeNegative = "-999999999999999999999999999999".toKBigInteger()
        assertEquals("999999999999999999999999999999", largeNegative.abs().toString())
    }

    @Test
    fun testSignum() {
        val positive = "123".toKBigInteger()
        val negative = "-123".toKBigInteger()
        val zero = KBigIntegerFactory.ZERO
        
        assertEquals(1, positive.signum())
        assertEquals(-1, negative.signum())
        assertEquals(0, zero.signum())
        
        // Test large numbers
        val largePositive = "999999999999999999999999999999".toKBigInteger()
        val largeNegative = "-999999999999999999999999999999".toKBigInteger()
        assertEquals(1, largePositive.signum())
        assertEquals(-1, largeNegative.signum())
    }

    @Test
    fun testNegate() {
        val positive = "123".toKBigInteger()
        val negative = "-123".toKBigInteger()
        val zero = KBigIntegerFactory.ZERO
        
        assertEquals("-123", positive.negate().toString())
        assertEquals("123", negative.negate().toString())
        assertEquals("0", zero.negate().toString())
        
        // Test large numbers
        val largePositive = "999999999999999999999999999999".toKBigInteger()
        assertEquals("-999999999999999999999999999999", largePositive.negate().toString())
    }

    @Test
    fun testIsZero() {
        val zero = KBigIntegerFactory.ZERO
        val positive = "123".toKBigInteger()
        val negative = "-123".toKBigInteger()
        
        assertTrue(zero.isZero())
        assertFalse(positive.isZero())
        assertFalse(negative.isZero())
        
        // Test with constructed zero
        val constructedZero = "0".toKBigInteger()
        assertTrue(constructedZero.isZero())
    }

    @Test
    fun testIsPositive() {
        val zero = KBigIntegerFactory.ZERO
        val positive = "123".toKBigInteger()
        val negative = "-123".toKBigInteger()
        
        assertFalse(zero.isPositive())
        assertTrue(positive.isPositive())
        assertFalse(negative.isPositive())
        
        // Test large numbers
        val largePositive = "999999999999999999999999999999".toKBigInteger()
        val largeNegative = "-999999999999999999999999999999".toKBigInteger()
        assertTrue(largePositive.isPositive())
        assertFalse(largeNegative.isPositive())
    }

    @Test
    fun testIsNegative() {
        val zero = KBigIntegerFactory.ZERO
        val positive = "123".toKBigInteger()
        val negative = "-123".toKBigInteger()
        
        assertFalse(zero.isNegative())
        assertFalse(positive.isNegative())
        assertTrue(negative.isNegative())
        
        // Test large numbers
        val largePositive = "999999999999999999999999999999".toKBigInteger()
        val largeNegative = "-999999999999999999999999999999".toKBigInteger()
        assertFalse(largePositive.isNegative())
        assertTrue(largeNegative.isNegative())
    }

    // CONVERSION TESTS
    @Test
    fun testToLong() {
        val small = "123".toKBigInteger()
        assertEquals(123L, small.toLong())
        
        val negative = "-456".toKBigInteger()
        assertEquals(-456L, negative.toLong())
        
        val zero = KBigIntegerFactory.ZERO
        assertEquals(0L, zero.toLong())
        
        val maxLong = Long.MAX_VALUE.toString().toKBigInteger()
        assertEquals(Long.MAX_VALUE, maxLong.toLong())
        
        val minLong = Long.MIN_VALUE.toString().toKBigInteger()
        assertEquals(Long.MIN_VALUE, minLong.toLong())
    }

    @Test
    fun testToPreciseNumberConversion() {
        val values = listOf("0", "1", "123", "-456", "999999999999999999")
        
        for (value in values) {
            val bigInt = value.toKBigInteger()
            val bigDec = bigInt.toPreciseNumber()
            
            assertEquals(value, bigDec.toString())
            assertTrue(bigDec is KBigDecimal)
        }
    }

    // EDGE CASES AND ERROR HANDLING
    @Test
    fun testLargeNumberOperations() {
        val veryLarge1 = "123456789012345678901234567890123456789012345678901234567890".toKBigInteger()
        val veryLarge2 = "987654321098765432109876543210987654321098765432109876543210".toKBigInteger()
        
        // Test that operations complete without error
        val sum = veryLarge1.add(veryLarge2)
        val diff = veryLarge2.subtract(veryLarge1)
        val product = veryLarge1.multiply("2".toKBigInteger())
        val quotient = veryLarge2.divide("2".toKBigInteger())
        
        assertTrue(sum.toString().length > 60)
        assertTrue(diff.toString().length >= 60)
        assertTrue(product.toString().length >= 60)
        assertTrue(quotient.toString().length >= 30)
    }

    @Test
    fun testZeroDivisionAndModulo() {
        val nonZero = "123".toKBigInteger()
        val zero = KBigIntegerFactory.ZERO
        
        // Division by zero should throw
        assertFailsWith<ArithmeticException> {
            nonZero.divide(zero)
        }
        
        // Modulo by zero should throw
        assertFailsWith<ArithmeticException> {
            nonZero.mod(zero)
        }
        
        // Zero divided by non-zero should be zero
        assertEquals("0", zero.divide(nonZero).toString())
        assertEquals("0", zero.mod(nonZero).toString())
    }

    @Test
    fun testFactoryConstantsConsistency() {
        // Verify constants are consistent across creation methods
        assertEquals(KBigIntegerFactory.ZERO.toString(), "0".toKBigInteger().toString())
        assertEquals(KBigIntegerFactory.ONE.toString(), "1".toKBigInteger().toString())
        assertEquals(KBigIntegerFactory.TEN.toString(), "10".toKBigInteger().toString())
        
        assertEquals(KBigIntegerFactory.ZERO.toString(), KBigIntegerFactory.fromInt(0).toString())
        assertEquals(KBigIntegerFactory.ONE.toString(), KBigIntegerFactory.fromInt(1).toString())
        assertEquals(KBigIntegerFactory.TEN.toString(), KBigIntegerFactory.fromInt(10).toString())
    }

    @Test
    fun testStringRepresentation() {
        val values = listOf(
            "0", "1", "-1", "123", "-123", 
            "999999999999999999999999999999",
            "-999999999999999999999999999999"
        )
        
        for (value in values) {
            val bigInt = value.toKBigInteger()
            assertEquals(value, bigInt.toString())
        }
    }
}
