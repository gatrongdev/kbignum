package io.github.gatrongdev.kbignum.math.math

import kotlin.test.Test

class KBigDecimalDebugTest {

    @Test
    fun debugTestValues() {
        val a = "123.45".toKBigDecimal()
        val b = "67.89".toKBigDecimal()

        val sum = a + b
        val difference = a - b
        val product = a * b
        val quotient = a.divide(b, 2, RoundingMode.HALF_UP)

        println("a = ${a.toString()}")
        println("b = ${b.toString()}")
        println("sum = ${sum.toString()}")
        println("difference = ${difference.toString()}")
        println("product = ${product.toString()}")
        println("quotient = ${quotient.toString()}")
        
        // Let's see what starts with
        println("product.toString().startsWith(\"8382.6\") = ${product.toString().startsWith("8382.6")}")
        println("quotient.toString().startsWith(\"1.8\") = ${quotient.toString().startsWith("1.8")}")
    }
}
