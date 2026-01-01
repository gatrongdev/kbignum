package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals

class KBigIntegerBitwiseTest {

    @Test
    fun testNot() {
        // ~0 = -1
        assertEquals(KBigInteger.ONE.negate(), KBigInteger.ZERO.not())
        // ~1 = -2
        assertEquals(KBigInteger.fromInt(-2), KBigInteger.ONE.not())
        // ~(-1) = 0
        assertEquals(KBigInteger.ZERO, KBigInteger.ONE.negate().not())
        // ~(-5) = 4
        assertEquals(KBigInteger.fromInt(4), KBigInteger.fromInt(-5).not())
    }

    @Test
    fun testAnd() {
        val a = KBigInteger.fromInt(12) // 1100
        val b = KBigInteger.fromInt(10) // 1010
        // 12 & 10 = 8 (1000)
        assertEquals(KBigInteger.fromInt(8), a and b)

        // -12 & 10
        // -12 = ...110100
        // 10  = ...001010
        // Res = ...000000 = 0? Not quite.
        // -12 (8-bit 2's comp): 11110100
        // 10  (8-bit):          00001010
        // AND:                  00000000 -> 0. Wait, 11110100 & 00001010
        // 4th bit: 1 & 1 = 1. (Index 3, val 8)
        // 12: 8+4. 10: 8+2.
        // 8 is present in both? Yes.
        // -12: -16 + 4. (10...0100)
        // Let's rely on standard logic test cases.
        // -5 & 3 -> 1
        // -5 = ...111011
        // 3  = ...000011
        // AND: ...000011 -> 3? No, -5 is ...11011 (-8 + 3? No. -5 is ~4 = ~(100) = ...11011 + 1? )
        // ~5 + 1? 5=0101. ~5=1010. +1=1011 -> -5.
        // 3 = 0011.
        // 1011 & 0011 = 0011 -> 3.
        // Let's verify: -5 = -8 + 3 = 1...1000 + 0011 = 1...1011. Correct.
        // So -5 & 3 = 3.
        assertEquals(KBigInteger.fromInt(3), KBigInteger.fromInt(-5) and KBigInteger.fromInt(3))
        
        // -5 & -3
        // -5 = ...1011
        // -3 = ...1101 (roughly)
        // AND= ...1001 = -7?
        // ~7 + 1 = 8?? No.
        // -7 = 1001.
        // Test: -5 & -3 = -7.
        assertEquals(KBigInteger.fromInt(-7), KBigInteger.fromInt(-5) and KBigInteger.fromInt(-3))
    }

    @Test
    fun testOr() {
        val a = KBigInteger.fromInt(12) // 1100
        val b = KBigInteger.fromInt(10) // 1010
        // 12 | 10 = 14 (1110)
        assertEquals(KBigInteger.fromInt(14), a or b)

        // -5 | 3
        // -5 = ...1011
        // 3  = ...0011
        // OR = ...1011 = -5
        assertEquals(KBigInteger.fromInt(-5), KBigInteger.fromInt(-5) or KBigInteger.fromInt(3))
    }

    @Test
    fun testXor() {
        val a = KBigInteger.fromInt(12) // 1100
        val b = KBigInteger.fromInt(10) // 1010
        // 12 ^ 10 = 6 (0110)
        assertEquals(KBigInteger.fromInt(6), a xor b)

        // -5 ^ 3
        // -5 = ...1011
        // 3  = ...0011
        // XOR= ...1000 = -8
        assertEquals(KBigInteger.fromInt(-8), KBigInteger.fromInt(-5) xor KBigInteger.fromInt(3))
    }
    
    @Test
    fun testLargeNumbers() {
        // Create numbers larger than 2^32 to force multi-word arithmetic
        // 2^40. Word 0: 0. Word 1: 2^(40-32) = 2^8 = 256.
        val a = KBigInteger.ONE shl 40
        val b = KBigInteger.ONE shl 41
        
        // OR: 2^40 | 2^41 = 2^40 + 2^41
        val expectedOr = a.add(b)
        assertEquals(expectedOr, a or b)
        
        // AND: 2^40 & 2^41 = 0
        assertEquals(KBigInteger.ZERO, a and b)
        
        // XOR: 2^40 ^ 2^41 = 2^40 + 2^41 (since bits don't overlap)
        assertEquals(expectedOr, a xor b)
        
        // Negative Multi-word
        // - (2^40)
        val negA = a.negate()
        // negA in 2's comp:
        // A = ...00100... (bit 40 is 1).
        // ~A = ...11011...
        // ~A + 1 = ...11100... (bits 0-39 are 0. Bit 40 is 1? No.)
        // A = 0x100 00000000.
        // -A = 0xFF...F00 00000000. (Infinite 1s, then ...)
        // Let's test negA | a. Should be -1?
        // -X | X = -1 (in 2's comp logic).
        // ...11100... | ...00100...
        // ...11111... -> -1.
        assertEquals(negA, negA or a)
        
        // negA & a
        // ...11100... & ...00100... -> ...00000...? No.
        // A (2^40) has bit 40 set.
        // -A (2's comp): 2^40 is 100...000.
        // Invert: 011...111.
        // Add 1: 100...000.
        // So -2^40 has bit 40 set! (And zeros below it).
        // So -2^40 & 2^40 = 2^40.
        assertEquals(a, negA and a)
    }

    @Test
    fun testAndNot() {
        // a & ~b
        val a = KBigInteger.fromInt(12)
        val b = KBigInteger.fromInt(10)
        // 12 & ~10
        // 10 = 1010
        // ~10 = ...10101 (inverted bits)
        // 12 = 01100
        // 12 & ~10 = 4?
        // 12 (1100) & NOT(1010)
        // Bits in 12 but not in 10: 4. (8 is in both, 4 in 12 only).
        assertEquals(KBigInteger.fromInt(4), a.andNot(b))
    }
}
