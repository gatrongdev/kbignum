# KBigNum - Kotlin Multiplatform Arbitrary Precision Library

[![Kotlin](https://img.shields.io/badge/kotlin-multiplatform-blue.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

KBigNum is a Kotlin Multiplatform library that provides arbitrary precision arithmetic for both decimal and integer numbers. It offers a unified API across Android and iOS platforms, making it easy to perform high-precision mathematical calculations in your multiplatform projects.

## Features

- **Arbitrary Precision Arithmetic**: Handle numbers of any size with precision
- **Multiplatform Support**: Works seamlessly on Android and iOS
- **Type Safety**: Strongly typed API with `KBigDecimal` and `KBigInteger`
- **Operator Overloading**: Natural mathematical syntax (`+`, `-`, `*`, `/`, `%`)
- **Rich Math Operations**: Square root, factorial, GCD, LCM, prime checking, and more
- **Platform Optimized**: Uses Java's BigDecimal/BigInteger on Android and Foundation's NSDecimalNumber on iOS

## Installation

Add the following to your `shared/build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("vn.com.gatrong:kbignum:1.0.0")
        }
    }
}
```

## Quick Start

### KBigDecimal Usage

```kotlin
import vn.com.gatrong.kbignum.math.*

// Create KBigDecimal instances
val a = "123.456".toKBigDecimal()
val b = "789.123".toKBigDecimal()

// Basic arithmetic operations
val sum = a + b
val difference = a - b
val product = a * b
val quotient = a.divide(b, scale = 4) // 4 decimal places

// Comparison and utility functions
val isEqual = a == b
val comparison = a.compareTo(b)
val absolute = a.abs()
val signum = a.signum()
```

### KBigInteger Usage

```kotlin
// Create KBigInteger instances
val x = "123456789012345678901234567890".toKBigInteger()
val y = "987654321098765432109876543210".toKBigInteger()

// Basic arithmetic operations
val sum = x + y
val difference = x - y
val product = x * y
val quotient = x / y
val remainder = x % y

// Advanced operations
val gcd = KBigMath.gcd(x, y)
val lcm = KBigMath.lcm(x, y)
val isPrime = KBigMath.isPrime(x)
```

### Advanced Math Operations

```kotlin
// Mathematical functions
val factorial = KBigMath.factorial(10.toKBigInteger()) // 10!
val power = KBigMath.pow(2.toKBigInteger(), 100.toKBigInteger()) // 2^100
val squareRoot = KBigMath.sqrt(16.toKBigDecimal()) // √16
val isPrime = KBigMath.isPrime(97.toKBigInteger()) // Check if 97 is prime
```

## API Reference

### KBigDecimal

#### Creation
- `String.toKBigDecimal()`: Convert string to KBigDecimal
- `Int.toKBigDecimal()`: Convert int to KBigDecimal
- `Long.toKBigDecimal()`: Convert long to KBigDecimal
- `KBigDecimalFactory.fromString(value: String)`
- `KBigDecimalFactory.fromLong(value: Long)`
- `KBigDecimalFactory.fromInt(value: Int)`

#### Operations
- **Arithmetic**: `add()`, `subtract()`, `multiply()`, `divide()`
- **Comparison**: `compareTo()`, `equals()`
- **Utility**: `abs()`, `signum()`, `setScale()`, `toBigInteger()`
- **Operators**: `+`, `-`, `*`, `/`

### KBigInteger

#### Creation
- `String.toKBigInteger()`: Convert string to KBigInteger
- `Int.toKBigInteger()`: Convert int to KBigInteger
- `Long.toKBigInteger()`: Convert long to KBigInteger
- `KBigIntegerFactory.fromString(value: String)`
- `KBigIntegerFactory.fromLong(value: Long)`
- `KBigIntegerFactory.fromInt(value: Int)`

#### Operations
- **Arithmetic**: `add()`, `subtract()`, `multiply()`, `divide()`, `mod()`
- **Comparison**: `compareTo()`, `equals()`
- **Utility**: `abs()`, `signum()`, `toBigDecimal()`
- **Operators**: `+`, `-`, `*`, `/`, `%`

### KBigMath

Utility class for advanced mathematical operations:

- `sqrt(value: KBigDecimal)`: Square root
- `factorial(n: KBigInteger)`: Factorial
- `pow(base: KBigInteger, exponent: KBigInteger)`: Power
- `gcd(a: KBigInteger, b: KBigInteger)`: Greatest Common Divisor
- `lcm(a: KBigInteger, b: KBigInteger)`: Least Common Multiple
- `isPrime(n: KBigInteger)`: Prime number check

## Platform Implementation

### Android
- Uses Java's `java.math.BigDecimal` and `java.math.BigInteger`
- Full precision and rounding mode support
- Optimized for Android's JVM environment

### iOS
- Uses Foundation's `NSDecimalNumber` for decimal operations
- Uses `NSNumber` with string conversion for integer operations
- Native iOS performance with Objective-C interop

## Rounding Modes

KBigDecimal supports various rounding modes for division and scaling operations:

- `0` - ROUND_UP
- `1` - ROUND_DOWN
- `2` - ROUND_CEILING
- `3` - ROUND_FLOOR
- `4` - ROUND_HALF_UP
- `5` - ROUND_HALF_DOWN
- `6` - ROUND_HALF_EVEN
- `7` - ROUND_UNNECESSARY

## Error Handling

The library provides proper error handling for:
- Invalid number formats
- Division by zero
- Overflow conditions
- Platform-specific limitations

## Testing

The library includes comprehensive test suites for both platforms:

```bash
./gradlew test                    # Run all tests
./gradlew :shared:allTests       # Run multiplatform tests
```

## Example Projects

Check out the `demo.kt` file for a complete demonstration of the library's capabilities.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Requirements

- Kotlin 1.9.0 or higher
- Android API level 21 or higher
- iOS 13.0 or higher

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For questions, issues, or contributions, please visit our [GitHub repository](https://github.com/yourusername/KBigNum).

---

Made with ❤️ using Kotlin Multiplatform
