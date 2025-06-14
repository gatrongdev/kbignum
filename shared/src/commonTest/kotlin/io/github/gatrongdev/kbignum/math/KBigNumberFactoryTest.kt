package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

class KBigNumberFactoryTest {

    // KBIGDECIMAL FACTORY TESTS
    @Test
    fun testKBigDecimalFactoryConstants() {
        assertEquals("0", KBigDecimalFactory.ZERO.toString())
        assertEquals("1", KBigDecimalFactory.ONE.toString())
        assertEquals("10", KBigDecimalFactory.TEN.toString())
        
        assertTrue(KBigDecimalFactory.ZERO.isZero())
        assertTrue(KBigDecimalFactory.ONE.isPositive())
        assertTrue(KBigDecimalFactory.TEN.isPositive())
        
        assertFalse(KBigDecimalFactory.ONE.isZero())
        assertFalse(KBigDecimalFactory.TEN.isZero())
        assertFalse(KBigDecimalFactory.ZERO.isPositive())
        assertFalse(KBigDecimalFactory.ZERO.isNegative())
    }

    @Test
    fun testKBigDecimalFromStringInvalidInputs() {
        assertFailsWith<NumberFormatException> {
            KBigDecimalFactory.fromString("abc")
        }
        
        assertFailsWith<NumberFormatException> {
            KBigDecimalFactory.fromString("123.45.67")
        }
        
        assertFailsWith<NumberFormatException> {
            KBigDecimalFactory.fromString("")
        }
        
        assertFailsWith<NumberFormatException> {
            KBigDecimalFactory.fromString("12.34.56")
        }
        
        assertFailsWith<NumberFormatException> {
            KBigDecimalFactory.fromString("12a.34")
        }
    }

    @Test
    fun testKBigDecimalFromInt() {
        assertEquals("0", KBigDecimalFactory.fromInt(0).toString())
        assertEquals("123", KBigDecimalFactory.fromInt(123).toString())
        assertEquals("-456", KBigDecimalFactory.fromInt(-456).toString())
        assertEquals("2147483647", KBigDecimalFactory.fromInt(Int.MAX_VALUE).toString())
        assertEquals("-2147483648", KBigDecimalFactory.fromInt(Int.MIN_VALUE).toString())
    }

    @Test
    fun testKBigDecimalFromLong() {
        assertEquals("0", KBigDecimalFactory.fromLong(0L).toString())
        assertEquals("123", KBigDecimalFactory.fromLong(123L).toString())
        assertEquals("-456", KBigDecimalFactory.fromLong(-456L).toString())
        assertEquals("9223372036854775807", KBigDecimalFactory.fromLong(Long.MAX_VALUE).toString())
        assertEquals("-9223372036854775808", KBigDecimalFactory.fromLong(Long.MIN_VALUE).toString())
    }

    @Test
    fun testKBigDecimalFactoryConsistency() {
        val stringZero = KBigDecimalFactory.fromString("0")
        val intZero = KBigDecimalFactory.fromInt(0)
        val longZero = KBigDecimalFactory.fromLong(0L)
        
        assertEquals(KBigDecimalFactory.ZERO.toString(), stringZero.toString())
        assertEquals(KBigDecimalFactory.ZERO.toString(), intZero.toString())
        assertEquals(KBigDecimalFactory.ZERO.toString(), longZero.toString())
        
        val stringOne = KBigDecimalFactory.fromString("1")
        val intOne = KBigDecimalFactory.fromInt(1)
        val longOne = KBigDecimalFactory.fromLong(1L)
        
        assertEquals(KBigDecimalFactory.ONE.toString(), stringOne.toString())
        assertEquals(KBigDecimalFactory.ONE.toString(), intOne.toString())
        assertEquals(KBigDecimalFactory.ONE.toString(), longOne.toString())
    }

    // KBIGINTEGER FACTORY TESTS
    @Test
    fun testKBigIntegerFactoryConstants() {
        assertEquals("0", KBigIntegerFactory.ZERO.toString())
        assertEquals("1", KBigIntegerFactory.ONE.toString())
        assertEquals("10", KBigIntegerFactory.TEN.toString())
        
        assertTrue(KBigIntegerFactory.ZERO.isZero())
        assertTrue(KBigIntegerFactory.ONE.isPositive())
        assertTrue(KBigIntegerFactory.TEN.isPositive())
        
        assertFalse(KBigIntegerFactory.ONE.isZero())
        assertFalse(KBigIntegerFactory.TEN.isZero())
        assertFalse(KBigIntegerFactory.ZERO.isPositive())
        assertFalse(KBigIntegerFactory.ZERO.isNegative())
    }

    @Test
    fun testKBigIntegerFromString() {
        assertEquals("123", KBigIntegerFactory.fromString("123").toString())
        assertEquals("-456", KBigIntegerFactory.fromString("-456").toString())
        assertEquals("0", KBigIntegerFactory.fromString("0").toString())
        assertEquals("999999999999999999999999999999", KBigIntegerFactory.fromString("999999999999999999999999999999").toString())
        assertEquals("-999999999999999999999999999999", KBigIntegerFactory.fromString("-999999999999999999999999999999").toString())
    }

    @Test
    fun testKBigIntegerFromStringEdgeCases() {
        // Leading zeros should be handled properly
        assertEquals("123", KBigIntegerFactory.fromString("00123").toString())
        assertEquals("0", KBigIntegerFactory.fromString("000").toString())
        
        // Very large numbers
        val largeNumber = "123456789012345678901234567890123456789012345678901234567890"
        assertEquals(largeNumber, KBigIntegerFactory.fromString(largeNumber).toString())
        
        val negativeLargeNumber = "-123456789012345678901234567890123456789012345678901234567890"
        assertEquals(negativeLargeNumber, KBigIntegerFactory.fromString(negativeLargeNumber).toString())
    }

    @Test
    fun testKBigIntegerFromStringInvalidInputs() {
        assertFailsWith<NumberFormatException> {
            KBigIntegerFactory.fromString("abc")
        }
        
        assertFailsWith<NumberFormatException> {
            KBigIntegerFactory.fromString("123.456")
        }
        
        assertFailsWith<NumberFormatException> {
            KBigIntegerFactory.fromString("")
        }
        
        assertFailsWith<NumberFormatException> {
            KBigIntegerFactory.fromString("12a34")
        }
        
        assertFailsWith<NumberFormatException> {
            KBigIntegerFactory.fromString("12.0")
        }
    }

    @Test
    fun testKBigIntegerFromInt() {
        assertEquals("0", KBigIntegerFactory.fromInt(0).toString())
        assertEquals("123", KBigIntegerFactory.fromInt(123).toString())
        assertEquals("-456", KBigIntegerFactory.fromInt(-456).toString())
        assertEquals("2147483647", KBigIntegerFactory.fromInt(Int.MAX_VALUE).toString())
        assertEquals("-2147483648", KBigIntegerFactory.fromInt(Int.MIN_VALUE).toString())
    }

    @Test
    fun testKBigIntegerFromLong() {
        assertEquals("0", KBigIntegerFactory.fromLong(0L).toString())
        assertEquals("123", KBigIntegerFactory.fromLong(123L).toString())
        assertEquals("-456", KBigIntegerFactory.fromLong(-456L).toString())
        assertEquals("9223372036854775807", KBigIntegerFactory.fromLong(Long.MAX_VALUE).toString())
        assertEquals("-9223372036854775808", KBigIntegerFactory.fromLong(Long.MIN_VALUE).toString())
    }

    @Test
    fun testKBigIntegerFactoryConsistency() {
        val stringZero = KBigIntegerFactory.fromString("0")
        val intZero = KBigIntegerFactory.fromInt(0)
        val longZero = KBigIntegerFactory.fromLong(0L)
        
        assertEquals(KBigIntegerFactory.ZERO.toString(), stringZero.toString())
        assertEquals(KBigIntegerFactory.ZERO.toString(), intZero.toString())
        assertEquals(KBigIntegerFactory.ZERO.toString(), longZero.toString())
        
        val stringOne = KBigIntegerFactory.fromString("1")
        val intOne = KBigIntegerFactory.fromInt(1)
        val longOne = KBigIntegerFactory.fromLong(1L)
        
        assertEquals(KBigIntegerFactory.ONE.toString(), stringOne.toString())
        assertEquals(KBigIntegerFactory.ONE.toString(), intOne.toString())
        assertEquals(KBigIntegerFactory.ONE.toString(), longOne.toString())
        
        val stringTen = KBigIntegerFactory.fromString("10")
        val intTen = KBigIntegerFactory.fromInt(10)
        val longTen = KBigIntegerFactory.fromLong(10L)
        
        assertEquals(KBigIntegerFactory.TEN.toString(), stringTen.toString())
        assertEquals(KBigIntegerFactory.TEN.toString(), intTen.toString())
        assertEquals(KBigIntegerFactory.TEN.toString(), longTen.toString())
    }

    // ROUNDING MODE TESTS
    @Test
    fun testRoundingModeConstants() {
        assertEquals(0, RoundingMode.UP)
        assertEquals(1, RoundingMode.DOWN)
        assertEquals(2, RoundingMode.CEILING)
        assertEquals(3, RoundingMode.FLOOR)
        assertEquals(4, RoundingMode.HALF_UP)
        assertEquals(5, RoundingMode.HALF_DOWN)
        assertEquals(6, RoundingMode.HALF_EVEN)
        assertEquals(7, RoundingMode.UNNECESSARY)
    }

    @Test
    fun testRoundingModeValues() {
        val roundingModes = listOf(
            RoundingMode.UP,
            RoundingMode.DOWN,
            RoundingMode.CEILING,
            RoundingMode.FLOOR,
            RoundingMode.HALF_UP,
            RoundingMode.HALF_DOWN,
            RoundingMode.HALF_EVEN,
            RoundingMode.UNNECESSARY
        )
        
        val expectedValues = listOf(0, 1, 2, 3, 4, 5, 6, 7)
        
        for (i in roundingModes.indices) {
            assertEquals(expectedValues[i], roundingModes[i])
        }
    }

    // CROSS-FACTORY INTEGRATION TESTS
    @Test
    fun testFactoryInteroperability() {
        val decimalFromInt = KBigDecimalFactory.fromInt(123)
        val integerFromString = KBigIntegerFactory.fromString("123")
        
        assertEquals("123", decimalFromInt.toString())
        assertEquals("123", integerFromString.toString())
        
        val convertedToPrecise = integerFromString.toPreciseNumber()
        assertEquals(decimalFromInt.toString(), convertedToPrecise.toString())
        
        val convertedToInteger = decimalFromInt.toBigInteger()
        assertEquals(integerFromString.toString(), convertedToInteger.toString())
    }

    @Test
    fun testFactoryWithExtensions() {
        val fromFactory = KBigDecimalFactory.fromString("123.456")
        val fromExtension = "123.456".toKBigDecimal()
        
        assertEquals(fromFactory.toString(), fromExtension.toString())
        
        val intFromFactory = KBigIntegerFactory.fromString("789")
        val intFromExtension = "789".toKBigInteger()
        
        assertEquals(intFromFactory.toString(), intFromExtension.toString())
    }

    // BOUNDARY VALUE TESTS
    @Test
    fun testFactoryBoundaryValues() {
        // Test with maximum and minimum primitive values
        val maxIntDecimal = KBigDecimalFactory.fromInt(Int.MAX_VALUE)
        val minIntDecimal = KBigDecimalFactory.fromInt(Int.MIN_VALUE)
        val maxLongDecimal = KBigDecimalFactory.fromLong(Long.MAX_VALUE)
        val minLongDecimal = KBigDecimalFactory.fromLong(Long.MIN_VALUE)
        
        assertEquals("2147483647", maxIntDecimal.toString())
        assertEquals("-2147483648", minIntDecimal.toString())
        assertEquals("9223372036854775807", maxLongDecimal.toString())
        assertEquals("-9223372036854775808", minLongDecimal.toString())
        
        val maxIntInteger = KBigIntegerFactory.fromInt(Int.MAX_VALUE)
        val minIntInteger = KBigIntegerFactory.fromInt(Int.MIN_VALUE)
        val maxLongInteger = KBigIntegerFactory.fromLong(Long.MAX_VALUE)
        val minLongInteger = KBigIntegerFactory.fromLong(Long.MIN_VALUE)
        
        assertEquals("2147483647", maxIntInteger.toString())
        assertEquals("-2147483648", minIntInteger.toString())
        assertEquals("9223372036854775807", maxLongInteger.toString())
        assertEquals("-9223372036854775808", minLongInteger.toString())
    }

    // PERFORMANCE AND STRESS TESTS
    @Test
    fun testFactoryPerformanceWithManyCreations() {
        // Test that factory methods can handle multiple creations without issues
        for (i in 0..100) {
            val decimal = KBigDecimalFactory.fromInt(i)
            val integer = KBigIntegerFactory.fromInt(i)
            
            assertEquals(i.toString(), decimal.toString())
            assertEquals(i.toString(), integer.toString())
        }
    }

    @Test
    fun testFactoryMemoryConsistency() {
        // Test that constants are truly constant (same instance)
        val zero1 = KBigDecimalFactory.ZERO
        val zero2 = KBigDecimalFactory.ZERO
        assertTrue(zero1.toString() == zero2.toString())
        
        val one1 = KBigIntegerFactory.ONE
        val one2 = KBigIntegerFactory.ONE
        assertTrue(one1.toString() == one2.toString())
    }

    // ERROR HANDLING COMPREHENSIVE TESTS
    @Test
    fun testFactoryErrorHandlingEdgeCases() {
        // Test various malformed string inputs
        val malformedInputs = listOf(
            "123.", ".123", "123..", "..123", "123.456.789",
            " 123", "123 ", " 123 ", "\t123", "123\n",
            "1.23e", "1.23e+", "1.23e-", "e123"
        )
        
        for (input in malformedInputs) {
            try {
                KBigDecimalFactory.fromString(input)
                // If we get here, check if it's a commonly acceptable format
                if (input in listOf("123..", "..123", "123.456.789", " 123", "123 ", "\t123", "123\n", "1.23e", "1.23e+", "1.23e-", "e123")) {
                    throw AssertionError("Expected NumberFormatException for: '$input'")
                }
            } catch (e: NumberFormatException) {
                // Expected for malformed strings
            }
        }
        
        val integerMalformedInputs = listOf(
            "123.0", "123.456", ".123", "123.", "123.."
        )
        
        for (input in integerMalformedInputs) {
            assertFailsWith<NumberFormatException> {
                KBigIntegerFactory.fromString(input)
            }
        }
    }

    @Test
    fun testFactoryWithSpecialCharacters() {
        assertFailsWith<NumberFormatException> {
            KBigDecimalFactory.fromString("123,456")
        }
        
        assertFailsWith<NumberFormatException> {
            KBigDecimalFactory.fromString("$123.45")
        }
        
        assertFailsWith<NumberFormatException> {
            KBigIntegerFactory.fromString("1_000_000")
        }
        
        assertFailsWith<NumberFormatException> {
            KBigIntegerFactory.fromString("1,000")
        }
    }

    // CONSISTENCY WITH EXTENSION FUNCTIONS TESTS
    @Test
    fun testFactoryExtensionConsistency() {
        val testValues = listOf("0", "1", "123", "-456", "789.123", "-987.654")
        
        for (value in testValues) {
            if (value.contains('.')) {
                val factoryResult = KBigDecimalFactory.fromString(value)
                val extensionResult = value.toKBigDecimal()
                assertEquals(factoryResult.toString(), extensionResult.toString())
            } else {
                val factoryDecimalResult = KBigDecimalFactory.fromString(value)
                val extensionDecimalResult = value.toKBigDecimal()
                assertEquals(factoryDecimalResult.toString(), extensionDecimalResult.toString())
                
                val factoryIntegerResult = KBigIntegerFactory.fromString(value)
                val extensionIntegerResult = value.toKBigInteger()
                assertEquals(factoryIntegerResult.toString(), extensionIntegerResult.toString())
            }
        }
    }

    @Test
    fun testFactoryMethodChaining() {
        val result = KBigDecimalFactory.fromString("10")
            .add(KBigDecimalFactory.fromInt(5))
            .multiply(KBigDecimalFactory.fromLong(2L))
        
        assertEquals("30", result.toString())
        
        val intResult = KBigIntegerFactory.fromString("20")
            .add(KBigIntegerFactory.fromInt(10))
            .multiply(KBigIntegerFactory.fromLong(3L))
        
        assertEquals("90", intResult.toString())
    }
}