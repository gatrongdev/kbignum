package io.github.gatrongdev.kbignum.math

import io.github.gatrongdev.kbignum.math.math.plus
import io.github.gatrongdev.kbignum.math.math.times
import io.github.gatrongdev.kbignum.math.math.toKBigDecimal
import io.github.gatrongdev.kbignum.math.math.toKBigInteger

class Greeting {
    private val platform: Platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }

    fun demonstrateBigNumbers(): String {
        val bigDecimal1 = "123456789.987654321".toKBigDecimal()
        val bigDecimal2 = "987654321.123456789".toKBigDecimal()
        
        val sum = bigDecimal1 + bigDecimal2
        val product = bigDecimal1 * bigDecimal2
        
        val bigInteger = "999999999999999999999999999".toKBigInteger()
        
        return """
            KBigNum Library Demo on ${platform.name}
            
            BigDecimal Operations:
            $bigDecimal1 + $bigDecimal2 = $sum
            $bigDecimal1 × $bigDecimal2 = $product
            
            BigInteger:
            Large number: $bigInteger
            As decimal: ${bigInteger.toPreciseNumber()}
        """.trimIndent()
    }
}