---
name: health-vault-wrap-up
description: Refresh the Personal Health Vault planning status after a work session. Use only when the user explicitly invokes `$health-vault-wrap-up` or `/health-vault-wrap-up` to inspect completed work, update the plan/dashboard with evidence-based progress, summarize the session, and prepare a commit/push for approval.
---

# Personal Health Vault Wrap-Up

Run this workflow only after the user explicitly invokes it. Do not automatically update the plan dashboard after ordinary work.

## 1. Collect evidence

1. Read the project `AGENTS.md`, this skill, and the planning source of truth:
   - `plans/personal-health-vault/README.md`
   - `plans/personal-health-vault/DECISIONS.md`
   - `plans/personal-health-vault/TRACEABILITY.md`
   - `plans/personal-health-vault/PROTOTYPE_FAST_TRACK.md`
   - relevant `MG-*` plans
   - `plans/personal-health-vault/personal-health-vault-dashboard.html`
2. Inspect `git status`, the diff, and recent commits. Run focused tests/builds relevant to the finished work and record the exact result.
3. For a changed feature or bug fix, verify that TDD evidence exists: agreed public seam, failing-test-first history or documented red/green slice, and passing focused tests. Do not claim TDD completion without evidence.
4. Treat code, tests, accepted decisions, and reviewed artifacts as evidence. Never infer completion from an intention, an unchecked plan item, or a UI mockup.

## 2. Determine status honestly

Use these mini-goal statuses in the dashboard and plan documents:

- `Planned` — scoped, no implementation evidence.
- `In progress` — implementation has begun but its exit gate is not met.
- `Complete` — the plan’s acceptance criteria and exit gate are met with recorded evidence.
- `Blocked` — work cannot safely proceed; record the concrete blocker and owner decision needed.
- `Deferred` — intentionally out of the current prototype/release scope.

Keep decision states (`Accepted`, `Open`, `Deferred`, `Partially accepted`) aligned with `DECISIONS.md`. Do not turn a deferred security requirement into “complete” because it is intentionally absent from the prototype.

## 3. Refresh the living plan view

1. Update only the documents affected by the evidence: relevant mini-goal plan, `README.md` delivery/status text, `DECISIONS.md`, and `TRACEABILITY.md` where needed.
2. Update `personal-health-vault-dashboard.html` to match the exact mini-goal counts, progress percentage, decision chart, current position, last-reviewed date, and any active blocker/gate. Preserve its tabs, keyboard behavior, dark theme, and responsive layout.
3. Before modifying the dashboard, read the `visualize` skill. Validate the resulting HTML structure and JavaScript interactions; inspect a rendered preview when practical.
4. Keep the dashboard static and self-contained. It must not fetch local files, use network data, or update itself.
5. Ensure the `README.md` dashboard link remains valid.

## 4. Report and request approval

Before staging anything, provide a concise wrap-up containing:

- completed, in-progress, blocked, and deferred mini-goals;
- decisions made or still needed;
- test/build/visual validation results;
- files changed, including the dashboard;
- the proposed commit message and target branch.

Then ask for explicit approval to commit and push to `origin`. Stop and wait. Do not stage, commit, rebase, force-push, or push without that approval.

## 5. After approval

1. Re-check `git status` and confirm the exact staged scope.
2. Commit only the approved changes with a descriptive message.
3. Push normally to the approved remote/branch. If the push is non-fast-forward, fetch and rebase; resolve only scoped conflicts, then push again. Never force-push unless the user explicitly asks.
4. Report the commit hash, push result, and any remaining uncommitted files.
