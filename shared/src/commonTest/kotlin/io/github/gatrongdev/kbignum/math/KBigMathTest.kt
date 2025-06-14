package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

class KBigMathTest {
    
    // SQRT FUNCTION TESTS
    @Test
    fun testSqrtPerfectSquares() {
        assertEquals("0", KBigMath.sqrt("0".toKBigDecimal()).toString())
        assertEquals("1", KBigMath.sqrt("1".toKBigDecimal()).toString())
        assertEquals("2", KBigMath.sqrt("4".toKBigDecimal()).toString())
        assertEquals("3", KBigMath.sqrt("9".toKBigDecimal()).toString())
        assertEquals("4", KBigMath.sqrt("16".toKBigDecimal()).toString())
        assertEquals("5", KBigMath.sqrt("25".toKBigDecimal()).toString())
        assertEquals("10", KBigMath.sqrt("100".toKBigDecimal()).toString())
        assertEquals("100", KBigMath.sqrt("10000".toKBigDecimal()).toString())
    }

    @Test
    fun testSqrtNonPerfectSquares() {
        val sqrt2 = KBigMath.sqrt("2".toKBigDecimal())
        assertTrue(sqrt2.toString().startsWith("1.4"))
        
        val sqrt3 = KBigMath.sqrt("3".toKBigDecimal())
        assertTrue(sqrt3.toString().startsWith("1.7"))
        
        val sqrt5 = KBigMath.sqrt("5".toKBigDecimal())
        assertTrue(sqrt5.toString().startsWith("2.2"))
        
        val sqrt8 = KBigMath.sqrt("8".toKBigDecimal())
        assertTrue(sqrt8.toString().startsWith("2.8"))
    }

    @Test
    fun testSqrtLargeNumbers() {
        val large = "999999999999999999".toKBigDecimal()
        val result = KBigMath.sqrt(large)
        assertTrue(result.toString().startsWith("999999999"))
        
        val veryLarge = "123456789012345678901234567890".toKBigDecimal()
        val largeResult = KBigMath.sqrt(veryLarge)
        assertTrue(largeResult.toString().length > 10)
    }

    @Test
    fun testSqrtDecimalNumbers() {
        val decimal = "2.25".toKBigDecimal()
        val result = KBigMath.sqrt(decimal)
        assertEquals("1.5", result.toString())
        
        val smallDecimal = "0.25".toKBigDecimal()
        val smallResult = KBigMath.sqrt(smallDecimal)
        assertEquals("0.5", smallResult.toString())
    }

    @Test
    fun testSqrtNegativeNumbers() {
        val negative = "-4".toKBigDecimal()
        assertFailsWith<ArithmeticException> {
            KBigMath.sqrt(negative)
        }
        
        val negativeDecimal = "-1.5".toKBigDecimal()
        assertFailsWith<ArithmeticException> {
            KBigMath.sqrt(negativeDecimal)
        }
    }

    // FACTORIAL FUNCTION TESTS
    @Test
    fun testFactorialBasicCases() {
        assertEquals("1", KBigMath.factorial("0".toKBigInteger()).toString())
        assertEquals("1", KBigMath.factorial("1".toKBigInteger()).toString())
        assertEquals("2", KBigMath.factorial("2".toKBigInteger()).toString())
        assertEquals("6", KBigMath.factorial("3".toKBigInteger()).toString())
        assertEquals("24", KBigMath.factorial("4".toKBigInteger()).toString())
        assertEquals("120", KBigMath.factorial("5".toKBigInteger()).toString())
        assertEquals("720", KBigMath.factorial("6".toKBigInteger()).toString())
        assertEquals("5040", KBigMath.factorial("7".toKBigInteger()).toString())
        assertEquals("40320", KBigMath.factorial("8".toKBigInteger()).toString())
        assertEquals("362880", KBigMath.factorial("9".toKBigInteger()).toString())
        assertEquals("3628800", KBigMath.factorial("10".toKBigInteger()).toString())
    }

    @Test
    fun testFactorialLargeNumbers() {
        val fact20 = KBigMath.factorial("20".toKBigInteger())
        assertEquals("2432902008176640000", fact20.toString())
        
        val fact50 = KBigMath.factorial("50".toKBigInteger())
        assertTrue(fact50.toString().length > 60)
        
        val fact100 = KBigMath.factorial("100".toKBigInteger())
        assertTrue(fact100.toString().length > 150)
    }

    @Test
    fun testFactorialNegativeNumbers() {
        val negative = "-1".toKBigInteger()
        assertFailsWith<ArithmeticException> {
            KBigMath.factorial(negative)
        }
        
        val largeNegative = "-100".toKBigInteger()
        assertFailsWith<ArithmeticException> {
            KBigMath.factorial(largeNegative)
        }
    }

    // GCD FUNCTION TESTS
    @Test
    fun testGcdBasicCases() {
        assertEquals("1", KBigMath.gcd("1".toKBigInteger(), "1".toKBigInteger()).toString())
        assertEquals("2", KBigMath.gcd("2".toKBigInteger(), "4".toKBigInteger()).toString())
        assertEquals("3", KBigMath.gcd("6".toKBigInteger(), "9".toKBigInteger()).toString())
        assertEquals("5", KBigMath.gcd("15".toKBigInteger(), "25".toKBigInteger()).toString())
        assertEquals("6", KBigMath.gcd("12".toKBigInteger(), "18".toKBigInteger()).toString())
        assertEquals("12", KBigMath.gcd("48".toKBigInteger(), "60".toKBigInteger()).toString())
    }

    @Test
    fun testGcdWithZero() {
        val a = "12".toKBigInteger()
        val zero = KBigIntegerFactory.ZERO
        
        assertEquals(a.toString(), KBigMath.gcd(a, zero).toString())
        assertEquals(a.toString(), KBigMath.gcd(zero, a).toString())
        assertEquals("0", KBigMath.gcd(zero, zero).toString())
    }

    @Test
    fun testGcdCoprimeNumbers() {
        assertEquals("1", KBigMath.gcd("7".toKBigInteger(), "13".toKBigInteger()).toString())
        assertEquals("1", KBigMath.gcd("8".toKBigInteger(), "9".toKBigInteger()).toString())
        assertEquals("1", KBigMath.gcd("15".toKBigInteger(), "28".toKBigInteger()).toString())
    }

    @Test
    fun testGcdLargeNumbers() {
        val large1 = "123456789012345678901234567890".toKBigInteger()
        val large2 = "987654321098765432109876543210".toKBigInteger()
        val result = KBigMath.gcd(large1, large2)
        assertTrue(result.toString().length > 0)
        
        val sameLarge = KBigMath.gcd(large1, large1)
        assertEquals(large1.toString(), sameLarge.toString())
    }

    @Test
    fun testGcdNegativeNumbers() {
        assertEquals("5", KBigMath.gcd("-15".toKBigInteger(), "25".toKBigInteger()).toString())
        assertEquals("5", KBigMath.gcd("15".toKBigInteger(), "-25".toKBigInteger()).toString())
        assertEquals("5", KBigMath.gcd("-15".toKBigInteger(), "-25".toKBigInteger()).toString())
    }

    // LCM FUNCTION TESTS
    @Test
    fun testLcmBasicCases() {
        assertEquals("6", KBigMath.lcm("2".toKBigInteger(), "3".toKBigInteger()).toString())
        assertEquals("12", KBigMath.lcm("3".toKBigInteger(), "4".toKBigInteger()).toString())
        assertEquals("20", KBigMath.lcm("4".toKBigInteger(), "5".toKBigInteger()).toString())
        assertEquals("30", KBigMath.lcm("6".toKBigInteger(), "10".toKBigInteger()).toString())
        assertEquals("60", KBigMath.lcm("12".toKBigInteger(), "15".toKBigInteger()).toString())
    }

    @Test
    fun testLcmWithZero() {
        val a = "12".toKBigInteger()
        val zero = KBigIntegerFactory.ZERO
        
        assertEquals("0", KBigMath.lcm(a, zero).toString())
        assertEquals("0", KBigMath.lcm(zero, a).toString())
        assertEquals("0", KBigMath.lcm(zero, zero).toString())
    }

    @Test
    fun testLcmSameNumbers() {
        val a = "12".toKBigInteger()
        assertEquals(a.toString(), KBigMath.lcm(a, a).toString())
        
        val large = "999999999999999999".toKBigInteger()
        assertEquals(large.toString(), KBigMath.lcm(large, large).toString())
    }

    @Test
    fun testLcmCoprimeNumbers() {
        val a = "7".toKBigInteger()
        val b = "13".toKBigInteger()
        val product = a.multiply(b)
        assertEquals(product.toString(), KBigMath.lcm(a, b).toString())
    }

    @Test
    fun testLcmLargeNumbers() {
        val large1 = "123456789012345".toKBigInteger()
        val large2 = "987654321098765".toKBigInteger()
        val result = KBigMath.lcm(large1, large2)
        assertTrue(result.toString().length > 25)
    }

    // ISPRIME FUNCTION TESTS
    @Test
    fun testIsPrimeSmallPrimes() {
        assertTrue(KBigMath.isPrime("2".toKBigInteger()))
        assertTrue(KBigMath.isPrime("3".toKBigInteger()))
        assertTrue(KBigMath.isPrime("5".toKBigInteger()))
        assertTrue(KBigMath.isPrime("7".toKBigInteger()))
        assertTrue(KBigMath.isPrime("11".toKBigInteger()))
        assertTrue(KBigMath.isPrime("13".toKBigInteger()))
        assertTrue(KBigMath.isPrime("17".toKBigInteger()))
        assertTrue(KBigMath.isPrime("19".toKBigInteger()))
        assertTrue(KBigMath.isPrime("23".toKBigInteger()))
        assertTrue(KBigMath.isPrime("29".toKBigInteger()))
    }

    @Test
    fun testIsPrimeSmallComposites() {
        assertFalse(KBigMath.isPrime("4".toKBigInteger()))
        assertFalse(KBigMath.isPrime("6".toKBigInteger()))
        assertFalse(KBigMath.isPrime("8".toKBigInteger()))
        assertFalse(KBigMath.isPrime("9".toKBigInteger()))
        assertFalse(KBigMath.isPrime("10".toKBigInteger()))
        assertFalse(KBigMath.isPrime("12".toKBigInteger()))
        assertFalse(KBigMath.isPrime("14".toKBigInteger()))
        assertFalse(KBigMath.isPrime("15".toKBigInteger()))
        assertFalse(KBigMath.isPrime("16".toKBigInteger()))
        assertFalse(KBigMath.isPrime("18".toKBigInteger()))
    }

    @Test
    fun testIsPrimeEdgeCases() {
        assertFalse(KBigMath.isPrime("0".toKBigInteger()))
        assertFalse(KBigMath.isPrime("1".toKBigInteger()))
        assertFalse(KBigMath.isPrime("-1".toKBigInteger()))
        assertFalse(KBigMath.isPrime("-2".toKBigInteger()))
        assertFalse(KBigMath.isPrime("-7".toKBigInteger()))
    }

    @Test
    fun testIsPrimeLargePrimes() {
        assertTrue(KBigMath.isPrime("97".toKBigInteger()))
        assertTrue(KBigMath.isPrime("101".toKBigInteger()))
        assertTrue(KBigMath.isPrime("103".toKBigInteger()))
        assertTrue(KBigMath.isPrime("107".toKBigInteger()))
        assertTrue(KBigMath.isPrime("109".toKBigInteger()))
        assertTrue(KBigMath.isPrime("113".toKBigInteger()))
    }

    @Test
    fun testIsPrimeLargeComposites() {
        assertFalse(KBigMath.isPrime("121".toKBigInteger())) // 11^2
        assertFalse(KBigMath.isPrime("169".toKBigInteger())) // 13^2
        assertFalse(KBigMath.isPrime("289".toKBigInteger())) // 17^2
        assertFalse(KBigMath.isPrime("361".toKBigInteger())) // 19^2
        assertFalse(KBigMath.isPrime("1000".toKBigInteger()))
        assertFalse(KBigMath.isPrime("9999".toKBigInteger()))
    }

    // POW FUNCTION TESTS
    @Test
    fun testPowBasicCases() {
        assertEquals("1", KBigMath.pow("2".toKBigInteger(), "0".toKBigInteger()).toString())
        assertEquals("2", KBigMath.pow("2".toKBigInteger(), "1".toKBigInteger()).toString())
        assertEquals("4", KBigMath.pow("2".toKBigInteger(), "2".toKBigInteger()).toString())
        assertEquals("8", KBigMath.pow("2".toKBigInteger(), "3".toKBigInteger()).toString())
        assertEquals("16", KBigMath.pow("2".toKBigInteger(), "4".toKBigInteger()).toString())
        assertEquals("32", KBigMath.pow("2".toKBigInteger(), "5".toKBigInteger()).toString())
    }

    @Test
    fun testPowWithZeroBase() {
        val zero = KBigIntegerFactory.ZERO
        val one = KBigIntegerFactory.ONE
        val two = "2".toKBigInteger()
        
        assertEquals("1", KBigMath.pow(zero, zero).toString()) // 0^0 = 1 by convention
        assertEquals("0", KBigMath.pow(zero, one).toString())
        assertEquals("0", KBigMath.pow(zero, two).toString())
    }

    @Test
    fun testPowWithOneBase() {
        val one = KBigIntegerFactory.ONE
        assertEquals("1", KBigMath.pow(one, "0".toKBigInteger()).toString())
        assertEquals("1", KBigMath.pow(one, "1".toKBigInteger()).toString())
        assertEquals("1", KBigMath.pow(one, "100".toKBigInteger()).toString())
        assertEquals("1", KBigMath.pow(one, "999999".toKBigInteger()).toString())
    }

    @Test
    fun testPowWithNegativeBase() {
        val negativeTwo = "-2".toKBigInteger()
        assertEquals("1", KBigMath.pow(negativeTwo, "0".toKBigInteger()).toString())
        assertEquals("-2", KBigMath.pow(negativeTwo, "1".toKBigInteger()).toString())
        assertEquals("4", KBigMath.pow(negativeTwo, "2".toKBigInteger()).toString())
        assertEquals("-8", KBigMath.pow(negativeTwo, "3".toKBigInteger()).toString())
        assertEquals("16", KBigMath.pow(negativeTwo, "4".toKBigInteger()).toString())
        assertEquals("-32", KBigMath.pow(negativeTwo, "5".toKBigInteger()).toString())
    }

    @Test
    fun testPowLargeExponents() {
        val base = "2".toKBigInteger()
        val exp10 = "10".toKBigInteger()
        assertEquals("1024", KBigMath.pow(base, exp10).toString())
        
        val exp20 = "20".toKBigInteger()
        assertEquals("1048576", KBigMath.pow(base, exp20).toString())
        
        val base3 = "3".toKBigInteger()
        val exp10_3 = "10".toKBigInteger()
        assertEquals("59049", KBigMath.pow(base3, exp10_3).toString())
    }

    @Test
    fun testPowLargeNumbers() {
        val largeBase = "123".toKBigInteger()
        val smallExp = "3".toKBigInteger()
        val result = KBigMath.pow(largeBase, smallExp)
        assertEquals("1860867", result.toString())
        
        val mediumBase = "10".toKBigInteger()
        val mediumExp = "15".toKBigInteger()
        assertEquals("1000000000000000", KBigMath.pow(mediumBase, mediumExp).toString())
    }

    @Test
    fun testPowNegativeExponent() {
        val base = "2".toKBigInteger()
        val negativeExp = "-1".toKBigInteger()
        
        assertFailsWith<ArithmeticException> {
            KBigMath.pow(base, negativeExp)
        }
        
        val largeNegativeExp = "-100".toKBigInteger()
        assertFailsWith<ArithmeticException> {
            KBigMath.pow(base, largeNegativeExp)
        }
    }

    // INTEGRATION TESTS COMBINING MULTIPLE FUNCTIONS
    @Test
    fun testMathematicalProperties() {
        val a = "12".toKBigInteger()
        val b = "18".toKBigInteger()
        
        val gcd = KBigMath.gcd(a, b)
        val lcm = KBigMath.lcm(a, b)
        val product = a.multiply(b)
        
        assertEquals(gcd.multiply(lcm).toString(), product.toString())
    }

    @Test
    fun testFactorialAndPowerRelationship() {
        val n = "5".toKBigInteger()
        val factorial = KBigMath.factorial(n)
        
        val manual = "1".toKBigInteger()
            .multiply("2".toKBigInteger())
            .multiply("3".toKBigInteger())
            .multiply("4".toKBigInteger())
            .multiply("5".toKBigInteger())
        
        assertEquals(factorial.toString(), manual.toString())
    }

    @Test
    fun testSqrtAndPowerInverse() {
        val number = "16".toKBigDecimal()
        val sqrt = KBigMath.sqrt(number)
        val squared = KBigMath.pow(sqrt.toBigInteger(), "2".toKBigInteger())
        
        assertEquals(number.toBigInteger().toString(), squared.toString())
    }

    @Test
    fun testMathematicalConstants() {
        assertEquals("1", KBigMath.factorial(KBigIntegerFactory.ZERO).toString())
        assertEquals("1", KBigMath.factorial(KBigIntegerFactory.ONE).toString())
        assertEquals("1", KBigMath.pow(KBigIntegerFactory.TEN, KBigIntegerFactory.ZERO).toString())
        assertEquals("10", KBigMath.pow(KBigIntegerFactory.TEN, KBigIntegerFactory.ONE).toString())
    }

    @Test
    fun testErrorHandlingConsistency() {
        val negative = "-5".toKBigInteger()
        val zero = KBigIntegerFactory.ZERO
        
        assertFailsWith<ArithmeticException> {
            KBigMath.factorial(negative)
        }
        
        assertFailsWith<ArithmeticException> {
            KBigMath.pow("2".toKBigInteger(), negative)
        }
        
        assertFailsWith<ArithmeticException> {
            KBigMath.sqrt("-4".toKBigDecimal())
        }
    }
}