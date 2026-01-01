package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KBigIntegerExtendedOperationsTest {

    @Test
    fun testDivideAndRemainder() {
        val a = KBigInteger.fromInt(10)
        val b = KBigInteger.fromInt(3)
        val result = a.divideAndRemainder(b)
        assertEquals(KBigInteger.fromInt(3), result.first)
        assertEquals(KBigInteger.ONE, result.second)
        
        // Divide by self
        val result2 = a.divideAndRemainder(a)
        assertEquals(KBigInteger.ONE, result2.first)
        assertEquals(KBigInteger.ZERO, result2.second)
        
        // Divide by larger
        val result3 = a.divideAndRemainder(KBigInteger.fromInt(20))
        assertEquals(KBigInteger.ZERO, result3.first)
        assertEquals(a, result3.second)
        
        // Divide by neg
        val result4 = a.divideAndRemainder(KBigInteger.fromInt(-3))
        assertEquals(KBigInteger.fromInt(-3), result4.first)
        assertEquals(KBigInteger.ONE, result4.second) // Remainder sign usually matches dividend
        
        assertFailsWith<ArithmeticException> {
            a.divideAndRemainder(KBigInteger.ZERO)
        }
    }

    @Test
    fun testShiftRightNegative() {
        // -10 shr 1 = -5
        assertEquals(KBigInteger.fromInt(-5), KBigInteger.fromInt(-10).shr(1))
        
        // -5 shr 1 = -3 (floor of -2.5)
        assertEquals(KBigInteger.fromInt(-3), KBigInteger.fromInt(-5).shr(1))
        
        // -1 shr 1 = -1
        assertEquals(KBigInteger.fromInt(-1), KBigInteger.fromInt(-1).shr(1))
        
        // Large negative shift (multi-word)
        // 2^64 = 
        val bigNeg = KBigInteger.ONE.shl(100).negate() 
        // -2^100 shr 100 = -1
        assertEquals(KBigInteger.fromInt(-1), bigNeg.shr(100))
        
        // -2^100 - 1 shr 100 = -2 (floor of (-2^100 - 1)/2^100 = -1 - epsilon = -2)
        val bigNegOdd = bigNeg.subtract(KBigInteger.ONE)
        assertEquals(KBigInteger.fromInt(-2), bigNegOdd.shr(100))
    }

    @Test
    fun testTestBit() {
        val a = KBigInteger.fromInt(10) // 1010
        assertFalse(a.testBit(0))
        assertTrue(a.testBit(1))
        assertFalse(a.testBit(2))
        assertTrue(a.testBit(3))
        assertFalse(a.testBit(4)) // Out of bounds positive
        
        val neg = KBigInteger.fromInt(-10) 
        // -10 = ...11110110 in 2's comp
        assertFalse(neg.testBit(0)) // 0
        assertTrue(neg.testBit(1))  // 1
        assertTrue(neg.testBit(2))  // 1
        assertFalse(neg.testBit(3)) // 0
        assertTrue(neg.testBit(4))  // 1 (sign extension)
        
        assertFailsWith<ArithmeticException> {
            a.testBit(-1)
        }
    }
    
    @Test
    fun testUtilities() {
        // Abs
        assertEquals(KBigInteger.TEN, KBigInteger.fromInt(-10).abs())
        assertEquals(KBigInteger.TEN, KBigInteger.fromInt(10).abs())
        assertEquals(KBigInteger.ZERO, KBigInteger.ZERO.abs())
        
        // Signum
        assertEquals(1, KBigInteger.TEN.signum())
        assertEquals(-1, KBigInteger.fromInt(-10).signum())
        assertEquals(0, KBigInteger.ZERO.signum())
        
        // Predicates
        assertTrue(KBigInteger.ZERO.isZero())
        assertFalse(KBigInteger.ONE.isZero())
        assertTrue(KBigInteger.ONE.isPositive())
        assertFalse(KBigInteger.fromInt(-1).isPositive())
        assertTrue(KBigInteger.fromInt(-1).isNegative())
        assertFalse(KBigInteger.ONE.isNegative())
        
        // Equals/HashCode
        assertEquals(KBigInteger.TEN, KBigInteger.fromInt(10))
        assertEquals(KBigInteger.TEN.hashCode(), KBigInteger.fromInt(10).hashCode())
        assertFalse(KBigInteger.TEN.equals(null))
        assertFalse(KBigInteger.TEN.equals("10"))
    }
    
    @Test
    fun testToLongOverflow() {
        val maxLong = KBigInteger.fromLong(Long.MAX_VALUE)
        assertEquals(Long.MAX_VALUE, maxLong.toLong())
        
        val minLong = KBigInteger.fromLong(Long.MIN_VALUE)
        assertEquals(Long.MIN_VALUE, minLong.toLong())
        
        assertFailsWith<ArithmeticException> {
             maxLong.add(KBigInteger.ONE).toLong()
        }
        assertFailsWith<ArithmeticException> {
             minLong.subtract(KBigInteger.ONE).toLong()
        }
        
        // Check exact boundary cases logic (lines 118-120 in KBigInteger)
        // 2^63 fits as -2^63 (Min Long), but not +2^63
        // Construct 2^63
        val twoPow63 = KBigInteger.ONE.shl(63)
        assertFailsWith<ArithmeticException> { twoPow63.toLong() } // Positive 2^63 overflows
        assertEquals(Long.MIN_VALUE, twoPow63.negate().toLong()) // Negative 2^63 fits
    }
    
    @Test
    fun testShiftRightSimple() {
        // Zero
        assertEquals(KBigInteger.ZERO, KBigInteger.ZERO.shl(5))
        assertEquals(KBigInteger.ZERO, KBigInteger.ZERO.shr(5))
        
        // Identity
        assertEquals(KBigInteger.TEN, KBigInteger.TEN.shl(0))
        assertEquals(KBigInteger.TEN, KBigInteger.TEN.shr(0))
        
        // Opposite direction
        assertEquals(KBigInteger.fromInt(20), KBigInteger.fromInt(10).shl(1))
        assertEquals(KBigInteger.fromInt(5), KBigInteger.fromInt(10).shr(1))
        
        assertEquals(KBigInteger.fromInt(20), KBigInteger.fromInt(10).shr(-1)) // shr(-1) == shl(1)
        assertEquals(KBigInteger.fromInt(5), KBigInteger.fromInt(10).shl(-1)) // shl(-1) == shr(1)
    }
}
