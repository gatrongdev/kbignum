package io.github.gatrongdev.kbignum.benchmark

import io.github.gatrongdev.kbignum.math.KBigDecimal
import io.github.gatrongdev.kbignum.math.KBigInteger
import io.github.gatrongdev.kbignum.math.KBRoundingMode
import io.github.gatrongdev.kbignum.math.pow
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
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
        
        // ============ KBigInteger Benchmarks ============
        println("\n## KBigInteger")
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
        
        // ============ KBigDecimal Benchmarks ============
        println("\n## KBigDecimal")
        println("| Operation | Iterations | Java BigDecimal (ms) | KBigDecimal (ms) | Relative Speed |")
        println("| :--- | :---: | :---: | :---: | :---: |")
        
        println("\n**Decimal Arithmetic (20 digits)**")
        benchmarkDecimalAddition(20, 50000)
        benchmarkDecimalSubtraction(20, 50000)
        benchmarkDecimalMultiplication(20, 10000)
        benchmarkDecimalDivision(20, 5000, 10)
        
        println("\n**Decimal Arithmetic (50 digits)**")
        benchmarkDecimalAddition(50, 25000)
        benchmarkDecimalSubtraction(50, 25000)
        benchmarkDecimalMultiplication(50, 5000)
        benchmarkDecimalDivision(50, 2000, 20)
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
    
    // ============ KBigDecimal Benchmark Functions ============
    
    private fun generateRandomDecimalString(intDigits: Int, fracDigits: Int = 10): String {
        val sb = StringBuilder()
        repeat(intDigits) { sb.append(Random.nextInt(0, 10)) }
        sb.append('.')
        repeat(fracDigits) { sb.append(Random.nextInt(0, 10)) }
        // Ensure it doesn't start with 0 (except for small numbers)
        if (sb[0] == '0' && intDigits > 1) sb[0] = '1'
        return sb.toString()
    }
    
    private fun benchmarkDecimalAddition(digits: Int, iterations: Int) {
        val random = Random(42)
        val listA = List(100) { BigDecimal(generateRandomDecimalString(digits)) }
        val listB = List(100) { BigDecimal(generateRandomDecimalString(digits)) }
        
        val kListA = listA.map { KBigDecimal.fromString(it.toPlainString()) }
        val kListB = listB.map { KBigDecimal.fromString(it.toPlainString()) }

        // Java Warmup
        repeat(100) { listA[it % 100].add(listB[it % 100]) }
        
        val javaTime = measureTimeMillis {
            repeat(iterations) { 
                val i = it % 100
                listA[i].add(listB[i])
            }
        }

        // KBigDecimal Warmup
        repeat(100) { kListA[it % 100].add(kListB[it % 100]) }

        val kTime = measureTimeMillis {
            repeat(iterations) {
                val i = it % 100
                kListA[i].add(kListB[i])
            }
        }

        printTableRow("Decimal Add ${digits}d", iterations, javaTime, kTime)
    }
    
    private fun benchmarkDecimalSubtraction(digits: Int, iterations: Int) {
        val listA = List(100) { BigDecimal(generateRandomDecimalString(digits)) }
        val listB = List(100) { BigDecimal(generateRandomDecimalString(digits / 2)) }
        
        val kListA = listA.map { KBigDecimal.fromString(it.toPlainString()) }
        val kListB = listB.map { KBigDecimal.fromString(it.toPlainString()) }

        // Java Warmup
        repeat(100) { listA[it % 100].subtract(listB[it % 100]) }
        
        val javaTime = measureTimeMillis {
            repeat(iterations) { 
                val i = it % 100
                listA[i].subtract(listB[i])
            }
        }

        // KBigDecimal Warmup
        repeat(100) { kListA[it % 100].subtract(kListB[it % 100]) }

        val kTime = measureTimeMillis {
            repeat(iterations) {
                val i = it % 100
                kListA[i].subtract(kListB[i])
            }
        }

        printTableRow("Decimal Sub ${digits}d", iterations, javaTime, kTime)
    }
    
    private fun benchmarkDecimalMultiplication(digits: Int, iterations: Int) {
        val listA = List(100) { BigDecimal(generateRandomDecimalString(digits)) }
        val listB = List(100) { BigDecimal(generateRandomDecimalString(digits)) }
        
        val kListA = listA.map { KBigDecimal.fromString(it.toPlainString()) }
        val kListB = listB.map { KBigDecimal.fromString(it.toPlainString()) }

        // Java Warmup
        repeat(100) { listA[it % 100].multiply(listB[it % 100]) }
        
        val javaTime = measureTimeMillis {
            repeat(iterations) { 
                val i = it % 100
                listA[i].multiply(listB[i])
            }
        }

        // KBigDecimal Warmup
        repeat(100) { kListA[it % 100].multiply(kListB[it % 100]) }

        val kTime = measureTimeMillis {
            repeat(iterations) {
                val i = it % 100
                kListA[i].multiply(kListB[i])
            }
        }

        printTableRow("Decimal Mul ${digits}d", iterations, javaTime, kTime)
    }
    
    private fun benchmarkDecimalDivision(digits: Int, iterations: Int, scale: Int) {
        val listA = List(100) { BigDecimal(generateRandomDecimalString(digits)) }
        // Use smaller divisors to avoid huge results
        val listB = List(100) { 
            BigDecimal(generateRandomDecimalString(digits / 2)).let { 
                if (it.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ONE else it 
            }
        }
        
        val kListA = listA.map { KBigDecimal.fromString(it.toPlainString()) }
        val kListB = listB.map { KBigDecimal.fromString(it.toPlainString()) }

        // Java Warmup
        repeat(100) { listA[it % 100].divide(listB[it % 100], scale, RoundingMode.HALF_UP) }
        
        val javaTime = measureTimeMillis {
            repeat(iterations) { 
                val i = it % 100
                listA[i].divide(listB[i], scale, RoundingMode.HALF_UP)
            }
        }

        // KBigDecimal Warmup
        repeat(100) { kListA[it % 100].divide(kListB[it % 100], scale, KBRoundingMode.HalfUp) }

        val kTime = measureTimeMillis {
            repeat(iterations) {
                val i = it % 100
                kListA[i].divide(kListB[i], scale, KBRoundingMode.HalfUp)
            }
        }

        printTableRow("Decimal Div ${digits}d", iterations, javaTime, kTime)
    }
}
