package io.github.gatrongdev.kbignum.math

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Covers the multi-word division dispatch boundary: the 128/128 shape and shapes meeting or
 * exceeding both the divisor-word and dividend-minus-divisor word-gap thresholds take the
 * recursive path, while shapes below either threshold stay on Knuth's Algorithm D.
 */
class KBigIntegerDivisionDispatchTest {
    private val divisorWords = 128
    private val quotientWords = 128

    @Test
    fun dispatchBoundaryDividesConstructedQuotientsExactly() {
        val shapes =
            listOf(
                divisorWords - 1 to quotientWords,
                divisorWords to quotientWords - 1,
                divisorWords to quotientWords,
                divisorWords to quotientWords + 1,
                divisorWords + 1 to quotientWords,
            )

        shapes.forEachIndexed { index, (words, quotientSize) ->
            val divisor = KBigInteger(1, deterministicMagnitude(words, seed = 100 + index, topBitSet = index % 2 == 0))
            val quotient = KBigInteger(1, deterministicMagnitude(quotientSize, seed = 200 + index))
            val remainder = KBigInteger(1, deterministicMagnitude(words / 2, seed = 300 + index)).mod(divisor)
            val dividend = quotient.multiply(divisor).add(remainder)

            assertDivision(dividend, divisor, quotient, remainder)
        }
    }

    @Test
    fun recursivePathHandlesExactAndNonExactMultiples() {
        val divisor = KBigInteger(1, deterministicMagnitude(divisorWords, seed = 11, topBitSet = false))
        val quotient = KBigInteger(1, deterministicMagnitude(quotientWords, seed = 12))
        val exact = quotient.multiply(divisor)

        assertDivision(exact, divisor, quotient, KBigInteger.ZERO)

        val remainder = divisor.subtract(KBigInteger.ONE)
        assertDivision(exact.add(remainder), divisor, quotient, remainder)
    }

    @Test
    fun recursivePathHandlesAllOnesQuotientAndTopBlockBoundary() {
        val divisor = KBigInteger(1, deterministicMagnitude(divisorWords, seed = 21))
        val blockBase = KBigInteger.ONE shl (quotientWords * 32)
        val allOnesQuotient = blockBase.subtract(KBigInteger.ONE)

        val exact = allOnesQuotient.multiply(divisor)
        assertDivision(exact, divisor, allOnesQuotient, KBigInteger.ZERO)
        assertDivision(
            exact.add(divisor.subtract(KBigInteger.ONE)),
            divisor,
            allOnesQuotient,
            divisor.subtract(KBigInteger.ONE),
        )

        // The dividend's high 128-word block equals the divisor, so block handling must not skip it.
        val remainder = KBigInteger(1, deterministicMagnitude(64, seed = 22)).mod(divisor)
        assertDivision(blockBase.multiply(divisor).add(remainder), divisor, blockBase, remainder)
    }

    @Test
    fun largeDeterministicDivisionSatisfiesQuotientRemainderIdentity() {
        repeat(4) { index ->
            val dividend = KBigInteger(1, deterministicMagnitude(256, seed = 400 + index))
            val divisor = KBigInteger(1, deterministicMagnitude(128, seed = 500 + index))

            val (quotient, remainder) = dividend.divideAndRemainder(divisor)

            assertEquals(dividend, quotient.multiply(divisor).add(remainder))
            assertTrue(remainder < divisor)
            assertTrue(remainder.signum() >= 0)
            assertEquals(quotient, dividend.divide(divisor))
            assertEquals(remainder, dividend.mod(divisor))
        }
    }

    @Test
    fun truncationAndRemainderSignsFollowTheDividend() {
        val dividend = KBigInteger(1, deterministicMagnitude(256, seed = 31))
        val divisor = KBigInteger(1, deterministicMagnitude(128, seed = 32))
        val quotient = dividend.divide(divisor)
        val remainder = dividend.mod(divisor)
        assertTrue(remainder.signum() > 0)

        assertEquals(quotient.negate(), dividend.negate().divide(divisor))
        assertEquals(remainder.negate(), dividend.negate().mod(divisor))
        assertEquals(quotient.negate(), dividend.divide(divisor.negate()))
        assertEquals(remainder, dividend.mod(divisor.negate()))
        assertEquals(quotient, dividend.negate().divide(divisor.negate()))
        assertEquals(remainder.negate(), dividend.negate().mod(divisor.negate()))
    }

    @Test
    fun dividendAtOrBelowDivisorShortCircuits() {
        val divisor = KBigInteger(1, deterministicMagnitude(divisorWords, seed = 41))
        val smaller = divisor.subtract(KBigInteger.ONE)

        assertEquals(KBigInteger.ZERO, smaller.divide(divisor))
        assertEquals(smaller, smaller.mod(divisor))
        assertEquals(KBigInteger.ONE, divisor.divide(divisor))
        assertEquals(KBigInteger.ZERO, divisor.mod(divisor))
        assertEquals(KBigInteger.ONE.negate(), divisor.negate().divide(divisor))
        assertEquals(KBigInteger.ZERO, divisor.negate().mod(divisor))
    }

    @Test
    fun zeroDivisorStillThrowsAtDispatchSizes() {
        val dividend = KBigInteger(1, deterministicMagnitude(256, seed = 51))

        assertFailsWith<ArithmeticException> { dividend.divide(KBigInteger.ZERO) }
        assertFailsWith<ArithmeticException> { dividend.mod(KBigInteger.ZERO) }
        assertFailsWith<ArithmeticException> { dividend.divideAndRemainder(KBigInteger.ZERO) }
    }

    private fun assertDivision(
        dividend: KBigInteger,
        divisor: KBigInteger,
        expectedQuotient: KBigInteger,
        expectedRemainder: KBigInteger,
    ) {
        assertEquals(expectedQuotient, dividend.divide(divisor))
        assertEquals(expectedRemainder, dividend.mod(divisor))

        val (quotient, remainder) = dividend.divideAndRemainder(divisor)
        assertEquals(expectedQuotient, quotient)
        assertEquals(expectedRemainder, remainder)
        assertEquals(dividend, quotient.multiply(divisor).add(remainder))
        assertTrue(remainder < divisor)
    }

    private fun deterministicMagnitude(
        words: Int,
        seed: Int,
        topBitSet: Boolean = true,
    ): IntArray {
        val random = Random(seed)
        val magnitude = IntArray(words) { random.nextInt() }
        magnitude[0] = magnitude[0] or 1
        magnitude[words - 1] =
            if (topBitSet) {
                magnitude[words - 1] or Int.MIN_VALUE
            } else {
                // A small top word forces a non-zero normalization shift.
                (magnitude[words - 1] and Int.MAX_VALUE) or 1
            }
        return magnitude
    }
}
