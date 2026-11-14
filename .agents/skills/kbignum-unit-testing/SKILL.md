---
name: kbignum-unit-testing
description: Use when adding, changing, or reviewing KBigNum tests, investigating coverage, or keeping the project's Kover coverage guardrails healthy.
---

# KBigNum unit testing

Use this skill for test work in the KBigNum library. Read [`docs/architecture.md`](../../../docs/architecture.md) for the source-set map and coverage policy. For arithmetic implementation changes, also follow [`kbignum-arithmetic`](../kbignum-arithmetic/SKILL.md).

## Choose the right test source set

- Put platform-independent contract tests in `shared/src/commonTest` and use `kotlin.test` assertions.
- Put tests that need `java.math.BigInteger`, `java.math.BigDecimal`, or another JVM-only API in `shared/src/androidUnitTest`. Keep production code and common tests free of Java-only dependencies.
- Extend an existing test class for the behavior under test when practical; avoid duplicate test scaffolding and tests that only call private implementation helpers.

## Add high-value coverage

1. Read the current implementation and nearby tests before writing new cases. Identify the observable branches changed by the patch.
2. Use the Kover HTML report to locate uncovered production lines and branches. Prioritize public arithmetic decisions and failure paths over trivial getters or redundant examples.
3. Assert exact expected values or a meaningful invariant. For arithmetic, cover relevant signs, zero, carry/borrow, word or scale boundaries, division/remainder identities, rounding ties, and exceptions.
4. Keep randomized tests deterministic with explicit seeds and bounded workloads. Use a reference oracle only in a source set where that oracle is available; do not convert exact values through floating-point types to simplify an assertion.
5. Re-run `./gradlew runAllChecks` after test changes. If common behavior needs iOS validation, run `./gradlew :shared:iosSimulatorArm64Test` when that target is available.

For multiplication changes, add focused magnitude cases immediately below and at the measured word-count dispatches, all-ones limbs that force carry propagation, unequal operand lengths, and Karatsuba half-differences that are positive, negative, or zero. Put `java.math.BigInteger` oracle cases in `androidUnitTest`; use `commonTest` for platform-independent fixed results and identities.

## Coverage guardrail

`runAllChecks` invokes `koverVerify`. The global minimum is 87% line coverage and 80% branch coverage, based on the current project baseline. Keep both floors or improve them when production code is added or changed.

When verification fails, inspect `shared/build/reports/kover/html/` and add focused tests for uncovered production paths. Do not lower the minimum, exclude real production classes, inflate percentages with duplicate assertions, or replace behavior checks with tests that merely execute a line. Generated and platform-specific exclusions must remain limited to code that genuinely cannot be covered by the host test setup.
