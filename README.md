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
| **Add** | 55 | 21 | **0.38x ✓** |
| **Subtract** | 29 | 30 | 1.03x |
| **Multiply** | 41 | 39 | **0.95x ✓** |
| **Divide** | 19 | 24 | 1.26x |
| **Modulo** | 19 | 24 | 1.26x |

#### 4096-bit Numbers
| Operation | Java (ms) | KBignum (ms) | Relative |
| :--- | :---: | :---: | :---: |
| **Add** | 9 | 8 | **0.89x ✓** |
| **Subtract** | 4 | 10 | 2.50x |
| **Multiply** | 56 | 82 | 1.46x |
| **Divide** | 28 | 21 | **0.75x ✓** |
| **Modulo** | 94 | 25 | **0.27x ✓** |

### Factorial
| Operation | Java (ms) | KBignum (ms) | Relative |
| :--- | :---: | :---: | :---: |
| **Factorial(100)** | 8 | 7 | **0.88x ✓** |
| **Factorial(500)** | 13 | 29 | 2.23x |
| **Factorial(1000)** | 11 | 25 | 2.27x |

### KBigMath (Integer)
| Operation | Java (ms) | KBignum (ms) | Relative |
| :--- | :---: | :---: | :---: |
| **GCD** | 1757 | 5603 | 3.19x |
| **LCM** | 44 | 145 | 3.30x |
| **Pow^100** | 57 | 79 | 1.39x |
### KBigDecimal

#### 600-digit (~2000 bits)
| Operation | Java (ms) | KBigDecimal (ms) | Relative |
| :--- | :---: | :---: | :---: |
| **Add** | 2 | 2 | **1.00x ✓** |
| **Sub** | 2 | 2 | **1.00x ✓** |
| **Mul** | 2 | 4 | 2.00x |
| **Div** | 3 | 5 | 1.67x |

#### 1200-digit (~4000 bits)
| Operation | Java (ms) | KBigDecimal (ms) | Relative |
| :--- | :---: | :---: | :---: |
| **Add** | 1 | 2 | 2.00x |
| **Sub** | 0 | 2 | Infinityx |
| **Mul** | 2 | 6 | 3.00x |
| **Div** | 3 | 7 | 2.33x |

### KBigMath (Decimal)
| Operation | Java (ms) | KBignum (ms) | Relative |
| :--- | :---: | :---: | :---: |
| **Sqrt** | 17 | 257 | 15.12x |
| **Sqrt** | 59 | 823 | 13.95x |
| **Sqrt** | 104 | 1329 | 12.78x |

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
val power = bigDecimal1.pow(5)
```

## Platform Support

### Android
- **Minimum API Level**: 21
- **Implementation**: Pure Kotlin (Single Codebase). No JNI involved.
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
- **Implementation**: Pure Kotlin (Single Codebase). No `NSDecimalNumber` bridging overhead.
- **Output**: XCFramework with arm64 and x64 support

## Requirements

- **Kotlin**: 1.9.10+
- **Gradle**: 8.11.1+
- **Android**: API level 21+
- **iOS**: 13.0+

## License

This project is licensed under the Apache License, Version 2.0 - see the [LICENSE](LICENSE) file for details.

## Author

**Gatrong Dev** - [GitHub](https://github.com/gatrongdev)
