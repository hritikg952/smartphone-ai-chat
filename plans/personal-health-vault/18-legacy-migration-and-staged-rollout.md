# MG-18 — Legacy Migration and Staged Rollout

## Outcome

Ship the product pivot without ambiguous coexistence, user-data loss, credential leakage, or an irreversible big-bang cutover.

## Dependencies

MG-02 through MG-17; decision D-018.

## Legacy inventory

- In-memory conversations (normally gone on process restart).
- Downloaded LiteRT model files under app-private storage.
- Hugging Face token configuration exposed through generated client `BuildConfig`.
- Chat routes/UI/state, placeholder attachments, model selector/download dialogs, conversation repository, chat tests/docs, and older tickets.

## Migration strategy

1. **Baseline tag/build:** archive a reproducible pre-pivot build and schema/data inventory.
2. **Parallel shell:** land MG-02 behind an internal feature flag while old chat remains isolated.
3. **Security foundation:** add MG-03/04 with synthetic data and destructive migration tests; no real health feature before gate.
4. **Internal dogfood:** local-only core vault on non-production/synthetic data, then approved testers with clear reset expectations.
5. **MVP verticals:** enable emergency/medication/vitals by independent flags and migrations.
6. **Full vault/integrations/AI:** stage each capability only after its own exit gate.
7. **Legacy retirement:** remove or quarantine chat code, HF token field, old model download path, stale docs/tickets, and unused permissions/dependencies.
8. **Public rollout:** internal → closed → small percentage → expanded rollout with pause/rollback criteria.

## Data handling decisions

- In-memory chats are not health records and should not be imported automatically.
- If chat export is required, label it as legacy text, exclude it from clinical analytics/AI grounding, and require explicit user action.
- Existing model files may be deleted after consent/notice or adopted only if their hash/license/manifest matches MG-16.
- Remove `HF_TOKEN` client build configuration before any health release, rotate any token that may have entered distributed artifacts, and scan history/artifacts according to security guidance.
- Package/application-ID change determines whether store upgrade migration is possible; record D-002 with product/release implications.

## Work packages

1. Create feature-flag and data-migration ownership matrix with rollback capability and kill switches for integrations/AI.
2. Add versioned onboarding explaining the pivot, data model, offline/account state, backup/recovery, emergency exposure, and AI optionality.
3. Define reset/export/recovery paths for alpha/beta users and support scripts that reveal no health content.
4. Build migration telemetry only from approved non-content counters; verify with privacy tests.
5. Rehearse upgrade, downgrade-block, failed migration, low storage, key invalidation, restore, and app-uninstall scenarios.
6. Update app/store identity, permissions, privacy policy, declarations, screenshots, help content, release notes, and security contact.
7. Remove dead code/dependencies/resources only after reachability and rollback window review; retain architectural history in docs, not production.
8. Define rollout pause criteria: crashes/ANRs, unlock/migration failure, data mismatch/loss, battery/storage regression, security/safety incident, policy rejection.

## Acceptance criteria

- [ ] Every released version has tested upgrade and rollback/forward-fix strategy.
- [ ] No legacy credential, permission, exported component, data file, or model path bypasses new security policy.
- [ ] Legacy chat data disposition is explicit and user-visible where relevant.
- [ ] Health features are independently gateable and failure does not lock users out of export/recovery.
- [ ] Store identity/declarations/support content match the new product.
- [ ] Rollout dashboards contain no health content and have objective pause thresholds.
- [ ] Legacy removal is verified by dependency/code/resource scans.

## Exit gate

The public health-vault build has passed upgrade rehearsal, security/compliance release gates, support readiness, and staged-rollout approval; legacy chat paths are unreachable or removed.

