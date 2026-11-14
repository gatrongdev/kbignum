---
name: kbignum-arithmetic
description: Use when changing KBigInteger, KBigDecimal, KBigMath, division or rounding behavior, or tests for arbitrary-precision arithmetic.
---

# KBigNum arithmetic changes

Use this workflow for arithmetic changes in `shared`. For general Gradle, release, or documentation-only work, follow the repository's `AGENTS.md` and existing contribution guidance instead.

## Contracts to preserve

- Read the current implementation and relevant tests first. `plan.md` is historical and may describe behavior that no longer exists.
- Keep platform-independent arithmetic in `shared/src/commonMain`; add platform-specific code only when a real platform dependency or API requires it.
- `KBigInteger` stores a sign separately from a little-endian array of 32-bit magnitude words. Check constructors, zero/sign handling, normalization, and mutability when changing this representation; preserve tested behavior and add coverage before tightening an invariant.
- `KBigDecimal` represents `unscaledValue × 10^-scale`. Check the current validation and scale behavior before changing it. The default `divide(other)` currently uses `max(this.scale, other.scale)` with `HalfUp`; treat a change to that behavior as an API behavior change.
- Preserve exactness, sign behavior, overflow handling, and error semantics established by tests. Do not route exact arithmetic through primitive numeric approximations.

## Workflow

1. Trace the affected public operation into its implementation and tests. Identify representation invariants and the observable behavior for zero, signs, boundaries, and errors that apply to the change.
2. Make the smallest cohesive change in common code where possible. Keep algorithm-specific helpers private unless they form a useful public API.
3. Add or update tests for the relevant normal cases and edge cases. For arithmetic, consider negative operands, zero, large magnitudes, division/remainder boundaries, scale, and rounding as applicable; avoid adding irrelevant cases. Follow [`kbignum-unit-testing`](../kbignum-unit-testing/SKILL.md) for source-set placement and coverage.
4. Run the checks that cover the modified source set. Use `./gradlew runAllChecks` for common/JVM and quality checks; use `./gradlew :shared:iosSimulatorArm64Test` when validating shared behavior on iOS.
5. If public behavior or API changes, update the README and `CHANGELOG.md` to match the implementation.
