package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

        val quotient = a.divide(b, 2, RoundingMode.HALF_UP)
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
        val scaled = decimal.setScale(2, RoundingMode.HALF_UP)
        assertEquals("123.46", scaled.toString())
    }

    @Test
    fun testAbsoluteValue() {
        val positive = "123.45".toKBigDecimal()
        val negative = "-123.45".toKBigDecimal()

        assertEquals("123.45", positive.abs().toString())
        assertEquals("123.45", negative.abs().toString())
    }
}
