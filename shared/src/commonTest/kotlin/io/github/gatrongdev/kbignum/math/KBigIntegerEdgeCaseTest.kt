package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class KBigIntegerEdgeCaseTest {
    // --- KBigInteger Exception Tests ---

    @Test
    fun testDivideByZero() {
        val a = KBigInteger.ONE
        assertFailsWith<ArithmeticException> {
            a.divide(KBigInteger.ZERO)
        }
    }

    @Test
    fun testModByZero() {
        val a = KBigInteger.ONE
        assertFailsWith<ArithmeticException> {
            a.mod(KBigInteger.ZERO)
        }
    }

    @Test
    fun testFromStringInvalid() {
        assertFailsWith<NumberFormatException> { KBigInteger.fromString("") }
        assertFailsWith<NumberFormatException> { KBigInteger.fromString("+") }
        assertFailsWith<NumberFormatException> { KBigInteger.fromString("-") }
        assertFailsWith<NumberFormatException> { KBigInteger.fromString("123-456") }
        assertFailsWith<NumberFormatException> { KBigInteger.fromString("123+456") }
    }

    @Test
    fun constructor_copiesAndNormalizesMagnitude() {
        val inputMagnitude = intArrayOf(5, 0)
        val value = KBigInteger(1, inputMagnitude)
        inputMagnitude[0] = 9

        val exposedMagnitude = value.magnitude
        exposedMagnitude[0] = 7

        assertEquals("5", value.toString())
        assertEquals(KBigInteger.ZERO, KBigInteger(0, intArrayOf(5)))
    }

    @Test
    fun multiply_resultMagnitudeRemainsIndependentOfOperandsAndGetterCopies() {
        val leftMagnitude = intArrayOf(-1, 1)
        val rightMagnitude = intArrayOf(7, 2)
        val left = KBigInteger(1, leftMagnitude)
        val right = KBigInteger(-1, rightMagnitude)
        val product = left.multiply(right)
        val expectedProduct = "-73786976346377814009"

        leftMagnitude[0] = 0
        rightMagnitude[0] = 0
        product.magnitude.fill(0)

        assertEquals(expectedProduct, product.toString())
        assertEquals("8589934591", left.toString())
        assertEquals("-8589934599", right.toString())
    }

    @Test
    fun pow_squareMatchesMultiplicationAtDispatchBoundaries() {
        for (wordCount in listOf(1, 39, 40, 47, 48, 79, 80, 128)) {
            val magnitude = IntArray(wordCount) { -1 }
            val value = KBigInteger(1, magnitude)
            val sameValue = KBigInteger(1, magnitude)
            val expected = value.multiply(sameValue)

            assertEquals(expected, value.multiply(value), "Square failed at $wordCount words")
            assertEquals(expected, value.pow(2), "pow(2) failed at $wordCount words")
        }

        val negative = KBigInteger.fromString("-123456789012345678901234567890")
        assertEquals("15241578753238836750495351562536198787501905199875019052100", negative.multiply(negative).toString())
        assertEquals(negative.multiply(negative), negative.pow(2))
        assertEquals(negative, negative.pow(1))
        assertEquals("-1728", KBigInteger.fromInt(-12).pow(3).toString())
        assertEquals(KBigInteger.ONE, negative.pow(0))
        assertEquals(KBigInteger.ZERO, KBigInteger.ZERO.pow(5))
    }

    // --- KBigInteger Edge Cases ---

    @Test
    fun testMinValueConversions() {
        // Int.MIN_VALUE = -2147483648
        assertEquals("-2147483648", KBigInteger.fromInt(Int.MIN_VALUE).toString())

        // Long.MIN_VALUE = -9223372036854775808
        assertEquals("-9223372036854775808", KBigInteger.fromLong(Long.MIN_VALUE).toString())

        // Zero variations
        assertEquals(KBigInteger.ZERO, KBigInteger.fromString("0"))
        assertEquals(KBigInteger.ZERO, KBigInteger.fromString("+0"))
        assertEquals(KBigInteger.ZERO, KBigInteger.fromString("-0"))
    }

    @Test
    fun testDivideEdgeCases() {
        val ten = KBigInteger.TEN
        val five = KBigInteger.fromInt(5)

        // dividend < divisor -> 0
        assertEquals(KBigInteger.ZERO, five.divide(ten))

        // dividend == divisor -> 1
        assertEquals(KBigInteger.ONE, ten.divide(ten))

        // dividend == -divisor -> -1
        assertEquals(KBigInteger.fromInt(-1), ten.divide(KBigInteger.fromInt(-10)))

        // 0 / anything -> 0
        assertEquals(KBigInteger.ZERO, KBigInteger.ZERO.divide(ten))
    }

    @Test
    fun testModEdgeCases() {
        val ten = KBigInteger.TEN
        val three = KBigInteger.fromInt(3)
        // 10 % 3 = 1
        assertEquals(KBigInteger.ONE, ten.mod(three))

        // 3 % 10 = 3 (dividend < divisor)
        assertEquals(three, three.mod(ten))

        // 10 % 10 = 0
        assertEquals(KBigInteger.ZERO, ten.mod(ten))

        // 0 % 10 = 0
        assertEquals(KBigInteger.ZERO, KBigInteger.ZERO.mod(ten))
    }

    // --- KBigNumberExtensions Tests ---

    @Test
    fun testMinMaxExtensions() {
        val a = KBigInteger.fromInt(10)
        val b = KBigInteger.fromInt(20)

        assertEquals(b, a.max(b))
        assertEquals(b, b.max(a))
        assertEquals(a, a.min(b))
        assertEquals(a, b.min(a))

        val da = KBigDecimal.fromInt(10)
        val db = KBigDecimal.fromInt(20)

        assertEquals(db, da.max(db))
        assertEquals(db, db.max(da))
        assertEquals(da, da.min(db))
        assertEquals(da, db.min(da))
    }

    @Test
    fun testRemOperator() {
        val a = KBigInteger.fromInt(10)
        val b = KBigInteger.fromInt(3)
        assertEquals(KBigInteger.ONE, a % b)
    }

    @Test
    fun testExtensionsConversions() {
        // String -> KBigInteger
        assertEquals(KBigInteger.TEN, "10".toKBigInteger())

        // Primitives -> KBigInteger
        assertEquals(KBigInteger.TEN, 10.toKBigInteger())
        assertEquals(KBigInteger.TEN, 10L.toKBigInteger())

        // String -> KBigDecimal
        // String -> KBigDecimal
        assertEquals(0, KBigDecimal.TEN.compareTo("10".toKBigDecimal()))
        assertEquals(0, KBigDecimal.TEN.compareTo("10.0".toKBigDecimal()))

        // Primitives -> KBigDecimal
        assertEquals(0, KBigDecimal.TEN.compareTo(10.toKBigDecimal()))
        assertEquals(0, KBigDecimal.TEN.compareTo(10L.toKBigDecimal()))

        // Float/Double likely produces "10.0"
        assertTrue(10.0.toKBigDecimal().toString().startsWith("10"))
        assertTrue(10.0f.toKBigDecimal().toString().startsWith("10"))
    }
}
