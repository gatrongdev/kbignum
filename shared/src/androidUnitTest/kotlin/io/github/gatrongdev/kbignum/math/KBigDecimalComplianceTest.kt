package io.github.gatrongdev.kbignum.math

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import kotlin.random.Random

class KBigDecimalComplianceTest {
    @Test
    fun testRandomAdditions() {
        repeat(10000) {
            val (a, b) = generateRandomPair()
            val expected = a.add(b)

            val realKA = createKBigDecimal(a)
            val realKB = createKBigDecimal(b)

            val result = realKA.add(realKB)

            assertEquals("Addition failed for $a + $b", expected.toString(), result.toJavaBigDecimal().toString())
        }
    }

    @Test
    fun testRandomSubtractions() {
        repeat(10000) {
            val (a, b) = generateRandomPair()
            val expected = a.subtract(b)

            val realKA = createKBigDecimal(a)
            val realKB = createKBigDecimal(b)

            val result = realKA.subtract(realKB)

            assertEquals("Subtraction failed for $a - $b", expected.toString(), result.toJavaBigDecimal().toString())
        }
    }

    @Test
    fun testRandomMultiplications() {
        repeat(10000) {
            val (a, b) = generateRandomPair()
            val expected = a.multiply(b)

            val realKA = createKBigDecimal(a)
            val realKB = createKBigDecimal(b)

            val result = realKA.multiply(realKB)

            assertEquals("Multiplication failed for $a * $b", expected.toString(), result.toJavaBigDecimal().toString())
        }
    }

    @Test
    fun testRandomDecimalDivisions() {
        // Test division with rounding HALF_UP
        repeat(5000) {
            val (a, b) = generateRandomPair()
            // Avoid zero divisor
            if (b.signum() == 0) return@repeat

            val scale = kotlin.math.max(a.scale(), b.scale())

            val expected =
                try {
                    a.divide(b, scale, java.math.RoundingMode.HALF_UP)
                } catch (e: ArithmeticException) {
                    return@repeat
                }

            val kA = KBigDecimal.fromString(a.toString())
            val kB = KBigDecimal.fromString(b.toString())

            val result = kA.divide(kB, scale, KBRoundingMode.HalfUp)

            assertEquals("Division failed for $a / $b scale=$scale", expected.toString(), result.toJavaBigDecimal().toString())
        }
    }

    // --- Precision & Scale Mismatch Tests ---

    @Test
    fun testScaleMismatchAddition() {
        val bigA = BigDecimal("1e100")
        val smallB = BigDecimal("1e-100")

        val expected = bigA.add(smallB)

        val kBigA = KBigDecimal.fromString(bigA.toPlainString())
        val kSmallB = KBigDecimal.fromString(smallB.toPlainString())

        val result = kBigA.add(kSmallB)

        assertEquals("Scale Mismatch Add failed", expected.toPlainString(), result.toJavaBigDecimal().toPlainString())
    }

    @Test
    fun testScaleMismatchSubtraction() {
        val a = BigDecimal.ONE
        val b = BigDecimal("1e-50")

        val expected = a.subtract(b)

        val kA = KBigDecimal.ONE
        val kB = KBigDecimal.fromString(b.toPlainString())

        val result = kA.subtract(kB)

        assertEquals("Scale Mismatch Sub failed", expected.toPlainString(), result.toJavaBigDecimal().toPlainString())
    }

    // --- Rounding Mode Tests ---

    @Test
    fun testRoundingModes() {
        val testValues =
            listOf(
                "2.5", "2.1", "2.9",
                "-2.5", "-2.1", "-2.9",
                "1.5", "0.5", "-1.5", "-0.5",
                "5.555", "5.55",
            )

        val scales = listOf(0, 1, 2)
        val modes =
            listOf(
                java.math.RoundingMode.UP,
                java.math.RoundingMode.DOWN,
                java.math.RoundingMode.CEILING,
                java.math.RoundingMode.FLOOR,
                java.math.RoundingMode.HALF_UP,
                java.math.RoundingMode.HALF_DOWN,
                java.math.RoundingMode.HALF_EVEN,
            )

        for (v in testValues) {
            val bd = BigDecimal(v)
            val kbd = KBigDecimal.fromString(v)

            for (scale in scales) {
                if (scale >= bd.scale()) continue

                for (mode in modes) {
                    val kMode = mapRoundingMode(mode)

                    val expected = bd.setScale(scale, mode)
                    // Simulate setScale using divide by 1
                    val result = kbd.divide(KBigDecimal.ONE, scale, kMode)

                    assertEquals(
                        "Rounding failed: $v scale=$scale mode=$mode",
                        expected.toPlainString(),
                        result.toJavaBigDecimal().toPlainString(),
                    )
                }
            }
        }
    }

    @Test
    fun testDivisionEdgeCases() {
        val one = KBigDecimal.ONE
        val three = KBigDecimal.fromInt(3)

        val result = one.divide(three, 10, KBRoundingMode.HalfUp)
        assertEquals("Simple 1/3 failed", "0.3333333333", result.toJavaBigDecimal().toPlainString())

        try {
            one.divide(KBigDecimal.ZERO, 5, KBRoundingMode.HalfUp)
            org.junit.Assert.fail("Should throw ArithmeticException for div by zero")
        } catch (e: ArithmeticException) {
            // Expected
        }
    }

    @Test
    fun testHugeRandomDecimalArithmetic() {
        // Test with 4096-bit unscaled values to stress test scale alignment logic with huge numbers
        repeat(200) {
            val (a, b) = generateHugeRandomPair(4096)

            // Addition
            assertEquals(
                "Huge Decimal Add failed",
                a.add(b).toPlainString(),
                createKBigDecimal(a).add(createKBigDecimal(b)).toJavaBigDecimal().toPlainString(),
            )

            // Subtraction
            assertEquals(
                "Huge Decimal Subtract failed",
                a.subtract(b).toPlainString(),
                createKBigDecimal(a).subtract(createKBigDecimal(b)).toJavaBigDecimal().toPlainString(),
            )

            // Multiplication
            assertEquals(
                "Huge Decimal Multiply failed",
                a.multiply(b).toPlainString(),
                createKBigDecimal(a).multiply(createKBigDecimal(b)).toJavaBigDecimal().toPlainString(),
            )
        }
    }

    @Test
    fun testCompareToVsEquals() {
        // BigDecimal semantics:
        // equals() checks value AND scale (1.0 != 1.00)
        // compareTo() checks value only (1.0 == 1.00)

        val val1 = KBigDecimal.fromString("1.0")
        val val2 = KBigDecimal.fromString("1.00")

        // Assert Equals: Should be FALSE because scales differ (1 vs 2)
        assertEquals("1.0 should not equal 1.00", false, val1.equals(val2))

        // Assert CompareTo: Should be 0 (equal)
        assertEquals("1.0 should compareTo 0 against 1.00", 0, val1.compareTo(val2))

        // Zero case
        val z1 = KBigDecimal.fromString("0")
        val z2 = KBigDecimal.fromString("0.0000")
        assertEquals("0 should not equal 0.0000", false, z1.equals(z2))
        assertEquals("0 should compareTo 0 against 0.0000", 0, z1.compareTo(z2))
    }

    @Test
    fun testParsingEdgeCases() {
        // Leading zeros
        val a = KBigDecimal.fromString("00123.456")
        assertEquals("Leading zeros check", "123.456", a.toJavaBigDecimal().toPlainString())

        // Trailing zeros in decimal (should be preserved in scale)
        val b = KBigDecimal.fromString("1.500")
        assertEquals("Trailing zeros check", 3, b.scale)
        assertEquals("Trailing zeros val check", "1.500", b.toString().toJavaValueStringFromDebug())

        // Just dot? Invalid usually, or 0.
        // Java BigDecimal "." is invalid.
        try {
            KBigDecimal.fromString(".")
            org.junit.Assert.fail("Should throw for single dot")
        } catch (e: Exception) {
            // Success
        }

        // Signs
        val c = KBigDecimal.fromString("+12.3")
        assertEquals("Positive sign", "12.3", c.toJavaBigDecimal().toPlainString())

        val d = KBigDecimal.fromString("-0.01")
        assertEquals("Negative sign", "-0.01", d.toJavaBigDecimal().toPlainString())
    }

    // Helper to extract value from "KBigDecimal(unscaled=..., scale=...)" for verification
    private fun String.toJavaValueStringFromDebug(): String {
        if (!this.startsWith("KBigDecimal")) return this
        val parts = this.removePrefix("KBigDecimal(").removeSuffix(")").split(", ")
        val unscaled = java.math.BigInteger(parts[0].substringAfter("="))
        val scale = parts[1].substringAfter("=").toInt()
        return BigDecimal(unscaled, scale).toPlainString()
    }

    // --- Helpers ---

    private fun generateRandomPair(): Pair<BigDecimal, BigDecimal> {
        val aUnscaled = Random.nextLong()
        val bUnscaled = Random.nextLong()
        val aScale = Random.nextInt(0, 10)
        val bScale = Random.nextInt(0, 10)

        return BigDecimal.valueOf(aUnscaled, aScale) to BigDecimal.valueOf(bUnscaled, bScale)
    }

    private fun generateHugeRandomPair(bits: Int): Pair<BigDecimal, BigDecimal> {
        val aUnscaled = generateHugeRandomBigInteger(bits)
        val bUnscaled = generateHugeRandomBigInteger(bits)

        val aScale = Random.nextInt(0, 50)
        val bScale = Random.nextInt(0, 50)

        return BigDecimal(aUnscaled, aScale) to BigDecimal(bUnscaled, bScale)
    }

    private fun createKBigDecimal(bd: BigDecimal): KBigDecimal {
        // Use toPlainString to avoid scientific notation and ensure full precision is passed
        return KBigDecimal.fromString(bd.toPlainString())
    }
}
