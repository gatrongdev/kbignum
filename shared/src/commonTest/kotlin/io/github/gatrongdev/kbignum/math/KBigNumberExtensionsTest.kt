package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

class KBigNumberExtensionsTest {

    // STRING CONVERSION EXTENSIONS TESTS
    @Test
    fun testStringToKBigDecimal() {
        assertEquals("0", "0".toKBigDecimal().toString())
        assertEquals("1", "1".toKBigDecimal().toString())
        assertEquals("123", "123".toKBigDecimal().toString())
        assertEquals("-456", "-456".toKBigDecimal().toString())
        assertEquals("3.14159", "3.14159".toKBigDecimal().toString())
        assertEquals("-2.718", "-2.718".toKBigDecimal().toString())
        assertEquals("999999999999999999.123456789", "999999999999999999.123456789".toKBigDecimal().toString())
    }

    @Test
    fun testStringToKBigInteger() {
        assertEquals("0", "0".toKBigInteger().toString())
        assertEquals("1", "1".toKBigInteger().toString()) 
        assertEquals("123", "123".toKBigInteger().toString())
        assertEquals("-456", "-456".toKBigInteger().toString())
        assertEquals("999999999999999999999999999999", "999999999999999999999999999999".toKBigInteger().toString())
    }

    @Test
    fun testInvalidStringConversions() {
        assertFailsWith<NumberFormatException> {
            "abc".toKBigDecimal()
        }
        
        assertFailsWith<NumberFormatException> {
            "123.45.67".toKBigDecimal()
        }
        
        assertFailsWith<NumberFormatException> {
            "".toKBigInteger()
        }
        
        assertFailsWith<NumberFormatException> {
            "12.34".toKBigInteger()
        }
    }

    // PRIMITIVE TYPE CONVERSION EXTENSIONS TESTS
    @Test
    fun testIntToKBigDecimal() {
        assertEquals("0", 0.toKBigDecimal().toString())
        assertEquals("123", 123.toKBigDecimal().toString())
        assertEquals("-456", (-456).toKBigDecimal().toString())
        assertEquals("2147483647", Int.MAX_VALUE.toKBigDecimal().toString())
        assertEquals("-2147483648", Int.MIN_VALUE.toKBigDecimal().toString())
    }

    @Test
    fun testIntToKBigInteger() {
        assertEquals("0", 0.toKBigInteger().toString())
        assertEquals("123", 123.toKBigInteger().toString())
        assertEquals("-456", (-456).toKBigInteger().toString())
        assertEquals("2147483647", Int.MAX_VALUE.toKBigInteger().toString())
        assertEquals("-2147483648", Int.MIN_VALUE.toKBigInteger().toString())
    }

    @Test
    fun testLongToKBigDecimal() {
        assertEquals("0", 0L.toKBigDecimal().toString())
        assertEquals("123", 123L.toKBigDecimal().toString())
        assertEquals("-456", (-456L).toKBigDecimal().toString())
        assertEquals("9223372036854775807", Long.MAX_VALUE.toKBigDecimal().toString())
        assertEquals("-9223372036854775808", Long.MIN_VALUE.toKBigDecimal().toString())
    }

    @Test
    fun testLongToKBigInteger() {
        assertEquals("0", 0L.toKBigInteger().toString())
        assertEquals("123", 123L.toKBigInteger().toString())
        assertEquals("-456", (-456L).toKBigInteger().toString())
        assertEquals("9223372036854775807", Long.MAX_VALUE.toKBigInteger().toString())
        assertEquals("-9223372036854775808", Long.MIN_VALUE.toKBigInteger().toString())
    }

    @Test
    fun testKBigIntegerArithmeticOperators() {
        val a = "123".toKBigInteger()
        val b = "45".toKBigInteger()
        
        assertEquals("168", (a + b).toString())
        assertEquals("78", (a - b).toString())
        assertEquals("5535", (a * b).toString())
        assertEquals("2", (a / b).toString())
        assertEquals("33", (a % b).toString())
    }

    @Test
    fun testUnaryOperators() {
        val positiveDec = "10.5".toKBigDecimal()
        val negativeDec = "-10.5".toKBigDecimal()
        val positiveInt = "123".toKBigInteger()
        val negativeInt = "-123".toKBigInteger()
        
        // Unary minus
        assertEquals("-10.5", (-positiveDec).toString())
        assertEquals("10.5", (-negativeDec).toString())
        assertEquals("-123", (-positiveInt).toString())
        assertEquals("123", (-negativeInt).toString())
        
        // Unary plus
        assertEquals("10.5", (+positiveDec).toString())
        assertEquals("-10.5", (+negativeDec).toString())
        assertEquals("123", (+positiveInt).toString())
        assertEquals("-123", (+negativeInt).toString())
    }

    @Test
    fun testMaxExtensions() {
        val a = "10".toKBigDecimal()
        val b = "20".toKBigDecimal()
        val c = "5".toKBigDecimal()

        assertEquals("20", a.max(b).toString())
        assertEquals("10", a.max(c).toString())
        assertEquals("20", b.max(c).toString())

        val intA = "15".toKBigInteger()
        val intB = "25".toKBigInteger()
        val intC = "8".toKBigInteger()

        assertEquals("25", intA.max(intB).toString())
        assertEquals("15", intA.max(intC).toString())
        assertEquals("25", intB.max(intC).toString())
    }

    @Test
    fun testMinExtensions() {
        val a = "10".toKBigDecimal()
        val b = "20".toKBigDecimal()
        val c = "5".toKBigDecimal()

        assertEquals("10", a.min(b).toString())
        assertEquals("5", a.min(c).toString())
        assertEquals("5", b.min(c).toString())

        val intA = "15".toKBigInteger()
        val intB = "25".toKBigInteger()
        val intC = "8".toKBigInteger()

        assertEquals("15", intA.min(intB).toString())
        assertEquals("8", intA.min(intC).toString())
        assertEquals("8", intB.min(intC).toString())
    }

    @Test 
    fun testComplexExpressions() {
        val a = "100".toKBigInteger()
        val b = "20".toKBigInteger()
        val c = "5".toKBigInteger()
        val d = "2".toKBigInteger()
        
        // Complex arithmetic expression
        val result = (a + b) * c - d
        assertEquals("598", result.toString())
        
        // With modulo operation
        val result2 = (a * b + c) % d
        assertEquals("1", result2.toString())
    }

    // EDGE CASES AND ERROR HANDLING TESTS
    @Test
    fun testZeroOperations() {
        val zero = "0".toKBigDecimal()
        val zeroInt = "0".toKBigInteger()
        val number = "10".toKBigDecimal()
        val numberInt = "10".toKBigInteger()
        
        assertEquals("10", (zero + number).toString())
        assertEquals("10", (number + zero).toString())
        assertEquals("-10", (zero - number).toString())
        assertEquals("10", (number - zero).toString())
        assertEquals("0", (zero * number).toString())
        assertEquals("0", (number * zero).toString())
        assertTrue((zero / number).toString().startsWith("0"))
        
        assertEquals("10", (zeroInt + numberInt).toString())
        assertEquals("0", (zeroInt * numberInt).toString())
        assertEquals("0", (zeroInt % numberInt).toString())
    }

    @Test
    fun testDivisionByZero() {
        val number = "10".toKBigDecimal()
        val zero = "0".toKBigDecimal()
        val numberInt = "10".toKBigInteger()
        val zeroInt = "0".toKBigInteger()
        
        assertFailsWith<ArithmeticException> {
            number / zero
        }
        
        assertFailsWith<ArithmeticException> {
            numberInt / zeroInt
        }
        
        assertFailsWith<ArithmeticException> {
            numberInt % zeroInt
        }
    }

    @Test
    fun testLargeNumberOperations() {
        val large1 = "999999999999999999999999999999.123456789".toKBigDecimal()
        val large2 = "111111111111111111111111111111.987654321".toKBigDecimal()
        
        val sum = large1 + large2
        assertTrue(sum.toString().contains("1111111111111111111111111111111"))
        
        val largeInt1 = "999999999999999999999999999999".toKBigInteger()
        val largeInt2 = "111111111111111111111111111111".toKBigInteger()
        
        val product = largeInt1 * largeInt2
        assertTrue(product.toString().length > 50)
    }

    @Test
    fun testNegativeNumberOperations() {
        val positive = "10".toKBigDecimal()
        val negative = "-5".toKBigDecimal()
        
        assertEquals("5", (positive + negative).toString())
        assertEquals("15", (positive - negative).toString())
        assertTrue((positive * negative).toString() == "-50.0" || (positive * negative).toString() == "-50")
        assertTrue((positive / negative).toString() == "-2.0000000000" || (positive / negative).toString().startsWith("-2"))
        
        val positiveInt = "15".toKBigInteger()
        val negativeInt = "-3".toKBigInteger()
        
        assertEquals("12", (positiveInt + negativeInt).toString())
        assertEquals("18", (positiveInt - negativeInt).toString())
        assertEquals("-45", (positiveInt * negativeInt).toString())
        assertEquals("-5", (positiveInt / negativeInt).toString())
    }

    @Test
    fun testOperatorAndMethodConsistency() {
        val a = "15".toKBigDecimal()
        val b = "3".toKBigDecimal()

        // Operator and method calls should produce same results
        assertEquals((a + b).toString(), a.add(b).toString())
        assertEquals((a - b).toString(), a.subtract(b).toString())
        assertEquals((a * b).toString(), a.multiply(b).toString())
        assertEquals((a / b).toString(), a.divide(b, 10, RoundingMode.HALF_UP).toString())

        val intA = "15".toKBigInteger()
        val intB = "3".toKBigInteger()

        assertEquals((intA + intB).toString(), intA.add(intB).toString())
        assertEquals((intA - intB).toString(), intA.subtract(intB).toString())
        assertEquals((intA * intB).toString(), intA.multiply(intB).toString())
        assertEquals((intA / intB).toString(), intA.divide(intB).toString())
        assertEquals((intA % intB).toString(), intA.mod(intB).toString())
    }

    @Test
    fun testExtensionFunctionChaining() {
        val result = "10".toKBigDecimal()
            .add("5".toKBigDecimal())
            .multiply("2".toKBigDecimal())
            .subtract("1".toKBigDecimal())
        
        assertEquals("29", result.toString())
        
        val operatorResult = ("10".toKBigDecimal() + "5".toKBigDecimal()) * "2".toKBigDecimal() - "1".toKBigDecimal()
        assertEquals(result.toString(), operatorResult.toString())
    }

    @Test
    fun testBoundaryValues() {
        // Test with boundary values for different types
        val maxInt = Int.MAX_VALUE.toKBigInteger()
        val minInt = Int.MIN_VALUE.toKBigInteger()
        val maxLong = Long.MAX_VALUE.toKBigInteger()
        val minLong = Long.MIN_VALUE.toKBigInteger()
        
        assertTrue(maxInt.isPositive())
        assertTrue(minInt.isNegative())
        assertTrue(maxLong.isPositive())
        assertTrue(minLong.isNegative())
        
        // Test operations with boundary values
        val sum = maxInt + minInt
        assertEquals("-1", sum.toString())
        
        val longSum = maxLong + minLong
        assertEquals("-1", longSum.toString())
    }
}