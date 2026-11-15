package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DivisionConfigTest {
    @Test
    fun divide_withoutConfig_usesDefaultScaleAndHalfUp() {
        val result = KBigDecimal.fromInt(1).divide(KBigDecimal.fromInt(3))

        assertEquals(10, result.scale)
        assertEquals("0.3333333333", result.toString())
        assertEquals(result, KBigDecimal.fromInt(1) / KBigDecimal.fromInt(3))
    }

    @Test
    fun divide_withCurrencyStrategy_roundsToTwoDecimalPlaces() {
        val result = KBigDecimal.fromString("10").divide(KBigDecimal.fromString("3"), DivisionStrategy.CURRENCY)

        assertEquals(PrecisionScale.CURRENCY, result.scale)
        assertEquals("3.33", result.toString())
    }

    @Test
    fun divide_withFinancialStrategy_usesHalfEvenRounding() {
        val result = KBigDecimal.fromString("2.5").divide(KBigDecimal.ONE, DivisionStrategy.FINANCIAL)

        assertEquals("2.50", result.toString())
        assertEquals("2.50", KBigDecimal.fromString("2.5").divide(KBigDecimal.ONE, DivisionConfig(2, KBRoundingMode.HalfEven)).toString())
    }

    @Test
    fun divide_withExactStrategy_throwsWhenRoundingIsNeeded() {
        assertFailsWith<ArithmeticException> {
            KBigDecimal.ONE.divide(KBigDecimal.fromInt(3), DivisionStrategy.EXACT)
        }

        assertEquals("0.5000000000", KBigDecimal.ONE.divide(KBigDecimal.fromInt(2), DivisionStrategy.EXACT).toString())
    }

    @Test
    fun divide_rejectsNegativeScale() {
        assertFailsWith<IllegalArgumentException> {
            KBigDecimal.ONE.divide(KBigDecimal.TEN, -1, KBRoundingMode.HalfUp)
        }
    }

    @Test
    fun divisionConfig_rejectsNegativeScale() {
        assertFailsWith<IllegalArgumentException> {
            DivisionConfig(scale = -1)
        }
    }

    @Test
    fun multiply_rejectsScaleOverflow() {
        assertFailsWith<IllegalArgumentException> {
            KBigDecimal(KBigInteger.ONE, Int.MAX_VALUE).multiply(KBigDecimal(KBigInteger.ONE, 1))
        }
    }
}
