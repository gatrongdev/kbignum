package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KBigIntegerCoverageFinalTest {

    @Test
    fun testFromStringInvalidEmbeddedSigns() {
        // Line 96 check: internal chunk starting with sign
        // Chunks are 9 digits. 
        // string: "123456789-1"
        assertFailsWith<NumberFormatException> {
            KBigInteger.fromString("123456789-1")
        }
        assertFailsWith<NumberFormatException> {
             KBigInteger.fromString("123456789+1")
        }
        
        // Line 89 check: first chunk logic?
        // "++1" -> cursor at 1. first char '+'. 
        // firstChunkStr -> "+1"?
        // No, cursor=1. length=3. substring(1, 1+3??)
        // logic: (len - cursor) % 9.
        // "++1" len=3. cursor=1. rem = 2.
        // cursor to cursor+2 -> "+1".
        // firstChunkStr checks startsWith('+'). Throws.
        assertFailsWith<NumberFormatException> {
            KBigInteger.fromString("++1")
        }
        assertFailsWith<NumberFormatException> {
            KBigInteger.fromString("--1")
        }
        assertFailsWith<NumberFormatException> {
            KBigInteger.fromString("-+1")
        }
    }
    
    @Test
    fun testShiftBoundaries() {
        val one = KBigInteger.ONE
        
        // Shift 31
        val s31 = one.shl(31) // 1 << 31 = 2^31 (signed int min value? no, straightforward bit set)
        assertEquals(KBigInteger.fromLong(2147483648L), s31)
        
        // Shift 32
        val s32 = one.shl(32) // Word boundary
        // Result magnitude size should increase
        assertEquals(KBigInteger.fromLong(4294967296L), s32)
        
        // Shift 33
        val s33 = one.shl(33)
        assertEquals(KBigInteger.fromLong(8589934592L), s33)
        
        // Right shifts
        // 2^33 >> 33 = 1
        assertEquals(KBigInteger.ONE, s33.shr(33))
        // 2^33 >> 32 = 2
        assertEquals(KBigInteger.fromInt(2), s33.shr(32))
        // 2^33 >> 31 = 4
        assertEquals(KBigInteger.fromInt(4), s33.shr(31))
    }
    
    @Test
    fun testLargeScaleDiffKBigDecimal() {
        // Trigger loops in divide or similar
        // Just verify standard behavior
        val a = KBigDecimal.fromInt(1)
        val b = KBigDecimal.fromInt(3)
        // 1/3 scale 20
        val quart = a.divide(b, 20, KBRoundingMode.HalfUp)
        assertEquals("0.33333333333333333333", quart.toString())
    }
    
    @Test
    fun testLargePowerOfTen() {
        // Validates pow cache misses and computation
        // fastPowerOfTen(200)
        val bigVal = KBigDecimal.fromInt(1).multiply(KBigDecimal.TEN.pow(200))
        // Should have 200 zeros
        val str = bigVal.toString() // "100...0" if scale=0? No, multiplying KBigDecimal checks unscaled.
        // KBigDecimal.TEN is unscaled 10, scale 0.
        // pow returns scale 0.
        // result unscaled is 10^200. scale 0.
        assertEquals(201, str.length)
        assertEquals('1', str[0])
        assertEquals('0', str[200])
    }
}
