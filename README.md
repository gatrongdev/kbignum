# KBigNum

[![Kotlin](https://img.shields.io/badge/kotlin-multiplatform-blue.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.gatrongdev/kbignum.svg)](https://central.sonatype.com/artifact/io.github.gatrongdev/kbignum)
[![License](https://img.shields.io/badge/license-Apache%202.0-green.svg)](LICENSE)
[![Build Status](https://img.shields.io/github/actions/workflow/status/gatrongdev/kbignum/ci.yml?branch=main)](https://github.com/gatrongdev/kbignum/actions)
[![Test Coverage](https://img.shields.io/badge/coverage-brightgreen.svg)](https://github.com/gatrongdev/kbignum)
[![API Documentation](https://img.shields.io/badge/docs-latest-blue.svg)](https://gatrongdev.github.io/kbignum/)


A Kotlin Multiplatform library for arbitrary precision mathematics, providing unified APIs for high-precision arithmetic operations across Android and iOS platforms.

## Features

- **Arbitrary Precision Arithmetic**: Handle numbers with unlimited precision using `KBigDecimal` and `KBigInteger`
- **Multiplatform Support**: Single codebase works seamlessly on Android and iOS
- **Type-Safe API**: Strongly typed interfaces with compile-time safety
- **Natural Syntax**: Operator overloading for intuitive mathematical expressions
- **Platform Optimized**: Uses native implementations (Java BigDecimal/BigInteger on Android, Foundation on iOS)
- **Comprehensive Math Operations**: Basic arithmetic, advanced functions, and utility operations

## Installation

### Gradle (Kotlin DSL)

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.gatrongdev:kbignum:0.0.17")
}
```

### Gradle (Groovy)

Add to your `build.gradle`:

```gradle
dependencies {
    implementation 'io.github.gatrongdev:kbignum:0.0.17'
}
```

## Quick Start

### Basic Usage

```kotlin
import io.github.gatrongdev.kbignum.math.math.*

// Creating big numbers
val bigDecimal1 = "123.456789".toKBigDecimal()
val bigDecimal2 = KBigDecimalFactory.create("987.654321")
val bigInteger = "12345678901234567890".toKBigInteger()

// Basic arithmetic
val sum = bigDecimal1 + bigDecimal2
val difference = bigDecimal1 - bigDecimal2
val product = bigDecimal1 * bigDecimal2

// Division with automatic scale
val simpleQuotient = bigDecimal1.divide(bigDecimal2)

// Division with predefined strategies (v0.0.17+)
val currencyResult = bigDecimal1.divide(bigDecimal2, DivisionStrategy.CURRENCY)
val preciseResult = bigDecimal1.divide(bigDecimal2, DivisionStrategy.SCIENTIFIC)

// Division with custom configuration
val customResult = bigDecimal1.divide(bigDecimal2, DivisionConfig(scale = 10, rounding = KBRoundingMode.HalfUp))

// Advanced operations
val sqrt = KBigMath.sqrt(bigDecimal1, 10)
val factorial = KBigMath.factorial(bigInteger)
val isPrime = KBigMath.isPrime(bigInteger)
```

### Extension Functions

```kotlin
// Convert from various types
val fromString = "999.999".toKBigDecimal()
val fromInt = 42.toKBigInteger()
val fromLong = 1234567890L.toKBigDecimal()

// Utility functions
val isZero = bigDecimal1.isZero()
val isPositive = bigDecimal1.isPositive()
val absolute = bigDecimal1.abs()
val sign = bigDecimal1.signum()
```

### Advanced Mathematics

```kotlin
// Mathematical operations
val gcd = KBigMath.gcd(bigInteger1, bigInteger2)
val lcm = KBigMath.lcm(bigInteger1, bigInteger2)
val power = KBigMath.pow(bigDecimal1, 5)

// Precision control
val scaled = bigDecimal1.setScale(5, KBRoundingMode.HalfUp)
val precision = bigDecimal1.precision()

// Division strategies (v0.0.17+)
val price = "100.00".toKBigDecimal()
val quantity = "3".toKBigDecimal()

// Use predefined strategies
val pricePerItem = price.divide(quantity, DivisionStrategy.CURRENCY)       // 33.33
val exchangeRate = price.divide(quantity, DivisionStrategy.EXCHANGE_RATE)  // 33.3333
val cryptoAmount = price.divide(quantity, DivisionStrategy.CRYPTOCURRENCY) // 33.33333333

// Use precision scale constants
val interestRate = price.divide(quantity, PrecisionScale.INTEREST_RATE, KBRoundingMode.HalfUp)
```

## API Reference

### KBigDecimal

Interface for arbitrary precision decimal numbers:

- `add(other: KBigDecimal): KBigDecimal`
- `subtract(other: KBigDecimal): KBigDecimal`
- `multiply(other: KBigDecimal): KBigDecimal`
- `divide(other: KBigDecimal): KBigDecimal`
- `divide(other: KBigDecimal, scale: Int): KBigDecimal`
- `divide(other: KBigDecimal, scale: Int, mode: Int): KBigDecimal`
- `divide(other: KBigDecimal, config: DivisionConfig): KBigDecimal` *(v0.0.17+)*
- `abs(): KBigDecimal`
- `signum(): Int`
- `setScale(scale: Int, rounding: KBRoundingMode): KBigDecimal`
- `setScale(scale: Int, roundingMode: Int): KBigDecimal`

### Division Helpers (v0.0.17+)

**PrecisionScale** - Constants for common decimal places:
- `CURRENCY` (2), `EXCHANGE_RATE` (4), `PERCENTAGE` (2)
- `SCIENTIFIC` (10), `HIGH_PRECISION` (20), `INTEREST_RATE` (6)
- `CRYPTOCURRENCY` (8)

**DivisionStrategy** - Predefined strategies:
- `CURRENCY`, `FINANCIAL`, `EXCHANGE_RATE`, `PERCENTAGE`
- `SCIENTIFIC`, `HIGH_PRECISION`, `INTEREST_RATE`
- `CRYPTOCURRENCY`, `EXACT`

**DivisionConfig** - Custom configuration:
- `DivisionConfig(scale: Int, rounding: KBRoundingMode = KBRoundingMode.HalfUp)`
- `DivisionConfig(scale: Int, roundingMode: Int = RoundingMode.HALF_UP)`

### Rounding Modes (v0.0.18+)

- Prefer `KBRoundingMode` for type-safe semantics that stay in sync across Android and iOS implementations.
- Legacy integer constants from `RoundingMode` remain available for compatibility and can be converted with `toKBRoundingMode()` / `toLegacyCode()` helpers.
- All rounding-aware APIs now expose overloads accepting `KBRoundingMode` (e.g., `setScale` and `divide`).

### KBigInteger

Interface for arbitrary precision integers:

- `add(other: KBigInteger): KBigInteger`
- `subtract(other: KBigInteger): KBigInteger`
- `multiply(other: KBigInteger): KBigInteger`
- `divide(other: KBigInteger): KBigInteger`
- `remainder(other: KBigInteger): KBigInteger`
- `pow(exponent: Int): KBigInteger`

### KBigMath

Utility class for advanced mathematical operations:

- `sqrt(value: KBigDecimal, scale: Int): KBigDecimal`
- `factorial(n: KBigInteger): KBigInteger`
- `gcd(a: KBigInteger, b: KBigInteger): KBigInteger`
- `lcm(a: KBigInteger, b: KBigInteger): KBigInteger`
- `isPrime(value: KBigInteger): Boolean`
- `pow(base: KBigDecimal, exponent: Int): KBigDecimal`

## Platform Support

### Android
- **Minimum API Level**: 21
- **Implementation**: Uses Java's `BigDecimal` and `BigInteger`
- **Output**: AAR library files

#### Android SDK configuration
- SDK levels are centralized in the root `gradle.properties` (`android.compileSdk`, `android.minSdk`, `android.targetSdk`).
- Override them per build by supplying Gradle properties, e.g.:
    ```bash
    ./gradlew :shared:assembleRelease -Pandroid.compileSdk=34 -Pandroid.targetSdk=34
    ```
- Module builds automatically pick up the configured values; no manual edits inside `shared/build.gradle.kts` are required.

### iOS
- **Minimum Version**: 13.0
- **Implementation**: Uses Foundation's `NSDecimalNumber` and `NSNumber`
- **Output**: XCFramework with arm64 and x64 support

## Requirements

- **Kotlin**: 1.9.10+
- **Gradle**: 8.11.1+
- **Android**: API level 21+
- **iOS**: 13.0+

## Building from Source

```bash
# Clone the repository
git clone https://github.com/gatrongdev/kbignum.git
cd kbignum

# Build for all platforms
./gradlew build

# Run tests
./gradlew test

# Generate coverage report
./gradlew koverXmlReport

# Run code quality checks
./gradlew runAllChecks
```

## Testing

The library includes comprehensive tests covering:

- Basic arithmetic operations
- Edge cases (division by zero, overflow, invalid formats)
- Platform-specific implementations
- Mathematical functions
- Factory methods and type conversions

Run tests with:
```bash
./gradlew test
```

## Code Quality

The project includes several code quality tools:

- **Kover**: Test coverage reporting
- **ktlint**: Kotlin code style checking
- **detekt**: Static code analysis

## Contributing

We welcome contributions! Please see our [Contributing Guide](.github/CONTRIBUTING.md) for detailed information.

### Quick Start for Contributors

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes and add tests
4. Run code quality checks (`./gradlew runAllChecks`)
5. Use conventional commits (`git commit -m "feat: add amazing feature"`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

### Release Process

Releases are automated based on version changes:

1. **Update version**: `./scripts/update-version.sh 1.0.1`
2. **Commit and push**: The release will be created automatically
3. **Semantic versioning** is enforced for all releases

See [CONTRIBUTING.md](.github/CONTRIBUTING.md) for complete guidelines.

## License

This project is licensed under the Apache License, Version 2.0 - see the [LICENSE](LICENSE) file for details.

## Author

**Gatrong Dev** - [GitHub](https://github.com/gatrongdev)

## Acknowledgments

- Built with Kotlin Multiplatform
- Uses platform-native arbitrary precision libraries for optimal performance
- Inspired by Java's BigDecimal and BigInteger APIs