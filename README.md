# KBigNum

[![Kotlin](https://img.shields.io/badge/kotlin-multiplatform-blue.svg?logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.gatrongdev/kbignum.svg?label=Maven%20Central&logo=apache-maven)](https://central.sonatype.com/artifact/io.github.gatrongdev/kbignum)
[![License](https://img.shields.io/badge/license-Apache%202.0-green.svg?logo=apache)](LICENSE)
[![Build Status](https://img.shields.io/github/actions/workflow/status/gatrongdev/kbignum/ci.yml?branch=main&logo=github&label=Build)](https://github.com/gatrongdev/kbignum/actions)
[![Test Coverage](https://img.shields.io/codecov/c/github/gatrongdev/kbignum/main?logo=codecov&label=Coverage)](https://codecov.io/gh/gatrongdev/kbignum)

A **Kotlin Multiplatform** library for arbitrary precision mathematics, providing unified APIs for high-precision arithmetic operations across Android, JVM, and iOS.

## Features

- **Arbitrary Precision Arithmetic**: Handle numbers with unlimited precision using `KBigDecimal` and `KBigInteger`.
- **Mathematical Mathematical**: `gcd`, `lcm`, `factorial`, `sqrt`, `pow`.
- **Bitwise Operations**: `and`, `or`, `xor`, `not`, `andNot` (with full 2's complement behavior).
- **Pure Kotlin Implementation**: No JNI or native dependencies required, ensuring maximum compatibility.
- **Multiplatform Support**: Single codebase works seamlessly on **Android**, **iOS**, **JVM**, **JS**, and **Native**.
- **Type-Safe API**: Strongly typed interfaces mimicking Java's `BigDecimal`/`BigInteger`.
- **Natural Syntax**: Operator overloading (`+`, `-`, `*`, `/`) for intuitive mathematical expressions.

See the [architecture and testing guide](docs/architecture.md) for the module layout, test placement, and enforced coverage floors.

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
The previous hand-timed figures were removed because their one-shot millisecond measurements and operand generation were not reproducible. This JVM baseline uses JMH to compare `KBigInteger` with `java.math.BigInteger` using the same seeded operands. Each result is checked for equality before timing.

The chart shows average latency in nanoseconds per operation across 1024-, 2048-, 2560-, 3072-, 4096-, and 8192-bit operands. Division and remainder use a 2:1 dividend-to-divisor bit-length ratio so the quotient is nontrivial. JMH runs one thread, three forks, two warmup iterations, and four measurement iterations of 250 ms each. Ratio markers distinguish separated confidence intervals from inconclusive overlaps.

![KBigInteger JVM latency by operand size](docs/assets/benchmark-integer-latency.svg)

The multiplication crossover chart compares the production multiplication path with a forced schoolbook reference around its measured dispatch points. It sweeps balanced operands and 2:1 and 4:1 size ratios; raw measurements are in [`benchmarks/results/multiplication-crossover.csv`](benchmarks/results/multiplication-crossover.csv).

![KBigInteger multiplication crossover](docs/assets/benchmark-multiplication-crossover.svg)

The BigDecimal chart compares `KBigDecimal` with `java.math.BigDecimal` at 34, 100, 500, and 1000 input digits. It covers same-scale and mismatched-scale addition and subtraction, multiplication, division at scale 16 with `HalfUp`, rescaling from 8 to 4 with `HalfUp`, parsing, and plain-string formatting. Inputs are seeded and signed; each corresponding result is checked before timing. Parsing and formatting are measured separately from arithmetic. The scale-alignment sweep reuses 32 seeded operand pairs and measures addition and subtraction with scale gaps of 1, 8, 32, and 128.

![KBigDecimal JVM latency by input precision](docs/assets/benchmark-bigdecimal-latency.svg)

![KBigDecimal scale-alignment latency by scale gap and input precision](docs/assets/benchmark-decimal-scale-alignment.svg)

<!-- BENCHMARK_SUMMARY_START -->

### KBigInteger latency ratio

Each cell is `KBigInteger ns/op ÷ Java BigInteger ns/op`. `↓`/`↑` mark non-overlapping JMH intervals; `†` marks overlapping intervals, so the ratio is inconclusive.

Interval overlap is a conservative visual cue, not a formal paired significance test.

| Operation (KBigInteger / Java BigInteger) | 1024 bits | 2048 bits | 2560 bits | 3072 bits | 4096 bits | 8192 bits |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| Addition | **↓ 0.64x** | **↓ 0.53x** | **↓ 0.66x** | **↓ 0.75x** | **↓ 0.74x** | **↓ 0.67x** |
| Subtraction | 1.61x† | **↑ 1.29x** | **↑ 1.14x** | 1.09x† | 0.80x† | 0.84x† |
| Multiplication | **↑ 3.38x** | **↑ 3.77x** | **↑ 2.80x** | **↑ 3.34x** | **↑ 3.66x** | **↑ 3.01x** |
| Division | 0.97x† | 0.99x† | 1.02x† | 1.00x† | 1.02x† | **↑ 1.65x** |
| Remainder | 1.00x† | 1.04x† | 0.98x† | 1.06x† | 0.98x† | **↑ 1.50x** |

### Current multiplication crossover check

| Operand ratio (larger : smaller) | First size where production CI is below schoolbook CI |
| :---: | :--- |
| 1:1 | 80 words |
| 2:1 | 128 words |
| 4:1 | 128 words |

Generated 2026-11-14 03:29 UTC on Darwin 25.2.0 / arm64 with JMH 1.36 and JVM 21.0.8+9-b1140.54. Data: [`latest.csv`](benchmarks/results/latest.csv) and [`multiplication-crossover.csv`](benchmarks/results/multiplication-crossover.csv).

<!-- BENCHMARK_SUMMARY_END -->

<!-- BIGDECIMAL_BENCHMARK_SUMMARY_START -->

### KBigDecimal latency ratio

Each cell is `KBigDecimal ns/op ÷ Java BigDecimal ns/op`. `↓`/`↑` mark non-overlapping JMH intervals; `†` marks overlapping intervals, so the ratio is inconclusive.

Interval overlap is a conservative visual cue, not a formal paired significance test.

| Operation (KBigDecimal / Java BigDecimal) | 34 digits | 100 digits | 500 digits | 1000 digits |
| :--- | :---: | :---: | :---: | :---: |
| Addition (scale 8) | **↑ 1.16x** | 1.22x† | 0.98x† | **↑ 1.38x** |
| Addition (scales 8 and 16) | **↑ 2.20x** | **↑ 2.25x** | **↑ 2.18x** | **↑ 2.35x** |
| Subtraction (scale 8) | 0.98x† | 1.19x† | 0.97x† | 0.96x† |
| Subtraction (scales 8 and 16) | **↑ 2.16x** | **↑ 2.22x** | **↑ 2.10x** | **↑ 2.11x** |
| Multiplication (scales 8 and 16) | **↑ 1.57x** | **↑ 2.55x** | **↑ 4.03x** | **↑ 3.01x** |
| Division (result scale 16, HalfUp) | 0.94x† | 0.95x† | **↑ 1.54x** | **↑ 1.50x** |
| setScale 8 to 4 (HalfUp) | 1.27x† | 1.06x† | 1.00x† | 0.84x† |
| Parsing | **↑ 1.22x** | 1.78x† | 1.08x† | 0.93x† |
| Plain-string formatting | 1.03x† | 0.70x† | **↓ 0.89x** | **↑ 1.11x** |

Across every tested input size in this run, KBigDecimal had separated slower intervals for Addition (scales 8 and 16), Subtraction (scales 8 and 16), Multiplication (scales 8 and 16). Treat these as profiling candidates for the recorded JVM and machine.

Generated 2026-11-14 04:25 UTC on Darwin 25.2.0 / arm64 with JMH 1.36 and JVM 21.0.8+9-b1140.54. Data: [`latest-bigdecimal.csv`](benchmarks/results/latest-bigdecimal.csv).

### Scale-alignment latency ratio

Addition and subtraction align a left operand at scale 8 with a right operand at scale `8 + gap`. Each cell is `KBigDecimal ns/op ÷ Java BigDecimal ns/op`; lower is faster. `↓`/`↑` mark non-overlapping JMH intervals and `†` marks overlap.

| Operation / scale gap | 34 digits | 100 digits | 500 digits | 1000 digits |
| :--- | :---: | :---: | :---: | :---: |
| Addition, gap 1 | **↑ 1.78x** | **↑ 1.78x** | **↑ 1.95x** | **↑ 2.18x** |
| Addition, gap 8 | **↑ 2.61x** | **↑ 2.23x** | **↑ 2.18x** | **↑ 2.22x** |
| Addition, gap 32 | **↑ 1.82x** | **↑ 1.57x** | **↑ 2.14x** | **↑ 2.18x** |
| Addition, gap 128 | **↑ 2.77x** | **↑ 2.12x** | **↑ 2.76x** | **↑ 2.71x** |
| Subtraction, gap 1 | **↑ 1.80x** | **↑ 1.74x** | **↑ 1.81x** | **↑ 2.05x** |
| Subtraction, gap 8 | **↑ 1.65x** | **↑ 2.45x** | **↑ 2.56x** | **↑ 2.14x** |
| Subtraction, gap 32 | **↑ 1.58x** | **↑ 1.74x** | **↑ 2.18x** | **↑ 2.17x** |
| Subtraction, gap 128 | **↑ 2.54x** | **↑ 2.16x** | **↑ 2.69x** | **↑ 3.24x** |

The chart and raw measurements are available in [`benchmark-decimal-scale-alignment.svg`](docs/assets/benchmark-decimal-scale-alignment.svg) and [`scale-alignment.csv`](benchmarks/results/scale-alignment.csv).

<!-- BIGDECIMAL_BENCHMARK_SUMMARY_END -->

### Regenerate the benchmark

Run `./gradlew updateBenchmark` with JDK 17 or newer and Python 3.8 or newer. This runs the integer, decimal, and multiplication crossover JMH suites, saves the measurements to CSV, updates all charts, and refreshes the ratio tables above.

The chart is a JVM snapshot for the recorded machine and JDK. It does not predict Android, iOS, JS, or Native performance and is not used as a CI pass/fail threshold.

## BigDecimal scale and rounding

`KBigDecimal` stores a value as `unscaledValue × 10^-scale`. Scale is part of representation: `1.0` and `1.00` compare numerically as equal, but `equals` returns false because their scales differ. Trailing zeros are preserved by parsing and formatting.

Division has explicit scale behavior. The `/` operator returns 10 fractional digits with `HalfUp`; `divide(other)` returns `max(this.scale, other.scale)` fractional digits with `HalfUp`; use the overload with `scale` and `rounding` when a calculation needs a specific result scale.

```kotlin
val amount = "1.00".toKBigDecimal()
val divisor = "3.0".toKBigDecimal()

val operatorResult = amount / divisor // scale 10, HalfUp
val defaultResult = amount.divide(divisor) // max input scale (2), HalfUp
val explicitResult = amount.divide(divisor, scale = 6, rounding = KBRoundingMode.HalfEven)
```


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
val bigInteger1 = "48".toKBigInteger()
val bigInteger2 = "18".toKBigInteger()
val bigDecimal1 = "12.5".toKBigDecimal()

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
