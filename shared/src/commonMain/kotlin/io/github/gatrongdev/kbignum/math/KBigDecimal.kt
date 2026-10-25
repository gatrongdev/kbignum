package io.github.gatrongdev.kbignum.math

/**
 * Arbitrary-precision decimal arithmetic.
 * Pure Kotlin implementation.
 */
class KBigDecimal(
    val unscaledValue: KBigInteger,
    val scale: Int = 0,
) : Comparable<KBigDecimal> {
    init {
        require(scale >= 0) { "Scale must be non-negative" }
    }

    companion object {
        val ZERO = KBigDecimal(KBigInteger.ZERO, 0)
        val ONE = KBigDecimal(KBigInteger.ONE, 0)
        val TEN = KBigDecimal(KBigInteger.TEN, 0)

        fun fromString(value: String): KBigDecimal {
            if (value.isEmpty()) throw NumberFormatException("Zero length string")

            val lowerExponentIndex = value.indexOf('e')
            val upperExponentIndex = value.indexOf('E')
            if (lowerExponentIndex >= 0 && upperExponentIndex >= 0) {
                throw NumberFormatException("Invalid decimal: $value")
            }

            val exponentIndex = maxOf(lowerExponentIndex, upperExponentIndex)
            if (exponentIndex >= 0) {
                if (exponentIndex == 0 || exponentIndex == value.lastIndex) {
                    throw NumberFormatException("Invalid decimal: $value")
                }
                val exponent =
                    value.substring(exponentIndex + 1).toLongOrNull()
                        ?: throw NumberFormatException("Invalid decimal exponent: $value")
                if (exponent !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
                    throw NumberFormatException("Decimal exponent is out of range: $value")
                }

                val mantissa = fromString(value.substring(0, exponentIndex))
                val resultingScale = mantissa.scale.toLong() - exponent
                if (resultingScale >= 0) {
                    if (resultingScale > Int.MAX_VALUE) {
                        throw NumberFormatException("Decimal scale is out of range: $value")
                    }
                    return KBigDecimal(mantissa.unscaledValue, resultingScale.toInt())
                }

                val integerShift = -resultingScale
                if (integerShift > Int.MAX_VALUE) {
                    throw NumberFormatException("Decimal exponent is out of range: $value")
                }
                val factor = KBigInteger.TEN.pow(integerShift.toInt())
                return KBigDecimal(mantissa.unscaledValue.multiply(factor), 0)
            }

            val dotIndex = value.indexOf('.')
            if (dotIndex == -1) {
                // Integer
                return KBigDecimal(KBigInteger.fromString(value), 0)
            }

            // Decimal
            val scale = value.length - 1 - dotIndex
            val unscaledStr = value.substring(0, dotIndex) + value.substring(dotIndex + 1)
            val unscaled = KBigInteger.fromString(unscaledStr)

            return KBigDecimal(unscaled, scale)
        }

        fun fromLong(value: Long): KBigDecimal {
            return KBigDecimal(KBigInteger.fromLong(value), 0)
        }

        fun fromInt(value: Int): KBigDecimal {
            return KBigDecimal(KBigInteger.fromInt(value), 0)
        }
    }

    fun add(other: KBigDecimal): KBigDecimal {
        val (scaledThis, scaledOther) = matchScales(this, other)
        val sum = scaledThis.unscaledValue.add(scaledOther.unscaledValue)
        return KBigDecimal(sum, scaledThis.scale)
    }

    fun subtract(other: KBigDecimal): KBigDecimal {
        val (scaledThis, scaledOther) = matchScales(this, other)
        val diff = scaledThis.unscaledValue.subtract(scaledOther.unscaledValue)
        return KBigDecimal(diff, scaledThis.scale)
    }

    fun multiply(other: KBigDecimal): KBigDecimal {
        val product = unscaledValue.multiply(other.unscaledValue)
        val newScaleLong = scale.toLong() + other.scale.toLong()
        require(newScaleLong <= Int.MAX_VALUE) { "Result scale is too large" }
        val newScale = newScaleLong.toInt()
        return KBigDecimal(product, newScale)
    }

    // Helper to align scales
    private fun matchScales(
        a: KBigDecimal,
        b: KBigDecimal,
    ): Pair<KBigDecimal, KBigDecimal> {
        if (a.scale == b.scale) return a to b
        if (a.scale > b.scale) {
            val diff = a.scale - b.scale
            val newMag = b.unscaledValue.multiply(powerOfTen(diff))
            return a to KBigDecimal(newMag, a.scale)
        } else {
            val diff = b.scale - a.scale
            val newMag = a.unscaledValue.multiply(powerOfTen(diff))
            return KBigDecimal(newMag, b.scale) to b
        }
    }

    private val powerOfTenCache = mutableMapOf<Int, KBigInteger>()

    private fun powerOfTen(n: Int): KBigInteger {
        if (n == 0) return KBigInteger.ONE
        if (n < 0) throw IllegalArgumentException("Negative power")

        powerOfTenCache[n]?.let { return it }
        val result = fastPowerOfTen(n)
        if (n <= 100) powerOfTenCache[n] = result
        return result
    }

    /**
     * Fast power of 10 using binary exponentiation.
     * 10^n = 10^(n/2) * 10^(n/2) [* 10 if n is odd]
     */
    private fun fastPowerOfTen(n: Int): KBigInteger {
        if (n == 0) return KBigInteger.ONE
        if (n == 1) return KBigInteger.TEN

        // Use 10^9 as a "super-digit" for faster computation
        val billion = KBigInteger.fromInt(1_000_000_000) // 10^9

        if (n < 9) {
            // Small powers: use lookup or simple multiply
            var result = KBigInteger.ONE
            repeat(n) { result = result.multiply(KBigInteger.TEN) }
            return result
        }

        // For larger powers, use 10^9 as base
        val fullNines = n / 9
        val remainder = n % 9

        // Binary exponentiation with 10^9 base
        var base = billion
        var exp = fullNines
        var result = KBigInteger.ONE

        while (exp > 0) {
            if (exp and 1 == 1) {
                result = result.multiply(base)
            }
            base = base.multiply(base)
            exp = exp shr 1
        }

        // Handle remainder
        if (remainder > 0) {
            var tenPow = KBigInteger.ONE
            repeat(remainder) { tenPow = tenPow.multiply(KBigInteger.TEN) }
            result = result.multiply(tenPow)
        }

        return result
    }

    fun divide(other: KBigDecimal): KBigDecimal {
        return divide(other, DivisionStrategy.DEFAULT)
    }

    fun divide(
        other: KBigDecimal,
        config: DivisionConfig,
    ): KBigDecimal = divide(other, config.scale, config.roundingMode)

    fun divide(
        other: KBigDecimal,
        scale: Int,
        rounding: KBRoundingMode,
    ): KBigDecimal {
        require(scale >= 0) { "Scale must be non-negative" }
        if (other.isZero()) throw ArithmeticException("Division by zero")

        // Scale calculation:
        // Result = (this.unscaled / other.unscaled) * 10^(other.scale - this.scale)
        // We want result to have scale 'S', so:
        // UnscaledResult = (this.unscaled / other.unscaled) * 10^(other.scale - this.scale + S)
        //
        // Let p = other.scale + scale - this.scale
        // If p > 0, multiply dividend by 10^p
        // If p < 0, multiply divisor by 10^(-p)

        val p = other.scale.toLong() + scale.toLong() - this.scale.toLong()

        var u = unscaledValue
        var v = other.unscaledValue

        if (p > 0) {
            require(p <= Int.MAX_VALUE) { "Requested scale is too large" }
            u = u.multiply(powerOfTen(p.toInt()))
        } else if (p < 0) {
            require(-p <= Int.MAX_VALUE) { "Requested scale is too large" }
            v = v.multiply(powerOfTen((-p).toInt()))
        }

        // Use divideAndRemainder for efficiency (single division operation)
        val (q, r) = u.divideAndRemainder(v)

        if (r.isZero()) {
            return KBigDecimal(q, scale)
        }

        // Rounding required
        if (rounding == KBRoundingMode.Unnecessary) throw ArithmeticException("Rounding necessary")

        // Check remainder relative to divisor/2
        // cmp = r * 2 vs v
        val r2 = r.abs().multiply(KBigInteger.fromInt(2))
        val vAbs = v.abs()
        val cmp = r2.compareTo(vAbs)

        // Sign of the result (u / v)
        val resultSign = if (u.signum() * v.signum() >= 0) 1 else -1

        val increment =
            when (rounding) {
                KBRoundingMode.Up -> true
                KBRoundingMode.Down -> false
                KBRoundingMode.Ceiling -> resultSign >= 0
                KBRoundingMode.Floor -> resultSign < 0
                KBRoundingMode.HalfUp -> cmp >= 0
                KBRoundingMode.HalfDown -> cmp > 0
                KBRoundingMode.HalfEven -> {
                    if (cmp > 0) {
                        true
                    } else if (cmp < 0) {
                        false
                    } else {
                        q.testBit(0) // odd ? up : down
                    }
                }
                KBRoundingMode.Unnecessary -> false // handled
            }

        val finalQ =
            if (increment) {
                if (resultSign >= 0) q.add(KBigInteger.ONE) else q.subtract(KBigInteger.ONE) // Magnitude increase in correct direction
            } else {
                q
            }

        return KBigDecimal(finalQ, scale)
    }

    // Overloads
    fun divide(
        other: KBigDecimal,
        scale: Int,
    ): KBigDecimal = divide(other, scale, KBRoundingMode.HalfUp)

    fun divide(
        other: KBigDecimal,
        scale: Int,
        mode: Int,
    ): KBigDecimal = divide(other, scale, KBRoundingMode.fromLegacyCode(mode))

    /**
     * Calculates this KBigDecimal raised to the power of the specified integer exponent.
     * Uses binary exponentiation.
     */
    fun pow(exponent: Int): KBigDecimal {
        if (exponent < 0) throw ArithmeticException("Negative exponent not supported")
        if (exponent == 0) return ONE

        var result = ONE
        var base = this
        var exp = exponent

        while (exp > 0) {
            if (exp % 2 == 1) result = result.multiply(base)
            base = base.multiply(base)
            exp /= 2
        }
        return result
    }

    fun abs(): KBigDecimal {
        return if (unscaledValue.signum() < 0) KBigDecimal(unscaledValue.negate(), scale) else this
    }

    fun signum(): Int = unscaledValue.signum()

    fun setScale(
        scale: Int,
        roundingMode: Int,
    ): KBigDecimal {
        return setScale(scale, KBRoundingMode.fromLegacyCode(roundingMode))
    }

    fun setScale(
        scale: Int,
        rounding: KBRoundingMode,
    ): KBigDecimal {
        return divide(ONE, scale, rounding)
    }

    fun toBigInteger(): KBigInteger {
        return setScale(0, KBRoundingMode.Down).unscaledValue
    }

    fun scale(): Int = scale

    fun isZero(): Boolean = unscaledValue.isZero()

    fun isPositive(): Boolean = unscaledValue.isPositive()

    fun isNegative(): Boolean = unscaledValue.isNegative()

    fun negate(): KBigDecimal = KBigDecimal(unscaledValue.negate(), scale)

    override fun toString(): String {
        val unscaledStr = unscaledValue.toString()
        if (scale == 0) return unscaledStr

        val negative = unscaledValue.signum() < 0
        val absStr = if (negative) unscaledStr.removePrefix("-") else unscaledStr

        val len = absStr.length
        val sb = StringBuilder()

        if (negative) sb.append('-')

        if (scale >= len) {
            sb.append("0.")
            repeat(scale - len) { sb.append('0') }
            sb.append(absStr)
        } else {
            val dotIndex = len - scale
            sb.append(absStr.substring(0, dotIndex))
            sb.append('.')
            sb.append(absStr.substring(dotIndex))
        }

        return sb.toString()
    }

    override fun compareTo(other: KBigDecimal): Int {
        if (scale == other.scale) {
            return unscaledValue.compareTo(other.unscaledValue)
        }
        val (scaledThis, scaledOther) = matchScales(this, other)
        return scaledThis.unscaledValue.compareTo(scaledOther.unscaledValue)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KBigDecimal) return false
        return unscaledValue == other.unscaledValue && scale == other.scale
    }

    override fun hashCode(): Int {
        var result = unscaledValue.hashCode()
        result = 31 * result + scale
        return result
    }
}
