# KBigNum

[![Kotlin](https://img.shields.io/badge/kotlin-multiplatform-blue.svg?logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.gatrongdev/kbignum.svg?label=Maven%20Central&logo=apache-maven)](https://central.sonatype.com/artifact/io.github.gatrongdev/kbignum)
[![License](https://img.shields.io/badge/license-Apache%202.0-green.svg?logo=apache)](LICENSE)
[![Build Status](https://img.shields.io/github/actions/workflow/status/gatrongdev/kbignum/ci.yml?branch=main&logo=github&label=Build)](https://github.com/gatrongdev/kbignum/actions)
[![Test Coverage](https://img.shields.io/codecov/c/github/gatrongdev/kbignum/main?logo=codecov&label=Coverage)](https://codecov.io/gh/gatrongdev/kbignum)

A **Kotlin Multiplatform** library for arbitrary precision mathematics, providing unified APIs for high-precision arithmetic operations across Android, iOS, and Web platforms.

## Features

- **Arbitrary Precision Arithmetic**: Handle numbers with unlimited precision using `KBigDecimal` and `KBigInteger`.
- **Mathematical Mathematical**: `gcd`, `lcm`, `factorial`, `sqrt`, `pow`.
- **Bitwise Operations**: `and`, `or`, `xor`, `not`, `andNot` (with full 2's complement behavior).
- **Pure Kotlin Implementation**: No JNI or native dependencies required, ensuring maximum compatibility.
- **Multiplatform Support**: Single codebase works seamlessly on **Android**, **iOS**, **JVM**, **JS**, and **Native**.
- **Type-Safe API**: Strongly typed interfaces mimicking Java's `BigDecimal`/`BigInteger`.
- **Natural Syntax**: Operator overloading (`+`, `-`, `*`, `/`) for intuitive mathematical expressions.

## Installation

### Gradle (Kotlin DSL)

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.gatrongdev:kbignum:VERSION")
}
```

### Gradle (Groovy)

Add to your `build.gradle`:

```gradle
dependencies {
    implementation 'io.github.gatrongdev:kbignum:VERSION'
}
```

## Performance
`KBignum` offers competitive performance by utilizing efficient algorithms (Knuth's Algorithm D for division, optimized magnitude arithmetic). Below is a comparison against Java's native implementations on JVM:

### KBigInteger

#### 2048-bit Numbers
| Operation | Java (ms) | KBignum (ms) | Relative |
| :--- | :---: | :---: | :---: |
| **Add** | 14 | 26 | 1.86x |
| **Subtract** | 16 | 29 | 1.81x |
| **Multiply** | 53 | 47 | **0.89x ✓** |
| **Divide** | 21 | 42 | 2.00x |
| **Modulo** | 24 | 39 | 1.63x |

#### 4096-bit Numbers
| Operation | Java (ms) | KBignum (ms) | Relative |
| :--- | :---: | :---: | :---: |
| **Add** | 3 | 4 | 1.33x |
| **Subtract** | 3 | 8 | 2.67x |
| **Multiply** | 54 | 74 | 1.37x |
| **Divide** | 27 | 22 | **0.81x ✓** |
| **Modulo** | 27 | 21 | **0.78x ✓** |
### KBigDecimal

#### 600-digit (~2000 bits)
| Operation | Java (ms) | KBigDecimal (ms) | Relative |
| :--- | :---: | :---: | :---: |
| **Add** | 2 | 2 | **1.00x ✓** |
| **Sub** | 4 | 2 | **0.50x ✓** |
| **Mul** | 1 | 4 | 4.00x |
| **Div** | 4 | 6 | 1.50x |

#### 1200-digit (~4000 bits)
| Operation | Java (ms) | KBigDecimal (ms) | Relative |
| :--- | :---: | :---: | :---: |
| **Add** | 3 | 1 | **0.33x ✓** |
| **Sub** | 1 | 2 | 2.00x |
| **Mul** | 2 | 6 | 3.00x |
| **Div** | 3 | 7 | 2.33x |

*Note: Benchmarks run on macOS/JVM. ✓ indicates KBignum is faster or equal to Java. KBignum prioritizes portability across KMP targets (Android, iOS, JS, Native).*


## Quick Start

### Basic Usage

```kotlin
import io.github.gatrongdev.kbignum.math.*

// Creating big numbers
val bigDecimal1 = "123.456789".toKBigDecimal()
val bigDecimal2 = KBigDecimal.fromString("987.654321")
val bigInteger = "12345678901234567890".toKBigInteger()

// Basic arithmetic
val sum = bigDecimal1 + bigDecimal2
val difference = bigDecimal1 - bigDecimal2
val product = bigDecimal1 * bigDecimal2

// Division (Operator '/' defaults to scale 10, HALF_UP)
val quotient = bigDecimal1 / bigDecimal2

// Precise Division
val preciseQuotient = bigDecimal1.divide(bigDecimal2, scale = 20, rounding = KBRoundingMode.HalfUp)

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
val power = bigDecimal1.pow(5) // Extension function
```

## API Reference

### KBigDecimal

Interface for arbitrary precision decimal numbers:

- `add(other: KBigDecimal): KBigDecimal`
- `subtract(other: KBigDecimal): KBigDecimal`
- `multiply(other: KBigDecimal): KBigDecimal`
- `divide(other: KBigDecimal): KBigDecimal`
- `divide(other: KBigDecimal, scale: Int): KBigDecimal`
- `divide(other: KBigDecimal, scale: Int, rounding: KBRoundingMode): KBigDecimal`
- `abs(): KBigDecimal`
- `signum(): Int`

### Rounding Modes (v0.0.18+)

- Prefer `KBRoundingMode` (Enum) for type-safety.
- Supported modes: `Up`, `Down`, `Ceiling`, `Floor`, `HalfUp`, `HalfDown`, `HalfEven`.

### KBigInteger

Interface for arbitrary precision integers:

- `add(other: KBigInteger): KBigInteger`
- `subtract(other: KBigInteger): KBigInteger`
- `multiply(other: KBigInteger): KBigInteger`
- `divide(other: KBigInteger): KBigInteger`
- `mod(other: KBigInteger): KBigInteger` (Remainder)
- `pow(exponent: Int): KBigInteger` (Extension)

### KBigMath

Utility class for advanced mathematical operations:

- `sqrt(value: KBigDecimal, scale: Int): KBigDecimal`
- `factorial(n: KBigInteger): KBigInteger`
- `gcd(a: KBigInteger, b: KBigInteger): KBigInteger`
- `lcm(a: KBigInteger, b: KBigInteger): KBigInteger`
- `isPrime(value: KBigInteger): Boolean`

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
