package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KBigIntegerScientificTest {

    @Test
    fun testScientificNotationInteger() {
        // "1E2" -> 100
        val bi = KBigInteger.fromString("1E2")
        assertEquals("100", bi.toString())
    }

    @Test
    fun testScientificNotationLargeInteger() {
        // "16116E+7" -> 161160000000
        val bi = KBigInteger.fromString("16116E+7")
        assertEquals("161160000000", bi.toString())
    }
    
    @Test
    fun testScientificNotationWithZeros() {
         // "10E2" -> 1000
         val bi = KBigInteger.fromString("10E2")
         assertEquals("1000", bi.toString())
    }

    @Test
    fun testScientificNotationNegativeExponent() {
        // "1E-2" -> Should throw NumberFormatException as KBigInteger is integer only
        assertFailsWith<NumberFormatException> {
            KBigInteger.fromString("1E-2")
        }
    }
    
    @Test
    fun testScientificNotationDecimalMantissa() {
        // "1.2E2" -> Should throw NumberFormatException (KBigInteger doesn't parse decimals)
        assertFailsWith<NumberFormatException> {
            KBigInteger.fromString("1.2E2")
        }
    }
}
