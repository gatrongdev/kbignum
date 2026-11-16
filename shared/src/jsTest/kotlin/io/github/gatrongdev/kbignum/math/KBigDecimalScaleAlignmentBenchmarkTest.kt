package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.TimeSource

private const val SCALE_ALIGNMENT_WARMUP_ITERATIONS = 200
private const val SCALE_ALIGNMENT_SAMPLE_COUNT = 7
private const val SCALE_ALIGNMENT_OPERATIONS_PER_SAMPLE = 50

private val isScaleAlignmentBenchmarkEnabled: Boolean =
    js("typeof process !== 'undefined' && process.env.KBIGNUM_DECIMAL_SCALE_ALIGNMENT_BENCHMARK === '1'") as Boolean
private val scaleAlignmentNodeRuntime: String =
    js("typeof process !== 'undefined' ? process.version : 'browser'") as String
private val scaleAlignmentNodePlatform: String =
    js("typeof process !== 'undefined' ? process.platform : 'browser'") as String

/** Optional Node.js measurement for same-scale, mismatched-scale, and zero operations. */
class KBigDecimalScaleAlignmentBenchmarkTest {
    @Test
    fun scaleAlignmentReportsMedianLatency() {
        if (!isScaleAlignmentBenchmarkEnabled) return

        for (digits in intArrayOf(500, 1000)) {
            for (scaleGap in intArrayOf(8, 128)) {
                val lowerScale = KBigDecimal(KBigInteger.fromString("9".repeat(digits)), 8)
                val sameScaleRight = KBigDecimal(KBigInteger.fromString("8".repeat(digits)), 8)
                val higherScaleRight = KBigDecimal(KBigInteger.fromString("8".repeat(digits)), 8 + scaleGap)
                val lowerScaleZero = KBigDecimal(KBigInteger.ZERO, 8)
                val higherScaleZero = KBigDecimal(KBigInteger.ZERO, 8 + scaleGap)

                val operations =
                    listOf(
                        "addSameScale" to { lowerScale.add(sameScaleRight) as Any },
                        "addMismatchedScale" to { lowerScale.add(higherScaleRight) as Any },
                        "subtractMismatchedScale" to { lowerScale.subtract(higherScaleRight) as Any },
                        "compareMismatchedScale" to { lowerScale.compareTo(higherScaleRight) as Any },
                        "addZeroMismatchedScale" to { lowerScaleZero.add(higherScaleRight) as Any },
                        "subtractZeroMismatchedScale" to { lowerScale.subtract(higherScaleZero) as Any },
                        "compareZeroMismatchedScale" to { lowerScaleZero.compareTo(higherScaleRight) as Any },
                    )

                assertEquals(8, operations[0].second().let { (it as KBigDecimal).scale })
                assertEquals(8 + scaleGap, operations[1].second().let { (it as KBigDecimal).scale })
                assertEquals(8 + scaleGap, operations[2].second().let { (it as KBigDecimal).scale })
                assertEquals(1, operations[3].second())
                assertEquals(higherScaleRight, operations[4].second())
                assertEquals(8 + scaleGap, operations[5].second().let { (it as KBigDecimal).scale })
                assertEquals(-1, operations[6].second())

                for ((name, operation) in operations) {
                    repeat(SCALE_ALIGNMENT_WARMUP_ITERATIONS) { operation() }
                    val samples =
                        DoubleArray(SCALE_ALIGNMENT_SAMPLE_COUNT) {
                            measureAverageNanos(SCALE_ALIGNMENT_OPERATIONS_PER_SAMPLE, operation)
                        }
                    println(
                        "DECIMAL_SCALE_ALIGNMENT_BENCHMARK digits=$digits scaleGap=$scaleGap " +
                            "operation=$name iterations=$SCALE_ALIGNMENT_OPERATIONS_PER_SAMPLE " +
                            "samples=$SCALE_ALIGNMENT_SAMPLE_COUNT runtime=$scaleAlignmentNodeRuntime " +
                            "platform=$scaleAlignmentNodePlatform medianNs=${median(samples)}",
                    )
                }
            }
        }
    }

    private fun measureAverageNanos(
        iterations: Int,
        operation: () -> Any,
    ): Double {
        var result: Any? = null
        val start = TimeSource.Monotonic.markNow()
        repeat(iterations) { result = operation() }
        val elapsed = start.elapsedNow().inWholeNanoseconds
        check(result != null)
        return elapsed.toDouble() / iterations
    }

    private fun median(samples: DoubleArray): Double {
        val sorted = samples.sortedArray()
        return sorted[sorted.size / 2]
    }
}
