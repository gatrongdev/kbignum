package io.github.gatrongdev.kbignum.math

import java.math.BigDecimal
import java.math.BigInteger
import kotlin.random.Random

/**
 * Shared utility functions for KBigNum compliance tests.
 */

fun KBigInteger.toJavaBigInteger(): BigInteger {
    if (signum == 0) return BigInteger.ZERO
    
    // Reconstruct from magnitude
    var res = BigInteger.ZERO
    for (i in magnitude.indices.reversed()) {
        val unsignedVal = magnitude[i].toLong() and 0xFFFFFFFFL
        res = res.shiftLeft(32).or(BigInteger.valueOf(unsignedVal))
    }
    
    return if (signum < 0) res.negate() else res
}

fun KBigDecimal.toJavaBigDecimal(): BigDecimal {
    val unscaledStr = this.unscaledValue.toString()
    // KBigInteger.toString() is reliable now.
    // But we can also use conversion if we trust toJavaBigInteger
    // return BigDecimal(this.unscaledValue.toJavaBigInteger(), this.scale)
    
    // Using string is generally safest for cross-verification if toString is correct.
    // Ideally we verify value content.
    val javaUnscaled = BigInteger(unscaledStr)
    return BigDecimal(javaUnscaled, this.scale)
}

fun generateHugeRandomBigInteger(bits: Int): BigInteger {
    val bytes = Random.nextBytes(bits / 8)
    // Force some variance
    if (bytes.isNotEmpty()) {
         bytes[0] = (bytes[0].toInt() or 1).toByte() // ensure not zero easily?
    }
    return BigInteger(bytes)
}

fun mapRoundingMode(mode: java.math.RoundingMode): KBRoundingMode {
    return when(mode) {
       java.math.RoundingMode.UP -> KBRoundingMode.Up
       java.math.RoundingMode.DOWN -> KBRoundingMode.Down
       java.math.RoundingMode.CEILING -> KBRoundingMode.Ceiling
       java.math.RoundingMode.FLOOR -> KBRoundingMode.Floor
       java.math.RoundingMode.HALF_UP -> KBRoundingMode.HalfUp
       java.math.RoundingMode.HALF_DOWN -> KBRoundingMode.HalfDown
       java.math.RoundingMode.HALF_EVEN -> KBRoundingMode.HalfEven
       java.math.RoundingMode.UNNECESSARY -> KBRoundingMode.Unnecessary
    }
}
