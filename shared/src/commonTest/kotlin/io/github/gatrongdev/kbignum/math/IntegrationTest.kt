package io.github.gatrongdev.kbignum.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IntegrationTest {
    // FACTORY + INTERFACE INTEGRATION TESTS
    @Test
    fun testFactoryWithInterfaceOperations() {
        val decimalFromFactory = KBigDecimal.fromString("123.456")
        val integerFromFactory = KBigInteger.fromString("789")

        // Test factory-created objects work with interface operations
        val sum = decimalFromFactory.add("100".toKBigDecimal())
        assertEquals("223.456", sum.toString())

        val product = integerFromFactory.multiply("2".toKBigInteger())
        assertEquals("1578", product.toString())

        // Test cross-type operations
        val preciseFromInt = KBigDecimal(integerFromFactory)
        val intFromDecimal = decimalFromFactory.toBigInteger()

        assertEquals("789", preciseFromInt.toString())
        assertEquals("123", intFromDecimal.toString())
    }

    @Test
    fun testFactoryConstantsWithOperations() {
        val zero = KBigDecimal.ZERO
        val one = KBigDecimal.ONE
        val ten = KBigDecimal.TEN

        // Test that constants work correctly in operations
        val result1 = zero.add(one).add(ten)
        assertEquals("11", result1.toString())

        val result2 = ten.multiply(one).subtract(zero)
        assertEquals("10", result2.toString())

        val intZero = KBigInteger.ZERO
        val intOne = KBigInteger.ONE
        val intTen = KBigInteger.TEN

        val intResult = intTen.divide(intOne).add(intZero)
        assertEquals("10", intResult.toString())
    }

    // CONVERSION CHAIN INTEGRATION TESTS
    @Test
    fun testComplexConversionChains() {
        // Test: String -> KBigDecimal -> KBigInteger -> KBigDecimal
        val original = "123.789"
        val decimal = original.toKBigDecimal()
        val integer = decimal.toBigInteger()
        val backToDecimal = KBigDecimal(integer)

        assertEquals("123.789", decimal.toString())
        assertEquals("123", integer.toString())
        assertEquals("123", backToDecimal.toString())

        // Test: Int -> KBigInteger -> KBigDecimal -> String representation
        val intValue = 456
        val bigInt = intValue.toKBigInteger()
        val bigDec = KBigDecimal(bigInt)
        val stringResult = bigDec.toString()

        assertEquals("456", stringResult)
    }

    @Test
    fun testPrimitiveTypeIntegration() {
        val int = 123
        val long = 456L
        val double = 789.012
        val float = 321.5f

        // Convert all to KBigDecimal and perform operations
        val intAsBigDec = int.toKBigDecimal()
        val longAsBigDec = long.toKBigDecimal()
        val doubleAsBigDec = double.toKBigDecimal()
        val floatAsBigDec = float.toKBigDecimal()

        val sum = intAsBigDec.add(longAsBigDec).add(doubleAsBigDec).add(floatAsBigDec)
        assertTrue(sum.toString().startsWith("1689"))

        // Convert integers to KBigInteger and perform operations
        val intAsBigInt = int.toKBigInteger()
        val longAsBigInt = long.toKBigInteger()

        val intProduct = intAsBigInt.multiply(longAsBigInt)
        assertEquals("56088", intProduct.toString())
    }

    // EXTENSION FUNCTION + MATH OPERATIONS INTEGRATION TESTS
    @Test
    fun testExtensionFunctionsWithMathOperations() {
        val base = "2".toKBigInteger()
        val exponent = "10".toKBigInteger()

        // Combine extension operators with KBigMath functions
        val powerResult = KBigMath.pow(base, exponent)
        val doubledPower = powerResult * "2".toKBigInteger()

        assertEquals("1024", powerResult.toString())
        assertEquals("2048", doubledPower.toString())

        // Test with factorial
        val n = "5".toKBigInteger()
        val factorial = KBigMath.factorial(n)
        val factorialPlusOne = factorial + KBigInteger.ONE

        assertEquals("120", factorial.toString())
        assertEquals("121", factorialPlusOne.toString())
    }

    @Test
    fun testMathFunctionsWithExtensionChaining() {
        val number = "16".toKBigDecimal()

        // Chain math operations with extensions
        val sqrt = KBigMath.sqrt(number)
        val doubled = sqrt * "2".toKBigDecimal()
        val result = doubled + "1".toKBigDecimal()

        assertEquals("4", sqrt.toString())
        assertEquals("8", doubled.toString())
        assertEquals("9", result.toString())

        // Test with integer math functions
        val a = "12".toKBigInteger()
        val b = "18".toKBigInteger()
        val gcd = KBigMath.gcd(a, b)
        val lcm = KBigMath.lcm(a, b)
        val gcdPlusLcm = gcd + lcm

        assertEquals("6", gcd.toString())
        assertEquals("36", lcm.toString())
        assertEquals("42", gcdPlusLcm.toString())
    }

    // MIXED TYPE OPERATIONS INTEGRATION TESTS
    @Test
    fun testMixedDecimalIntegerOperations() {
        val decimal = "10.5".toKBigDecimal()
        val integer = "3".toKBigInteger()

        // Operations between KBigDecimal and KBigInteger via conversion
        val intAsDecimal = KBigDecimal(integer)
        val sum = decimal + intAsDecimal
        assertEquals("13.5", sum.toString())

        val decimalAsInt = decimal.toBigInteger()
        val intSum = decimalAsInt + integer
        assertEquals("13", intSum.toString())

        // Test with mathematical operations
        val product = decimal * intAsDecimal
        assertEquals("31.5", product.toString())
    }

    @Test
    fun testCrossTypeComparisonOperations() {
        val decimal1 = "10.0".toKBigDecimal()
        val integer1 = "10".toKBigInteger()

        // Convert for comparison
        val intAsDecimal = KBigDecimal(integer1)
        val decimalAsInt = decimal1.toBigInteger()

        assertEquals(0, decimal1.compareTo(intAsDecimal))
        assertEquals(0, decimalAsInt.compareTo(integer1))

        val decimal2 = "10.5".toKBigDecimal()
        val decimalAsInt2 = decimal2.toBigInteger()
        assertTrue(decimalAsInt2.compareTo(integer1) == 0) // Both are 10
    }

    // REAL-WORLD CALCULATION SCENARIOS
    @Test
    fun testFinancialCalculationScenario() {
        // Simulate compound interest calculation: A = P(1 + r/n)^(nt)
        val principal = "1000.00".toKBigDecimal()
        val rate = "0.05".toKBigDecimal() // 5% annual rate
        val timesCompounded = "12".toKBigDecimal() // Monthly
        val years = "10".toKBigDecimal()

        // Calculate r/n
        val ratePerPeriod = rate.divide(timesCompounded, 10, 4)

        // Calculate nt
        val totalPeriods = timesCompounded.multiply(years)

        // For simplification, test the components
        assertTrue(ratePerPeriod.toString().startsWith("0.004166"))
        assertEquals("120", totalPeriods.toString())

        // Test basic compounding calculation
        val onePlusRate = KBigDecimal.ONE.add(ratePerPeriod)
        assertTrue(onePlusRate.toString().startsWith("1.004166"))
    }

    @Test
    fun testScientificCalculationScenario() {
        // Calculate factorial of 20 and perform operations
        val factorial20 = KBigMath.factorial("20".toKBigInteger())
        assertEquals("2432902008176640000", factorial20.toString())

        // Convert to decimal and perform calculations
        val factorialAsDecimal = KBigDecimal(factorial20)
        val sqrt = KBigMath.sqrt(factorialAsDecimal)

        assertTrue(sqrt.toString().startsWith("1559776"))

        // Test with very large calculations
        val factorial50 = KBigMath.factorial("50".toKBigInteger())
        assertTrue(factorial50.toString().length > 60)

        val gcdWithFactorial = KBigMath.gcd(factorial20, "100".toKBigInteger())
        assertEquals("100", gcdWithFactorial.toString())
    }

    // COMPLEX WORKFLOW INTEGRATION TESTS
    @Test
    fun testCompleteWorkflowIntegration() {
        // Start with string input
        val inputString = "123.456"

        // Convert to KBigDecimal
        val decimal = inputString.toKBigDecimal()

        // Perform various operations
        val scaled = decimal.setScale(2, 4)
        val doubled = scaled.multiply("2".toKBigDecimal())
        val asInteger = doubled.toBigInteger()

        // Use in mathematical operations
        val factorial = KBigMath.factorial("5".toKBigInteger())
        val combined = asInteger.add(factorial)

        // Convert back to decimal
        val finalResult = KBigDecimal(combined)

        assertEquals("123.46", scaled.toString())
        assertEquals("246.92", doubled.toString())
        assertEquals("246", asInteger.toString())
        assertEquals("120", factorial.toString())
        assertEquals("366", combined.toString())
        assertEquals("366", finalResult.toString())
    }

    @Test
    fun testErrorHandlingInIntegratedWorkflow() {
        val decimal = "10.5".toKBigDecimal()
        var result = decimal

        try {
            // Perform a series of operations, one of which will fail
            result = result.multiply("2".toKBigDecimal())
            result = result.add("3.5".toKBigDecimal())
            result = result.divide(KBigDecimal.ZERO, 2, 4) // This will fail
            result = result.subtract("1".toKBigDecimal()) // This won't execute
        } catch (e: ArithmeticException) {
            // Error occurred, but previous operations were successful
            assertEquals("24.5", result.toString()) // Should be 10.5 * 2 + 3.5
        }
    }

    // DATA TYPE INTEROPERABILITY TESTS
    @Test
    fun testDataTypeInteroperability() {
        // Test seamless conversion between all supported types
        val values =
            mapOf(
                "int" to 123,
                "long" to 456L,
                "double" to 789.012,
                "float" to 321.5f,
                "string" to "999.888",
            )

        // Convert all to KBigDecimal
        val decimals = mutableMapOf<String, KBigDecimal>()
        decimals["int"] = (values["int"] as Int).toKBigDecimal()
        decimals["long"] = (values["long"] as Long).toKBigDecimal()
        decimals["double"] = (values["double"] as Double).toKBigDecimal()
        decimals["float"] = (values["float"] as Float).toKBigDecimal()
        decimals["string"] = (values["string"] as String).toKBigDecimal()

        // Verify conversions
        assertEquals("123", decimals["int"]?.toString())
        assertEquals("456", decimals["long"]?.toString())
        assertTrue(decimals["double"]?.toString()?.startsWith("789") == true)
        assertTrue(decimals["float"]?.toString()?.startsWith("321") == true)
        assertEquals("999.888", decimals["string"]?.toString())

        // Perform combined operations
        val sum = decimals.values.reduce { acc, dec -> acc.add(dec) }
        assertTrue(sum.toString().startsWith("2689"))
    }

    // BOUNDARY CONDITIONS INTEGRATION TESTS
    @Test
    fun testBoundaryConditionsIntegration() {
        // Test with maximum primitive values
        val maxInt = Int.MAX_VALUE.toKBigInteger()
        val maxLong = Long.MAX_VALUE.toKBigInteger()

        // Perform operations that exceed primitive limits
        val beyondInt = maxInt.add(KBigInteger.ONE)
        val beyondLong = maxLong.add(KBigInteger.ONE)

        assertEquals("2147483648", beyondInt.toString())
        assertEquals("9223372036854775808", beyondLong.toString())

        // Use in mathematical operations
        val factorial = KBigMath.factorial("21".toKBigInteger())
        val gcdWithMax = KBigMath.gcd(factorial, maxLong)

        assertTrue(factorial.toString().length > 19)
        assertTrue(gcdWithMax.toString().length > 0)
    }

    // CONSISTENCY VERIFICATION TESTS
    @Test
    fun testOperationalConsistency() {
        val value = "123.456".toKBigDecimal()

        // Test that equivalent operations produce same results
        val method1 = value.add(value).add(value) // 3 * value
        val method2 = value.multiply("3".toKBigDecimal()) // value * 3

        assertEquals(method1.toString(), method2.toString())

        // Test with integers
        val intValue = "789".toKBigInteger()
        val intMethod1 = intValue.add(intValue)
        val intMethod2 = intValue.multiply("2".toKBigInteger())

        assertEquals(intMethod1.toString(), intMethod2.toString())
    }

    @Test
    fun testMathematicalPropertyConsistency() {
        val a = "12".toKBigInteger()
        val b = "18".toKBigInteger()

        // Test GCD * LCM = A * B property
        val gcd = KBigMath.gcd(a, b)
        val lcm = KBigMath.lcm(a, b)
        val product = a.multiply(b)
        val gcdLcmProduct = gcd.multiply(lcm)

        assertEquals(product.toString(), gcdLcmProduct.toString())

        // Test commutative property
        val sum1 = a.add(b)
        val sum2 = b.add(a)
        assertEquals(sum1.toString(), sum2.toString())

        val mult1 = a.multiply(b)
        val mult2 = b.multiply(a)
        assertEquals(mult1.toString(), mult2.toString())
    }
}
