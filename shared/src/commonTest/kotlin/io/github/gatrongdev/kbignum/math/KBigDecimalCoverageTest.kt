package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KBigDecimalCoverageTest {
    @Test
    fun testRoundingModes() {
        // Val = 2.5
        val value25 = KBigDecimal.fromString("2.5")

        // 2.5 -> Round Up -> 3
        assertEquals("3", value25.setScale(0, KBRoundingMode.Up).toString())

        // 2.5 -> Round Down -> 2
        assertEquals("2", value25.setScale(0, KBRoundingMode.Down).toString())

        // 2.5 -> Ceiling -> 3
        assertEquals("3", value25.setScale(0, KBRoundingMode.Ceiling).toString())

        // 2.5 -> Floor -> 2
        assertEquals("2", value25.setScale(0, KBRoundingMode.Floor).toString())

        // 2.5 -> HalfUp -> 3
        assertEquals("3", value25.setScale(0, KBRoundingMode.HalfUp).toString())

        // 2.5 -> HalfDown -> 2
        assertEquals("2", value25.setScale(0, KBRoundingMode.HalfDown).toString())

        // 2.5 -> HalfEven -> 2 (2 is even)
        assertEquals("2", value25.setScale(0, KBRoundingMode.HalfEven).toString())

        // 3.5 -> HalfEven -> 4 (4 is even)
        val value35 = KBigDecimal.fromString("3.5")
        assertEquals("4", value35.setScale(0, KBRoundingMode.HalfEven).toString())

        // Negative Cases
        // -2.5
        val negative25 = value25.negate()

        // -2.5 -> Up -> -3 (Away from zero)
        assertEquals("-3", negative25.setScale(0, KBRoundingMode.Up).toString())

        // -2.5 -> Down -> -2 (Towards zero)
        assertEquals("-2", negative25.setScale(0, KBRoundingMode.Down).toString())

        // -2.5 -> Ceiling -> -2 (Towards positive infinity)
        assertEquals("-2", negative25.setScale(0, KBRoundingMode.Ceiling).toString())

        // -2.5 -> Floor -> -3 (Towards negative infinity)
        assertEquals("-3", negative25.setScale(0, KBRoundingMode.Floor).toString())
    }

    @Test
    fun testRoundingUnnecessary() {
        val val2 = KBigDecimal.fromInt(2)
        // 2.00 has scale 2. setScale(2) -> OK
        assertEquals("2.00", val2.setScale(2, KBRoundingMode.Unnecessary).toString())

        // 2.5 setScale(0) -> Error
        val value25 = KBigDecimal.fromString("2.5")
        assertFailsWith<ArithmeticException> {
            value25.setScale(0, KBRoundingMode.Unnecessary)
        }
    }

    @Test
    fun testDivideExceptions() {
        assertFailsWith<ArithmeticException> {
            KBigDecimal.ONE.divide(KBigDecimal.ZERO)
        }
    }

    @Test
    fun testPowExceptions() {
        assertFailsWith<ArithmeticException> {
            KBigDecimal.ONE.pow(-1)
        }
    }

    @Test
    fun testFromStringInvalid() {
        assertFailsWith<NumberFormatException> {
            KBigDecimal.fromString("")
        }
        // fromString calls KBigInteger.fromString internally, so bad chars are handled there.
        // But we should verify correct propagation or KBigDecimal specific parsing?
        // KBigDecimal logic:
        // dotIndex checks.
    }

    @Test
    fun testUtilities() {
        val a = KBigDecimal.fromString("1.23")
        assertTrue(a.isPositive())
        assertFalse(a.isNegative())
        assertFalse(a.isZero())

        val z = KBigDecimal.ZERO
        assertTrue(z.isZero())

        val n = KBigDecimal.fromString("-1.23")
        assertTrue(n.isNegative())

        assertEquals(KBigInteger.ONE, KBigDecimal.fromString("1.9").toBigInteger()) // Floor/Down by default?
        // Implementation says: setScale(0, Down)
        assertEquals(KBigInteger.ONE, KBigDecimal.fromString("1.9").toBigInteger())
        assertEquals(KBigInteger.fromInt(-1), KBigDecimal.fromString("-1.9").toBigInteger())
    }

    @Test
    fun testScaleMismatch() {
        // Test mathScales branches
        val a = KBigDecimal.fromInt(1) // scale 0
        val b = KBigDecimal.fromString("0.001") // scale 3

        // a + b -> 1.001
        assertEquals("1.001", a.add(b).toString())
        // b + a -> 1.001
        assertEquals("1.001", b.add(a).toString())

        // Compare
        assertTrue(a > b)
        assertTrue(b < a)

        // Subtract
        assertEquals("0.999", a.subtract(b).toString())
        assertEquals("-0.999", b.subtract(a).toString())
    }
}
