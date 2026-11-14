package io.github.gatrongdev.kbignum.math

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigInteger
import java.util.Random

/**
 * Validates KBigInteger bitwise operations against Java's BigInteger.
 * Run on JVM/Android.
 */
class KBigIntegerBitwiseComplianceTest {
    @Test
    fun testBitwiseOperationsAgainstJava_FuzzTest() {
        val random = Random(12345)
        // Fuzz Testing: 1000 iterations with random bit lengths and signs
        repeat(1000) { iter ->
            // Random bit length from 0 to 4096 bits
            val numBits = random.nextInt(4097)
            val javaA = BigInteger(numBits, random)
            val javaB = BigInteger(numBits, random)

            // Randomly flip signs to cover all 4 quadrants (++ , +- , -+ , --)
            val signA = if (random.nextBoolean()) 1 else -1
            val signB = if (random.nextBoolean()) 1 else -1

            val finalJavaA = if (signA < 0) javaA.negate() else javaA
            val finalJavaB = if (signB < 0) javaB.negate() else javaB

            val kA = KBigInteger.fromString(finalJavaA.toString())
            val kB = KBigInteger.fromString(finalJavaB.toString())

            try {
                // AND
                assertEquals(
                    "AND failed (iter $iter) for $finalJavaA and $finalJavaB",
                    finalJavaA.and(finalJavaB).toString(),
                    kA.and(kB).toString(),
                )

                // OR
                assertEquals(
                    "OR failed (iter $iter) for $finalJavaA and $finalJavaB",
                    finalJavaA.or(finalJavaB).toString(),
                    kA.or(kB).toString(),
                )

                // XOR
                assertEquals(
                    "XOR failed (iter $iter) for $finalJavaA and $finalJavaB",
                    finalJavaA.xor(finalJavaB).toString(),
                    kA.xor(kB).toString(),
                )

                // AND NOT
                assertEquals(
                    "AND NOT failed (iter $iter) for $finalJavaA and $finalJavaB",
                    finalJavaA.andNot(finalJavaB).toString(),
                    kA.andNot(kB).toString(),
                )

                // NOT
                assertEquals(
                    "NOT failed (iter $iter) for $finalJavaA",
                    finalJavaA.not().toString(),
                    kA.not().toString(),
                )
            } catch (e: Throwable) {
                // Determine which specific case failed for better debugging
                println("Failed at iteration $iter")
                println("A: $finalJavaA")
                println("B: $finalJavaB")
                throw e
            }
        }
    }

    @Test
    fun testSpecificEdgeCases() {
        // -1 (all 1s in 2's comp)
        val jMinus1 = BigInteger.valueOf(-1)
        val kMinus1 = KBigInteger.fromInt(-1)

        // 0
        val jZero = BigInteger.ZERO
        val kZero = KBigInteger.ZERO

        // -1 & 0
        assertEquals(jMinus1.and(jZero).toString(), kMinus1.and(kZero).toString())

        // -1 | 0
        assertEquals(jMinus1.or(jZero).toString(), kMinus1.or(kZero).toString())

        // -1 ^ 0
        assertEquals(jMinus1.xor(jZero).toString(), kMinus1.xor(kZero).toString())

        // Large negative
        val jLargeNeg = BigInteger("-123456789123456789")
        val kLargeNeg = KBigInteger.fromString("-123456789123456789")

        assertEquals(jLargeNeg.not().toString(), kLargeNeg.not().toString())
    }
}
