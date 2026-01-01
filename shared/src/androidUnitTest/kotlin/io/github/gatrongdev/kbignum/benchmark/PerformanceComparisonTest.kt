package io.github.gatrongdev.kbignum.benchmark

import io.github.gatrongdev.kbignum.math.KBigInteger
import io.github.gatrongdev.kbignum.math.pow
import org.junit.Test
import java.math.BigInteger
import kotlin.random.Random
import kotlin.system.measureTimeMillis

/**
 * A manual benchmark tool to generate performance stats for README.
 * Run this test and check the console output.
 */
class PerformanceComparisonTest {

    @Test
    fun runBenchmarksAndPrintMarkdown() {
        println("### Performance Benchmarks (Java JVM)")
        println("| Operation | Iterations | Java BigInteger (ms) | KBignum (ms) | Relative Speed |")
        println("| :--- | :---: | :---: | :---: | :---: |")

        // Basic Arithmetic Operations
        println("\n**Basic Arithmetic (2048-bit numbers)**")
        benchmarkAddition(2048, 50000)
        benchmarkSubtraction(2048, 50000)
        benchmarkMultiplication(2048, 10000)
        benchmarkDivision(2048, 5000)
        benchmarkModulo(2048, 5000)

        // Basic Arithmetic with larger numbers
        println("\n**Basic Arithmetic (4096-bit numbers)**")
        benchmarkAddition(4096, 25000)
        benchmarkSubtraction(4096, 25000)
        benchmarkMultiplication(4096, 5000)
        benchmarkDivision(4096, 2000)
        benchmarkModulo(4096, 2000)

        // Factorial (tests repeated multiplication)
        println("\n**Factorial (Repeated Multiplication)**")
        benchmarkFactorial(100, 1000)
        benchmarkFactorial(500, 200)
        benchmarkFactorial(1000, 50)
    }

    private fun benchmarkAddition(bits: Int, iterations: Int) {
        val listA = List(100) { BigInteger(bits, java.util.Random(42)) }
        val listB = List(100) { BigInteger(bits, java.util.Random(43)) }

        val kListA = listA.map { KBigInteger.fromString(it.toString()) }
        val kListB = listB.map { KBigInteger.fromString(it.toString()) }

        // Java Warmup
        repeat(100) { listA[it % 100].add(listB[it % 100]) }

        val javaTime = measureTimeMillis {
            repeat(iterations) {
                val i = it % 100
                listA[i].add(listB[i])
            }
        }

        // KBignum Warmup
        repeat(100) { kListA[it % 100].add(kListB[it % 100]) }

        val kTime = measureTimeMillis {
            repeat(iterations) {
                val i = it % 100
                kListA[i].add(kListB[i])
            }
        }

        printTableRow("Add ${bits}-bit", iterations, javaTime, kTime)
    }

    private fun benchmarkSubtraction(bits: Int, iterations: Int) {
        val listA = List(100) { BigInteger(bits, java.util.Random(42)) }
        val listB = List(100) { BigInteger(bits / 2, java.util.Random(43)) } // Smaller to avoid negative

        val kListA = listA.map { KBigInteger.fromString(it.toString()) }
        val kListB = listB.map { KBigInteger.fromString(it.toString()) }

        // Java Warmup
        repeat(100) { listA[it % 100].subtract(listB[it % 100]) }

        val javaTime = measureTimeMillis {
            repeat(iterations) {
                val i = it % 100
                listA[i].subtract(listB[i])
            }
        }

        // KBignum Warmup
        repeat(100) { kListA[it % 100].subtract(kListB[it % 100]) }

        val kTime = measureTimeMillis {
            repeat(iterations) {
                val i = it % 100
                kListA[i].subtract(kListB[i])
            }
        }

        printTableRow("Subtract ${bits}-bit", iterations, javaTime, kTime)
    }

    private fun benchmarkDivision(bits: Int, iterations: Int) {
        val listA = List(100) { BigInteger(bits, java.util.Random(42)) }
        val listB = List(100) { BigInteger(bits / 2, java.util.Random(43)).max(BigInteger.ONE) } // Ensure non-zero

        val kListA = listA.map { KBigInteger.fromString(it.toString()) }
        val kListB = listB.map { KBigInteger.fromString(it.toString()) }

        // Java Warmup
        repeat(100) { listA[it % 100].divide(listB[it % 100]) }

        val javaTime = measureTimeMillis {
            repeat(iterations) {
                val i = it % 100
                listA[i].divide(listB[i])
            }
        }

        // KBignum Warmup
        repeat(100) { kListA[it % 100].divide(kListB[it % 100]) }

        val kTime = measureTimeMillis {
            repeat(iterations) {
                val i = it % 100
                kListA[i].divide(kListB[i])
            }
        }

        printTableRow("Divide ${bits}-bit", iterations, javaTime, kTime)
    }

    private fun benchmarkModulo(bits: Int, iterations: Int) {
        val listA = List(100) { BigInteger(bits, java.util.Random(42)) }
        val listB = List(100) { BigInteger(bits / 2, java.util.Random(43)).max(BigInteger.ONE) }

        val kListA = listA.map { KBigInteger.fromString(it.toString()) }
        val kListB = listB.map { KBigInteger.fromString(it.toString()) }

        // Java Warmup
        repeat(100) { listA[it % 100].mod(listB[it % 100]) }

        val javaTime = measureTimeMillis {
            repeat(iterations) {
                val i = it % 100
                listA[i].mod(listB[i])
            }
        }

        // KBignum Warmup
        repeat(100) { kListA[it % 100].mod(kListB[it % 100]) }

        val kTime = measureTimeMillis {
            repeat(iterations) {
                val i = it % 100
                kListA[i].mod(kListB[i])
            }
        }

        printTableRow("Modulo ${bits}-bit", iterations, javaTime, kTime)
    }

    private fun benchmarkFactorial(n: Int, iterations: Int) {
        // Java Warmup
        repeat(10) { calculateFactorialJava(n) }
        val javaTime = measureTimeMillis {
            repeat(iterations) { calculateFactorialJava(n) }
        }

        // KBigInt Warmup
        repeat(10) { calculateFactorialKBig(n) }
        val kTime = measureTimeMillis {
            repeat(iterations) { calculateFactorialKBig(n) }
        }

        printTableRow("Factorial($n)", iterations, javaTime, kTime)
    }

    private fun benchmarkMultiplication(bits: Int, iterations: Int) {
        val random = Random(42) // Fixed seed for reproducibility
        val listA = List(100) { BigInteger(bits, java.util.Random()) }
        val listB = List(100) { BigInteger(bits, java.util.Random()) }

        val kListA = listA.map { KBigInteger.fromString(it.toString()) }
        val kListB = listB.map { KBigInteger.fromString(it.toString()) }

        // Java Warmup
        var idx = 0
        repeat(100) {
            listA[idx].multiply(listB[idx])
            idx = (idx + 1) % 100
        }

        val javaTime = measureTimeMillis {
            repeat(iterations) {
               val i = it % 100
               listA[i].multiply(listB[i])
            }
        }

        // KBignum Warmup
        repeat(100) {
            kListA[idx].multiply(kListB[idx])
            idx = (idx + 1) % 100
        }

        val kTime = measureTimeMillis {
            repeat(iterations) {
                val i = it % 100
                kListA[i].multiply(kListB[i])
            }
        }

        printTableRow("Multiply ${bits}-bit", iterations, javaTime, kTime)
    }

    private fun calculateFactorialJava(n: Int): BigInteger {
        var result = BigInteger.ONE
        for (i in 1..n) {
            result = result.multiply(BigInteger.valueOf(i.toLong()))
        }
        return result
    }

    private fun calculateFactorialKBig(n: Int): KBigInteger {
        var result = KBigInteger.ONE
        for (i in 1..n) {
            result = result.multiply(KBigInteger.fromLong(i.toLong()))
        }
        return result
    }

    private fun printTableRow(op: String, runs: Int, javaMs: Long, kMs: Long) {
        val relative = String.format("%.2fx", kMs.toDouble() / javaMs.toDouble())
        // If KMs < JavaMs, it's faster? Usually Java is gold standard optimized C intrinsics.
        // We expect KBig to be slower (Pure Kotlin).
        println("| mean $op | $runs | $javaMs | $kMs | $relative |")
    }
}
