package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KBigNumberExtensionsTest {

    // STRING TO BIGNUMBER CONVERSION TESTS
    @Test
    fun stringToKBigDecimal_withValidDecimalString_succeeds() {
        val validDecimal = "123.456"
        val expected = "123.456"
        val actual = validDecimal.toKBigDecimal()
        assertEquals(expected, actual.toString())
    }

    @Test
    fun stringToKBigDecimal_withValidIntegerString_succeeds() {
        val validInteger = "123"
        val expected = "123.0" // Note: Basic string parsing might induce scale? No, KBigDecimal(unscaled, scale).
        // Let's verify exact behavior: "123" -> scale 0?
        // Checked logic: if no dot, scale is 0.
        // But KBigDecimal.toString() puts KBigDecimal(unscaled=123, scale=0)
        // Wait, standard toString() uses debug format?
        // Let's check pure value via toKBigDecimal extension which uses fromString.
        val res = validInteger.toKBigDecimal()
        assertEquals(0, res.scale)
        assertEquals("123", res.unscaledValue.toString())
    }

    @Test
    fun stringToKBigDecimal_withLeadingPlusSign_isParsedCorrectly() {
        val positiveString = "+123.456"
        val expected = "123.456" // Assuming + is stripped in logic or handled unscaled
        val actual = positiveString.toKBigDecimal()
        // KBigInteger handles +, so unscaled handles +123456. scale is 3.
        assertEquals(3, actual.scale)
        assertEquals("123456", actual.unscaledValue.toString())
    }

    @Test
    fun stringToKBigInteger_withValidIntegerString_succeeds() {
        val validInteger = "123456789"
        val expected = "123456789"
        val actual = validInteger.toKBigInteger()
        assertEquals(expected, actual.toString())
    }

    // PRIMITIVE TO BIGNUMBER CONVERSION TESTS
    @Test
    fun intToKBigDecimal_convertsCorrectly() {
        val intValue = 123
        val actual = intValue.toKBigDecimal()
        assertEquals(0, actual.scale)
        assertEquals("123", actual.unscaledValue.toString())
    }

    @Test
    fun doubleToKBigDecimal_withStandardValue_convertsCorrectly() {
        val doubleValue = 123.456
        val actual = doubleValue.toKBigDecimal() // Uses double.toString() -> "123.456"
        assertEquals(3, actual.scale)
        assertEquals("123456", actual.unscaledValue.toString())
    }

    @Test
    fun floatToKBigDecimal_convertsCorrectly() {
        val floatValue = 123.456f
        val actual = floatValue.toKBigDecimal()
        // Float toString might be "123.456", check internal rep
        // "123.456" -> scale 3, unscaled 123456
        assertEquals(3, actual.scale)
    }

    // OPERATORS TESTS
    @Test
    fun plusOperator_matchesAddMethod() {
        val a = "10".toKBigInteger()
        val b = "20".toKBigInteger()
        assertEquals("30", (a + b).toString())
    }

    @Test
    fun minusOperator_matchesSubtractMethod() {
        val a = "10".toKBigInteger()
        val b = "20".toKBigInteger()
        assertEquals("-10", (a - b).toString())
    }

    @Test
    fun timesOperator_matchesMultiplyMethod() {
        val a = "10".toKBigInteger()
        val b = "20".toKBigInteger()
        assertEquals("200", (a * b).toString())
    }

    @Test
    fun divOperator_matchesDivideMethod() {
        val a = "20".toKBigInteger()
        val b = "10".toKBigInteger()
        assertEquals("2", (a / b).toString())
    }
    
    @Test
    fun unaryMinus_matchesNegate() {
        val a = "123".toKBigInteger()
        assertEquals("-123", (-a).toString())
        
        val b = "12.3".toKBigDecimal()
        val negB = -b
        assertEquals(1, negB.scale)
        assertEquals("-123", negB.unscaledValue.toString())
    }

    @Test
    fun divOperator_KBigDecimal_usesDefaultContext() {
        val a = "1".toKBigDecimal()
        val b = "3".toKBigDecimal()
        // 1/3 with scale 10 and HALF_UP
        val result = a / b
        assertEquals(10, result.scale)
        // 0.3333333333
        assertEquals("3333333333", result.unscaledValue.toString())
    }


    

}
