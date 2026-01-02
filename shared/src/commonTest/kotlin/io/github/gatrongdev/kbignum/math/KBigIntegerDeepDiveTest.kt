package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals

class KBigIntegerDeepDiveTest {
    @Test
    fun testSingleWordDivisorOptimization() {
        // Target divideMagnitude optimized loop for v.size == 1
        // Need u.size > 1.
        // u = 2^64 (represented as [0, 0, 1]). v = 3.
        // Divisor needs to be single Int.
        val u = KBigInteger.ONE.shl(64)
        val v = KBigInteger.fromInt(3)
        // 2^64 = 18446744073709551616
        // / 3 = 6148914691236517205 ... 1
        val (q, r) = u.divideAndRemainder(v)

        // Check results using multiply reconstruction
        val check = q.multiply(v).add(r)
        assertEquals(u, check)
        assertEquals(KBigInteger.ONE, r)

        // Also test exact division
        // u = 2^32 * 3.
        val u2 = KBigInteger.fromLong(12884901888L) // 3 * 2^32
        val (q2, r2) = u2.divideAndRemainder(v)
        assertEquals(KBigInteger(1, intArrayOf(0, 1)), q2) // 2^32
        assertEquals(KBigInteger.ZERO, r2)
    }

    @Test
    fun testBitwiseNegativeResult() {
        // Target bitwiseOp resultNegative branch
        // needs signA op signB to be 1.
        // -1 & -1 -> -1 (neg). 1 & 1 -> 1.
        // -2 (..1110) & -4 (...1100) -> -4 (...1100).
        val a = KBigInteger.fromInt(-2)
        val b = KBigInteger.fromInt(-4)
        val result = a.and(b)
        assertEquals(KBigInteger.fromInt(-4), result)

        // Or (-1 | -1) -> -1
        assertEquals(KBigInteger.fromInt(-1), KBigInteger.fromInt(-1).or(KBigInteger.fromInt(-1)))
    }

    @Test
    fun testKnuthZeroShift() {
        // Target "shift == 0" branch in knuthDivide
        // v must be normalized (MSB set, i.e., negative as Int)
        // v = [0xFFFFFFFF] (as unsigned 2^32-1) -> -1 signed int.
        // v = [0x80000000] -> -2147483648

        // v size must be >= 2 for knuthDivide (size 1 handled by optimization)
        // v = [1, 0x80000000] (High word has top bit set)

        val vMag = intArrayOf(1, -2147483648)
        val v = KBigInteger(1, vMag)

        // u need to be larger
        val u = v.multiply(KBigInteger.TEN).add(KBigInteger.fromInt(5))

        val (q, r) = u.divideAndRemainder(v)
        assertEquals(KBigInteger.TEN, q)
        assertEquals(KBigInteger.fromInt(5), r)
    }
}
