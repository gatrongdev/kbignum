---
name: kbignum-arithmetic
description: Use when changing KBigInteger, KBigDecimal, KBigMath, division or rounding behavior, or tests for arbitrary-precision arithmetic.
---

# KBigNum arithmetic changes

Use this workflow for arithmetic changes in `shared`. For public API, naming, representation, or platform support decisions, also follow [kbignum-architecture](../kbignum-architecture/SKILL.md). For general Gradle, release, or documentation-only work, follow the repository's `AGENTS.md` and existing contribution guidance instead.

## Contracts to preserve

- Read the current implementation and relevant tests first. `plan.md` is historical and may describe behavior that no longer exists.
- Keep platform-independent arithmetic in `shared/src/commonMain`; add platform-specific code only when a real platform dependency or API requires it.
- `KBigInteger` stores a sign separately from a little-endian array of 32-bit magnitude words. Its constructor validates the sign, copies and normalizes the input, and its public `magnitude` getter returns a copy. Preserve those value and zero/sign invariants when changing the representation.
- `KBigDecimal` represents `unscaledValue × 10^-scale`. Check the current validation and scale behavior before changing it. Both `divide(other)` and `/` currently use scale 10 with `HalfUp`; `DivisionConfig` makes alternate precision and rounding explicit. Treat changes to these defaults as API behavior changes.
- Preserve exactness, sign behavior, overflow handling, and error semantics established by tests. Do not route exact arithmetic through primitive numeric approximations.

## Multiplication performance work

- Profile the current leaf kernel and dispatch before changing thresholds. Keep the schoolbook, Karatsuba, and any future Toom-Cook code in `commonMain` unless a concrete platform API requirement justifies an `expect`/`actual` path.
- Calibrate top-level and recursive thresholds independently with the checked-in JMH suite. Keep seeded inputs, JVM, fork count, warmup, and measurement time fixed between baseline and candidate runs; use repeated runs when differences are near the target.
- Keep the forced-schoolbook crossover reference in sync with the production leaf kernel. Treat an algorithm crossover as stable only when confidence intervals separate for several adjacent operand sizes; isolated points can be scheduling noise.
- Measure both `KBigInteger` and `KBigDecimal` multiplication. Include scale-mismatched decimal operations that multiply by powers of ten, and screen unrelated arithmetic for regressions before accepting a dispatch change.
- Check trivial magnitudes such as `+1` and `-1` before entering a general multiplication kernel. Preserve operand immutability and sign semantics, and verify both operand positions. For `KBigDecimal`, separately assert that the unscaled result and summed scale remain unchanged; benchmark a large coefficient multiplied by a scaled unit as well as balanced large coefficients.
- `pow()` uses binary exponentiation; avoid computing a final square after the last exponent bit. A dedicated symmetric square kernel was tested and regressed Kotlin/JS Node.js by 16–35%, so do not add a common square-specific path unless repeated JVM and JS measurements show a win on both.
- Compare balanced and unbalanced operand ratios before changing a shared threshold. Consider scratch-buffer allocation reduction before higher-order algorithms when allocation profiles justify it.
- JMH measures the JVM target only. For a shared dispatch change, use a separate deterministic Kotlin/JS measurement on Node.js or a browser and report target-specific results; common tests establish correctness, not performance parity.
- Do not assume HotSpot's `java.math.BigInteger` intrinsic applies to Kotlin code. HotSpot can JIT Kotlin/JVM bytecode, but a library intrinsic is registered for a particular JDK method and signature; Android ART and Kotlin/Native use different runtimes.
- Update the focused A/B CSV/chart and architecture guidance when a measured optimization changes the shared multiplication path. Report run-to-run spread separately from formal confidence intervals.
- For integer or decimal `pow()` changes, record JMH baseline/candidate data and an opt-in deterministic JS Node.js measurement. Do not add timing thresholds to correctness tests; mark overlapping benchmark intervals as inconclusive.
- Refresh crossover-only artifacts with `scripts/render_benchmark_charts.py <filtered-jmh-json> --crossover-only`; use the full benchmark renderer when updating the complete operation comparison.

## Workflow

1. Trace the affected public operation into its implementation and tests. Identify representation invariants and the observable behavior for zero, signs, boundaries, and errors that apply to the change.
2. Make the smallest cohesive change in common code where possible. Keep algorithm-specific helpers private unless they form a useful public API.
3. Add or update tests for the relevant normal cases and edge cases. For arithmetic, consider negative operands, zero, large magnitudes, division/remainder boundaries, scale, and rounding as applicable; avoid adding irrelevant cases. Follow [`kbignum-unit-testing`](../kbignum-unit-testing/SKILL.md) for source-set placement and coverage.
4. Run the checks that cover the modified source set. Use `./gradlew runAllChecks` for common/JVM and quality checks; use `./gradlew :shared:iosSimulatorArm64Test` when validating shared behavior on iOS.
5. If public behavior or API changes, update the README and `CHANGELOG.md` to match the implementation.
