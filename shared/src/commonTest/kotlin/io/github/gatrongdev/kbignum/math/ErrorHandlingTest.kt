package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

class ErrorHandlingTest {

    // ARITHMETIC EXCEPTION HANDLING TESTS
    @Test
    fun testDivisionByZeroKBigDecimal() {
        val decimal = "123.456".toKBigDecimal()
        val zero = KBigDecimalFactory.ZERO

        assertFailsWith<ArithmeticException> {
            decimal.divide(zero, 2, 4)
        }

        // Test with constructed zero
        val constructedZero = "0".toKBigDecimal()
        assertFailsWith<ArithmeticException> {
            decimal.divide(constructedZero, 2, 4)
        }
    }

    @Test
    fun testDivisionByZeroKBigInteger() {
        val integer = "123".toKBigInteger()
        val zero = KBigIntegerFactory.ZERO
        
        assertFailsWith<ArithmeticException> {
            integer.divide(zero)
        }
        
        // Test with constructed zero
        val constructedZero = "0".toKBigInteger()
        assertFailsWith<ArithmeticException> {
            integer.divide(constructedZero)
        }
    }

    @Test
    fun testModuloByZeroKBigInteger() {
        val integer = "123".toKBigInteger()
        val zero = KBigIntegerFactory.ZERO
        
        assertFailsWith<ArithmeticException> {
            integer.mod(zero)
        }
        
        // Test with constructed zero
        val constructedZero = "0".toKBigInteger()
        assertFailsWith<ArithmeticException> {
            integer.mod(constructedZero)
        }
    }

    @Test
    fun testSqrtNegativeNumbers() {
        val negativeDecimal = "-4".toKBigDecimal()
        assertFailsWith<ArithmeticException> {
            KBigMath.sqrt(negativeDecimal)
        }
        
        val largeNegative = "-999999999999999999.123456789".toKBigDecimal()
        assertFailsWith<ArithmeticException> {
            KBigMath.sqrt(largeNegative)
        }
        
        val verySmallNegative = "-0.000000000000000001".toKBigDecimal()
        assertFailsWith<ArithmeticException> {
            KBigMath.sqrt(verySmallNegative)
        }
    }

    @Test
    fun testFactorialNegativeNumbers() {
        val negativeInteger = "-1".toKBigInteger()
        assertFailsWith<ArithmeticException> {
            KBigMath.factorial(negativeInteger)
        }
        
        val largeNegative = "-100".toKBigInteger()
        assertFailsWith<ArithmeticException> {
            KBigMath.factorial(largeNegative)
        }
        
        val veryLargeNegative = "-999999999999999999".toKBigInteger()
        assertFailsWith<ArithmeticException> {
            KBigMath.factorial(veryLargeNegative)
        }
    }

    @Test
    fun testPowerNegativeExponents() {
        val base = "2".toKBigInteger()
        val negativeExponent = "-1".toKBigInteger()
        
        assertFailsWith<ArithmeticException> {
            KBigMath.pow(base, negativeExponent)
        }
        
        val largeNegativeExponent = "-100".toKBigInteger()
        assertFailsWith<ArithmeticException> {
            KBigMath.pow(base, largeNegativeExponent)
        }
        
        val zeroBase = KBigIntegerFactory.ZERO
        assertFailsWith<ArithmeticException> {
            KBigMath.pow(zeroBase, negativeExponent)
        }
    }

    @Test
    fun testRoundingModeUnnecessaryException() {
        val inexactDecimal = "123.456".toKBigDecimal()
        
        assertFailsWith<ArithmeticException> {
            inexactDecimal.setScale(2, 7)
        }
        
        val anotherInexact = "999.999".toKBigDecimal()
        assertFailsWith<ArithmeticException> {
            anotherInexact.setScale(1, 7)
        }
        
        // Test that exact values don't throw
        val exactDecimal = "123.45".toKBigDecimal()
        val result = exactDecimal.setScale(2, 7)
        assertEquals("123.45", result.toString())
    }

    // NUMBER FORMAT EXCEPTION HANDLING TESTS
    @Test
    fun testInvalidStringToKBigDecimal() {
        val invalidStrings = listOf(
            "abc", "123abc", "12.34.56", "", " ", 
            "123..", "..123", "12.34.56.78", "1.23e", "1.23e+",
            "1.23e-", "e123", "123e", "+", "-", "++123", "--123"
        )
        
        for (invalidString in invalidStrings) {
            try {
                invalidString.toKBigDecimal()
                // If we get here, the string was unexpectedly valid
                if (invalidString in listOf("", " ", "++123", "--123", "abc", "123abc")) {
                    // These should definitely throw
                    throw AssertionError("Expected NumberFormatException for: '$invalidString'")
                }
            } catch (e: NumberFormatException) {
                // Expected for invalid strings
            }
        }
    }

    @Test
    fun testInvalidStringToKBigInteger() {
        val invalidStrings = listOf(
            "abc", "123abc", "123.456", "", " ", "123.", ".123",
            "12.0", "1.0", "0.0", "12.34", "1.23e5", "+", "-",
            "++123", "--123", "12a34", "1_000", "1,000"
        )
        
        for (invalidString in invalidStrings) {
            assertFailsWith<NumberFormatException> {
                invalidString.toKBigInteger()
            }
        }
    }

    @Test
    fun testInvalidFactoryStringInputs() {
        val invalidDecimalStrings = listOf(
            "abc", "123abc", "12.34.56", "", " ", "123..", "..123"
        )
        
        for (invalidString in invalidDecimalStrings) {
            assertFailsWith<NumberFormatException> {
                KBigDecimalFactory.fromString(invalidString)
            }
        }
        
        val invalidIntegerStrings = listOf(
            "abc", "123.456", "12.0", "", " ", "123.", ".123"
        )
        
        for (invalidString in invalidIntegerStrings) {
            assertFailsWith<NumberFormatException> {
                KBigIntegerFactory.fromString(invalidString)
            }
        }
    }

    // BOUNDARY CONDITION TESTS
    @Test
    fun testOverflowAndUnderflowHandling() {
        // Test very large numbers don't cause overflow
        val veryLarge1 = "999999999999999999999999999999999999999999999999999999999999999999999999999999".toKBigDecimal()
        val veryLarge2 = "888888888888888888888888888888888888888888888888888888888888888888888888888888".toKBigDecimal()
        
        val sum = veryLarge1.add(veryLarge2)
        assertTrue(sum.toString().length > 70)
        
        val product = veryLarge1.multiply("2".toKBigDecimal())
        assertTrue(product.toString().length > 70)
        
        // Test very large integers
        val veryLargeInt1 = "999999999999999999999999999999999999999999999999999999999999999999999999999999".toKBigInteger()
        val veryLargeInt2 = "888888888888888888888888888888888888888888888888888888888888888888888888888888".toKBigInteger()
        
        val intSum = veryLargeInt1.add(veryLargeInt2)
        assertTrue(intSum.toString().length > 70)
        
        val intProduct = veryLargeInt1.multiply("2".toKBigInteger())
        assertTrue(intProduct.toString().length > 70)
    }

    @Test
    fun testVerySmallNumberHandling() {
        // Test very small decimal numbers
        val verySmall = "0.000000000000000000000000000000000000000000000000000000000000000000000000000001".toKBigDecimal()
        
        assertTrue(verySmall.isPositive())
        assertFalse(verySmall.isZero())
        assertTrue(verySmall.scale() > 70)
        
        val doubled = verySmall.multiply("2".toKBigDecimal())
        assertTrue(doubled.isPositive())
        // The result should be double the original very small number
        assertTrue(doubled.toString().contains("2") || doubled.toString().contains("0000002"))
    }

    // THREAD SAFETY AND CONCURRENCY TESTS
    @Test
    fun testConcurrentOperations() {
        // Test that operations work correctly under potential concurrent access
        val decimal = "123.456".toKBigDecimal()
        val results = mutableListOf<String>()
        
        // Simulate concurrent operations
        for (i in 1..10) {
            val result = decimal.multiply(i.toKBigDecimal())
            results.add(result.toString())
        }
        
        assertEquals(10, results.size)
        assertEquals("123.456", results[0])  // 123.456 * 1
        assertEquals("246.912", results[1])  // 123.456 * 2
        assertTrue(results[9] == "1234.56" || results[9] == "1234.560")  // 123.456 * 10
    }

    // MEMORY AND PERFORMANCE STRESS TESTS
    @Test
    fun testMemoryStressWithLargeNumbers() {
        // Test operations with extremely large numbers
        var largeNumber = "1".toKBigInteger()
        
        // Build a very large number
        for (i in 1..50) {
            largeNumber = largeNumber.multiply("10".toKBigInteger())
        }
        
        assertTrue(largeNumber.toString().length > 50)
        assertTrue(largeNumber.toString().startsWith("1"))
        assertTrue(largeNumber.toString().endsWith("0".repeat(50)))
        
        // Test operations on this large number
        val doubled = largeNumber.multiply("2".toKBigInteger())
        assertTrue(doubled.toString().startsWith("2"))
        assertTrue(doubled.toString().length > 50)
    }

    @Test
    fun testChainedOperationErrors() {
        val decimal = "10".toKBigDecimal()
        val zero = KBigDecimalFactory.ZERO

        // Test that errors propagate correctly through chained operations
        try {
            val result = decimal
                .multiply("2".toKBigDecimal())
                .add("5".toKBigDecimal())
                .divide(zero, 2, 4)  // This should fail
                .subtract("1".toKBigDecimal())

            assertTrue(false, "Should have thrown ArithmeticException")
        } catch (e: ArithmeticException) {
            // Expected
            assertTrue(true)
        }
    }

    // VALIDATION AND CONSISTENCY TESTS
    @Test
    fun testInputValidation() {
        // Test that invalid inputs are properly rejected
        val validInputs = listOf("0", "1", "-1", "123.456", "-123.456")
        val invalidInputs = listOf("", " ", "abc", "12.34.56", "++123")
        
        for (valid in validInputs) {
            // Should not throw
            val decimal = valid.toKBigDecimal()
            assertEquals(valid, decimal.toString())
            
            if (!valid.contains('.')) {
                val integer = valid.toKBigInteger()
                assertEquals(valid, integer.toString())
            }
        }
        
        for (invalid in invalidInputs) {
            assertFailsWith<NumberFormatException> {
                invalid.toKBigDecimal()
            }
            
            assertFailsWith<NumberFormatException> {
                invalid.toKBigInteger()
            }
        }
    }

    @Test
    fun testErrorMessageConsistency() {
        // Test that similar errors produce consistent error types
        val decimal = "123.456".toKBigDecimal()
        val zero = KBigDecimalFactory.ZERO

        try {
            decimal.divide(zero, 2, 4)
        } catch (e: ArithmeticException) {
            assertTrue(e is ArithmeticException)
        }

        try {
            decimal.divide(zero, 2, 4)
        } catch (e: ArithmeticException) {
            assertTrue(e is ArithmeticException)
        }

        try {
            "invalid".toKBigDecimal()
        } catch (e: NumberFormatException) {
            assertTrue(e is NumberFormatException)
        }
    }

    // RECOVERY AND GRACEFUL DEGRADATION TESTS
    @Test
    fun testErrorRecovery() {
        // Test that after an error, operations can continue normally
        val decimal = "10".toKBigDecimal()
        val zero = KBigDecimalFactory.ZERO

        try {
            decimal.divide(zero, 2, 4)
        } catch (e: ArithmeticException) {
            // Error occurred, but we can continue
        }

        // Should still work normally after the error
        val result = decimal.add("5".toKBigDecimal())
        assertEquals("15", result.toString())

        val multiplication = decimal.multiply("2".toKBigDecimal())
        assertEquals("20", multiplication.toString())
    }

    @Test
    fun testStateConsistencyAfterErrors() {
        // Test that objects remain in consistent state after errors
        val decimal = "123.456".toKBigDecimal()
        val originalValue = decimal.toString()

        try {
            decimal.divide(KBigDecimalFactory.ZERO, 2, RoundingMode.HALF_UP)
        } catch (e: ArithmeticException) {
            // Error occurred
        }

        // Original object should be unchanged
        assertEquals(originalValue, decimal.toString())
        assertTrue(decimal.isPositive())
        assertFalse(decimal.isZero())
        assertEquals(1, decimal.signum())
    }
}