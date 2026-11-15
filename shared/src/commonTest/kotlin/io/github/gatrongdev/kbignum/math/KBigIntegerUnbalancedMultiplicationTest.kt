package io.github.gatrongdev.kbignum.math

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class KBigIntegerUnbalancedMultiplicationTest {
    @Test
    fun multiply_lopsidedOperands_matchesSchoolbookAcrossChunkBoundaries() {
        val random = Random(20261115)

        for (shorterLength in listOf(39, 40, 47, 48, 79, 80, 96, 128)) {
            val longerLength = shorterLength * 4 + 3
            val longerMagnitude = randomMagnitude(longerLength, random)
            val shorterMagnitude = randomMagnitude(shorterLength, random)
            val expectedMagnitude = schoolbookMultiply(longerMagnitude, shorterMagnitude)

            val longer = KBigInteger(1, longerMagnitude)
            val shorter = KBigInteger(1, shorterMagnitude)
            val expected = KBigInteger(1, expectedMagnitude)

            assertEquals(expected, longer.multiply(shorter))
            assertEquals(expected, shorter.multiply(longer))
            assertEquals(KBigInteger(-1, expectedMagnitude), KBigInteger(-1, longerMagnitude).multiply(shorter))
        }
    }

    private fun randomMagnitude(
        length: Int,
        random: Random,
    ): IntArray {
        val magnitude = IntArray(length) { random.nextInt() }
        magnitude[length - 1] = magnitude[length - 1] or Int.MIN_VALUE
        return magnitude
    }

    private fun schoolbookMultiply(
        x: IntArray,
        y: IntArray,
    ): IntArray {
        val result = IntArray(x.size + y.size)
        for (i in x.indices) {
            val xValue = x[i].toLong() and 0xFFFFFFFFL
            var carry = 0L
            for (j in y.indices) {
                val product =
                    xValue * (y[j].toLong() and 0xFFFFFFFFL) +
                        (result[i + j].toLong() and 0xFFFFFFFFL) + carry
                result[i + j] = product.toInt()
                carry = product ushr 32
            }
            result[i + y.size] = carry.toInt()
        }

        var length = result.size
        while (length > 0 && result[length - 1] == 0) length--
        return result.copyOf(length)
    }
}
