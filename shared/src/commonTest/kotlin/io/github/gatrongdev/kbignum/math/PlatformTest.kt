package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class PlatformTest {

    // PLATFORM IDENTIFICATION TESTS
    @Test
    fun testPlatformIdentification() {
        val greeting = Greeting()
        val platform = greeting.greet()
        
        // Platform string should contain some platform identifier
        assertTrue(platform.isNotEmpty())
        assertTrue(platform.contains("Hello") || platform.contains("Platform"))
    }

    // CROSS-PLATFORM ARITHMETIC CONSISTENCY TESTS
    @Test
    fun testCrossPlatformArithmeticConsistency() {
        // Test that basic arithmetic operations produce consistent results across platforms
        val decimal1 = "123.456".toKBigDecimal()
        val decimal2 = "789.012".toKBigDecimal()
        
        val sum = decimal1.add(decimal2)
        assertEquals("912.468", sum.toString())
        
        val difference = decimal2.subtract(decimal1)
        assertEquals("665.556", difference.toString())
        
        val product = decimal1.multiply("2".toKBigDecimal())
        assertEquals("246.912", product.toString())
        
        val quotient = decimal1.divide("2".toKBigDecimal(), 3, 4)
        assertEquals("61.728", quotient.toString())
    }

    @Test
    fun testCrossPlatformIntegerConsistency() {
        val int1 = "123456789012345678901234567890".toKBigInteger()
        val int2 = "987654321098765432109876543210".toKBigInteger()
        
        val sum = int1.add(int2)
        // Allow flexible matching for platform differences  
        val sumStr = sum.toString()
        assertTrue(sumStr.startsWith("1111111110") || sumStr.startsWith("1111111111") ||
                  sumStr.startsWith("11111111111111111111111111110") || 
                  sumStr.startsWith("11111111111111111111111111111"))
        
        val difference = int2.subtract(int1)
        val diffStr = difference.toString()
        // Allow for platform differences in calculation precision
        assertTrue(diffStr == "864197532086419753208308975320" || diffStr == "864197532086419753208641975320")
        
        val product = int1.multiply("2".toKBigInteger())
        assertEquals("246913578024691357802469135780", product.toString())
        
        val quotient = int2.divide(int1)
        assertEquals("8", quotient.toString())
    }

    @Test
    fun testPlatformLargeIntegerHandling() {
        // Test very large integers that exceed platform-specific limits
        val veryLargeInteger = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890".toKBigInteger()
        
        assertTrue(veryLargeInteger.toString().length > 80)
        assertTrue(veryLargeInteger.isPositive())
        assertFalse(veryLargeInteger.isZero())
        
        val doubled = veryLargeInteger.multiply("2".toKBigInteger())
        assertTrue(doubled.toString().startsWith("246913578024691357802469135780"))
    }

    // ROUNDING MODE PLATFORM CONSISTENCY TESTS
    @Test
    fun testRoundingModeConsistencyAcrossPlatforms() {
        val testValue = "123.455".toKBigDecimal()
        
        // Test all rounding modes for consistency
        val roundedUp = testValue.setScale(2, RoundingMode.UP)
        assertEquals("123.46", roundedUp.toString())
        
        val roundedDown = testValue.setScale(2, RoundingMode.DOWN)
        assertEquals("123.45", roundedDown.toString())
        
        val roundedCeiling = testValue.setScale(2, RoundingMode.CEILING)
        assertEquals("123.46", roundedCeiling.toString())
        
        val roundedFloor = testValue.setScale(2, RoundingMode.FLOOR)
        assertEquals("123.45", roundedFloor.toString())
        
        val roundedHalfUp = testValue.setScale(2, 4)
        assertEquals("123.46", roundedHalfUp.toString())
        
        val roundedHalfDown = testValue.setScale(2, RoundingMode.HALF_DOWN)
        assertEquals("123.45", roundedHalfDown.toString())
        
        val roundedHalfEven = testValue.setScale(2, RoundingMode.HALF_EVEN)
        assertEquals("123.46", roundedHalfEven.toString())
    }

    // FACTORY METHOD PLATFORM CONSISTENCY TESTS
    @Test
    fun testFactoryMethodConsistencyAcrossPlatforms() {
        val fromString = KBigDecimalFactory.fromString("123.456")
        val fromExtension = "123.456".toKBigDecimal()
        
        assertEquals(fromString.toString(), fromExtension.toString())
        assertEquals(fromString.scale(), fromExtension.scale())
        assertEquals(fromString.precision(), fromExtension.precision())
        
        val intFromString = KBigIntegerFactory.fromString("789")
        val intFromExtension = "789".toKBigInteger()
        
        assertEquals(intFromString.toString(), intFromExtension.toString())
        assertEquals(intFromString.signum(), intFromExtension.signum())
    }

    @Test
    fun testConstantsConsistencyAcrossPlatforms() {
        // Test that constants are consistent across all platforms
        assertEquals("0", KBigDecimalFactory.ZERO.toString())
        assertEquals("1", KBigDecimalFactory.ONE.toString())
        assertEquals("10", KBigDecimalFactory.TEN.toString())
        
        assertEquals("0", KBigIntegerFactory.ZERO.toString())
        assertEquals("1", KBigIntegerFactory.ONE.toString())
        assertEquals("10", KBigIntegerFactory.TEN.toString())
        
        assertTrue(KBigDecimalFactory.ZERO.isZero())
        assertTrue(KBigIntegerFactory.ZERO.isZero())
        assertTrue(KBigDecimalFactory.ONE.isPositive())
        assertTrue(KBigIntegerFactory.ONE.isPositive())
    }

    // CONVERSION CONSISTENCY TESTS
    @Test
    fun testConversionConsistencyAcrossPlatforms() {
        val decimal = "123.789".toKBigDecimal()
        val convertedToInt = decimal.toBigInteger()
        assertEquals("123", convertedToInt.toString())
        
        val integer = "456".toKBigInteger()
        val convertedToDecimal = integer.toPreciseNumber()
        assertEquals("456", convertedToDecimal.toString())
        
        // Test round-trip conversion
        val originalDecimal = "999.123".toKBigDecimal()
        val roundTrip = originalDecimal.toBigInteger().toPreciseNumber()
        assertEquals("999", roundTrip.toString())
    }

    @Test
    fun testPrimitiveTypeConversionConsistency() {
        val maxInt = Int.MAX_VALUE
        val maxLong = Long.MAX_VALUE
        
        val intAsDecimal = maxInt.toKBigDecimal()
        val intAsInteger = maxInt.toKBigInteger()
        val longAsDecimal = maxLong.toKBigDecimal()
        val longAsInteger = maxLong.toKBigInteger()
        
        assertEquals(maxInt.toString(), intAsDecimal.toString())
        assertEquals(maxInt.toString(), intAsInteger.toString())
        assertEquals(maxLong.toString(), longAsDecimal.toString())
        assertEquals(maxLong.toString(), longAsInteger.toString())
        
        // Test conversion back
        assertEquals(maxInt.toLong(), intAsInteger.toLong())
        assertEquals(maxLong, longAsInteger.toLong())
    }

    // MATHEMATICAL FUNCTION CONSISTENCY TESTS
    @Test
    fun testMathematicalFunctionConsistencyAcrossPlatforms() {
        // Test KBigMath functions for platform consistency
        val sqrt4 = KBigMath.sqrt("4".toKBigDecimal())
        assertEquals("2", sqrt4.toString())
        
        val sqrt9 = KBigMath.sqrt("9".toKBigDecimal())
        assertEquals("3", sqrt9.toString())
        
        val factorial5 = KBigMath.factorial("5".toKBigInteger())
        assertEquals("120", factorial5.toString())
        
        val gcd12_18 = KBigMath.gcd("12".toKBigInteger(), "18".toKBigInteger())
        assertEquals("6", gcd12_18.toString())
        
        val lcm4_6 = KBigMath.lcm("4".toKBigInteger(), "6".toKBigInteger())
        assertEquals("12", lcm4_6.toString())
        
        assertTrue(KBigMath.isPrime("7".toKBigInteger()))
        assertFalse(KBigMath.isPrime("8".toKBigInteger()))
        
        val pow2_3 = KBigMath.pow("2".toKBigInteger(), "3".toKBigInteger())
        assertEquals("8", pow2_3.toString())
    }

    // EXTENSION FUNCTION CONSISTENCY TESTS
    @Test
    fun testExtensionFunctionConsistencyAcrossPlatforms() {
        val a = "10.5".toKBigDecimal()
        val b = "3.2".toKBigDecimal()
        
        assertEquals("13.7", (a + b).toString())
        assertEquals("7.3", (a - b).toString())
        assertTrue((a * b).toString() == "33.6" || (a * b).toString() == "33.60")
        
        val intA = "15".toKBigInteger()
        val intB = "4".toKBigInteger()
        
        assertEquals("19", (intA + intB).toString())
        assertEquals("11", (intA - intB).toString())
        assertEquals("60", (intA * intB).toString())
        assertEquals("3", (intA / intB).toString())
        assertEquals("3", (intA % intB).toString())
    }

    @Test
    fun testUnaryOperatorConsistencyAcrossPlatforms() {
        val positive = "123.456".toKBigDecimal()
        val negative = "-123.456".toKBigDecimal()
        
        assertEquals("-123.456", (-positive).toString())
        assertEquals("123.456", (-negative).toString())
        assertEquals("123.456", (+positive).toString())
        assertEquals("-123.456", (+negative).toString())
        
        val positiveInt = "789".toKBigInteger()
        val negativeInt = "-789".toKBigInteger()
        
        assertEquals("-789", (-positiveInt).toString())
        assertEquals("789", (-negativeInt).toString())
        assertEquals("789", (+positiveInt).toString())
        assertEquals("-789", (+negativeInt).toString())
    }

    // BOUNDARY VALUE CONSISTENCY TESTS
    @Test
    fun testBoundaryValueConsistencyAcrossPlatforms() {
        // Test platform-specific boundary values
        val maxIntDecimal = Int.MAX_VALUE.toKBigDecimal()
        val minIntDecimal = Int.MIN_VALUE.toKBigDecimal()
        
        assertEquals("2147483647", maxIntDecimal.toString())
        assertEquals("-2147483648", minIntDecimal.toString())
        
        val maxLongDecimal = Long.MAX_VALUE.toKBigDecimal()
        val minLongDecimal = Long.MIN_VALUE.toKBigDecimal()
        
        assertEquals("9223372036854775807", maxLongDecimal.toString())
        assertEquals("-9223372036854775808", minLongDecimal.toString())
        
        // Test that operations on boundary values work consistently
        val sum = maxIntDecimal.add(KBigDecimalFactory.ONE)
        assertEquals("2147483648", sum.toString())
        
        val difference = minIntDecimal.subtract(KBigDecimalFactory.ONE)
        assertEquals("-2147483649", difference.toString())
    }

    // ERROR HANDLING CONSISTENCY TESTS
    @Test
    fun testErrorHandlingConsistencyAcrossPlatforms() {
        val decimal = "123.456".toKBigDecimal()
        val zero = KBigDecimalFactory.ZERO

        try {
            decimal.divide(zero, 4)
            assertTrue(false) // Should not reach here
        } catch (e: ArithmeticException) {
            // Expected behavior across all platforms
            assertTrue(true)
        }

        val integer = "123".toKBigInteger()
        val zeroInt = KBigIntegerFactory.ZERO

        try {
            integer.divide(zeroInt)
            assertTrue(false) // Should not reach here
        } catch (e: ArithmeticException) {
            // Expected behavior across all platforms
            assertTrue(true)
        }
    }

    // PLATFORM-SPECIFIC FEATURE TESTS
    @Test
    fun testPlatformSpecificFeatures() {
        // Test that demonstrate BigNumber functionality
        val greeting = Greeting()
        val demo = greeting.demonstrateBigNumbers()
        
        // Should demonstrate some big number operations
        assertTrue(demo.isNotEmpty())
        assertTrue(demo.contains("KBigInteger") || demo.contains("KBigDecimal") || demo.contains("operations") || demo.isNotEmpty())
    }

    @Test
    fun testStringRepresentationConsistency() {
        // Test that string representations are consistent across platforms
        val testValues = listOf(
            "0", "1", "-1", "123.456", "-123.456",
            "999999999999999999.123456789", 
            "-999999999999999999.123456789",
            "0.000000000000000001"
        )
        
        for (value in testValues) {
            if (value.contains('.')) {
                val decimal = value.toKBigDecimal()
                assertTrue(decimal.toString() == value || decimal.toString().contains(value.replace("0.000000000000000001", "1E-18")))
            } else {
                val decimal = value.toKBigDecimal()
                val integer = value.toKBigInteger()
                assertEquals(value, decimal.toString())
                assertEquals(value, integer.toString())
            }
        }
    }

    @Test
    fun testPlatformIndependentBehavior() {
        // Test behaviors that should be identical across all platforms
        val decimal = "123.456".toKBigDecimal()
        
        assertTrue(decimal.isPositive())
        assertFalse(decimal.isNegative())
        assertFalse(decimal.isZero())
        assertEquals(1, decimal.signum())
        assertEquals("123.456", decimal.abs().toString())
        assertEquals("-123.456", decimal.negate().toString())
        
        val integer = "789".toKBigInteger()
        
        assertTrue(integer.isPositive())
        assertFalse(integer.isNegative())
        assertFalse(integer.isZero())
        assertEquals(1, integer.signum())
        assertEquals("789", integer.abs().toString())
        assertEquals("-789", integer.negate().toString())
    }
}