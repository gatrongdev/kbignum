// Quick debug test to check specific issues
import io.github.gatrongdev.kbignum.math.*

fun main() {
    println("=== Testing Precision ===")
    val decimal1 = "123.456".toKBigDecimal()
    val decimal2 = "000123.456000".toKBigDecimal()
    val decimal3 = "0.001".toKBigDecimal()
    val zero = KBigDecimalFactory.ZERO

    println("decimal1 precision: ${decimal1.precision()} (expected 6)")
    println("decimal2 precision: ${decimal2.precision()} (expected 9)")
    println("decimal3 precision: ${decimal3.precision()} (expected 1)")
    println("zero precision: ${zero.precision()} (expected 1)")
    
    println("\n=== Testing Scale Operations ===")
    val decimal = "123.456789".toKBigDecimal()
    println("decimal scale: ${decimal.scale()} (expected 6)")
    
    val scaled0 = decimal.setScale(0, 4)
    println("scaled0: $scaled0 scale: ${scaled0.scale()} (expected 0)")
    
    val scaled4 = decimal.setScale(4, 4)
    println("scaled4: $scaled4 scale: ${scaled4.scale()} (expected 4)")
    
    val scaled8 = decimal.setScale(8, 4)
    println("scaled8: $scaled8 scale: ${scaled8.scale()} (expected 8)")
    
    println("\n=== Testing Basic Arithmetic ===")
    val a = "123.45".toKBigDecimal()
    val b = "67.89".toKBigDecimal()
    
    val sum = a + b
    val product = a * b
    val quotient = a.divide(b, 2, 4)
    
    println("$a + $b = $sum (expected 191.34)")
    println("$a * $b = $product (should start with 8381.0)")
    println("$a / $b = $quotient (should start with 1.8)")
}
