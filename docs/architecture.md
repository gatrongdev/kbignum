# KBigNum architecture and testing

This document describes the structure that exists in the current source tree. Keep it aligned with `shared/build.gradle.kts`, the source sets, and the tests; `plan.md` is historical context.

## Module and source-set map

| Area | Responsibility |
| --- | --- |
| `shared/src/commonMain` | Public arbitrary-precision integer, decimal, rounding, extensions, and math APIs; platform-independent implementation. |
| `shared/src/androidMain`, `desktopMain`, `iosMain`, `jsMain` | Platform identity `actual` implementations. Arithmetic stays in common code unless a concrete platform API requires otherwise. The JS actual reports the Node.js version when `process.version` exists, reports `Browser` when the page globals exist, and reads every global only behind an existence check. |
| `shared/src/commonTest` | Behavioral tests that use only common APIs and run on every supported test target. |
| `shared/src/androidHostTest` | JVM-specific compliance tests and `java.math` reference helpers. Keep Java-only APIs out of common tests. |
| `benchmarks` | JVM JMH code and benchmark-only references; this is measurement tooling, not a production arithmetic layer. |

The configured library targets are Android, desktop JVM 17, iOS (`iosX64`, `iosArm64`, and `iosSimulatorArm64`), and Kotlin/JS IR on Node.js and in browsers (`js { nodejs(); browser() }`). CI runs host checks on Linux with JDK 17 and 21, common tests on an iOS simulator on macOS, and common tests on both Node.js and browser JS. Both JavaScript environments share the same `jsMain` and `jsTest` compilations. The Kotlin Gradle plugin's default source-set hierarchy creates the shared `iosMain` intermediate source set, so the build does not declare manual `dependsOn` edges. Node tests use the built-in Node.js runner, while browser tests currently use Karma with headless Chrome. Revisit the browser test driver separately from toolchain upgrades, keeping the browser CI job operational. Wasm is not configured. Kover reports host test execution; it does not claim Native or JS execution coverage. See the [Kover Gradle documentation](https://kotlin.github.io/kotlinx-kover/gradle-plugin/) for report and verification behavior.

## Core model and boundaries

- `KBigInteger` stores a sign and a little-endian `IntArray` magnitude using 32-bit words. Its constructor validates the sign, copies and normalizes the input, and its public `magnitude` property returns a copy. Preserve these value and zero/sign invariants when changing the representation.
- `KBigDecimal` represents an unscaled `KBigInteger` and a non-negative decimal scale. Preserve the relationship `value = unscaledValue × 10^-scale`, along with current parsing, scale, and rounding semantics. Multiplication and division check scale arithmetic for overflow. Scale alignment operates on unscaled integers without constructing temporary decimals, and the per-instance powers-of-ten map is allocated only when needed. The caches remain mutable and unsynchronized; review concurrent access on JVM and Native before claiming value-like thread safety.
- `KBigMath` holds higher-level operations over the public integer and decimal APIs. Keep primitive floating-point approximations out of exact results; an approximation may only be an initial estimate where the algorithm refines or checks the result with the library's arithmetic.
- `KBigNumberExtensions` and `KBRoundingMode` provide focused conversion and rounding helpers. Avoid introducing generic service layers or wrappers around the arithmetic core without a concrete use case.
- `Platform`/`getPlatform()` is public expect/actual code but is not used by arithmetic. Decide whether it is product API or platform-template scaffolding before adding targets; each target needs an actual implementation.

## API and naming policy

KBigInteger and KBigDecimal expose descriptive arithmetic methods and Kotlin operator extensions. Keep results and error semantics aligned. Both `divide(other)` and `/` use scale 10 with `HalfUp`; use `DivisionConfig` when an operation needs a different scale or rounding rule.

The public surface currently has signum() alongside the signum property, scale() alongside the scale property, PascalCase KBRoundingMode entries, and fromString factories beside String conversion extensions. These names are already public. For new APIs, use PascalCase types, lowerCamelCase members, isX Boolean queries, parse(String) as the canonical text entry point, fromInt/fromLong for primitive factories, toX for conversions, familiar operators, and one accessor per concept. Keep String.toKBigInteger/toKBigDecimal as ergonomic forwarding extensions. Prefer uppercase enum entries for future rounding modes. Add aliases and deprecate legacy names before removing them in a breaking release.

The Gradle configuration does not currently declare a public API dump or compatibility check. Consider adding a reviewable API snapshot as the library approaches a stable 1.0 surface. Document exactness, valid input, overflow, rounding, and exceptions in KDoc and examples.

## Platform support policy

Keep exact arithmetic in commonMain and avoid expect/actual forks for performance without target-specific measurements. Add a target only when it serves a real consumer need and the project can compile, execute relevant common tests, publish, document, and maintain it.

Kotlin/JS on Node.js and in browsers is configured, and the common suite runs in both environments in CI. Add another runtime, such as Wasm, only when there is a consumer need and the project can compile, test, publish, document, and maintain it. JS and Wasm have different runtimes and compatibility requirements; recheck target stability when changing the Kotlin toolchain.

For every target claimed as supported, compile the library and common tests, regularly execute common tests on that target runtime (or document a tiered CI schedule), provide actual implementations for remaining expect declarations, publish the target artifact, and document target-specific requirements. Keep common tests common; add platform tests only for real platform behavior.

See [Kotlin's multiplatform library guidance](https://kotlinlang.org/docs/api-guidelines-build-for-multiplatform.html), [target stability table](https://kotlinlang.org/docs/multiplatform/supported-platforms.html), and [web target overview](https://kotlinlang.org/docs/web-overview.html) when evaluating targets.

## Multiplication performance path

`KBigInteger` keeps multiplication in `commonMain`. The current measured dispatch uses schoolbook multiplication below 40 32-bit words, Karatsuba at the top level from 40 words, and a separately measured 48-word recursive leaf cutoff. The schoolbook kernel handles its first row without reading zero-filled output limbs; Karatsuba uses absolute half-differences and combines the middle product in one carry pass.

These cutoffs are JVM JMH defaults, not cross-platform performance guarantees. HotSpot applies general JIT optimization to Kotlin/JVM bytecode, but its `BigInteger` multiplication intrinsic is specific to the JDK method. Do not assume that intrinsic transfers to `KBigInteger`, Android ART, Kotlin/Native, or Kotlin/JS. Keep exact arithmetic portable; remeasure on a target before changing shared dispatch based on a JVM result.

When changing this path, compare repeated baseline and candidate JMH runs with the same seeded operands and configuration. Record integer multiplication and BigDecimal multiplication together, then check scale-alignment operations that invoke multiplication. Keep the focused A/B result in `benchmarks/results/multiplication-optimization.csv` and its chart in `docs/assets/benchmark-multiplication-optimization.svg`. To refresh only the crossover graph after a filtered crossover run, pass its JMH JSON to `scripts/render_benchmark_charts.py --crossover-only`.

`KBigInteger.pow` and `KBigDecimal.pow` use binary exponentiation. Their loops skip the final square after the last exponent bit and return the immutable receiver for exponent 1. A dedicated symmetric square kernel was measured and rejected: its Kotlin/JS Node.js implementation was 16–35% slower than the generic multiplication path at the measured sizes. Reconsider a square-specific kernel only when one common implementation wins on both JVM and JS. Future multiplication work should start with allocation profiling before introducing scratch-buffer reuse or a higher-order algorithm. Keep each candidate's measurements and correctness cases separate.

`KBigInteger.multiply` also returns through a small-magnitude fast path when either operand is `+1` or `-1`; it reuses the immutable other operand when the result sign is unchanged and negates it otherwise. `KBigDecimal.multiply` continues to multiply the unscaled values and add their scales, so a large decimal coefficient times a scaled unit avoids the general limb kernel while retaining its exact representation. This does not accelerate balanced large-by-large multiplication. The focused A/B evidence and reproducible inputs are recorded in `benchmarks/results/kbigdecimal-multiply-unit.csv` and `docs/assets/benchmark-bigdecimal-multiply-unit.svg`.

## Decimal scale-alignment performance path

`KBigDecimal.add` and `subtract` keep same-scale arithmetic as a direct unscaled-integer operation. For different scales, they align only the smaller-scale unscaled value to `maxOf(scale, other.scale)`; do not create temporary `KBigDecimal` operands or a `Pair` just to carry aligned values. The result scale remains the larger operand scale. When a zero operand can be returned without changing that representation, reuse the existing immutable decimal value. `compareTo` compares same-scale coefficients directly and resolves differing signs or zero before constructing any power of ten; same-sign nonzero values still require exact scale alignment.

The per-instance powers-of-ten map is lazy so result decimals that never need scale alignment do not allocate an empty map. Keep cache behavior per instance and bounded as it is today; do not add a shared mutable cache without a cross-platform concurrency design. Measure JVM time and allocation plus an opt-in Node.js harness before changing this path. The focused baseline/candidate data and chart are `benchmarks/results/decimal-scale-alignment-optimization.csv` and `docs/assets/benchmark-decimal-scale-alignment-optimization.svg`. The JMH and Node.js runs are diagnostic machine measurements, not CI thresholds; overlapping JMH confidence intervals and Node.js medians without confidence intervals must not be presented as stable wins.

## Test strategy

Use `shared/src/commonTest` for observable behavior that should be the same across platforms. Keep tests deterministic; seeded property or fuzz tests are useful when they compare against a clear invariant or exact expected result. The same common test suite runs on desktop JVM, Android host tests, the iOS simulator, Kotlin/JS on Node.js, and Kotlin/JS in headless Chrome. Because both JS runtimes share `jsTest`, the platform-identity test checks whether `window` exists and asserts the matching runtime name. Use `androidHostTest` for the `java.math.BigInteger` / `BigDecimal` oracle, since those Java classes are not available from common code. `runAllChecks` covers host tests; dedicated CI jobs execute common tests on iOS, Node.js, and browser JS.

For arithmetic changes, cover the applicable normal path and boundaries: signs, zero, carry/borrow, word or scale boundaries, division and remainder identities, rounding ties, and documented exceptions. Assert the public contract rather than implementation details. A high coverage percentage does not replace meaningful assertions, and tests must not be weakened or excluded just to satisfy a report.

## Coverage gate and checks

The current Kover baseline is approximately 87.58% line coverage and 80.59% branch coverage. The project enforces floors of **87% lines** and **80% branches** through `koverVerify`, which is part of `./gradlew runAllChecks`. Keep or improve both floors when production code changes; add focused tests for the uncovered path instead of lowering the gate.

- `./gradlew runAllChecks` runs host JVM/Android tests, Kover verification/reporting, ktlint, and detekt.
- `./gradlew :shared:jsNodeTest` runs the common tests on the Kotlin/JS Node.js target; CI runs it in the dedicated JS/Node job and collects its test report.
- `./gradlew :shared:jsBrowserTest` runs the common tests in headless Chrome; CI installs Chrome, runs it in the dedicated JS/browser job, and collects its test report.
- `./gradlew :shared:koverHtmlReport` regenerates the navigable report under `shared/build/reports/kover/html/` for locating uncovered lines and branches.
- `./gradlew :shared:iosSimulatorArm64Test` runs common tests on the iOS simulator when shared behavior needs that target checked.

Coverage is measured for the host JVM/Android test executions supported by Kover. Use the iOS simulator and JS Node/browser test tasks to validate common behavior on those runtimes; do not treat the Kover percentage as proof of native or JS execution.
