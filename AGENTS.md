# KBigNum development guidance

KBigNum is a Kotlin Multiplatform arbitrary-precision math library. Its arithmetic implementation lives in `shared/src/commonMain`; shared behavior is tested in `shared/src/commonTest`. Android and iOS source sets currently provide platform identity actuals, so keep arithmetic in common code unless a real platform API requires otherwise.

## Architecture and code quality

- Use [`docs/architecture.md`](docs/architecture.md) for the current module, source-set, and test layout; update it when those boundaries change.
- Keep the mathematical core small and cohesive. This is a library, so do not add app-style layers, repositories, interfaces, or wrappers without a concrete need.
- Keep public APIs focused and stable. Prefer direct implementations and small private helpers over speculative abstractions.
- Make numeric edge cases and error behavior explicit. Do not convert whole values through `Float`, `Double`, `Long`, or similar types in exact operations. Fixed-width limb arithmetic is fine when the algorithm handles overflow deliberately.
- Name algorithms and intermediate values for their role. Comments should explain an invariant or a non-obvious algorithmic choice, not repeat the code.
- Treat `plan.md` as historical context. Check current source, tests, README, and changelog before relying on its descriptions or unchecked items.

## Tests and coverage

- Put platform-independent behavioral tests in `shared/src/commonTest`; use `shared/src/androidUnitTest` for JVM-only reference-oracle or Android-specific tests.
- Add tests for changed behavior, including relevant boundary, sign, zero, rounding, and failure paths. Prefer observable results over tests coupled to private helpers.
- Keep Kover's aggregate minimums at 87% line coverage and 80% branch coverage or higher. `runAllChecks` runs `koverVerify`; do not lower or bypass the gate to accommodate uncovered production logic.
- Use `./gradlew runAllChecks` before integration. Use `./gradlew :shared:iosSimulatorArm64Test` when validating shared behavior on the iOS simulator.

## Worktrees

- For any repository change intended for integration, start in a dedicated worktree based on the latest `origin/dev` (fetch first). Read-only reviews and questions do not need a worktree.
- Before creating it, inspect `git status --short --branch` and `git worktree list`; preserve all existing user changes and worktrees. Do not implement in the primary checkout and move the finished patch afterward.
- Use a dedicated branch named `codex/<short-task-name>` and a sibling worktree directory such as `../kbignum-<short-task-name>`. For example: `git worktree add -b codex/<short-task-name> ../kbignum-<short-task-name> origin/dev`. Confirm the branch and path are unused before creating it.
- Implement, verify, and commit the change in that worktree. Push the branch and open a GitHub pull request targeting `dev` when the change is ready. Do not merge the pull request unless the user explicitly asks.
- Do not switch branches in, reset, clean, or remove another worktree. Keep the completed task worktree available for review; before removing one, confirm it is clean and task-owned. If a change was started in another checkout, transfer only the task's changes and preserve all other edits.
- In the handoff, report the branch, worktree path, checks run, and pull request URL. Do not merge or delete another checkout as part of unrelated work.

## Verification

- Use `./gradlew runAllChecks` for the module's test and quality checks.
- Use `./gradlew :shared:iosSimulatorArm64Test` when checking shared behavior on the iOS simulator; this runs the common tests on that target.
- Choose checks based on the change and available platform environment. Update tests and user-facing docs when public behavior changes.
