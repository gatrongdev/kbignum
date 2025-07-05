package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

class KBigMathTest {
    
    // SQRT FUNCTION TESTS
    @Test
    fun sqrt_onPerfectSquare_returnsExactIntegerRoot() {
        // Arrange
        val perfectSquare = "16".toKBigDecimal()
        val expected = "4".toKBigDecimal()

        // Act
        val actual = KBigMath.sqrt(perfectSquare)

        // Assert
        assertEquals(expected.toString(), actual.toString())
    }
    
    @Test
    fun sqrt_onNonPerfectSquare_returnsCorrectlyRoundedResult() {
        // Arrange
        val nonPerfectSquare = "2".toKBigDecimal()

        // Act
        val actual = KBigMath.sqrt(nonPerfectSquare)

        // Assert
        assertTrue(actual.toString().startsWith("1.4"))
    }
    
    @Test
    fun sqrt_onDecimalNumber_returnsCorrectResult() {
        // Arrange
        val decimal = "2.25".toKBigDecimal()
        val expected = "1.5".toKBigDecimal()

        // Act
        val actual = KBigMath.sqrt(decimal)

        // Assert
        assertEquals(expected.toString(), actual.toString())
    }
    
    @Test
    fun sqrt_onZero_returnsZero() {
        // Arrange
        val zero = KBigDecimalFactory.ZERO

        // Act
        val actual = KBigMath.sqrt(zero)

        // Assert
        assertTrue(actual.isZero())
    }
    
    @Test
    fun sqrt_onVerySmallDecimal_returnsResultWithHighPrecision() {
        // Arrange
        val verySmall = "0.0001".toKBigDecimal()

        // Act
        val actual = KBigMath.sqrt(verySmall)

        // Assert
        assertEquals("0.01", actual.toString())
    }
    
    @Test
    fun sqrt_onNegativeNumber_throwsArithmeticException() {
        // Arrange
        val negative = "-4".toKBigDecimal()

        // Act & Assert
        assertFailsWith<ArithmeticException> {
            KBigMath.sqrt(negative)
        }
    }
    
    // FACTORIAL FUNCTION TESTS
    @Test
    fun factorial_ofZero_returnsOne() {
        // Arrange
        val zero = KBigIntegerFactory.ZERO
        val expected = KBigIntegerFactory.ONE

        // Act
        val actual = KBigMath.factorial(zero)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun factorial_ofPositiveInteger_returnsCorrectResult() {
        // Arrange
        val five = "5".toKBigInteger()
        val expected = "120".toKBigInteger()

        // Act
        val actual = KBigMath.factorial(five)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun factorial_ofLargeInteger_handlesCorrectly() {
        // Arrange
        val twenty = "20".toKBigInteger()
        val expected = "2432902008176640000".toKBigInteger()

        // Act
        val actual = KBigMath.factorial(twenty)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun factorial_ofNegativeInteger_throwsArithmeticException() {
        // Arrange
        val negative = "-5".toKBigInteger()

        // Act & Assert
        assertFailsWith<ArithmeticException> {
            KBigMath.factorial(negative)
        }
    }
    
    // GCD FUNCTION TESTS
    @Test
    fun gcd_ofTwoPositiveIntegers_returnsCorrectResult() {
        // Arrange
        val a = "48".toKBigInteger()
        val b = "60".toKBigInteger()
        val expected = "12".toKBigInteger()

        // Act
        val actual = KBigMath.gcd(a, b)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun gcd_ofCoprimeIntegers_returnsOne() {
        // Arrange
        val a = "7".toKBigInteger()
        val b = "13".toKBigInteger()
        val expected = KBigIntegerFactory.ONE

        // Act
        val actual = KBigMath.gcd(a, b)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun gcd_withZero_returnsTheOtherNumber() {
        // Arrange
        val number = "12".toKBigInteger()
        val zero = KBigIntegerFactory.ZERO

        // Act & Assert
        assertEquals(number, KBigMath.gcd(number, zero))
        assertEquals(number, KBigMath.gcd(zero, number))
    }
    
    @Test
    fun gcd_withNegativeNumbers_returnsPositiveGcd() {
        // Arrange
        val negative = "-15".toKBigInteger()
        val positive = "25".toKBigInteger()
        val expected = "5".toKBigInteger()

        // Act
        val actual = KBigMath.gcd(negative, positive)

        // Assert
        assertEquals(expected, actual)
    }
    
    // LCM FUNCTION TESTS
    @Test
    fun lcm_ofTwoPositiveIntegers_returnsCorrectResult() {
        // Arrange
        val a = "12".toKBigInteger()
        val b = "15".toKBigInteger()
        val expected = "60".toKBigInteger()

        // Act
        val actual = KBigMath.lcm(a, b)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun lcm_withZero_returnsZero() {
        // Arrange
        val number = "12".toKBigInteger()
        val zero = KBigIntegerFactory.ZERO

        // Act & Assert
        assertTrue(KBigMath.lcm(number, zero).isZero())
        assertTrue(KBigMath.lcm(zero, number).isZero())
    }
    
    @Test
    fun lcm_verifiesGcdLcmProperty() {
        // Arrange
        val a = "12".toKBigInteger()
        val b = "18".toKBigInteger()

        // Act
        val gcd = KBigMath.gcd(a, b)
        val lcm = KBigMath.lcm(a, b)
        val product = a.multiply(b)

        // Assert
        assertEquals(product, gcd.multiply(lcm))
    }
    
    // ISPRIME FUNCTION TESTS
    @Test
    fun isPrime_onPrimeNumber_returnsTrue() {
        // Arrange
        val prime = "17".toKBigInteger()

        // Act
        val actual = KBigMath.isPrime(prime)

        // Assert
        assertTrue(actual)
    }
    
    @Test
    fun isPrime_onCompositeNumber_returnsFalse() {
        // Arrange
        val composite = "15".toKBigInteger()

        // Act
        val actual = KBigMath.isPrime(composite)

        // Assert
        assertFalse(actual)
    }
    
    @Test
    fun isPrime_onZeroAndOne_returnsFalse() {
        // Arrange
        val zero = KBigIntegerFactory.ZERO
        val one = KBigIntegerFactory.ONE

        // Act & Assert
        assertFalse(KBigMath.isPrime(zero))
        assertFalse(KBigMath.isPrime(one))
    }
    
    @Test
    fun isPrime_onNegativeNumber_returnsFalse() {
        // Arrange
        val negative = "-7".toKBigInteger()

        // Act
        val actual = KBigMath.isPrime(negative)

        // Assert
        assertFalse(actual)
    }
    
    @Test
    fun isPrime_onLargePrimeNumber_returnsTrue() {
        // Arrange
        val largePrime = "97".toKBigInteger()

        // Act
        val actual = KBigMath.isPrime(largePrime)

        // Assert
        assertTrue(actual)
    }
    
    // POW FUNCTION TESTS
    @Test
    fun pow_withPositiveExponent_returnsCorrectResult() {
        // Arrange
        val base = "2".toKBigInteger()
        val exponent = "5".toKBigInteger()
        val expected = "32".toKBigInteger()

        // Act
        val actual = KBigMath.pow(base, exponent)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun pow_withZeroExponent_returnsOne() {
        // Arrange
        val base = "123".toKBigInteger()
        val zeroExponent = KBigIntegerFactory.ZERO
        val expected = KBigIntegerFactory.ONE

        // Act
        val actual = KBigMath.pow(base, zeroExponent)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun pow_withOneExponent_returnsBase() {
        // Arrange
        val base = "123".toKBigInteger()
        val oneExponent = KBigIntegerFactory.ONE

        // Act
        val actual = KBigMath.pow(base, oneExponent)

        // Assert
        assertEquals(base, actual)
    }
    
    @Test
    fun pow_withZeroBase_returnsZero() {
        // Arrange
        val zeroBase = KBigIntegerFactory.ZERO
        val exponent = "5".toKBigInteger()

        // Act
        val actual = KBigMath.pow(zeroBase, exponent)

        // Assert
        assertTrue(actual.isZero())
    }
    
    @Test
    fun pow_withZeroBaseAndZeroExponent_returnsOne() {
        // Arrange
        val zeroBase = KBigIntegerFactory.ZERO
        val zeroExponent = KBigIntegerFactory.ZERO
        val expected = KBigIntegerFactory.ONE

        // Act
        val actual = KBigMath.pow(zeroBase, zeroExponent)

        // Assert
        assertEquals(expected, actual)
    }
    
    @Test
    fun pow_withNegativeBase_returnsCorrectlySignedResult() {
        // Arrange
        val negativeBase = "-2".toKBigInteger()
        val evenExponent = "4".toKBigInteger()
        val oddExponent = "3".toKBigInteger()

        // Act
        val evenResult = KBigMath.pow(negativeBase, evenExponent)
        val oddResult = KBigMath.pow(negativeBase, oddExponent)

        // Assert
        assertEquals("16", evenResult.toString())
        assertEquals("-8", oddResult.toString())
    }
    
    @Test
    fun pow_withNegativeExponent_throwsArithmeticException() {
        // Arrange
        val base = "2".toKBigInteger()
        val negativeExponent = "-1".toKBigInteger()

        // Act & Assert
        assertFailsWith<ArithmeticException> {
            KBigMath.pow(base, negativeExponent)
        }
    }
}