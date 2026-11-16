package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.TimeSource

private const val WARMUP_ITERATIONS = 2_000
private const val SAMPLE_COUNT = 15
private const val OPERATIONS_PER_SAMPLE = 100
private const val BOUNDARY_WARMUP_ITERATIONS = 400
private const val BOUNDARY_OPERATIONS_PER_SAMPLE = 20
private const val LARGE_BALANCED_WARMUP_ITERATIONS = 100
private const val LARGE_BALANCED_OPERATIONS_PER_SAMPLE = 10

private val isDecimalMultiplyBenchmarkEnabled: Boolean =
    js("typeof process !== 'undefined' && process.env.KBIGNUM_DECIMAL_MULTIPLY_BENCHMARK === '1'") as Boolean
private val nodeRuntime: String = js("typeof process !== 'undefined' ? process.version : 'browser'") as String
private val nodePlatform: String = js("typeof process !== 'undefined' ? process.platform : 'browser'") as String

/** Optional Node.js microbenchmark. Set KBIGNUM_DECIMAL_MULTIPLY_BENCHMARK=1 to print measurements. */
class KBigDecimalMultiplyBenchmarkTest {
    @Test
    fun thresholdBoundaryMultiplicationReportsMedianLatency() {
        if (!isDecimalMultiplyBenchmarkEnabled) return

        for (digits in intArrayOf(400, 450, 500)) {
            reportBalancedMultiplication(
                digits = digits,
                warmupIterations = BOUNDARY_WARMUP_ITERATIONS,
                operationsPerSample = BOUNDARY_OPERATIONS_PER_SAMPLE,
            )
        }
    }

    @Test
    fun largeValueMultiplicationReportsMedianLatency() {
        if (!isDecimalMultiplyBenchmarkEnabled) return

        for (digits in intArrayOf(1000, 2000)) {
            reportBalancedMultiplication(
                digits = digits,
                warmupIterations = LARGE_BALANCED_WARMUP_ITERATIONS,
                operationsPerSample = LARGE_BALANCED_OPERATIONS_PER_SAMPLE,
            )
        }
    }

    @Test
    fun scaledUnitMultiplicationReportsMedianLatency() {
        if (!isDecimalMultiplyBenchmarkEnabled) return

        for (digits in intArrayOf(500, 1000, 2000)) {
            val largeValue = KBigDecimal(createUnscaledValue(digits, digits), 8)
            val scaledUnit = KBigDecimal(KBigInteger.ONE, 16)
            val result = largeValue.multiply(scaledUnit)

            assertEquals(24, result.scale)
            assertEquals(largeValue.unscaledValue, result.unscaledValue)
            assertTrue(result.signum() > 0)

            repeat(WARMUP_ITERATIONS) { largeValue.multiply(scaledUnit) }
            val samples =
                DoubleArray(SAMPLE_COUNT) {
                    measureAverageNanos(OPERATIONS_PER_SAMPLE) { largeValue.multiply(scaledUnit) }
                }
            println(
                "DECIMAL_MULTIPLY_UNIT_BENCHMARK digits=$digits " +
                    "iterations=$OPERATIONS_PER_SAMPLE samples=$SAMPLE_COUNT " +
                    "runtime=$nodeRuntime platform=$nodePlatform medianNs=${median(samples)}",
            )
        }
    }

    private fun reportBalancedMultiplication(
        digits: Int,
        warmupIterations: Int,
        operationsPerSample: Int,
    ) {
        val left = KBigDecimal(createUnscaledValue(digits, digits), 8)
        val right = KBigDecimal(createUnscaledValue(digits, digits xor 0x5A5A), 16)
        val operation = { left.multiply(right) }
        val result = operation()

        assertEquals(24, result.scale)
        assertTrue(result.signum() > 0)

        repeat(warmupIterations) { operation() }
        val samples =
            DoubleArray(SAMPLE_COUNT) {
                measureAverageNanos(operationsPerSample, operation)
            }
        println(
            "DECIMAL_MULTIPLY_BALANCED_BENCHMARK digits=$digits " +
                "iterations=$operationsPerSample samples=$SAMPLE_COUNT " +
                "runtime=$nodeRuntime platform=$nodePlatform medianNs=${median(samples)}",
        )
    }

    private fun createUnscaledValue(
        digits: Int,
        seed: Int,
    ): KBigInteger {
        val wordCount = (digits * 3_322 + 31_999) / 32_000
        val magnitude = IntArray(wordCount) { index -> (index * 1_664_525 + 1_013_904_223) xor seed }
        magnitude[0] = magnitude[0] or 1
        magnitude[wordCount - 1] = magnitude.last() or Int.MIN_VALUE
        return KBigInteger(1, magnitude)
    }

    private fun measureAverageNanos(
        iterations: Int,
        operation: () -> KBigDecimal,
    ): Double {
        var result = KBigDecimal.ZERO
        val start = TimeSource.Monotonic.markNow()
        repeat(iterations) { result = operation() }
        val elapsed = start.elapsedNow().inWholeNanoseconds
        check(result.signum() != 0)
        return elapsed.toDouble() / iterations
    }

    private fun median(samples: DoubleArray): Double {
        val sorted = samples.sortedArray()
        return sorted[sorted.size / 2]
    }
}
