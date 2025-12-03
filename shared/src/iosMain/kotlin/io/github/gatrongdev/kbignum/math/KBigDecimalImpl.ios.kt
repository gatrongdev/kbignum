package io.github.gatrongdev.kbignum.math

actual internal class KBigDecimalImpl : KBigDecimal {
    private val intVal: KBigInteger
    private val _scale: Int

    actual constructor(value: String) {
        if (value.isEmpty()) throw NumberFormatException("Empty string")
        
        // Handle scientific notation
        val parts = value.uppercase().split("E")
        if (parts.size > 2) throw NumberFormatException("Invalid scientific notation")
        
        val mantissaStr = parts[0]
        val exponent = if (parts.size == 2) parts[1].toInt() else 0
        
        val dotIndex = mantissaStr.indexOf('.')
        
        if (dotIndex == -1) {
            intVal = KBigIntegerImpl(mantissaStr)
            _scale = -exponent
        } else {
            val integerPart = mantissaStr.substring(0, dotIndex)
            val fractionalPart = mantissaStr.substring(dotIndex + 1)
            intVal = KBigIntegerImpl(integerPart + fractionalPart)
            _scale = fractionalPart.length - exponent
        }
    }

    internal constructor(intVal: KBigInteger, scale: Int) {
        this.intVal = intVal
        this._scale = scale
    }

    actual companion object {
        actual fun fromLong(value: Long): KBigDecimalImpl {
            return KBigDecimalImpl(KBigIntegerImpl.fromLong(value), 0)
        }

        actual fun fromInt(value: Int): KBigDecimalImpl {
            return KBigDecimalImpl(KBigIntegerImpl.fromInt(value), 0)
        }

        actual fun fromString(value: String): KBigDecimalImpl {
            return KBigDecimalImpl(value)
        }

        actual val ZERO: KBigDecimalImpl = KBigDecimalImpl(KBigIntegerImpl.ZERO, 0)
    }

    actual override fun add(other: KBigDecimal): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        val maxScale = maxOf(_scale, otherImpl._scale)
        
        val scaledThis = scaleVal(this.intVal, maxScale - _scale)
        val scaledOther = scaleVal(otherImpl.intVal, maxScale - otherImpl._scale)
        
        return KBigDecimalImpl(scaledThis.add(scaledOther), maxScale)
    }

    actual override fun subtract(other: KBigDecimal): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        val maxScale = maxOf(_scale, otherImpl._scale)
        
        val scaledThis = scaleVal(this.intVal, maxScale - _scale)
        val scaledOther = scaleVal(otherImpl.intVal, maxScale - otherImpl._scale)
        
        return KBigDecimalImpl(scaledThis.subtract(scaledOther), maxScale)
    }

    actual override fun multiply(other: KBigDecimal): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        return KBigDecimalImpl(
            this.intVal.multiply(otherImpl.intVal),
            this._scale + otherImpl._scale
        )
    }

    actual override fun divide(other: KBigDecimal): KBigDecimal {
        val preferredScale = maxOf(_scale, other.scale())
        return divide(other, preferredScale, RoundingMode.HALF_UP)
    }

    actual override fun divide(other: KBigDecimal, scale: Int): KBigDecimal {
        return divide(other, scale, RoundingMode.HALF_UP)
    }

    actual override fun divide(other: KBigDecimal, scale: Int, mode: RoundingMode): KBigDecimal {
        val otherImpl = other as KBigDecimalImpl
        if (otherImpl.intVal.isZero()) throw ArithmeticException("Division by zero")

        val power = otherImpl._scale - this._scale + scale
        
        val dividend: KBigInteger
        val divisor: KBigInteger
        
        if (power >= 0) {
            dividend = this.intVal.multiply(tenPow(power))
            divisor = otherImpl.intVal
        } else {
            dividend = this.intVal
            divisor = otherImpl.intVal.multiply(tenPow(-power))
        }
        
        val quotient = dividend.divide(divisor)
        val remainder = dividend.mod(divisor)
        
        if (remainder.isZero()) {
            return KBigDecimalImpl(quotient, scale)
        }
        
        if (mode == RoundingMode.UNNECESSARY) {
            throw ArithmeticException("Rounding necessary")
        }
        
        val roundedQuotient = applyRounding(quotient, remainder, divisor, mode)
        return KBigDecimalImpl(roundedQuotient, scale)
    }

    actual override fun abs(): KBigDecimal {
        return KBigDecimalImpl(intVal.abs(), _scale)
    }

    actual override fun signum(): Int {
        return intVal.signum()
    }

    actual override fun setScale(scale: Int, roundingMode: RoundingMode): KBigDecimal {
        if (scale == _scale) return this
        
        val diff = scale - _scale
        if (diff > 0) {
            return KBigDecimalImpl(intVal.multiply(tenPow(diff)), scale)
        } else {
            val divisor = tenPow(-diff)
            val quotient = intVal.divide(divisor)
            val remainder = intVal.mod(divisor)
            
            if (remainder.isZero()) {
                return KBigDecimalImpl(quotient, scale)
            }
            
            if (roundingMode == RoundingMode.UNNECESSARY) {
                throw ArithmeticException("Rounding necessary")
            }
            
            val rounded = applyRounding(quotient, remainder, divisor, roundingMode)
            return KBigDecimalImpl(rounded, scale)
        }
    }

    actual override fun toBigInteger(): KBigInteger {
        if (_scale == 0) return intVal
        if (_scale < 0) return intVal.multiply(tenPow(-_scale))
        return intVal.divide(tenPow(_scale))
    }

    actual override fun compareTo(other: KBigDecimal): Int {
        val otherImpl = other as KBigDecimalImpl
        val maxScale = maxOf(_scale, otherImpl._scale)
        
        val scaledThis = scaleVal(this.intVal, maxScale - _scale)
        val scaledOther = scaleVal(otherImpl.intVal, maxScale - otherImpl._scale)
        
        return scaledThis.compareTo(scaledOther)
    }

    actual override fun toString(): String {
        val str = intVal.abs().toString()
        val sign = if (intVal.signum() < 0) "-" else ""
        
        if (_scale == 0) return sign + str
        
        if (_scale > 0) {
            if (str.length > _scale) {
                val intPart = str.substring(0, str.length - _scale)
                val fracPart = str.substring(str.length - _scale)
                return "$sign$intPart.$fracPart"
            } else {
                val padding = "0".repeat(_scale - str.length)
                return "${sign}0.$padding$str"
            }
        } else {
            return sign + str + "0".repeat(-_scale)
        }
    }

    override fun scale(): Int = _scale

    override fun precision(): Int {
        val str = intVal.abs().toString()
        return str.length
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KBigDecimalImpl) return false
        return _scale == other._scale && intVal == other.intVal
    }

    override fun hashCode(): Int {
        var result = intVal.hashCode()
        result = 31 * result + _scale
        return result
    }
    
    private fun scaleVal(value: KBigInteger, shift: Int): KBigInteger {
        if (shift == 0) return value
        if (shift < 0) throw IllegalArgumentException("Shift must be non-negative")
        return value.multiply(tenPow(shift))
    }
    
    private fun tenPow(n: Int): KBigInteger {
        return KBigIntegerFactory.TEN.pow(n)
    }
    
    private fun applyRounding(
        quotient: KBigInteger, 
        remainder: KBigInteger, 
        divisor: KBigInteger, 
        mode: RoundingMode
    ): KBigInteger {
        val qSign = quotient.signum()
        
        var increment = false
        
        val absRem = remainder.abs()
        val absDiv = divisor.abs()
        val cmpHalf = absRem.multiply(KBigIntegerFactory.fromInt(2)).compareTo(absDiv)
        
        val isPositive = (qSign >= 0)
        
        when (mode) {
            RoundingMode.UP -> increment = true
            RoundingMode.DOWN -> increment = false
            RoundingMode.CEILING -> increment = isPositive
            RoundingMode.FLOOR -> increment = !isPositive
            RoundingMode.HALF_UP -> increment = cmpHalf >= 0
            RoundingMode.HALF_DOWN -> increment = cmpHalf > 0
            RoundingMode.HALF_EVEN -> {
                if (cmpHalf > 0) {
                    increment = true
                } else if (cmpHalf == 0) {
                    val two = KBigIntegerFactory.fromInt(2)
                    val isOdd = !quotient.mod(two).isZero()
                    increment = isOdd
                }
            }
            RoundingMode.UNNECESSARY -> {}
        }
        
        if (increment) {
            return if (isPositive) quotient.add(KBigIntegerFactory.ONE) 
                   else quotient.subtract(KBigIntegerFactory.ONE)
        }
        return quotient
    }
}

