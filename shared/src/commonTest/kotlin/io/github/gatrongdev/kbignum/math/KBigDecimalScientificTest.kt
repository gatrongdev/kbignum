package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KBigDecimalScientificTest {

    @Test
    fun testScientificNotationInteger() {
        // "16116E+7" -> 161160000000
        val bd = KBigDecimal.fromString("16116E+7")
        assertEquals(0, bd.scale)
        assertEquals("161160000000", bd.toString())
    }

    @Test
    fun testScientificNotationDecimalPositiveExp() {
        // "1.23E2" -> 123
        val bd = KBigDecimal.fromString("1.23E2")
        assertEquals(0, bd.scale)
        assertEquals("123", bd.toString())
    }

    @Test
    fun testScientificNotationDecimalNegativeExp() {
        // "1.23E-2" -> 0.0123
        val bd = KBigDecimal.fromString("1.23E-2")
        assertEquals(4, bd.scale)
        assertEquals("0.0123", bd.toString())
    }
    
    @Test
    fun testScientificNotationIntegerNegativeExp() {
        // "16116E-2" -> 161.16
        val bd = KBigDecimal.fromString("16116E-2")
        assertEquals(2, bd.scale)
        assertEquals("161.16", bd.toString())
    }
}
