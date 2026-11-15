---
name: kbignum-architecture
description: Use when reviewing or changing KBigNum public APIs, naming, module boundaries, Kotlin Multiplatform source sets or targets, publication compatibility, or cross-platform test and CI policy.
---

# KBigNum architecture and API

Read AGENTS.md, docs/architecture.md, shared/build.gradle.kts, the current source sets, relevant public declarations and tests, README.md, and CI workflows before recommending changes. Treat plan.md as historical context. Separate verified repository facts from recommendations.

## Keep the arithmetic core portable

- Put exact integer and decimal behavior in shared/src/commonMain. Keep JVM, Android, and native APIs out of common code unless a real platform capability requires them.
- Preserve sign, zero, normalization, overflow, parsing, rounding, and exception behavior. Never route exact results through floating-point or fixed-width conversions that can lose information.
- Keep KBigInteger, KBigDecimal, and KBigMath cohesive. Split files only when the new boundary makes algorithms easier to understand or maintain; do not add service layers or wrappers without a concrete need.
- Treat numeric values as immutable. Audit constructor inputs, mutable arrays, cached state, and returned collections for aliases or races. A private cache is acceptable only when its behavior is safe on every supported target and its benefit is measured.
- Use expect/actual only for real target-specific behavior. Check whether an existing public expect/actual API is part of the product contract or leftover platform scaffolding before adding more actual implementations.

Known current risks to check first:
- KBigInteger exposes a public sign/magnitude constructor and a public IntArray magnitude property. The input and returned array can be mutated, and the constructor does not enforce a canonical sign/magnitude pair. Do not assume an instance is immutable or normalized at API boundaries.
- KBigDecimal keeps a mutable per-instance power-of-ten cache. Review concurrent use on JVM and Native targets before relying on value-like thread safety.
- Platform/getPlatform is public expect/actual code but is not used by the arithmetic implementation. Every added target currently needs an actual implementation unless this API is redesigned.

Treat these as review findings, not permission to make a source- or binary-breaking change. Prefer a staged safe API and migration plan; remove or restrict public declarations only with compatibility and release-version consequences made explicit.

## Design and name the public API consistently

- Use PascalCase for types and lowerCamelCase for functions and properties. Use isX names for Boolean questions.
- Use parse(String) as the canonical text entry point, fromInt/fromLong for primitive factories, and toX for conversions. Keep String.toKBigInteger/toKBigDecimal as ergonomic extensions that delegate to the parser.
- Keep named arithmetic methods and operators behaviorally aligned. Document non-obvious defaults, especially decimal division precision and rounding.
- Avoid introducing another public spelling for an existing property or method. Choose one canonical signum and scale accessor after checking Kotlin, Java, and Swift call sites; deprecate duplicates through a compatibility plan.
- Use one consistent style for enum entries. For new rounding-mode names, prefer conventional uppercase names such as HALF_UP, while keeping current entries source-compatible through aliases or deprecation when needed.
- Document valid inputs, exactness, overflow, rounding, and thrown exceptions in KDoc and examples. Distinguish exact conversions from approximate Float/Double conversions.

The current API already has named arithmetic methods and Kotlin operators, but it also has duplicate property/function accessors, PascalCase rounding enum entries, and fromString factories beside String conversion extensions. Do not rename these in place. Add parse(String) as the canonical parser with compatibility forwarding, deprecate old names when appropriate, and remove them only in a planned breaking release.

For a public API change, inspect all targets' generated API shape, including Swift-facing names where relevant. The current Gradle setup has no declared API dump or compatibility check; consider adding one as the public surface stabilizes, and keep the snapshot reviewable.

## Add platform targets only with a support plan

Read the declared targets in shared/build.gradle.kts and the current Kotlin target-stability documentation; do not infer support from common code compiling on one host.

- Add a target when there is a real consumer or product need and the project can compile, run relevant common tests, publish, document, and maintain it.
- A target is supported only when the CI/build and publication configuration cover it. Keep common arithmetic shared rather than adding platform forks for unmeasured performance assumptions.
- Prefer Kotlin's default source-set hierarchy. Add custom dependsOn edges only for a real shared platform-specific source set, and resolve hierarchy warnings before expanding the target matrix.
- Account for target-specific requirements such as Node/browser runners, an Apple host for simulator execution, and one actual implementation for each public expect declaration.
- Re-evaluate platform stability and Kotlin/Gradle compatibility at the time of a change; these facts can change between releases.

As of the current dev source tree, the configured targets are Android, desktop JVM, and iOS (iosX64, iosArm64, iosSimulatorArm64). The repo has no JS or Wasm target. Keep the existing CI job for the configured iOS simulator tests. If there is demand for web support, Kotlin/JS on Node is a practical next target for this pure common library; Kotlin's current documentation lists JS as stable and Wasm as beta. Choose a browser target from consumer needs rather than treating JS and Wasm as interchangeable.

## Test the contract on the targets you support

- Put portable behavior tests in shared/src/commonTest using kotlin.test. Keep deterministic seeds and bounded property/fuzz tests with exact expected values or mathematical invariants.
- Keep java.math.BigInteger and BigDecimal oracle tests in shared/src/androidUnitTest. Never pull JVM-only APIs into commonTest.
- Use commonTest once and execute it on the supported runtime matrix; do not copy the same contract tests into every platform source set.
- Add platform-specific tests only for actual platform behavior. Ensure each supported target at least compiles in CI and regularly executes common tests on representative JVM, Native, and any added JS/Wasm backend.
- The current workflow runs Linux host checks on JDK 17 and 21 and an iOS simulator test job on macOS. Keep both paths active when changing CI.
- If JS/Node is added, run the generated Node test task in CI. If an Apple target is supported, run simulator tests on a macOS runner where available. Report unavailable target execution as a gap, not as a passing check.
- Treat Kover host coverage as host coverage only. Keep the existing line and branch gates; do not present them as evidence that Native or web code executed.

## Review output

Summarize what is in the repository, the concrete risks, and the recommended order of work. Make breaking API risks, target support gaps, and unverified platform behavior explicit. Update docs/architecture.md and user-facing platform/API docs when actual support or public contracts change.
