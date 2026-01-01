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
    private fun subtractMagnitude(big: IntArray, little: IntArray): IntArray {
        val bigLen = big.size
        val littleLen = little.size
        val result = IntArray(bigLen)

        var borrow: Long = 0

        // Subtract common parts
        for (i in 0 until littleLen) {
            var diff = (big[i].toLong() and 0xFFFFFFFFL) - (little[i].toLong() and 0xFFFFFFFFL) - borrow
            if (diff < 0) {
                diff += 0x100000000L
                borrow = 1
            } else {
                borrow = 0
            }
            result[i] = diff.toInt()
        }

        // Propagate borrow
        for (i in littleLen until bigLen) {
            var diff = (big[i].toLong() and 0xFFFFFFFFL) - borrow
            if (diff < 0) {
                diff += 0x100000000L
                borrow = 1
            } else {
                borrow = 0
            }
            result[i] = diff.toInt()
        }

        // Strip leading zeros
        var resultLen = bigLen
        while (resultLen > 0 && result[resultLen - 1] == 0) {
            resultLen--
        }

        if (resultLen == 0) return IntArray(0)
        return result.copyOf(resultLen)
    }
    fun multiply(other: KBigInteger): KBigInteger {
        if (signum == 0 || other.signum == 0) return ZERO

        val newMag = multiplyMagnitude(magnitude, other.magnitude)
        val resultSign = if (signum == other.signum) 1 else -1
        return KBigInteger(resultSign, newMag)
    }

    // Helper: Multiplies two absolute values
    private fun multiplyMagnitude(x: IntArray, y: IntArray): IntArray {
        val xLen = x.size
        val yLen = y.size
        val result = IntArray(xLen + yLen)

        for (i in 0 until xLen) {
            val xVal = x[i].toUInt().toULong()
            var carry: ULong = 0u
            for (j in 0 until yLen) {
                val yVal = y[j].toUInt().toULong()
                val product = xVal * yVal + result[i + j].toUInt().toULong() + carry
                result[i + j] = product.toInt()
                carry = product shr 32 // ULong shift is logical
            }
            result[i + yLen] = carry.toInt()
        }

        // Strip leading zeros
        var resultLen = result.size
        while (resultLen > 0 && result[resultLen - 1] == 0) {
            resultLen--
        }

        return if (resultLen == 0) IntArray(0) else result.copyOf(resultLen)
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
        if (other.signum == 0) throw ArithmeticException("Division by zero")
        if (signum == 0) return ZERO

        val cmp = compareMagnitude(other)
        if (cmp < 0) return this
        if (cmp == 0) return ZERO

        val (_, remainderMag) = divideMagnitude(magnitude, other.magnitude)
        return if (remainderMag.isEmpty()) ZERO else KBigInteger(signum, remainderMag)
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

        return bitwiseDivide(u, v)
    }

    // Generic Bitwise Long Division
    // Slower than Knuth D but easier to ensure correctness without complex estimation logic.
    private fun bitwiseDivide(uMag: IntArray, vMag: IntArray): Pair<IntArray, IntArray> {
        val uBits = bitLength(uMag)
        val vBits = bitLength(vMag)

        if (uBits < vBits) {
             return IntArray(0) to uMag
        }

        // Quotient length in bits = uBits - vBits + 1
        // We construct quotient bit by bit.

        // To avoid excessive array shifting, we can work on a mutable copy of U (remainder)
        // and check against V shifted.
        // But shifting V is expensive.

        // Better: Compare V against top bits of U.
        // Copy U to a working array.
        val rem = uMag.copyOf()
        val qBits = uBits - vBits
        val quotient = IntArray(qBits / 32 + 1)

        // We will shift V left by 'shift' bits to align with U.
        // Instead of shifting V, we can conceptually shift V and subtract from Rem.
        // Or simpler:
        // Create 'divisor' = v << shift
        // But that array is huge.

        // Let's iterate 'i' from qBits down to 0.
        // In each step, we want to know if rem >= (v << i)
        // Comparison (v << i) vs rem is:
        // Compare rem[i_bits ..] vs v.

        // Implementing 'subtractShifted' helper:
        // subtractShifted(rem, v, shiftBits) -> subtracts (v << shiftBits) from rem IF (rem >= v << shiftBits)
        // Returns true if subtracted (q bit = 1), false otherwise.

        // Optimization: Normalize V first to have MSB at bit 31.
        // But let's stick to functional simplicity first.

        // This loop runs 'qBits' times.
        for (i in qBits downTo 0) {
            if (subtractIfGreater(rem, vMag, i)) {
                setBit(quotient, i)
            }
        }

        return stripZeros(quotient) to stripZeros(rem)
    }

    // Checks if rem >= (v << shiftBits). If so, rem -= (v << shiftBits) and returns true.
    private fun subtractIfGreater(rem: IntArray, v: IntArray, shiftBits: Int): Boolean {
        // 1. Comparison
        // We need to compare specific region of 'rem' against 'v'.
        // (v << shiftBits) effectively places v starting at 'shiftBits' position.

        // Shift amount in words and bits
        val shiftWords = shiftBits / 32
        val shiftRem = shiftBits % 32

        // If rem is too small to even contain shifted V
        // highest set bit of (v << shiftBits) is (bitLength(v) - 1) + shiftBits
        // highest set bit of rem is bitLength(rem) - 1
        // But we are mutating rem, so its 'real' bitlength decreases.
        // Checking bounds is tricky. Let's do a direct "simulated shift compare".

        val vLen = v.size
        // Determine the range in 'rem' that corresponds to 'v' shifted.
        // V[0] maps to Rem[shiftWords] (with bit shift)

        // This is complex to get bug-free.
        // Let's use the simplest, dumbest approach:
        // Construct 'shiftedV' = shiftLeft(v, shiftBits).
        // Since i decreases, shiftBits decreases.
        // We allocate 'shiftedV' every time? BAD.

        // Actually, we can allocate ONE big array for V aligned to U's size?

        return subtractIfGreaterSlowAllocator(rem, v, shiftBits)
    }



    // Slow allocator version: straightforward correctness.
    private fun subtractIfGreaterSlowAllocator(rem: IntArray, v: IntArray, shiftBits: Int): Boolean {
        // Construct candidate subtrahend
        val shiftedV = shiftLeft(v, shiftBits)

        if (compareArrays(rem, shiftedV) >= 0) {
            subtractInPlace(rem, shiftedV)
            return true
        }
        return false
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
