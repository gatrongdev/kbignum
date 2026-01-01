package io.github.gatrongdev.kbignum.math

/**
 * Arbitrary-precision decimal arithmetic.
 * Pure Kotlin implementation.
 */
class KBigDecimal(
    val unscaledValue: KBigInteger,
    val scale: Int = 0
) : Comparable<KBigDecimal> {

    companion object {
        val ZERO = KBigDecimal(KBigInteger.ZERO, 0)
        val ONE = KBigDecimal(KBigInteger.ONE, 0)
        val TEN = KBigDecimal(KBigInteger.TEN, 0)

        fun fromString(value: String): KBigDecimal {
            if (value.isEmpty()) throw NumberFormatException("Zero length string")
            
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
        val newScale = scale + other.scale
        return KBigDecimal(product, newScale)
    }
    
    // Helper to align scales
    private fun matchScales(a: KBigDecimal, b: KBigDecimal): Pair<KBigDecimal, KBigDecimal> {
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
    
    private fun powerOfTen(n: Int): KBigInteger {
        // Simple implementation: repeated multiplication or lookup
        // Use 10^9 radix repeatedly
        if (n == 0) return KBigInteger.ONE
        if (n < 0) throw IllegalArgumentException("Negative power")
        
        // TODO: Optimize with a cache or faster pow
        var res = KBigInteger.ONE
        val ten = KBigInteger.TEN
        repeat(n) { res = res.multiply(ten) }
        return res
    }

    fun divide(other: KBigDecimal): KBigDecimal {
        // Default behavior: preferred scale = this.scale - other.scale (if exact)
        // But for compatibility with Java BigDecimal default, we might error on non-terminating?
        // Let's implement simpler: match max scale or use a default precision?
        // Java BigDecimal.divide(BigDecimal) defaults to exact result or throwing ArithmeticException.
        // Let's assume we want at least max(this.scale, other.scale) + extra?
        
        // Actually, let's implement the standard: divide(other, RoundingMode.UNNECESSARY) if possible?
        // But for now, let's just make it work for simple cases or default to 6 decimal places if needed?
        // No, let's require explicit scale for safety or default to `this.scale`.
        
        // BETTER: default to specific scale strategies if user doesn't provide.
        // Let's route to a default divide using `this.scale`? No, usually `div` implies "exact if possible".
        // Let's throw "NotImplemented" for the complex "Exact determination" logic and only support explicit scale for now.
        // Or better: Use division with default rounding (HALF_UP) and max scale?
        
        // Let's implement `divide(other, scale, rounding)` and call it with a sensible default.
        return divide(other, kotlin.math.max(this.scale, other.scale), KBRoundingMode.HalfUp)
    }

    fun divide(other: KBigDecimal, scale: Int, rounding: KBRoundingMode): KBigDecimal {
        if (other.isZero()) throw ArithmeticException("Division by zero")
        
        // Formula:
        // Result = (this.unscaled * 10^(descale + other.scale - this.scale)) / other.unscaled
        // We want result * 10^scale = (A * 10^scale) / B
        // But A is scaled by 10^-Sa, B by 10^-Sb.
        // A_val / 10^Sa / (B_val / 10^Sb) = (A_val / B_val) * 10^(Sb - Sa)
        // We want result to have scale 'S'.
        // So we need UnscaledResult s.t. UnscaledResult * 10^-S = RealResult
        // UnscaledResult = RealResult * 10^S = (A_val / B_val) * 10^(Sb - Sa + S)
        
        // We multiply Dividend by 10^(Sb - Sa + S)
        val desiredShift = other.scale - this.scale + scale
        
        val dividend: KBigInteger
        if (desiredShift >= 0) {
            dividend = unscaledValue.multiply(powerOfTen(desiredShift))
        } else {
            // Negative shift means we need to divide dividend? No, shift right? 
            // Better to align by multiplying Divisor? 
            // (A / 10^Sa) / (B / 10^Sb) = (A * 10^Sb) / (B * 10^Sa)
            // = (A * 10^Sb * 10^S) / (B * 10^Sa)
            // If scale adjustment is negative, it implies we are reducing precision heavily?
            // Actually, usually we are increasing precision so shift is positive.
            // If negative, we divide dividend? No.
             // dividend = unscaledValue; divisor = other.unscaledValue * 10^(-desiredShift)
             // But we want integer arithmetic.
             // We can just divide normally and shift result?
             // Let's handle generic case:
             // Scale up dividend by P = Sb + S. Scale up divisor by Q = Sa.
             // Then divide.
             // But simpler: multiply dividend by 10^k where k ensures integer division gives needed precision.
             
             // Let's use the straightforward:
             // Target Unscaled = (this.unscaled * 10^(other.scale + scale)) / (other.unscaled * 10^this.scale)
             // Clean up powers:
             // Numerator = this.unscaled * 10^(other.scale + scale)
             // Denominator = other.unscaled * 10^this.scale
             // Simplify powers of ten.
             dividend = unscaledValue
             // We can cancel out 10^this.scale from numerator if exists?
        }
        
        // Refined Logic:
        val p = other.scale.toLong() + scale.toLong() - this.scale.toLong()
        // If p > 0, multiply dividend by 10^p.
        // If p < 0, multiply divisor by 10^(-p).
        
        var u = unscaledValue
        var v = other.unscaledValue
        
        if (p > 0) {
            u = u.multiply(powerOfTen(p.toInt()))
        } else if (p < 0) {
            v = v.multiply(powerOfTen((-p).toInt()))
        }
        
        // Do division with remaining
        // We need remainder for rounding.
        // Since we don't have `divideAndRemainder` exposed efficiently, use `divide` and `mod` or `divide` then check `mod`.
        // Better: implement `divideAndRemainder`? I'll use `divide` and verify remainder manually for now (slower but works).
        // Actually `KBigInteger` has `mod`.
        
        val q = u.divide(v)
        val r = u.mod(v)
        
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

        val increment = when(rounding) {
             KBRoundingMode.Up -> true
             KBRoundingMode.Down -> false
             KBRoundingMode.Ceiling -> resultSign >= 0
             KBRoundingMode.Floor -> resultSign < 0
             KBRoundingMode.HalfUp -> cmp >= 0
             KBRoundingMode.HalfDown -> cmp > 0
             KBRoundingMode.HalfEven -> {
                  if (cmp > 0) true 
                  else if (cmp < 0) false
                  else q.testBit(0) // odd ? up : down
             }
             KBRoundingMode.Unnecessary -> false // handled
        }
        
        val finalQ = if (increment) {
             if (resultSign >= 0) q.add(KBigInteger.ONE) else q.subtract(KBigInteger.ONE) // Magnitude increase in correct direction
        } else {
             q
        }
        
        return KBigDecimal(finalQ, scale)
    }

    // Overloads
    fun divide(other: KBigDecimal, scale: Int): KBigDecimal = divide(other, scale, KBRoundingMode.HalfUp)
    fun divide(other: KBigDecimal, scale: Int, mode: Int): KBigDecimal = divide(other, scale, KBRoundingMode.fromLegacyCode(mode))
    
    // Support older interface methods if needed, or clean up
    fun divide(other: KBigDecimal, config: DivisionConfig): KBigDecimal = throw NotImplementedError()


    fun abs(): KBigDecimal {
        return if (unscaledValue.signum() < 0) KBigDecimal(unscaledValue.negate(), scale) else this
    }

    fun signum(): Int = unscaledValue.signum()

    fun setScale(scale: Int, roundingMode: Int): KBigDecimal {
         throw NotImplementedError()
    }

    fun setScale(scale: Int, rounding: KBRoundingMode): KBigDecimal {
         throw NotImplementedError()
    }

    fun toBigInteger(): KBigInteger {
        // Simple truncation for now (only if scale=0)
        if (scale == 0) return unscaledValue
        throw NotImplementedError("Scaling not implemented")
    }
    
    fun scale(): Int = scale
    
    fun precision(): Int = throw NotImplementedError()
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
