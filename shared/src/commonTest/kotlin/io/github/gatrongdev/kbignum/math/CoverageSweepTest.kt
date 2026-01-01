package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CoverageSweepTest {

    // --- KBigMath Tests ---

    @Test
    fun testSqrtExceptions() {
        val neg = KBigDecimal.fromInt(-1)
        assertFailsWith<ArithmeticException> {
            KBigMath.sqrt(neg)
        }
    }
    
    @Test
    fun testSqrtZero() {
        assertEquals(KBigDecimal.ZERO, KBigMath.sqrt(KBigDecimal.ZERO))
    }

    @Test
    fun testFactorialExceptions() {
        val neg = KBigInteger.fromInt(-1)
        assertFailsWith<ArithmeticException> {
            KBigMath.factorial(neg)
        }
    }
    
    @Test
    fun testLcmZero() {
        val a = KBigInteger.fromInt(10)
        assertEquals(KBigInteger.ZERO, KBigMath.lcm(a, KBigInteger.ZERO))
        assertEquals(KBigInteger.ZERO, KBigMath.lcm(KBigInteger.ZERO, a))
    }
    
    @Test
    fun testIsPrimeEdgeCases() {
        assertFalse(KBigMath.isPrime(KBigInteger.fromInt(0)))
        assertFalse(KBigMath.isPrime(KBigInteger.fromInt(1)))
        assertFalse(KBigMath.isPrime(KBigInteger.fromInt(-5)))
        
        assertTrue(KBigMath.isPrime(KBigInteger.fromInt(2)))
        assertTrue(KBigMath.isPrime(KBigInteger.fromInt(3)))
        
        // Even number > 2
        assertFalse(KBigMath.isPrime(KBigInteger.fromInt(4)))
        
        // Composite odd
        assertFalse(KBigMath.isPrime(KBigInteger.fromInt(9)))
    }
    
    @Test
    fun testBigPowExceptions() {
        val base = KBigInteger.TEN
        val negExp = KBigInteger.fromInt(-1)
        assertFailsWith<ArithmeticException> {
            KBigMath.pow(base, negExp)
        }
    }
    
    @Test
    fun testBigPowZeroExp() {
        val base = KBigInteger.TEN
        val zero = KBigInteger.ZERO
        assertEquals(KBigInteger.ONE, KBigMath.pow(base, zero))
    }

    // --- KBigNumberExtensions Trivial Hits ---
    
    @Test
    fun testOperators() {
        // Unary Plus
        val a = KBigInteger.TEN
        assertEquals(a, +a)
        
        val b = KBigDecimal.TEN
        assertEquals(b, +b)
        
        // Remainder operator (previously tested? verify)
        val ten = KBigInteger.TEN
        val three = KBigInteger.fromInt(3)
        // 10 % 3 = 1
        assertEquals(KBigInteger.ONE, ten % three)
    }
    
    @Test
    fun testPlatform() {
        val p = getPlatform()
        // Check property to ensure class is loaded/covered
        assertTrue(p.name.isNotEmpty())
    }
}
