package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        assertEquals("999999999999999999", bigInt.getString())
        assertEquals("999999999999999999", bigInt.toString())
    }
}
