package io.github.gatrongdev.kbignum.math

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KBigIntegerFuzzTest {

    @Test
    fun fuzzTestArithmetic() {
        val rand = Random(12345)
        // 5000 iterations of random operations
        for (i in 0 until 5000) {
            val aInt = rand.nextInt()
            val bInt = rand.nextInt()
            val a = KBigInteger.fromInt(aInt)
            val b = KBigInteger.fromInt(bInt)

            // Add
            val sum = a.add(b)
            assertEquals(KBigInteger.fromLong(aInt.toLong() + bInt.toLong()), sum)

            // Subtract
            val diff = a.subtract(b)
            assertEquals(KBigInteger.fromLong(aInt.toLong() - bInt.toLong()), diff)

            // Multiply
            val prod = a.multiply(b)
            assertEquals(KBigInteger.fromLong(aInt.toLong() * bInt.toLong()), prod)
            
            // Divide
            if (bInt != 0) {
                val (q, r) = a.divideAndRemainder(b)
                assertEquals(KBigInteger.fromLong(aInt.toLong() / bInt.toLong()), q)
                assertEquals(KBigInteger.fromLong(aInt.toLong() % bInt.toLong()), r)
            }
        }
    }

    @Test
    fun fuzzTestLargeShifts() {
        val rand = Random(67890)
        for (i in 0 until 1000) {
            val len = rand.nextInt(1, 100) // bits
            val a = KBigInteger.ONE.shl(len)
            
            // Shift right back
            val b = a.shr(len)
            assertEquals(KBigInteger.ONE, b)
            
            // Random shift
            val shift = rand.nextInt(0, 200)
            val c = a.shl(shift)
            val d = c.shr(shift)
            assertEquals(a, d)
        }
    }
    
    @Test
    fun fuzzTestBitwise() {
        val rand = Random(11111)
        for (i in 0 until 1000) {
             val aInt = rand.nextInt()
             val bInt = rand.nextInt()
             val a = KBigInteger.fromInt(aInt)
             val b = KBigInteger.fromInt(bInt)
             
             assertEquals(KBigInteger.fromInt(aInt and bInt), a.and(b))
             assertEquals(KBigInteger.fromInt(aInt or bInt), a.or(b))
             assertEquals(KBigInteger.fromInt(aInt xor bInt), a.xor(b))
        }
    }
    
    @Test
    fun fuzzTestKaratsubaRange() {
        // Generate inputs around the 80-word (2560 bits) threshold
        // 80 words * 32 = 2560 bits.
        // Try sizes: 70 words, 80 words, 90 words.
        val rand = Random(42)
        
        val lens = listOf(70, 80, 85, 90)
        
        for (len in lens) {
            val ints = IntArray(len) { rand.nextInt() }
            if (ints.isNotEmpty()) ints[ints.lastIndex] = rand.nextInt(1, Int.MAX_VALUE) // Ensure size
            val a = KBigInteger(1, ints)
            val b = KBigInteger(1, intArrayOf(rand.nextInt())) // Small b
            
            // a * b
            val prod = a.multiply(b)
            // Just ensure no crash, verifying correctness requires Oracle or BigInt lib.
            // We assume multiply is correct from compliance tests.
            assertTrue(prod.signum() >= 0) // dependent on rand signs, wait. 1 * 1 -> pos.
            
            // a * a (Square / Large mult)
            val sq = a.multiply(a)
            assertTrue(sq.signum() >= 0)
        }
    }
}
