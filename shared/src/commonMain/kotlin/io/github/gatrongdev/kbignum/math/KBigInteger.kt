package io.github.gatrongdev.kbignum.math

/**
 * arbitrary-precision integer arithmetic.
 * Pure Kotlin implementation.
 */
class KBigInteger(
    val signum: Int,
    val magnitude: IntArray
) : Comparable<KBigInteger> {

    companion object {
        val ZERO = KBigInteger(0, IntArray(0))
        val ONE = KBigInteger(1, intArrayOf(1))
        val TEN = KBigInteger(1, intArrayOf(10))
        
        /**
         * Threshold for switching from schoolbook to Karatsuba multiplication.
         * Value is in number of Ints (32-bit words).
         * 80 ints ~= 2560 bits.
         * Tuned to outperform schoolbook at around 3000-4000 bits.
         */
        private const val KARATSUBA_THRESHOLD = 80

        fun fromLong(value: Long): KBigInteger {
            if (value == 0L) return ZERO
            if (value == Long.MIN_VALUE) return fromString("-9223372036854775808")

            val sign = if (value < 0) -1 else 1
            val absVal = kotlin.math.abs(value)

            val high = (absVal ushr 32).toInt()
            val low = absVal.toInt()

            val mag = if (high != 0) {
                intArrayOf(low, high) // Little-endian
            } else {
                intArrayOf(low)
            }

            return KBigInteger(sign, mag)
        }

        fun fromInt(value: Int): KBigInteger {
            if (value == 0) return ZERO
            if (value == Int.MIN_VALUE) return fromLong(value.toLong())

            val sign = if (value < 0) -1 else 1
            val mag = intArrayOf(kotlin.math.abs(value))
            return KBigInteger(sign, mag)
        }

        fun fromString(value: String): KBigInteger {
            if (value.isEmpty()) throw NumberFormatException("Zero length string")

            var cursor = 0
            val sign: Int
            val firstChar = value[0]

            if (firstChar == '-') {
                sign = -1
                cursor = 1
            } else if (firstChar == '+') {
                sign = 1
                cursor = 1
            } else {
                sign = 1
                cursor = 0
            }

            if (cursor >= value.length) throw NumberFormatException("Zero length string")

            if (value == "0" || (cursor == 1 && value.length == 1)) return ZERO // -0 or +0?

            // Optimization: Parse in chunks of 9 digits (fit in Int) and multiply
            // 10^9 = 1000000000
            val radixVal = 1000000000
            val radixBig = fromInt(radixVal)

            var result = ZERO

            // We can process 9 digits at a time
            val len = value.length
            var firstGroupLen = (len - cursor) % 9
            if (firstGroupLen == 0) firstGroupLen = 9

            // Process first group
            val firstChunkStr = value.substring(cursor, cursor + firstGroupLen)
            if (firstChunkStr.startsWith('+') || firstChunkStr.startsWith('-')) throw NumberFormatException("Invalid integer: $value")
            val firstChunk = firstChunkStr.toInt()
            result = fromInt(firstChunk)
            cursor += firstGroupLen

            while (cursor < len) {
                val chunkStr = value.substring(cursor, cursor + 9)
                if (chunkStr.startsWith('+') || chunkStr.startsWith('-')) throw NumberFormatException("Invalid integer: $value")
                val chunk = chunkStr.toInt()
                result = result.multiply(radixBig).add(fromInt(chunk))
                cursor += 9
            }

            return if (sign < 0) result.negate() else result
        }
    }

    /**
     * Converts this KBigInteger to a Long value.
     */
    fun toLong(): Long {
        if (signum == 0) return 0L
        if (magnitude.size > 2) throw ArithmeticException("BigInteger out of Long range")

        val low = magnitude[0].toLong() and 0xFFFFFFFFL
        val high = if (magnitude.size > 1) magnitude[1].toLong() and 0xFFFFFFFFL else 0L

        // Unsigned magnitude check: max magnitude is 2^63 (for MIN_VALUE) or less.
        // 2^63 is high=0x80000000, low=0
        if (high > 0x80000000L) throw ArithmeticException("BigInteger out of Long range")
        if (high == 0x80000000L && low != 0L) throw ArithmeticException("BigInteger out of Long range")

        val res = (high shl 32) or low

        if (signum > 0) {
            if (res < 0) throw ArithmeticException("BigInteger out of Long range") // 2^63 is overflow for positive long
            return res
        } else {
            // Negative: if res (magnitude) is 2^63 (Long.MIN_VALUE as bits), it fits.
            // other magnitudes are < 2^63 (res > 0). -res fits.
            return -res
        }
    }

    infix fun shl(n: Int): KBigInteger {
        if (n == 0) return this
        if (n < 0) return shr(-n)
        return KBigInteger(signum, shiftLeft(magnitude, n))
    }

    infix fun shr(n: Int): KBigInteger {
        if (n == 0) return this
        if (n < 0) return shl(-n)

        if (signum > 0) {
            val mag = shiftRight(magnitude, n)
            return if (mag.isEmpty()) ZERO else KBigInteger(1, mag)
        } else if (signum < 0) {
             // Negative right shift (arithmetic shift)
             // Result = floor(this / 2^n)
             // Use divide logic to ensure correct rounding
             // Optimization: can use bit manipulation but complex to handle 2's complement simulation.
             // Using division is safer but slower.
             // But we cannot use default division logic which truncates towards zero.
             // We need floor.
             // But since we don't have existing powerOfTwo helper efficiently exposed,
             // let's just use shiftRight on magnitude and adjust.

             // (-M) >> n.
             // M = q * 2^n + r.
             // -M = -q * 2^n - r.
             // -M / 2^n = -q with remainder -r.
             // Floor(-q - r/2^n).
             // If r == 0, floor(-q) = -q.
             // If r != 0, floor(-q - eps) = -q - 1.

             val qMag = shiftRight(magnitude, n)
             // Check if any bits were shifted out (remainder != 0).
             // shiftRight doesn't tell us remainder.
             // We can check low 'n' bits of magnitude.
             val hasRemainder =  testLowBits(n)

             val q = if (qMag.isEmpty()) ZERO else KBigInteger(-1, qMag)
             return if (hasRemainder) q.subtract(ONE) else q
        }
        return ZERO
    }

    private fun testLowBits(n: Int): Boolean {
        // Checks if any of the lowest n bits are set
        if (n == 0) return false
        val wordIndex = n / 32
        val bitIndex = n % 32

        // check full words
        for (i in 0 until wordIndex) {
            if (i < magnitude.size && magnitude[i] != 0) return true
        }
        // check partial word
        if (wordIndex < magnitude.size) {
            val mask = (1 shl bitIndex) - 1
            if ((magnitude[wordIndex] and mask) != 0) return true
        }
        return false
    }

    private fun shiftRight(mag: IntArray, shift: Int): IntArray {
        if (shift == 0) return mag
        val wordShift = shift / 32
        val bitShift = shift % 32

        val magLen = mag.size
        // Shift by words
        // Result length roughly len - wordShift.
        if (wordShift >= magLen) return IntArray(0)

        val newLen = magLen - wordShift
        val result = IntArray(newLen)

        if (bitShift == 0) {
            for (i in 0 until newLen) result[i] = mag[i + wordShift]
        } else {
             // 32 - bitShift
             val antiShift = 32 - bitShift
             for (i in 0 until newLen) {
                 val high = mag[i + wordShift].toUInt().toLong() ushr bitShift
                 val low = if (i + wordShift + 1 < magLen)
                     (mag[i + wordShift + 1].toLong() and 0xFFFFFFFFL) shl antiShift
                     else 0L
                 // Wait, right shift logic.
                 // We want high bits of current word to move lower.
                 // And low bits of NEXT word (higher significance) to move into high bits of THIS word.
                 // current dest[i] corresponds to source[i+wordShift]
                 // dest[i] should combine source[i+wordShift] >>> bitShift  |  source[i+wordShift+1] << (32 - bitShift)

                 val current = mag[i + wordShift].toUInt().toLong()
                 val next = if (i + wordShift + 1 < magLen) mag[i + wordShift + 1].toUInt().toLong() else 0L

                 val val1 = current ushr bitShift
                 val val2 = next shl antiShift

                 result[i] = (val1 or val2).toInt()
             }
        }

        return stripZeros(result)
    }

    override fun toString(): String {
        if (signum == 0) return "0"

        // Simple implementation: repeated division by 10^9 (fit in Int) for efficiency
        val sb = StringBuilder()
        var currentMag = magnitude
        val radix = 1_000_000_000 // 10^9

        // We work with absolute magnitude
        // While currentMag > 0
        while (currentMag.isNotEmpty() && !(currentMag.size == 1 && currentMag[0] == 0)) {
            val (q, r) = divideMagnitude(currentMag, intArrayOf(radix))
            currentMag = q

            val rem = r.firstOrNull() ?: 0
            val remStr = rem.toString()

            sb.append(remStr.reversed())

            if (currentMag.isNotEmpty() && !(currentMag.size == 1 && currentMag[0] == 0)) {
                // Pad with zeros if this isn't the last chunk
                repeat(9 - remStr.length) { sb.append('0') }
            }
        }

        if (signum < 0) {
            sb.append('-')
        }

        return sb.reverse().toString()
    }


    fun add(other: KBigInteger): KBigInteger {
        if (signum == 0) return other
        if (other.signum == 0) return this

        if (signum == other.signum) {
            // Same sign: result = sign * (|this| + |other|)
            val newMag = addMagnitude(magnitude, other.magnitude)
            return KBigInteger(signum, newMag)
        }

        // Different signs: result = larger_mag_sign * (|larger| - |smaller|)
        val cmp = compareMagnitude(other)
        if (cmp == 0) return ZERO

        val newMag = if (cmp > 0) {
            subtractMagnitude(magnitude, other.magnitude)
        } else {
            subtractMagnitude(other.magnitude, magnitude)
        }

        // Result sign follows the operand with larger magnitude
        val resultSign = if (cmp > 0) signum else other.signum
        return KBigInteger(resultSign, newMag)
    }

    fun subtract(other: KBigInteger): KBigInteger {
        if (other.signum == 0) return this
        if (signum == 0) return other.negate()

        if (signum != other.signum) {
            // Different signs: a - (-b) = a + b. Result sign is same as a's sign.
            val newMag = addMagnitude(magnitude, other.magnitude)
            return KBigInteger(signum, newMag)
        }

        // Same signs: a - b.
        val cmp = compareMagnitude(other)
        if (cmp == 0) return ZERO

        val newMag = if (cmp > 0) {
            subtractMagnitude(magnitude, other.magnitude)
        } else {
            subtractMagnitude(other.magnitude, magnitude)
        }

        // If |a| > |b|, sign is a.sign
        // If |a| < |b|, sign is -a.sign (or -other.sign)
        val resultSign = if (cmp > 0) signum else -signum
        return KBigInteger(resultSign, newMag)
    }

    // Helper: Adds two absolute values (little-endian arrays)
    private fun addMagnitude(x: IntArray, y: IntArray): IntArray {
        // Ensure x is the longer array
        val (xMag, yMag) = if (x.size >= y.size) x to y else y to x

        val xLen = xMag.size
        val yLen = yMag.size
        val result = IntArray(xLen + 1) // Potential carry

        var carry: Long = 0

        // Add common parts
        for (i in 0 until yLen) {
            val sum = (xMag[i].toLong() and 0xFFFFFFFFL) + (yMag[i].toLong() and 0xFFFFFFFFL) + carry
            result[i] = sum.toInt()
            carry = sum ushr 32
        }

        // Propagate carry through the rest of x
        for (i in yLen until xLen) {
            val sum = (xMag[i].toLong() and 0xFFFFFFFFL) + carry
            result[i] = sum.toInt()
            carry = sum ushr 32
        }

        // Final carry
        if (carry != 0L) {
            result[xLen] = carry.toInt()
            return result
        }

        // If no final carry, result might be shorter
        return if (result.last() == 0) result.copyOf(xLen) else result
    }

    // Helper: Subtracts smaller magnitude from larger magnitude
    // Precondition: big >= little
    // Optimized: branchless borrow, track last non-zero inline
    private fun subtractMagnitude(big: IntArray, little: IntArray): IntArray {
        val bigLen = big.size
        val littleLen = little.size
        val result = IntArray(bigLen)
        
        var borrow: Long = 0
        var lastNonZero = -1
        
        // Subtract common parts - branchless borrow
        for (i in 0 until littleLen) {
            val diff = (big[i].toLong() and 0xFFFFFFFFL) - (little[i].toLong() and 0xFFFFFFFFL) - borrow
            borrow = (diff ushr 63)
            val digit = (diff + (borrow shl 32)).toInt()
            result[i] = digit
            if (digit != 0) lastNonZero = i
        }
        
        // Propagate borrow - branchless
        for (i in littleLen until bigLen) {
            val diff = (big[i].toLong() and 0xFFFFFFFFL) - borrow
            borrow = (diff ushr 63)
            val digit = (diff + (borrow shl 32)).toInt()
            result[i] = digit
            if (digit != 0) lastNonZero = i
        }
        
        // Return correctly sized array
        return when {
            lastNonZero < 0 -> IntArray(0)
            lastNonZero == bigLen - 1 -> result
            else -> result.copyOf(lastNonZero + 1)
        }
    }
    fun multiply(other: KBigInteger): KBigInteger {
        if (signum == 0 || other.signum == 0) return ZERO

        val newMag = multiplyMagnitude(magnitude, other.magnitude)
        val resultSign = if (signum == other.signum) 1 else -1
        return KBigInteger(resultSign, newMag)
    }

    // Helper: Multiplies two absolute values
    // Uses optimized schoolbook algorithm - O(n²) but low overhead
    // Karatsuba (O(n^1.585)) is only beneficial for extremely large numbers (>10000 bits)
    // and its overhead from recursion/allocations makes it slower for typical use cases
    private fun multiplyMagnitude(x: IntArray, y: IntArray): IntArray {
        if (x.size < KARATSUBA_THRESHOLD || y.size < KARATSUBA_THRESHOLD) {
            return schoolbookMultiply(x, y)
        }
        return karatsubaMultiply(x, y)
    }

    /**
     * Karatsuba multiplication algorithm: O(n^1.585)
     * xy = z2*B^2 + z1*B + z0
     * z2 = x1*y1
     * z0 = x0*y0
     * z1 = (x1+x0)*(y1+y0) - z2 - z0
     */
    private fun karatsubaMultiply(x: IntArray, y: IntArray): IntArray {
        val n = kotlin.math.max(x.size, y.size)
        
        // Fallback to schoolbook if small enough
        if (n < KARATSUBA_THRESHOLD) {
            return schoolbookMultiply(x, y)
        }
        
        // Split position
        val half = (n + 1) / 2
        
        // x = x1 * 2^(32*half) + x0
        // y = y1 * 2^(32*half) + y0
        
        val x0 = getLower(x, half)
        val x1 = getUpper(x, half)
        val y0 = getLower(y, half)
        val y1 = getUpper(y, half)
        
        val p1 = multiplyMagnitude(x1, y1) // z2
        val p2 = multiplyMagnitude(x0, y0) // z0
        
        val xSum = addMagnitude(x0, x1)
        val ySum = addMagnitude(y0, y1)
        
        val p3 = multiplyMagnitude(xSum, ySum) // (x1+x0)*(y1+y0)
        
        // z1 = p3 - p1 - p2
        // Calculate p3 - p1
        val p3MinusP1 = subtractMagnitude(p3, p1)
        // Calculate (p3 - p1) - p2
        val z1 = subtractMagnitude(p3MinusP1, p2)
        
        // result = p1 << (2*half*32) + z1 << (half*32) + p2
        // Note: shiftLeft creates new arrays.
        // Optimized assembly:
        // Result buffer size
        val resultFn = IntArray(x.size + y.size)
        // Copy p2 into low part
        p2.copyInto(resultFn)
        
        // Add z1 shifted by half words
        addWithOffset(resultFn, z1, half)
        
        // Add p1 shifted by 2*half words
        addWithOffset(resultFn, p1, 2 * half)
        
        return stripZeros(resultFn)
    }
    
    private fun getLower(a: IntArray, n: Int): IntArray {
        if (a.size <= n) return a
        // Lower n words
        val result = IntArray(n)
        for (i in 0 until n) result[i] = a[i]
        // Strip zeros not strictly needed for correctness but good for canonical form
        return stripZeros(result)
    }

    private fun getUpper(a: IntArray, n: Int): IntArray {
        if (a.size <= n) return IntArray(0)
        val len = a.size - n
        val result = IntArray(len)
        for (i in 0 until len) result[i] = a[i + n]
        return stripZeros(result)
    }
    
    // Adds source to dest starting at word offset `offset`
    private fun addWithOffset(dest: IntArray, source: IntArray, offset: Int) {
        var carry: Long = 0
        for (i in source.indices) {
            if (offset + i >= dest.size) break // Should not happen if size calc is correct
            val sum = (dest[offset + i].toLong() and 0xFFFFFFFFL) + (source[i].toLong() and 0xFFFFFFFFL) + carry
            dest[offset + i] = sum.toInt()
            carry = sum ushr 32
        }
        // Propagate carry
        var i = offset + source.size
        while (carry != 0L && i < dest.size) {
            val sum = (dest[i].toLong() and 0xFFFFFFFFL) + carry
            dest[i] = sum.toInt()
            carry = sum ushr 32
            i++
        }
    }

    
    /**
     * Schoolbook O(n²) multiplication - fast for small numbers
     */
    private fun schoolbookMultiply(x: IntArray, y: IntArray): IntArray {
        val xLen = x.size
        val yLen = y.size
        val result = IntArray(xLen + yLen)

        for (i in 0 until xLen) {
            val xVal = x[i].toLong() and 0xFFFFFFFFL
            var carry: Long = 0
            for (j in 0 until yLen) {
                val yVal = y[j].toLong() and 0xFFFFFFFFL
                val product = xVal * yVal + (result[i + j].toLong() and 0xFFFFFFFFL) + carry
                result[i + j] = product.toInt()
                carry = product ushr 32
            }
            result[i + yLen] = carry.toInt()
        }

        return stripZeros(result)
    }


    fun divide(other: KBigInteger): KBigInteger {
        if (other.signum == 0) throw ArithmeticException("Division by zero")
        if (signum == 0) return ZERO

        val resultSign = if (signum == other.signum) 1 else -1
        val cmp = compareMagnitude(other)
        if (cmp < 0) return ZERO
        if (cmp == 0) return if (resultSign == 1) ONE else KBigInteger(-1, intArrayOf(1))

        val (quotientMag, _) = divideMagnitude(magnitude, other.magnitude)
        return if (quotientMag.isEmpty()) ZERO else KBigInteger(resultSign, quotientMag)
    }

    fun mod(other: KBigInteger): KBigInteger {
        if (other.signum() == 0) throw ArithmeticException("Division by zero")
        if (signum == 0) return ZERO

        val cmp = compareMagnitude(other)
        if (cmp < 0) return this
        if (cmp == 0) return ZERO

        val (_, remainderMag) = divideMagnitude(magnitude, other.magnitude)
        return if (remainderMag.isEmpty()) ZERO else KBigInteger(signum, remainderMag)
    }

    /**
     * Calculates this KBigInteger raised to the power of the specified integer exponent.
     * Uses binary exponentiation for efficiency.
     */
    /**
     * Calculates this KBigInteger raised to the power of the specified integer exponent.
     * Uses binary exponentiation for efficiency.
     */
    fun pow(exponent: Int): KBigInteger {
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

    /**
     * Returns the bitwise NOT of this value.
     * ~x = -x - 1
     */
    fun not(): KBigInteger {
        return this.negate().subtract(ONE)
    }

    /**
     * Returns the bitwise AND of this value and [other].
     * (this & other)
     */
    fun and(other: KBigInteger): KBigInteger = bitwiseOp(other) { a, b -> a and b }

    /**
     * Returns the bitwise OR of this value and [other].
     * (this | other)
     */
    fun or(other: KBigInteger): KBigInteger = bitwiseOp(other) { a, b -> a or b }

    /**
     * Returns the bitwise XOR of this value and [other].
     * (this ^ other)
     */
    fun xor(other: KBigInteger): KBigInteger = bitwiseOp(other) { a, b -> a xor b }

    /**
     * Returns the bitwise AND NOT of this value and [other].
     * (this & ~other)
     */
    fun andNot(other: KBigInteger): KBigInteger = this.and(other.not())

    /**
     * Helper for bitwise operations simulating Two's Complement.
     */
    private inline fun bitwiseOp(other: KBigInteger, op: (Int, Int) -> Int): KBigInteger {
        if (signum == 0 && other.signum == 0) return ZERO // 0 op 0

        // Determine result sign ("infinite" sign bit)
        // Pos (0) / Neg (1)
        val signA = if (signum < 0) -1 else 0
        val signB = if (other.signum < 0) -1 else 0
        
        // Result sign bit: op(-1, -1) -> ?
        // We use -1 to represent infinite 1s, 0 to represent infinite 0s
        val resultSignBit = op(signA, signB) 
        // if resultSignBit is 0, result is positive. If -1 (all 1s), result is negative.
        val resultNegative = (resultSignBit != 0)

        // Length needed: max magnitude + 1 (for sign bit safety, though loop usually handles it)
        val len = kotlin.math.max(this.magnitude.size, other.magnitude.size) + 1
        val resultMag = IntArray(len)
        
        // State for handling finding "first non-zero" for 2's complement conversion
        // For -M, 2's comp is (~M + 1).
        // This is equivalent to: from LSB, 0s stay 0, first non-zero k becomes -k (aka ~k + 1), subsequent w become ~w.
        
        // We pre-calculate "first non-zero index" for negative numbers to optimize the loop
        val diffA = if (signum < 0) getFirstNonZeroIndex(magnitude) else -1
        val diffB = if (other.signum < 0) getFirstNonZeroIndex(other.magnitude) else -1
        
        for (i in 0 until len) {
            val a = getVirtualTwosCompWord(this.magnitude, signum, i, diffA)
            val b = getVirtualTwosCompWord(other.magnitude, other.signum, i, diffB)
            
            resultMag[i] = op(a, b)
        }
        
        // If result is negative, we have R (Two's Comp) and need M (Magnitude).
        // Val = -M. TwoComp(Val) = R.
        // R = inv(M) + 1  => M = inv(R - 1).
        // Algorithm for inv(R - 1):
        // R - 1 is: from LSB, 0s become 1s (borrow), first non-zero k becomes k-1. Subsequent unchanged.
        // Then invert. 
        // Equivalent to: From LSB, if R[i] is 0, M[i] = inv(1s) = 0? No.
        // Let's use the same "first non-zero" logic in reverse?
        // Actually, if we treat R as the 2's comp representation, we want to find -Val.
        // -Val in 2's comp is mathematically -R (arithmetic negation of word array).
        // And -R corresponds to magnitude M directly? No.
        // If Val is negative, Val = -|Val|. R represents -|Val|.
        // We want |Val|.
        // |Val| = -(-|Val|) = - (Value of R).
        // So we just need to perform arithmetic negation of the array R to get M.
        // Negation of R: ~R + 1.
        
        if (resultNegative) {
            // Apply 2's complement negation: ~R + 1
            // Logic: Flip bits, then add 1.
            // Or use the "Twos Complement word" logic again:
            // First non-zero word w -> -w. Subsequent -> ~w. Leading 0s -> 0.
            
            // Note: R might have infinite leading 1s (since resultNegative=true).
            // We only computed 'len' words. The 'virtual' higher words are all -1 (0xFFFFFFFF).
            // Arithmetic negation of ...1111 is ...0000 + 1. 
            // So if we just negate the computed words properly, high words become 0, which is correct for Magnitude.
            
            var carry = 1
            for (i in 0 until len) {
                val r = resultMag[i]
                // ~r + carry
                val inv = r.inv()
                val sum = (inv.toLong() and 0xFFFFFFFFL) + carry
                resultMag[i] = sum.toInt()
                carry = (sum ushr 32).toInt()
            }
            // Carry usually 0 after processing, unless we expand? 
            // If result is power of 2, magnitude might grow?
            // "len" was max+1. If we have carry out, we might need extensions.
            // However, result of bitwise ops usually bounded.
            // AND: bounded by smaller mag.
            // OR: bounded by larger mag.
            // XOR: bounded by larger mag.
            // If Negative, magnitude can be larger.
        }
        
        val finalMag = stripZeros(resultMag)
        return if (finalMag.isEmpty()) ZERO
               else KBigInteger(if (resultNegative) -1 else 1, finalMag)
    }


    private fun getFirstNonZeroIndex(mag: IntArray): Int {
        for (i in mag.indices) {
            if (mag[i] != 0) return i
        }
        return -1
    }

    private fun getVirtualTwosCompWord(mag: IntArray, sign: Int, index: Int, firstNonZeroIndex: Int): Int {
        if (index >= mag.size) {
            // Virtual sign extension
            return if (sign < 0) -1 else 0
        }
        val w = mag[index]
        if (sign >= 0) return w
        
        // Negative logic: inv(M) + 1
        // Using precomputed firstNonZeroIndex
        // If index < firstNonZeroIndex => word is 0.
        // If index == firstNonZeroIndex => word is -w (which is ~w + 1).
        // If index > firstNonZeroIndex => word is ~w.
        
        // Wait, if w is 0 (below firstNonZero), it stays 0.
        // Check: M = ... 1 0 0. inv = ... 0 1 1. +1 = ... 1 0 0. 
        // Correct.
        
        return if (index < firstNonZeroIndex) {
            0
        } else if (index == firstNonZeroIndex) {
            -w
        } else {
            w.inv()
        }
    }


    
    /**
     * Returns both quotient and remainder in a single operation.
     * More efficient than calling divide() and mod() separately.
     */
    fun divideAndRemainder(other: KBigInteger): Pair<KBigInteger, KBigInteger> {
        if (other.signum == 0) throw ArithmeticException("Division by zero")
        if (signum == 0) return ZERO to ZERO

        val resultSign = if (signum == other.signum) 1 else -1
        val cmp = compareMagnitude(other)
        if (cmp < 0) return ZERO to this
        if (cmp == 0) {
            val q = if (resultSign == 1) ONE else KBigInteger(-1, intArrayOf(1))
            return q to ZERO
        }

        val (quotientMag, remainderMag) = divideMagnitude(magnitude, other.magnitude)
        val quotient = if (quotientMag.isEmpty()) ZERO else KBigInteger(resultSign, quotientMag)
        val remainder = if (remainderMag.isEmpty()) ZERO else KBigInteger(signum, remainderMag)
        return quotient to remainder
    }

    // Helper: Returns pair of (Quotient, Remainder) magnitudes
    // Uses Knuth's Algorithm D (simplified for base 2^32)
    private fun divideMagnitude(u: IntArray, v: IntArray): Pair<IntArray, IntArray> {
        // Base case: simple division if v is single digit
        if (v.size == 1) {
             val vLong = v[0].toUInt().toULong()
             var rem: ULong = 0u
             val q = IntArray(u.size)
             for (i in u.lastIndex downTo 0) {
                 val dividend = (rem shl 32) or u[i].toUInt().toULong()
                 q[i] = (dividend / vLong).toInt()
                 rem = dividend % vLong
             }
             // Strip zeros from q
             var qLen = q.size
             while (qLen > 0 && q[qLen-1] == 0) qLen--
             val qRes = if (qLen == 0) IntArray(0) else q.copyOf(qLen)

             val rRes = if (rem == 0uL) IntArray(0) else intArrayOf(rem.toInt())
             return qRes to rRes
        }

        return knuthDivide(u, v)
    }

    /**
     * Knuth's Algorithm D for multi-word division.
     * Works with base 2^32 (each Int is one "digit").
     * Much faster than bitwise division: O(n) word iterations vs O(n*32) bit iterations.
     */
    private fun knuthDivide(uMag: IntArray, vMag: IntArray): Pair<IntArray, IntArray> {
        val n = vMag.size
        val m = uMag.size - n
        
        if (m < 0) {
            return IntArray(0) to uMag.copyOf()
        }

        // Step 1: Normalize - shift so that v[n-1] >= 2^31 (MSB is set)
        val shift = numberOfLeadingZeros(vMag[n - 1])
        
        // Create normalized copies
        val v = if (shift > 0) {
            shiftLeftInWords(vMag, shift)
        } else {
            vMag.copyOf()
        }
        
        // u needs one extra word for potential overflow during normalization
        val u = IntArray(uMag.size + 1)
        if (shift > 0) {
            var carry = 0
            for (i in uMag.indices) {
                val newVal = (uMag[i].toLong() and 0xFFFFFFFFL) shl shift
                u[i] = (newVal.toInt() or carry)
                carry = (newVal ushr 32).toInt()
            }
            u[uMag.size] = carry
        } else {
            uMag.copyInto(u)
        }
        
        // Quotient array
        val q = IntArray(m + 1)
        
        // Step 2: Main loop - calculate each quotient digit
        for (j in m downTo 0) {
            // Step 2a: Estimate quotient digit qHat
            val uH = u[j + n].toLong() and 0xFFFFFFFFL
            val uL = u[j + n - 1].toLong() and 0xFFFFFFFFL
            val vH = v[n - 1].toLong() and 0xFFFFFFFFL
            
            var qHat: Long
            var rHat: Long
            
            if (uH == vH) {
                qHat = 0xFFFFFFFFL
                rHat = uH + uL
            } else {
                // Use unsigned division via ULong
                val dividendU = ((uH.toULong() shl 32) or uL.toULong())
                val vHU = vH.toULong()
                qHat = (dividendU / vHU).toLong()
                rHat = (dividendU % vHU).toLong()
            }
            
            // Step 2b: Refine qHat estimate
            if (n >= 2 && qHat > 0) {
                val vL = v[n - 2].toLong() and 0xFFFFFFFFL
                val uLL = u[j + n - 2].toLong() and 0xFFFFFFFFL
                
                // Use ULong for unsigned comparisons
                while (qHat.toULong() >= 0x100000000uL || 
                       (qHat.toULong() * vL.toULong()) > ((rHat.toULong() shl 32) or uLL.toULong())) {
                    qHat--
                    rHat += vH
                    if (rHat.toULong() >= 0x100000000uL) break
                }
            }
            
            // Step 2c: Multiply and subtract: u[j..j+n] -= qHat * v[0..n-1]
            val borrow = mulSub(u, v, qHat.toInt(), n, j)
            
            // Step 2d: If we borrowed too much, add back
            if (borrow < 0) {
                qHat--
                addBack(u, v, n, j)
            }
            
            q[j] = qHat.toInt()
        }
        
        // Step 3: Denormalize remainder
        val remainder = if (shift > 0) {
            val r = IntArray(n)
            for (i in 0 until n) {
                val lo = (u[i].toLong() and 0xFFFFFFFFL) ushr shift
                val hi = if (i + 1 < u.size) (u[i + 1].toLong() and 0xFFFFFFFFL) shl (32 - shift) else 0L
                r[i] = (lo or hi).toInt()
            }
            r
        } else {
            u.copyOf(n)
        }
        
        return stripZeros(q) to stripZeros(remainder)
    }
    
    /**
     * Multiply-subtract: u[offset..offset+len] -= q * v[0..len-1]
     * Returns the final borrow (negative if underflow occurred)
     */
    private fun mulSub(u: IntArray, v: IntArray, q: Int, len: Int, offset: Int): Long {
        val qLong = q.toLong() and 0xFFFFFFFFL
        var carry: Long = 0
        
        for (i in 0 until len) {
            val product = qLong * (v[i].toLong() and 0xFFFFFFFFL) + carry
            val diff = (u[offset + i].toLong() and 0xFFFFFFFFL) - (product and 0xFFFFFFFFL)
            u[offset + i] = diff.toInt()
            carry = (product ushr 32) + (if (diff < 0) 1 else 0)
        }
        
        val finalDiff = (u[offset + len].toLong() and 0xFFFFFFFFL) - carry
        u[offset + len] = finalDiff.toInt()
        return finalDiff
    }
    
    /**
     * Add back: u[offset..offset+len] += v[0..len-1]
     * Used when qHat was overestimated
     */
    private fun addBack(u: IntArray, v: IntArray, len: Int, offset: Int) {
        var carry: Long = 0
        for (i in 0 until len) {
            val sum = (u[offset + i].toLong() and 0xFFFFFFFFL) + 
                      (v[i].toLong() and 0xFFFFFFFFL) + carry
            u[offset + i] = sum.toInt()
            carry = sum ushr 32
        }
        u[offset + len] = (u[offset + len].toLong() + carry).toInt()
    }
    
    /**
     * Shift left within word array (in-place style but returns new array)
     */
    private fun shiftLeftInWords(mag: IntArray, shift: Int): IntArray {
        if (shift == 0) return mag.copyOf()
        val result = IntArray(mag.size)
        var carry = 0
        for (i in mag.indices) {
            val newVal = (mag[i].toLong() and 0xFFFFFFFFL) shl shift
            result[i] = (newVal.toInt() or carry)
            carry = (newVal ushr 32).toInt()
        }
        return result
    }

    // Helper: Compare magnitude of two arrays (assuming signed logic=0 handling)
    private fun compareArrays(a: IntArray, b: IntArray): Int {
        val aLen = realSize(a)
        val bLen = realSize(b)
        if (aLen != bLen) return aLen - bLen
        for (i in aLen - 1 downTo 0) {
           val av = a[i].toUInt().toLong()
           val bv = b[i].toUInt().toLong()
           if (av != bv) return if (av < bv) -1 else 1
        }
        return 0
    }

    private fun realSize(mag: IntArray): Int {
        var i = mag.size
        while (i > 0 && mag[i-1] == 0) i--
        return i
    }

    // Helper: In-place subtract a -= b. Assumes a >= b.
    private fun subtractInPlace(a: IntArray, b: IntArray) {
        // a and b might look different sizes but 'a' handles the storage.
        // b might be smaller or same size (stripped).

        var borrow: ULong = 0u
        val len = a.size
        // Iterate through all of 'a' because borrow might propagate
        for (i in 0 until len) {
            val bv = if (i < b.size) b[i].toUInt().toULong() else 0uL
            val av = a[i].toUInt().toULong()

            // diff = av - bv - borrow
            // logic with unsigned:
            // if av >= bv + borrow, no new borrow.
            // else borrow 1 from next.

            val sub = bv + borrow
            var diff = av
            if (av < sub) {
                diff = av + 0x100000000uL - sub
                borrow = 1u
            } else {
                diff = av - sub
                borrow = 0u
            }
            a[i] = diff.toInt()
        }
    }

    private fun setBit(mag: IntArray, bitIndex: Int) {
        val wordIndex = bitIndex / 32
        val bitOffset = bitIndex % 32
        if (wordIndex < mag.size) {
            mag[wordIndex] = mag[wordIndex] or (1 shl bitOffset)
        }
    }

    private fun stripZeros(mag: IntArray): IntArray {
        var len = mag.size
        while (len > 0 && mag[len-1] == 0) len--
        return if (len == 0) IntArray(0) else mag.copyOf(len)
    }

    private fun shiftLeft(mag: IntArray, shift: Int): IntArray {
        if (shift == 0) return mag
        val wordShift = shift / 32
        val bitShift = shift % 32

        val newLen = mag.size + wordShift + (if (bitShift > 0) 1 else 0)
        val result = IntArray(newLen)

        // Copy with shift
        if (bitShift == 0) {
            for (i in mag.indices) result[i + wordShift] = mag[i]
        } else {
            var carry = 0
            val limit = mag.size
            for (i in 0 until limit) {
                val `val` = mag[i]
                val shifted = (`val` shl bitShift) or carry
                result[i + wordShift] = shifted
                carry = `val` ushr (32 - bitShift)
            }
            if (carry != 0) {
                result[limit + wordShift] = carry
            }
        }
        return stripZeros(result)
    }

    private fun bitLength(mag: IntArray): Int {
         if (mag.isEmpty()) return 0
         val len = (mag.size - 1) * 32
         val msb = 32 - numberOfLeadingZeros(mag.last())
         return len + msb
    }


    fun abs(): KBigInteger = if (signum < 0) KBigInteger(1, magnitude) else this
    fun signum(): Int = signum

    fun negate(): KBigInteger {
        if (signum == 0) return this
        return KBigInteger(-signum, magnitude)
    }

    override fun compareTo(other: KBigInteger): Int {
        if (signum != other.signum) return signum.compareTo(other.signum)
        if (signum == 0) return 0

        // Same sign
        val magCompare = compareMagnitude(other)
        return if (signum > 0) magCompare else -magCompare
    }

    private fun compareMagnitude(other: KBigInteger): Int {
        if (magnitude.size != other.magnitude.size) {
            return magnitude.size.compareTo(other.magnitude.size)
        }
        // Compare from most significant int (end of array in little-endian)
        for (i in magnitude.lastIndex downTo 0) {
            val a = magnitude[i].toLong() and 0xFFFFFFFFL
            val b = other.magnitude[i].toLong() and 0xFFFFFFFFL
            if (a != b) return a.compareTo(b)
        }
        return 0
    }

    fun isZero(): Boolean = signum == 0
    fun isPositive(): Boolean = signum > 0
    fun isNegative(): Boolean = signum < 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KBigInteger) return false
        if (signum != other.signum) return false
        return magnitude.contentEquals(other.magnitude)
    }

    fun testBit(n: Int): Boolean {
        if (n < 0) throw ArithmeticException("Negative bit address")
        val intIndex = n / 32
        val bitIndex = n % 32
        if (intIndex >= magnitude.size) return signum < 0 // Implicit sign extension for negative numbers?
        // Java BigInteger.testBit operates on two's complement.
        // My KBigInteger is Sign-Magnitude.
        // This is tricky. Rounding HALF_EVEN usually applies to the MAGNITUDE of the quotient if we treat it as positive?
        // Actually, Java BigInteger `divide` returns a result where `testBit(0)` checks key bit.
        // If my `q` is computed as magnitude then applied sign, `testBit(0)` on `q` (which might be negative) should be "is value odd?".

        // For rounding logic in divide, we usually round the MAGNITUDE.
        // "q.testBit(0)" is used to check if the last digit is odd.
        // If q is negative, -3 is "odd". -2 is "even".
        // So checking magnitude[0] & 1 is sufficient regardless of sign for parity.
        return (magnitude.getOrElse(intIndex) { 0 } and (1 shl bitIndex)) != 0
    }

    private fun numberOfLeadingZeros(i: Int): Int {
        if (i == 0) return 32
        var n = 1
        var x = i
        if (x ushr 16 == 0) { n += 16; x = x shl 16 }
        if (x ushr 24 == 0) { n += 8; x = x shl 8 }
        if (x ushr 28 == 0) { n += 4; x = x shl 4 }
        if (x ushr 30 == 0) { n += 2; x = x shl 2 }
        n -= x ushr 31
        return n
    }

    override fun hashCode(): Int {
        var result = signum
        result = 31 * result + magnitude.contentHashCode()
        return result
    }
}
