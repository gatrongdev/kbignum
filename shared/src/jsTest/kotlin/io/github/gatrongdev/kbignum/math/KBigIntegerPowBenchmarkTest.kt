package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.TimeSource

private const val WARMUP_ITERATIONS = 1
private const val SAMPLE_COUNT = 5

private val isPowBenchmarkEnabled: Boolean =
    js("typeof process !== 'undefined' && process.env.KBIGNUM_POW_BENCHMARK === '1'") as Boolean
private val nodeRuntime: String = js("typeof process !== 'undefined' ? process.version : 'browser'") as String
private val nodePlatform: String = js("typeof process !== 'undefined' ? process.platform : 'browser'") as String

/** Optional Node.js microbenchmark. Set KBIGNUM_POW_BENCHMARK=1 to print measurements. */
class KBigIntegerPowBenchmarkTest {
    @Test
    fun powVersusBinaryExponentiationWithFinalUnusedSquare() {
        if (!isPowBenchmarkEnabled) return

        for (bitLength in intArrayOf(512, 1024, 2048, 4096)) {
            val wordCount = (bitLength + 31) / 32
            val magnitude = IntArray(wordCount) { index -> (index * 1_664_525 + 1_013_904_223) xor wordCount }
            magnitude[wordCount - 1] = magnitude.last() or Int.MIN_VALUE
            val value =
                KBigInteger(
                    1,
                    magnitude,
                )
            val measuredIterations =
                when {
                    bitLength <= 512 -> 16
                    bitLength <= 1024 -> 8
                    bitLength <= 2048 -> 2
                    else -> 1
                }

            assertEquals(legacyPow(value, 5), value.pow(5))
            repeat(WARMUP_ITERATIONS) {
                value.pow(5)
                legacyPow(value, 5)
            }

            val optimizedSamples = DoubleArray(SAMPLE_COUNT)
            val legacySamples = DoubleArray(SAMPLE_COUNT)
            repeat(SAMPLE_COUNT) { sample ->
                if (sample % 2 == 0) {
                    optimizedSamples[sample] = measureAverageNanos(measuredIterations) { value.pow(5) }
                    legacySamples[sample] = measureAverageNanos(measuredIterations) { legacyPow(value, 5) }
                } else {
                    legacySamples[sample] = measureAverageNanos(measuredIterations) { legacyPow(value, 5) }
                    optimizedSamples[sample] = measureAverageNanos(measuredIterations) { value.pow(5) }
                }
            }

            println(
                "POW_BENCHMARK bits=$bitLength " +
                    "iterations=$measuredIterations samples=$SAMPLE_COUNT " +
                    "runtime=$nodeRuntime platform=$nodePlatform " +
                    "optimizedNs=${median(optimizedSamples)} " +
                    "legacyNs=${median(legacySamples)}",
            )
        }
    }

    private fun legacyPow(
        value: KBigInteger,
        exponent: Int,
    ): KBigInteger {
        var result = KBigInteger.ONE
        var base = value
        var exp = exponent
        while (exp > 0) {
            if (exp % 2 == 1) result = result.multiply(base)
            base = base.multiply(base)
            exp /= 2
        }
        return result
    }

    private fun measureAverageNanos(
        iterations: Int,
        operation: () -> KBigInteger,
    ): Double {
        var result = KBigInteger.ZERO
        val start = TimeSource.Monotonic.markNow()
        repeat(iterations) { result = operation() }
        val elapsed = start.elapsedNow().inWholeNanoseconds
        check(result.signum == 1)
        return elapsed.toDouble() / iterations
    }

    private fun median(samples: DoubleArray): Double {
        val sorted = samples.sortedArray()
        return sorted[sorted.size / 2]
    }
}
