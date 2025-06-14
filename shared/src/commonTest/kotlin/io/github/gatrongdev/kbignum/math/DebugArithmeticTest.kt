package io.github.gatrongdev.kbignum.math

import kotlin.test.Test

class DebugArithmeticTest {
    @Test
    fun debugArithmetic() {
        println("=== Addition Tests ===")
        val decimal = "123.456".toKBigDecimal()
        val one = KBigDecimalFactory.ONE
        val zero = KBigDecimalFactory.ZERO
        
        val addOne = decimal.add(one)
        val addZero = decimal.add(zero)
        val zeroAddDecimal = zero.add(decimal)
        
        println("decimal: $decimal")
        println("one: $one")
        println("zero: $zero")
        println("decimal + one = $addOne (expected: 124.456)")
        println("decimal + zero = $addZero (expected: 123.456)")
        println("zero + decimal = $zeroAddDecimal (expected: 123.456)")
        
        println("\n=== Large Number Addition ===")
        val large1 = "999999999999999999.123456789".toKBigDecimal()
        val large2 = "111111111111111111.987654321".toKBigDecimal()
        val sum = large1.add(large2)
        println("$large1 + $large2 = $sum")
        println("Expected: 1111111111111111111.111111110")
        
        println("\n=== Double/Float Conversion ===")
        val doubleZero = 0.0.toKBigDecimal()
        val double123 = 123.0.toKBigDecimal()
        val doubleNeg = (-456.0).toKBigDecimal()
        
        println("0.0.toKBigDecimal() = $doubleZero (expected: 0.0)")
        println("123.0.toKBigDecimal() = $double123 (expected: 123.0)")
        println("(-456.0).toKBigDecimal() = $doubleNeg (expected: -456.0)")
    }
}
