package io.github.gatrongdev.kbignum.math.math

// Extension functions for converting common types to KBigDecimal
fun String.toKBigDecimal(): KBigDecimal = KBigDecimalFactory.fromString(this)

fun Int.toKBigDecimal(): KBigDecimal = KBigDecimalFactory.fromInt(this)

fun Long.toKBigDecimal(): KBigDecimal = KBigDecimalFactory.fromLong(this)

fun Double.toKBigDecimal(): KBigDecimal = KBigDecimalFactory.fromDouble(this)

fun Float.toKBigDecimal(): KBigDecimal = KBigDecimalFactory.fromFloat(this)

// Extension functions for converting common types to KBigInteger
fun String.toKBigInteger(): KBigInteger = KBigIntegerFactory.fromString(this)

fun Int.toKBigInteger(): KBigInteger = KBigIntegerFactory.fromInt(this)

fun Long.toKBigInteger(): KBigInteger = KBigIntegerFactory.fromLong(this)

// Operator overloads for KBigDecimal
operator fun KBigDecimal.plus(other: KBigDecimal): KBigDecimal = this.add(other)

operator fun KBigDecimal.minus(other: KBigDecimal): KBigDecimal = this.subtract(other)

operator fun KBigDecimal.times(other: KBigDecimal): KBigDecimal = this.multiply(other)

operator fun KBigDecimal.div(other: KBigDecimal): KBigDecimal = this.divide(other, 10, 4)

operator fun KBigDecimal.unaryMinus(): KBigDecimal = KBigDecimalFactory.ZERO.subtract(this)

operator fun KBigDecimal.unaryPlus(): KBigDecimal = this

// Operator overloads for KBigInteger
operator fun KBigInteger.plus(other: KBigInteger): KBigInteger = this.add(other)

operator fun KBigInteger.minus(other: KBigInteger): KBigInteger = this.subtract(other)

operator fun KBigInteger.times(other: KBigInteger): KBigInteger = this.multiply(other)

operator fun KBigInteger.div(other: KBigInteger): KBigInteger = this.divide(other)

operator fun KBigInteger.rem(other: KBigInteger): KBigInteger = this.mod(other)

operator fun KBigInteger.unaryMinus(): KBigInteger = KBigIntegerFactory.ZERO.subtract(this)

operator fun KBigInteger.unaryPlus(): KBigInteger = this

// Comparison operators for KBigDecimal
operator fun KBigDecimal.compareTo(other: KBigDecimal): Int = this.compareTo(other)

// Comparison operators for KBigInteger
operator fun KBigInteger.compareTo(other: KBigInteger): Int = this.compareTo(other)

// Math functions for KBigDecimal
fun KBigDecimal.pow(exponent: Int): KBigDecimal {
    var result = KBigDecimalFactory.ONE
    var base = this
    var exp = exponent

    if (exp < 0) {
        throw IllegalArgumentException("Negative exponent not supported")
    }

    while (exp > 0) {
        if (exp % 2 == 1) {
            result = result.multiply(base)
        }
        base = base.multiply(base)
        exp /= 2
    }

    return result
}

// Math functions for KBigInteger
fun KBigInteger.pow(exponent: Int): KBigInteger {
    var result = KBigIntegerFactory.ONE
    var base = this
    var exp = exponent

    if (exp < 0) {
        throw IllegalArgumentException("Negative exponent not supported")
    }

    while (exp > 0) {
        if (exp % 2 == 1) {
            result = result.multiply(base)
        }
        base = base.multiply(base)
        exp /= 2
    }

    return result
}

fun KBigDecimal.isZero(): Boolean = this.compareTo(KBigDecimalFactory.ZERO) == 0

fun KBigDecimal.isPositive(): Boolean = this.signum() > 0

fun KBigDecimal.isNegative(): Boolean = this.signum() < 0

fun KBigInteger.isZero(): Boolean = this.compareTo(KBigIntegerFactory.ZERO) == 0

fun KBigInteger.isPositive(): Boolean = this.signum() > 0

fun KBigInteger.isNegative(): Boolean = this.signum() < 0

fun KBigDecimal.max(other: KBigDecimal): KBigDecimal = if (this >= other) this else other

fun KBigDecimal.min(other: KBigDecimal): KBigDecimal = if (this <= other) this else other

fun KBigInteger.max(other: KBigInteger): KBigInteger = if (this >= other) this else other

fun KBigInteger.min(other: KBigInteger): KBigInteger = if (this <= other) this else other
