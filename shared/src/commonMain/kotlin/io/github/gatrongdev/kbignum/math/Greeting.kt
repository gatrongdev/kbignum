package io.github.gatrongdev.kbignum.math

class Greeting {
    private val platform: Platform = getPlatform()

    /**
     * Returns a greeting message based on the current platform.
     */
    fun greet(): String {
        return "Hello, ${platform.name}!"
    }

    /**
     * Demonstrates the usage of KBigNum library with BigDecimal and BigInteger operations.
     * This function performs addition and multiplication of large decimal numbers,
     * and also shows how to handle large integers.
     */
    fun demonstrateBigNumbers(): String {
        val bigDecimal1 = "123456789.987654321".toKBigDecimal()
        val bigDecimal2 = "987654321.123456789".toKBigDecimal()

        val sum = bigDecimal1 + bigDecimal2
        val product = bigDecimal1 * bigDecimal2

        val bigInteger = "999999999999999999999999999".toKBigInteger()

        return """
        |KBigNum Library Demo on ${platform.name}
        |
        |BigDecimal Operations:
        |$bigDecimal1 + $bigDecimal2 = $sum
        |$bigDecimal1 × $bigDecimal2 = $product
        |
        |BigInteger:
        |Large number: $bigInteger
        |As decimal: ${bigInteger.toPreciseNumber()}
            """.trimMargin()
    }
}
