package io.github.gatrongdev.kbignum.math

import org.junit.Test
import java.math.MathContext
import java.math.RoundingMode
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import java.math.BigDecimal as JBigDecimal
import java.math.BigInteger as JBigInteger

class KBigMathComplianceTest {
    private val randomSeed = 123456L
    private val iterations = 1000

    // ============ Helper Functions for Sqrt Compliance ============

    /**
     * Compares two BigDecimals with relative error tolerance.
     * relativeError = |a - b| / max(|a|, |b|)
     */
    private fun assertRelativelyEqual(
        expected: JBigDecimal,
        actual: JBigDecimal,
        tolerance: JBigDecimal,
        message: String,
    ) {
        if (expected.compareTo(JBigDecimal.ZERO) == 0 && actual.compareTo(JBigDecimal.ZERO) == 0) {
            return // Both zero
        }

        val diff = expected.subtract(actual).abs()
        val maxVal = expected.abs().max(actual.abs())

        if (maxVal.compareTo(JBigDecimal.ZERO) == 0) {
            return // Avoid division by zero
        }

        val relError = diff.divide(maxVal, 50, RoundingMode.HALF_UP)

        assertTrue(
            relError.compareTo(tolerance) <= 0,
            "Relative error $relError exceeds tolerance $tolerance. $message",
        )
    }

    /**
     * Calls Java BigDecimal.sqrt(MathContext) via reflection (Java 9+).
     * Returns null if sqrt is not available.
     */
    private fun javaSqrt(
        value: JBigDecimal,
        precision: Int,
    ): JBigDecimal? {
        return try {
            val method = JBigDecimal::class.java.getMethod("sqrt", MathContext::class.java)
            val mc = MathContext(precision)
            method.invoke(value, mc) as JBigDecimal
        } catch (e: Exception) {
            null // Java < 9 or other error
        }
    }

    /**
     * Converts KBigDecimal result to Java BigDecimal for comparison.
     */
    private fun kSqrtToJava(
        value: KBigDecimal,
        scale: Int,
    ): JBigDecimal {
        val result = KBigMath.sqrt(value, scale)
        return JBigDecimal(result.toString())
    }

    /**
     * Creates tolerance based on scale: 10^(-(scale - 2))
     */
    private fun toleranceForScale(scale: Int): JBigDecimal {
        val toleranceScale = maxOf(scale - 2, 1)
        return JBigDecimal.ONE.divide(JBigDecimal.TEN.pow(toleranceScale), toleranceScale + 10, RoundingMode.HALF_UP)
    }

    // ============ Existing Tests ============

    @Test
    fun testGcdCompliance() {
        val rand = Random(randomSeed)
        for (i in 0 until iterations) {
            val aBytes = Random.nextBytes(rand.nextInt(1, 100))
            val bBytes = Random.nextBytes(rand.nextInt(1, 100))

            val jA = JBigInteger(aBytes)
            val jB = JBigInteger(bBytes)

            val kA = KBigInteger.fromString(jA.toString())
            val kB = KBigInteger.fromString(jB.toString())

            val jGcd = jA.gcd(jB)
            val kGcd = KBigMath.gcd(kA, kB)

            assertEquals(jGcd.toString(), kGcd.toString(), "GCD failed for $jA, $jB")
        }
    }

    @Test
    fun testLcmCompliance() {
        // LCM = (a * b) / GCD(a, b)
        val rand = Random(randomSeed + 1)
        for (i in 0 until iterations) {
            // Keep numbers smaller for LCM to avoid excessive overflow in Java if testing against naive
            val aBytes = Random.nextBytes(rand.nextInt(1, 20))
            val bBytes = Random.nextBytes(rand.nextInt(1, 20))

            var jA = JBigInteger(aBytes)
            var jB = JBigInteger(bBytes)
            if (jA.signum() == 0) jA = JBigInteger.ONE
            if (jB.signum() == 0) jB = JBigInteger.ONE

            val kA = KBigInteger.fromString(jA.toString())
            val kB = KBigInteger.fromString(jB.toString())

            // Java BigInteger doesn't have explicit LCM, implement manually for verification
            // abs(a * b) / gcd(a, b)
            val jLcm = jA.multiply(jB).abs().divide(jA.gcd(jB))

            val kLcm = KBigMath.lcm(kA, kB)

            assertEquals(jLcm.toString(), kLcm.toString(), "LCM failed for $jA, $jB")
        }
    }

    @Test
    fun testPowCompliance() {
        val rand = Random(randomSeed + 2)
        for (i in 0 until iterations) {
            val baseBytes = Random.nextBytes(rand.nextInt(1, 10))
            val jBase = JBigInteger(baseBytes)
            val exp = rand.nextInt(0, 100) // Keep exponent reasonable

            val kBase = KBigInteger.fromString(jBase.toString())
            val kExp = KBigInteger.fromInt(exp)

            val jPow = jBase.pow(exp)
            val kPow = KBigMath.pow(kBase, kExp)

            assertEquals(jPow.toString(), kPow.toString(), "Pow failed for $jBase ^ $exp")
        }
    }

    @Test
    fun testFactorialReasonable() {
        // Factorial grows super fast. Test small inputs.
        // 0! = 1
        assertEquals("1", KBigMath.factorial(KBigInteger.ZERO).toString())
        // 5! = 120
        assertEquals("120", KBigMath.factorial(KBigInteger.fromInt(5)).toString())

        // 20!
        var jFact = JBigInteger.ONE
        for (i in 1..20) {
            jFact = jFact.multiply(JBigInteger.valueOf(i.toLong()))
        }

        assertEquals(jFact.toString(), KBigMath.factorial(KBigInteger.fromInt(20)).toString())
    }

    @Test
    fun testFactorialComprehensiveCompliance() {
        // Test all factorials from 0 to 100, comparing with Java calculation
        for (n in 0..100) {
            var jFact = JBigInteger.ONE
            for (i in 1..n) {
                jFact = jFact.multiply(JBigInteger.valueOf(i.toLong()))
            }

            val kFact = KBigMath.factorial(KBigInteger.fromInt(n))

            assertEquals(
                jFact.toString(),
                kFact.toString(),
                "Factorial($n) mismatch",
            )
        }
    }

    @Test
    fun testFactorialLargeNumbers() {
        // Test larger factorials: 200!, 500!, 1000!
        val largeValues = listOf(200, 500, 1000)

        for (n in largeValues) {
            var jFact = JBigInteger.ONE
            for (i in 1..n) {
                jFact = jFact.multiply(JBigInteger.valueOf(i.toLong()))
            }

            val kFact = KBigMath.factorial(KBigInteger.fromInt(n))

            assertEquals(
                jFact.toString(),
                kFact.toString(),
                "Factorial($n) mismatch for large number",
            )
        }
    }

    @Test
    fun testFactorialEdgeCases() {
        // 0! = 1
        assertEquals("1", KBigMath.factorial(KBigInteger.ZERO).toString())

        // 1! = 1
        assertEquals("1", KBigMath.factorial(KBigInteger.ONE).toString())

        // Negative factorial should throw
        assertFailsWith<ArithmeticException> {
            KBigMath.factorial(KBigInteger.fromInt(-1))
        }

        assertFailsWith<ArithmeticException> {
            KBigMath.factorial(KBigInteger.fromInt(-100))
        }
    }

    // ============ isPrime Compliance Tests ============

    @Test
    fun testIsPrimeKnownPrimes() {
        // First 100 prime numbers
        val knownPrimes =
            listOf(
                2, 3, 5, 7, 11, 13, 17, 19, 23, 29,
                31, 37, 41, 43, 47, 53, 59, 61, 67, 71,
                73, 79, 83, 89, 97, 101, 103, 107, 109, 113,
                127, 131, 137, 139, 149, 151, 157, 163, 167, 173,
                179, 181, 191, 193, 197, 199, 211, 223, 227, 229,
                233, 239, 241, 251, 257, 263, 269, 271, 277, 281,
                283, 293, 307, 311, 313, 317, 331, 337, 347, 349,
                353, 359, 367, 373, 379, 383, 389, 397, 401, 409,
                419, 421, 431, 433, 439, 443, 449, 457, 461, 463,
                467, 479, 487, 491, 499, 503, 509, 521, 523, 541,
            )

        for (p in knownPrimes) {
            val kNum = KBigInteger.fromInt(p)
            assertTrue(
                KBigMath.isPrime(kNum),
                "$p should be prime",
            )
        }
    }

    @Test
    fun testIsPrimeKnownComposites() {
        // Known composite numbers
        val composites =
            listOf(
                4, 6, 8, 9, 10, 12, 14, 15, 16, 18, 20,
                21, 22, 24, 25, 26, 27, 28, 30, 32, 33,
                34, 35, 36, 38, 39, 40, 42, 44, 45, 46,
                48, 49, 50, 51, 52, 54, 55, 56, 57, 58,
                60, 62, 63, 64, 65, 66, 68, 69, 70, 72,
                74, 75, 76, 77, 78, 80, 81, 82, 84, 85,
                86, 87, 88, 90, 91, 92, 93, 94, 95, 96,
                98, 99, 100, 1000, 10000, 123456,
            )

        for (c in composites) {
            val kNum = KBigInteger.fromInt(c)
            assertTrue(
                !KBigMath.isPrime(kNum),
                "$c should NOT be prime",
            )
        }
    }

    @Test
    fun testIsPrimeCompareWithJava() {
        // Compare with Java BigInteger.isProbablePrime(100)
        // isProbablePrime with certainty 100 is extremely reliable
        val rand = Random(randomSeed + 30)

        for (i in 0 until 500) {
            // Generate random number between 2 and 10000
            val n = rand.nextInt(2, 10000)
            val jNum = JBigInteger.valueOf(n.toLong())
            val kNum = KBigInteger.fromInt(n)

            val jIsPrime = jNum.isProbablePrime(100)
            val kIsPrime = KBigMath.isPrime(kNum)

            assertEquals(
                jIsPrime,
                kIsPrime,
                "isPrime($n) mismatch: Java=$jIsPrime, K=$kIsPrime",
            )
        }
    }

    @Test
    fun testIsPrimeLargerNumbers() {
        // Test some larger known primes
        val largePrimes =
            listOf(
                "104729",
                // 10000th prime
                "1299709",
                // 100000th prime
                "15485863",
                // 1000000th prime
                "982451653",
                // Large prime
                "2147483647",
                // Mersenne prime M31 = 2^31 - 1
            )

        for (pStr in largePrimes) {
            val kNum = KBigInteger.fromString(pStr)
            val jNum = JBigInteger(pStr)

            assertTrue(
                jNum.isProbablePrime(100),
                "Java confirms $pStr is prime",
            )
            assertTrue(
                KBigMath.isPrime(kNum),
                "$pStr should be prime",
            )
        }

        // Test some larger known composites
        val largeComposites =
            listOf(
                "104730",
                // 104729 + 1
                "1299710",
                // 1299709 + 1
                "15485864",
                // 15485863 + 1
                "1000000000",
                // 10^9
                "2147483646",
                // 2^31 - 2
            )

        for (cStr in largeComposites) {
            val kNum = KBigInteger.fromString(cStr)
            assertTrue(
                !KBigMath.isPrime(kNum),
                "$cStr should NOT be prime",
            )
        }
    }

    @Test
    fun testIsPrimeEdgeCases() {
        // 0 is not prime
        assertTrue(!KBigMath.isPrime(KBigInteger.ZERO), "0 should not be prime")

        // 1 is not prime
        assertTrue(!KBigMath.isPrime(KBigInteger.ONE), "1 should not be prime")

        // 2 is prime (smallest prime)
        assertTrue(KBigMath.isPrime(KBigInteger.fromInt(2)), "2 should be prime")

        // 3 is prime
        assertTrue(KBigMath.isPrime(KBigInteger.fromInt(3)), "3 should be prime")

        // Negative numbers are not prime
        assertTrue(!KBigMath.isPrime(KBigInteger.fromInt(-1)), "-1 should not be prime")
        assertTrue(!KBigMath.isPrime(KBigInteger.fromInt(-7)), "-7 should not be prime")
        assertTrue(!KBigMath.isPrime(KBigInteger.fromInt(-97)), "-97 should not be prime")
    }

    @Test
    fun testIsPrimeSpecialCases() {
        // Perfect squares are not prime (except edge cases)
        val squares = listOf(4, 9, 16, 25, 36, 49, 64, 81, 100, 121, 144, 169, 196, 225)
        for (sq in squares) {
            assertTrue(
                !KBigMath.isPrime(KBigInteger.fromInt(sq)),
                "$sq (perfect square) should not be prime",
            )
        }

        // Powers of 2 > 2 are not prime
        for (exp in 2..20) {
            val pow2 = 1 shl exp
            assertTrue(
                !KBigMath.isPrime(KBigInteger.fromInt(pow2)),
                "2^$exp = $pow2 should not be prime",
            )
        }

        // Mersenne primes: 2^p - 1 where p is prime
        // M2 = 3, M3 = 7, M5 = 31, M7 = 127, M13 = 8191, M17 = 131071, M19 = 524287
        val mersennePrimes = listOf(3, 7, 31, 127, 8191, 131071, 524287)
        for (mp in mersennePrimes) {
            assertTrue(
                KBigMath.isPrime(KBigInteger.fromInt(mp)),
                "Mersenne prime $mp should be prime",
            )
        }
    }

    // ============ Sqrt Compliance Tests ============

    @Test
    fun testSqrtPerfectSquaresCompliance() {
        val perfectSquares = listOf(1, 4, 9, 16, 25, 36, 49, 64, 81, 100, 10000, 1000000)
        val scale = 10

        for (n in perfectSquares) {
            val jValue = JBigDecimal(n)
            val kValue = KBigDecimal.fromInt(n)

            val jResult = javaSqrt(jValue, scale + 5)
            if (jResult == null) {
                // Java sqrt not available, skip
                return
            }

            val kResult = kSqrtToJava(kValue, scale)

            // For perfect squares, expect exact integer result
            val expectedRoot = kotlin.math.sqrt(n.toDouble()).toInt()
            assertEquals(
                expectedRoot.toString(),
                kResult.stripTrailingZeros().toPlainString(),
                "Perfect square sqrt($n) mismatch",
            )

            // Also verify Java result matches
            assertEquals(
                expectedRoot.toString(),
                jResult.stripTrailingZeros().toPlainString(),
                "Java sqrt($n) unexpected result",
            )
        }
    }

    @Test
    fun testSqrtNonPerfectSquaresCompliance() {
        val values = listOf(2, 3, 5, 7, 10, 11, 13, 17, 19, 23, 123, 456789)
        val scales = listOf(10, 20, 50, 100)

        for (n in values) {
            for (scale in scales) {
                val jValue = JBigDecimal(n)
                val kValue = KBigDecimal.fromInt(n)

                // Java uses precision (significant digits), we use scale (decimal places)
                // For sqrt, precision ~= scale + log10(sqrt(n)) + 1
                val precision = scale + 10
                val jResult = javaSqrt(jValue, precision)
                if (jResult == null) {
                    return // Java sqrt not available
                }

                val kResult = kSqrtToJava(kValue, scale)
                val tolerance = toleranceForScale(scale)

                assertRelativelyEqual(
                    jResult,
                    kResult,
                    tolerance,
                    "sqrt($n) at scale $scale",
                )
            }
        }
    }

    @Test
    fun testSqrtDecimalNumbersCompliance() {
        val decimals = listOf("0.25", "0.5", "1.5", "2.5", "2.25", "6.25", "123.456", "0.0001", "0.000001")
        val scale = 20

        for (decStr in decimals) {
            val jValue = JBigDecimal(decStr)
            val kValue = KBigDecimal.fromString(decStr)

            val jResult = javaSqrt(jValue, scale + 10)
            if (jResult == null) {
                return // Java sqrt not available
            }

            val kResult = kSqrtToJava(kValue, scale)
            val tolerance = toleranceForScale(scale)

            assertRelativelyEqual(
                jResult,
                kResult,
                tolerance,
                "sqrt($decStr) at scale $scale",
            )
        }
    }

    @Test
    fun testSqrtRandomCompliance() {
        val rand = Random(randomSeed + 10)
        val scale = 20

        for (i in 0 until iterations) {
            // Generate random positive number with up to 18 integer digits and up to 10 decimal digits
            val intPart = StringBuilder()
            val intDigits = rand.nextInt(1, 18)
            repeat(intDigits) { intPart.append(rand.nextInt(0, 10)) }
            // Ensure not starting with 0 for multi-digit
            if (intPart.length > 1 && intPart[0] == '0') intPart[0] = '1'
            if (intPart.isEmpty() || intPart.toString() == "0") intPart.clear().append("1")

            val fracPart = StringBuilder()
            val fracDigits = rand.nextInt(0, 10)
            repeat(fracDigits) { fracPart.append(rand.nextInt(0, 10)) }

            val numStr = if (fracPart.isNotEmpty()) "$intPart.$fracPart" else intPart.toString()

            val jValue = JBigDecimal(numStr)
            val kValue = KBigDecimal.fromString(numStr)

            val kResult = kSqrtToJava(kValue, scale)

            // Verify by squaring: (sqrt(n))^2 should be close to n
            val squared = kResult.multiply(kResult)
            val diff = squared.subtract(jValue).abs()

            // For numbers with integer part, relative error check
            val relativeError =
                if (jValue.compareTo(JBigDecimal.ZERO) != 0) {
                    diff.divide(jValue, 50, RoundingMode.HALF_UP)
                } else {
                    JBigDecimal.ZERO
                }

            val tolerance = toleranceForScale(scale)

            assertTrue(
                relativeError.compareTo(tolerance) <= 0,
                "sqrt($numStr)^2 relative error $relativeError exceeds tolerance $tolerance at iteration $i",
            )
        }
    }

    @Test
    fun testSqrtLargeNumbersCompliance() {
        val rand = Random(randomSeed + 20)
        // Test with large integers including those > Long.MAX_VALUE
        val digitCounts = listOf(20, 50, 100, 200)
        val scale = 50
        val tolerance = toleranceForScale(scale)

        for (digits in digitCounts) {
            // Generate large random INTEGER
            val sb = StringBuilder()
            sb.append(rand.nextInt(1, 10)) // First digit non-zero
            repeat(digits - 1) { sb.append(rand.nextInt(0, 10)) }

            val numStr = sb.toString()
            val jValue = JBigDecimal(numStr)
            val kValue = KBigDecimal.fromString(numStr)

            val kResult = kSqrtToJava(kValue, scale)

            // Verify by squaring: (sqrt(n))^2 should be close to n
            val squared = kResult.multiply(kResult)
            val diff = squared.subtract(jValue).abs()

            // Relative error check
            val relativeError = diff.divide(jValue, 60, RoundingMode.HALF_UP)

            assertTrue(
                relativeError.compareTo(tolerance) <= 0,
                "sqrt of $digits-digit integer: squared diff relative error $relativeError exceeds tolerance $tolerance",
            )
        }
    }

    @Test
    fun testSqrtVerySmallNumbersCompliance() {
        val smallNumbers = listOf("0.1", "0.01", "0.001", "0.0001", "0.00001", "0.000001")
        val scale = 30
        // Use a more relaxed tolerance for very small numbers
        val tolerance = JBigDecimal("1E-26") // 4 digits buffer

        for (numStr in smallNumbers) {
            val jValue = JBigDecimal(numStr)
            val kValue = KBigDecimal.fromString(numStr)

            val kResult = kSqrtToJava(kValue, scale)

            // Verify by squaring: (sqrt(n))^2 should be close to n
            val squared = kResult.multiply(kResult)
            val diff = squared.subtract(jValue).abs()

            // For very small numbers, use absolute error comparison
            // since relative error can be sensitive
            val relativeError =
                if (jValue.compareTo(JBigDecimal.ZERO) != 0) {
                    diff.divide(jValue, 50, RoundingMode.HALF_UP)
                } else {
                    JBigDecimal.ZERO
                }

            assertTrue(
                relativeError.compareTo(tolerance) <= 0,
                "sqrt($numStr)^2 relative error $relativeError exceeds tolerance $tolerance",
            )
        }
    }

    @Test
    fun testSqrtEdgeCasesCompliance() {
        // sqrt(0) = 0
        val zeroResult = KBigMath.sqrt(KBigDecimal.ZERO, 10)
        assertTrue(zeroResult.isZero(), "sqrt(0) should be 0")

        // sqrt(1) = 1
        val oneResult = KBigMath.sqrt(KBigDecimal.ONE, 10)
        assertEquals("1", oneResult.toString(), "sqrt(1) should be 1")

        // sqrt(negative) should throw exception
        assertFailsWith<ArithmeticException> {
            KBigMath.sqrt(KBigDecimal.fromString("-4"), 10)
        }
    }

    /**
     * Test that previously buggy edge cases now work correctly.
     * These cases used to fail due to overflow in toDoubleApproximation()
     * when unscaledValue exceeded Long.MAX_VALUE.
     */
    @Test
    fun testSqrtPreviouslyBuggyEdgeCases() {
        val edgeCases =
            listOf(
                // Previously BUG 1: Many decimals + large integer
                "29846292161500.897228",
                // 14 int + 6 dec
                "29846292161500.897228439",
                // 14 int + 9 dec
                "12345678901234.897228439",
                // 14 int + 9 dec
                // Previously BUG 2: >= 20 digit integers
                "12345678901234567890",
                // 20 digits
                "123456789012345678901234567890",
                // 30 digits
            )

        val scale = 20
        val tolerance = toleranceForScale(scale)

        for (input in edgeCases) {
            val kValue = KBigDecimal.fromString(input)
            val result = KBigMath.sqrt(kValue, scale)
            val squared = result.multiply(result)

            val jInput = JBigDecimal(input)
            val jSquared = JBigDecimal(squared.toString())
            val diff = jSquared.subtract(jInput).abs()
            val relError = diff.divide(jInput, 50, RoundingMode.HALF_UP)

            assertTrue(
                relError.compareTo(tolerance) <= 0,
                "sqrt($input) failed: relative error $relError exceeds tolerance $tolerance",
            )
        }
    }

    @Test
    fun testSqrtPrecisionScalesCompliance() {
        // Test sqrt(2) at various scales and verify precision
        val value = 2
        val jValue = JBigDecimal(value)
        val kValue = KBigDecimal.fromInt(value)

        val scales = listOf(10, 20, 50, 100)

        for (scale in scales) {
            val jResult = javaSqrt(jValue, scale + 5)
            if (jResult == null) {
                return // Java sqrt not available
            }

            val kResult = kSqrtToJava(kValue, scale)
            val tolerance = toleranceForScale(scale)

            assertRelativelyEqual(
                jResult,
                kResult,
                tolerance,
                "sqrt(2) at scale $scale",
            )

            // Verify the result squared is close to 2
            val squared = kResult.multiply(kResult)
            val diff = squared.subtract(jValue).abs()
            val maxError = JBigDecimal.ONE.divide(JBigDecimal.TEN.pow(scale - 2), scale, RoundingMode.HALF_UP)

            assertTrue(
                diff.compareTo(maxError) <= 0,
                "sqrt(2)^2 should be close to 2 at scale $scale, diff=$diff",
            )
        }
    }
}
