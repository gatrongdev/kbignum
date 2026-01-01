package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KBigIntegerKnuthTest {

    @Test
    fun testKnuthNormalization() {
        // Divisor needing normalization (MSB not set)
        // v = [1] (value 1). 
        // Normalization shift = 31.
        val a = KBigInteger.fromInt(100)
        val b = KBigInteger.fromInt(1)
        val (q, r) = a.divideAndRemainder(b)
        assertEquals(KBigInteger.fromInt(100), q)
        assertEquals(KBigInteger.ZERO, r)
        
        // v = [3]. Shift = 30.
        val c = KBigInteger.fromInt(100)
        val d = KBigInteger.fromInt(3)
        val (q2, r2) = c.divideAndRemainder(d)
        assertEquals(KBigInteger.fromInt(33), q2)
        assertEquals(KBigInteger.ONE, r2)
    }

    @Test
    fun testKnuthQHatCorrection() {
        // We want to trigger the "qHat--" and "addBack" logic.
        // Hand-crafting specific large integers is easier via IntArray if constructor is accessible.
        // KBigInteger constructor is public.
        // Magnitude is Little-Endian.
        
        // Case 1: uH == vH -> qHat = B-1
        // v = [0xFFFFFFFF, 0x80000000]. (Normalized, MSB of high word set)
        // u = [0xFFFFFFFF, 0xFFFFFFFF, 0x80000000].
        // uH = 0x80000000, vH = 0x80000000.
        
        val vMag = intArrayOf(-1, -2147483648) // [0xFFFFFFFF, 0x80000000]
        val uMag = intArrayOf(-1, -1, -2147483648) // [0xFFFFFFFF, 0xFFFFFFFF, 0x80000000]
        
        val v = KBigInteger(1, vMag)
        val u = KBigInteger(1, uMag)
        
        // q should be roughly B.
        // 0x80... / 0x80... is 1. plus lower words.
        // u approx v * B + (B-1).
        // q should be B + something?
        // u = v * B + (0xFFFFFFFF)
        // q = B (which is [0, 1] in mag). r = 0xFFFFFFFF.
        
        val (q, r) = u.divideAndRemainder(v)
        
        // Check results
        assertEquals(KBigInteger(1, intArrayOf(0, 1)), q) // q = 2^32 (represented as [0, 1])
        assertEquals(KBigInteger(1, intArrayOf(-1)), r)   // r = 2^32 - 1
    }

    @Test
    fun testKnuthAddBack() {
        // Trigger step 2d: addBack (qHat underestimated? No, overestimated and subtracted too much)
        // This requires mulSub to return a negative borrow that isn't fully handled?
        // Actually code says: if (borrow < 0) { qHat--; addBack(...) }
        // This happens if the estimated qHat*v > u_segment.
        
        // Classic case:
        // Divisor v = [0, 0x80000000] (2^63)
        // Dividend u ...
        // Needs careful selection.
        
        // Let's use a known regression test case for Knuth D types
        // M = 1, D = 1 (words) -> covered by simple
        // M = 3, D = 2.
        
        // Try random Fuzzing for this specific shape?
        // Or specific pattern:
        // v = [1, 0x80000000] (2^63 + 1)
        // u = [X, 0, 0x80000000] roughly.
        
        // If we simply rely on random large numbers, we might hit it.
        // But let's verify a 3-word by 2-word division.
        
        val v = KBigInteger(1, intArrayOf(1, -2147483648)) // 2^63 + 1
        val u = KBigInteger(1, intArrayOf(0, 0, -2147483648)) // 2^95
        
        // 2^95 / (2^63 + 1) ~= 2^32
        val (q, r) = u.divideAndRemainder(v)
        
        // q * v + r == u
        val check = q.multiply(v).add(r)
        assertEquals(u, check)
    }
}
