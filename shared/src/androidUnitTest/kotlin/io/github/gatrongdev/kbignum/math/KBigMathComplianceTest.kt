package io.github.gatrongdev.kbignum.math

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import java.math.BigInteger as JBigInteger
import java.math.BigDecimal as JBigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.random.Random
import io.github.gatrongdev.kbignum.math.KBigInteger
import io.github.gatrongdev.kbignum.math.KBigDecimal
import io.github.gatrongdev.kbignum.math.KBigMath

class KBigMathComplianceTest {

    private val RANDOM_SEED = 123456L
    private val ITERATIONS = 1000

    // ============ Helper Functions for Sqrt Compliance ============

    /**
     * Compares two BigDecimals with relative error tolerance.
     * relativeError = |a - b| / max(|a|, |b|)
     */
    private fun assertRelativelyEqual(
        expected: JBigDecimal,
        actual: JBigDecimal,
        tolerance: JBigDecimal,
        message: String
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
            "Relative error $relError exceeds tolerance $tolerance. $message"
        )
    }

    /**
     * Calls Java BigDecimal.sqrt(MathContext) via reflection (Java 9+).
     * Returns null if sqrt is not available.
     */
    private fun javaSqrt(value: JBigDecimal, precision: Int): JBigDecimal? {
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
    private fun kSqrtToJava(value: KBigDecimal, scale: Int): JBigDecimal {
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
        val rand = Random(RANDOM_SEED)
        for (i in 0 until ITERATIONS) {
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
        val rand = Random(RANDOM_SEED + 1)
        for (i in 0 until ITERATIONS) {
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
        val rand = Random(RANDOM_SEED + 2)
        for (i in 0 until ITERATIONS) {
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
                "Perfect square sqrt($n) mismatch"
            )

            // Also verify Java result matches
            assertEquals(
                expectedRoot.toString(),
                jResult.stripTrailingZeros().toPlainString(),
                "Java sqrt($n) unexpected result"
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
                    "sqrt($n) at scale $scale"
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
                "sqrt($decStr) at scale $scale"
            )
        }
    }

    @Test
    fun testSqrtRandomCompliance() {
        val rand = Random(RANDOM_SEED + 10)
        val scale = 20

        for (i in 0 until ITERATIONS) {
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
            val relativeError = if (jValue.compareTo(JBigDecimal.ZERO) != 0) {
                diff.divide(jValue, 50, RoundingMode.HALF_UP)
            } else {
                JBigDecimal.ZERO
            }

            val tolerance = toleranceForScale(scale)

            assertTrue(
                relativeError.compareTo(tolerance) <= 0,
                "sqrt($numStr)^2 relative error $relativeError exceeds tolerance $tolerance at iteration $i"
            )
        }
    }

    @Test
    fun testSqrtLargeNumbersCompliance() {
        val rand = Random(RANDOM_SEED + 20)
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
                "sqrt of $digits-digit integer: squared diff relative error $relativeError exceeds tolerance $tolerance"
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
            val relativeError = if (jValue.compareTo(JBigDecimal.ZERO) != 0) {
                diff.divide(jValue, 50, RoundingMode.HALF_UP)
            } else {
                JBigDecimal.ZERO
            }

            assertTrue(
                relativeError.compareTo(tolerance) <= 0,
                "sqrt($numStr)^2 relative error $relativeError exceeds tolerance $tolerance"
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
        val edgeCases = listOf(
            // Previously BUG 1: Many decimals + large integer
            "29846292161500.897228",      // 14 int + 6 dec
            "29846292161500.897228439",   // 14 int + 9 dec
            "12345678901234.897228439",   // 14 int + 9 dec
            // Previously BUG 2: >= 20 digit integers
            "12345678901234567890",       // 20 digits
            "123456789012345678901234567890", // 30 digits
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
                "sqrt($input) failed: relative error $relError exceeds tolerance $tolerance"
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
                "sqrt(2) at scale $scale"
            )

            // Verify the result squared is close to 2
            val squared = kResult.multiply(kResult)
            val diff = squared.subtract(jValue).abs()
            val maxError = JBigDecimal.ONE.divide(JBigDecimal.TEN.pow(scale - 2), scale, RoundingMode.HALF_UP)

            assertTrue(
                diff.compareTo(maxError) <= 0,
                "sqrt(2)^2 should be close to 2 at scale $scale, diff=$diff"
            )
        }
    }
}
