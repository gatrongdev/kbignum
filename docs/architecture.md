# KBigNum architecture and testing

This document describes the structure that exists in the current source tree. Keep it aligned with `shared/build.gradle.kts`, the source sets, and the tests; `plan.md` is historical context.

## Module and source-set map

| Area | Responsibility |
| --- | --- |
| `shared/src/commonMain` | Public arbitrary-precision integer, decimal, rounding, extensions, and math APIs; platform-independent implementation. |
| `shared/src/androidMain`, `desktopMain`, `iosMain` | Platform identity `actual` implementations. Arithmetic stays in common code unless a concrete platform API requires otherwise. |
| `shared/src/commonTest` | Behavioral tests that use only common APIs and can run on supported test targets. |
| `shared/src/androidUnitTest` | JVM-specific compliance tests and `java.math` reference helpers. Keep Java-only APIs out of common tests. |
| `benchmarks` | JVM JMH code and benchmark-only references; this is measurement tooling, not a production arithmetic layer. |

The configured library targets are Android, desktop JVM, and iOS (`iosX64`, `iosArm64`, and `iosSimulatorArm64`). Kover reports host JVM/Android test coverage; they do not claim execution coverage for iOS native code. See the [Kover Gradle documentation](https://kotlin.github.io/kotlinx-kover/gradle-plugin/) for report and verification behavior.

## Core model and boundaries

- `KBigInteger` stores a sign and a little-endian `IntArray` magnitude using 32-bit words. Arithmetic helpers normalize their results; check constructor, sign, zero, and array-mutability behavior before changing representation assumptions.
- `KBigDecimal` represents an unscaled `KBigInteger` and a decimal scale. Preserve the relationship `value = unscaledValue × 10^-scale`, along with current parsing, scale, and rounding semantics.
- `KBigMath` holds higher-level operations over the public integer and decimal APIs. Keep primitive floating-point approximations out of exact results; an approximation may only be an initial estimate where the algorithm refines or checks the result with the library's arithmetic.
- `KBigNumberExtensions` and `KBRoundingMode` provide focused conversion and rounding helpers. Avoid introducing generic service layers or wrappers around the arithmetic core without a concrete use case.

## Multiplication performance path

`KBigInteger` keeps multiplication in `commonMain`. The current measured dispatch uses schoolbook multiplication below 40 32-bit words, Karatsuba at the top level from 40 words, and a separately measured 48-word recursive leaf cutoff. The schoolbook kernel handles its first row without reading zero-filled output limbs; Karatsuba uses absolute half-differences and combines the middle product in one carry pass.

These cutoffs are JVM JMH defaults, not cross-platform performance guarantees. HotSpot applies general JIT optimization to Kotlin/JVM bytecode, but its `BigInteger` multiplication intrinsic is specific to the JDK method. Do not assume that intrinsic transfers to `KBigInteger`, Android ART, or Kotlin/Native. Keep exact arithmetic portable; remeasure on a target before changing shared dispatch based on a JVM result.

When changing this path, compare repeated baseline and candidate JMH runs with the same seeded operands and configuration. Record integer multiplication and BigDecimal multiplication together, then check scale-alignment operations that invoke multiplication. Keep the focused A/B result in `benchmarks/results/multiplication-optimization.csv` and its chart in `docs/assets/benchmark-multiplication-optimization.svg`. To refresh only the crossover graph after a filtered crossover run, pass its JMH JSON to `scripts/render_benchmark_charts.py --crossover-only`.

The next candidates to measure are a dedicated square kernel (used repeatedly by `KBigInteger.pow`) and a size-ratio-aware dispatch. Keep the forced-schoolbook crossover reference synchronized with the production leaf kernel, and require separated intervals at three adjacent sizes before publishing a stable crossover. If allocation profiling becomes dominant, try a reusable scratch buffer before adding a higher-order algorithm; each candidate needs its own benchmark and correctness cases.

## Test strategy

Use `shared/src/commonTest` for observable behavior that should be the same across platforms. Keep tests deterministic; seeded property or fuzz tests are useful when they compare against a clear invariant or exact expected result. Use `androidUnitTest` for the `java.math.BigInteger` / `BigDecimal` oracle, since those Java classes are not available from common code.

For arithmetic changes, cover the applicable normal path and boundaries: signs, zero, carry/borrow, word or scale boundaries, division and remainder identities, rounding ties, and documented exceptions. Assert the public contract rather than implementation details. A high coverage percentage does not replace meaningful assertions, and tests must not be weakened or excluded just to satisfy a report.

## Coverage gate and checks

The current Kover baseline is approximately 87.58% line coverage and 80.59% branch coverage. The project enforces floors of **87% lines** and **80% branches** through `koverVerify`, which is part of `./gradlew runAllChecks`. Keep or improve both floors when production code changes; add focused tests for the uncovered path instead of lowering the gate.

- `./gradlew runAllChecks` runs tests, Kover verification/reporting, ktlint, and detekt.
- `./gradlew :shared:koverHtmlReport` regenerates the navigable report under `shared/build/reports/kover/html/` for locating uncovered lines and branches.
- `./gradlew :shared:iosSimulatorArm64Test` runs common tests on the iOS simulator when shared behavior needs that target checked.

Coverage is measured for the host JVM/Android test executions supported by Kover. Use the iOS simulator test task to validate common behavior there; do not treat the Kover percentage as proof of native execution.
