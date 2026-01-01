package io.github.gatrongdev.kbignum.math

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigInteger
import kotlin.random.Random

class KBigIntegerComplianceTest {

    @Test
    fun testRandomAdditions() {
        repeat(10000) {
            val (a, b) = generateRandomPair()
            val expected = a.add(b)
            
            val kA = KBigInteger.fromLong(a.toLong()) // Limited to Long for now for Phase 1/2 safety
            val kB = KBigInteger.fromLong(b.toLong())
            
            val result = kA.add(kB)
            
            assertEquals("Addition failed for $a + $b", expected.toString(), result.toString())
        }
    }
    
    @Test
    fun testRandomSubtractions() {
        repeat(10000) {
            val (a, b) = generateRandomPair()
            val expected = a.subtract(b)
            
            val kA = KBigInteger.fromLong(a.toLong())
            val kB = KBigInteger.fromLong(b.toLong())
            
            val result = kA.subtract(kB)
            
            assertEquals("Subtraction failed for $a - $b", expected.toString(), result.toString())
        }
    }
    
    @Test
    fun testRandomMultiplications() {
        repeat(10000) {
            val a = Random.nextLong(1000000)
            val b = Random.nextLong(1000000)
            val bigA = BigInteger.valueOf(a)
            val bigB = BigInteger.valueOf(b)
            
            val expected = bigA.multiply(bigB)
            
            val kA = KBigInteger.fromLong(a)
            val kB = KBigInteger.fromLong(b)
            
            val result = kA.multiply(kB)
            
            assertEquals("Multiplication failed for $a * $b", expected.toString(), result.toString())
        }
    }

    @Test
    fun testRandomLargeDivisions() {
        // Test division with numbers larger than Long to exercise multi-word logic
        repeat(5000) {
            // Generate random byte arrays to create large BigIntegers
            val aBytes = Random.nextBytes(32) // ~256 bits
            val bBytes = Random.nextBytes(16) // ~128 bits
            
            // Ensure positive for simplicity in initial phase
            aBytes[0] = (aBytes[0].toInt() and 0x7F).toByte()
            bBytes[0] = (bBytes[0].toInt() and 0x7F).toByte()
            
            // Avoid zero divisor
            if (bBytes.all { it == 0.toByte() }) bBytes[0] = 1
            
            val bigA = BigInteger(aBytes)
            val bigB = BigInteger(bBytes)
            
            // Ensure B is not 0 just in case
            val safeBigB = if (bigB == BigInteger.ZERO) BigInteger.ONE else bigB
            
            val expectedQuotient = bigA.divide(safeBigB)
            
            // Create KBigIntegers
            // Since fromString is basic, let's use string parsing (verified via toString)
            val kA = KBigInteger.fromString(bigA.toString())
            val kB = KBigInteger.fromString(safeBigB.toString())
            
            val result = kA.divide(kB)
            
            assertEquals("Division failed for $bigA / $safeBigB", expectedQuotient.toString(), result.toString())
        }
    }
    
    @Test
    fun testLargeMultiplication() {
        // Test multiplication that overflows Long to verify IntArray logic
        val a = Long.MAX_VALUE / 2
        val b = 10L
        val bigA = BigInteger.valueOf(a)
        val bigB = BigInteger.valueOf(b)
        
        val expected = bigA.multiply(bigB)
        
        val kA = KBigInteger.fromLong(a)
        val kB = KBigInteger.fromLong(b)
        
        val result = kA.multiply(kB)
        
        // Cannot use toLong() for verification if it overflows
        // We need a way to verify larger numbers.
        // For Phase 2, let's verify mostly within Long range or implement toJavaBigInteger helper in test
        
        // Helper verification
        assertEquals("Large Mul failed", expected.toString(), result.toJavaBigInteger().toString())
    }

    private fun generateRandomPair(): Pair<BigInteger, BigInteger> {
        val a = Random.nextLong()
        val b = Random.nextLong()
        return BigInteger.valueOf(a) to BigInteger.valueOf(b)
    }
    
    // Helper to convert our KBigInteger to Java BigInteger for verification
    // private fun KBigInteger.toJavaBigInteger() -> Moved to TestUtils.kt

    @Test
    fun testFactorial() {
        // Calculate 100! which is a very large number
        val n = 100
        var expected = BigInteger.ONE
        var actual = KBigInteger.ONE
        
        for (i in 1..n) {
            val bigI = BigInteger.valueOf(i.toLong())
            val kI = KBigInteger.fromLong(i.toLong())
            
            expected = expected.multiply(bigI)
            actual = actual.multiply(kI)
        }
        
        assertEquals("Factorial 100! failed", expected.toString(), actual.toString())
    }

    @Test
    fun testFibonacci() {
        // Calculate F(1000)
        // F(0)=0, F(1)=1, F(n)=F(n-1)+F(n-2)
        val n = 1000
        
        var a = BigInteger.ZERO
        var b = BigInteger.ONE
        var kA = KBigInteger.ZERO
        var kB = KBigInteger.ONE
        
        for (i in 2..n) {
            val temp = a.add(b)
            a = b
            b = temp
            
            val kTemp = kA.add(kB)
            kA = kB
            kB = kTemp
        }
        
        assertEquals("Fibonacci F($n) failed", b.toString(), kB.toString())
    }

    @Test
    fun testHugeRandomArithmetic() {
        // Test with 4096-bit numbers (approx 128 integers in magnitude array)
        repeat(200) {
            val (a, b) = generateHugeRandomPair(4096)
            
            // Addition
            assertEquals("Huge Add failed", a.add(b).toString(), 
                createKBigInteger(a).add(createKBigInteger(b)).toString())
                
            // Subtraction
            assertEquals("Huge Subtract failed", a.subtract(b).toString(), 
                createKBigInteger(a).subtract(createKBigInteger(b)).toString())
                
            // Multiplication
            assertEquals("Huge Multiply failed", a.multiply(b).toString(), 
                createKBigInteger(a).multiply(createKBigInteger(b)).toString())
                
            // Division (ensure b is not zero)
            if (b.signum() != 0) {
                // Determine sensible expected behavior vs actual
                val kA = createKBigInteger(a)
                val kB = createKBigInteger(b)
                
                assertEquals("Huge Divide failed for $a / $b", a.divide(b).toString(), 
                    kA.divide(kB).toString())
                    
                // Check remainder consistency if implemented?
                // The current implementation of KBigInteger has 'mod' and 'divide'.
                // Let's stick to 'divide' first which is safer.
                // assertEquals("Huge Mod failed", a.remainder(b).toString(), kA.mod(kB).toString())
            }
        }
    }

    @Test
    fun testDivisionSignMatrix() {
        val valA = 100L
        val valB = 30L 
        
        val signs = listOf(1L, -1L)
        
        for (sA in signs) {
            for (sB in signs) {
                val a = BigInteger.valueOf(valA * sA)
                val b = BigInteger.valueOf(valB * sB)
                val kA = KBigInteger.fromLong(valA * sA)
                val kB = KBigInteger.fromLong(valB * sB)
                
                assertEquals("Divide $a / $b failed", a.divide(b).toString(), kA.divide(kB).toString())
            }
        }
    }

    @Test
    fun testParsingEdgeCases() {
        // Valid inputs
        assertEquals("Parse normal", "12345", KBigInteger.fromString("12345").toString())
        assertEquals("Parse +sign", "123", KBigInteger.fromString("+123").toString())
        assertEquals("Parse -sign", "-123", KBigInteger.fromString("-123").toString())
        
        // Leading zeros
        assertEquals("Leading zeros", "7", KBigInteger.fromString("007").toString())
        assertEquals("Negative leading zeros", "-7", KBigInteger.fromString("-007").toString())
        
        // Zero
        assertEquals("Zero", "0", KBigInteger.fromString("0").toString())
        assertEquals("Zero +", "0", KBigInteger.fromString("+0").toString())
        assertEquals("Zero -", "0", KBigInteger.fromString("-0").toString())
        assertEquals("Multiple zeros", "0", KBigInteger.fromString("0000").toString())
        
        // Invalid
        try {
            KBigInteger.fromString("")
            org.junit.Assert.fail("Should throw for empty")
        } catch (e: NumberFormatException) { }
        
        try {
            KBigInteger.fromString("-")
            org.junit.Assert.fail("Should throw for only sign")
        } catch (e: NumberFormatException) { }
    }

    private fun generateHugeRandomPair(bits: Int): Pair<BigInteger, BigInteger> {
        return generateHugeRandomBigInteger(bits) to generateHugeRandomBigInteger(bits)
    }
    
    // Helper needed for new tests if not already present
    private fun createKBigInteger(bi: BigInteger): KBigInteger {
        return KBigInteger.fromString(bi.toString())
    }
}
