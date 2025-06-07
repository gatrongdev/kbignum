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
    implementation("io.github.gatrongdev:kbignum:0.0.1")
}
```

### Gradle (Groovy)

Add to your `build.gradle`:

```gradle
dependencies {
    implementation 'io.github.gatrongdev:kbignum:0.0.1'
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
val quotient = bigDecimal1.divide(bigDecimal2, 10) // 10 decimal places

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
val scaled = bigDecimal1.setScale(5, RoundingMode.HALF_UP)
val precision = bigDecimal1.precision()
```

## API Reference

### KBigDecimal

Interface for arbitrary precision decimal numbers:

- `add(other: KBigDecimal): KBigDecimal`
- `subtract(other: KBigDecimal): KBigDecimal`
- `multiply(other: KBigDecimal): KBigDecimal`
- `divide(other: KBigDecimal, scale: Int): KBigDecimal`
- `abs(): KBigDecimal`
- `signum(): Int`
- `setScale(scale: Int, roundingMode: Int): KBigDecimal`

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