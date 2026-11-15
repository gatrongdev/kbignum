---
name: kbignum-opencode-orchestrator
description: Use when OpenCode (running DeepSeek) implements, continues, or hands off KBigNum work that Codex scopes, coordinates, and reviews — covering the division of labor, worktree rules, one-session-per-task caching, parallelization, and handoffs.
---

# Codex / OpenCode orchestration

This repository splits implementation work between two agents. Follow this division for any code change.

- **Codex** owns scope, coordination, independent review, and QC. It reads `AGENTS.md`, decides what gets built, prepares the worktree and branch, assigns tasks, reviews OpenCode's output against the requirements and tests, runs final checks, commits, and handles publication.
- **OpenCode (running DeepSeek)** is the primary implementation executor. It turns an assigned task into code and tests, runs the checks that cover the change, and returns a concise handoff.

Codex drives; OpenCode implements. OpenCode does not expand scope or self-approve; publication stays with Codex unless a specific step is explicitly delegated.

## Worktree rules

Implementation happens in the dedicated worktree and branch that Codex prepared. Follow `AGENTS.md` ("Worktrees") and do not deviate:

- Work in the assigned worktree only. Do not implement in the primary checkout and move the patch afterward.
- Do not switch branches in, reset, clean, or remove any worktree — yours or another's. Preserve all existing user changes and worktrees.
- The branch is named `codex/<short-task-name>`. Confirm the branch and path are unused before starting; report the branch, worktree path, and checks in the handoff.
- Codex handles integration per `AGENTS.md`: it commits the reviewed result, pushes the branch, and opens the pull request targeting `dev`. OpenCode does not push branches or open/merge pull requests unless Codex explicitly delegates a specific step.

If no worktree was prepared, stop and ask Codex to set one up rather than implementing in the current checkout.

## Load the right skills

Load the project and global OpenCode skills that apply to the task, and skip unrelated ones. Loading a skill you do not need adds noise; not loading one you do need risks breaking an invariant.

- For arithmetic changes (KBigInteger, KBigDecimal, KBigMath, division, rounding, or their tests), invoke `@kbignum-arithmetic` and follow [`kbignum-arithmetic`](../kbignum-arithmetic/SKILL.md).
- For test changes, coverage, or Kover guardrails, invoke `@kbignum-unit-testing` and follow [`kbignum-unit-testing`](../kbignum-unit-testing/SKILL.md).
- For general Gradle, release, or documentation-only work, follow `AGENTS.md` and existing contribution guidance.
- Read `docs/architecture.md` for the module, source-set, and test layout before touching boundaries.

## Goal tracking (Codex)

Codex uses its native Goal tracking for sustained objectives that span multiple sessions or have meaningful milestones. OpenCode keeps implementation continuity through its session ID instead (see next section).

- Create a Goal when the objective spans sessions or has milestones worth tracking; short self-contained tasks do not need one.
- Keep the objective and its progress current as work proceeds.
- Mark a Goal complete only after the outcome is actually achieved, not when work merely stops.
- This is Codex coordination. OpenCode does not manage Codex Goals; it continues its own session by ID.

## One session per task

Keep one OpenCode session per task and continue that same session for every follow-up on it. When supported, the provider may reuse cached prompt context across turns; caching is provider-managed and not guaranteed.

- Start one session per task; continue it for revisions, review fixes, and added tests.
- Do not start a fresh session just because a follow-up arrived — use the existing session ID (see CLI examples below).
- Do not configure periodic cache-warming (no `warming` config, no cron or background warm-up job). Natural continuation is the intended mechanism.

## Parallelization

Parallelize only work that is independent and non-overlapping, and keep it concrete:

- Safe: separate read-only investigations (independent questions, no shared edits), or clearly owned implementation slices where each session/agent owns disjoint files and none depends on another's output.
- Unsafe: multiple writers on the same files, edits that depend on each other's output, or shared build/coverage state. Serialize those.
- When in doubt, run serially. Do not fan out subagents or sessions to speed up overlapping edits.

## Handoff

Return a concise handoff to Codex with exactly:

- **Changed files** — the files edited and, for each, what changed.
- **Decisions** — the non-obvious choices made and why (algorithm selection, threshold, API behavior, test placement).
- **Checks** — the commands run and their results (`./gradlew runAllChecks`, `./gradlew :shared:iosSimulatorArm64Test`, benchmarks).
- **Known issues** — anything incomplete, untested, or needing Codex's independent review.

Keep it factual; do not claim QC is done. Codex handles publication unless it explicitly delegates a specific step.

## CLI reference

The project default model is already `opencode-go/deepseek-v4-pro` (set in the root `opencode.json`), so a plain `opencode run` in this repo selects DeepSeek. State the model explicitly when you want to be certain.

Start a new DeepSeek implementation session and capture its session ID. Use `--title` to label the task and `--format json` so the returned session ID is observable in the output:

```sh
opencode run --model opencode-go/deepseek-v4-pro --title "kbignum <task>" --format json "<task description>"
```

Use `--session <id>` for every follow-up on that task:

```sh
opencode run --session ses_4f2a1c "<follow-up>"
```

Reserve `--continue` for when the most recent session is unambiguous (only one task active):

```sh
opencode run --continue "<follow-up>"
```

## What not to do

- Do not use `--auto` (auto-approves permissions).
- Do not configure periodic cache-warming or any background warm-up job.
- Do not change global OpenCode configuration (`~/.config/opencode/`). Only this project's root `opencode.json` and `.agents/skills/` are in scope.
- Do not expand scope or self-approve work that Codex owns. Do not push branches, open PRs, or merge unless Codex explicitly delegates a specific step.
