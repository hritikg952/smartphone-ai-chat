# MG-12 — Health Connect and Wearable Integrations

## Outcome

Ingest selected contextual health/fitness records with least-privilege permissions, provenance, deterministic sync, and graceful degradation.

## Dependencies

MG-01, MG-04, MG-05, MG-10; decision D-014.

## Platform boundary

Android uses Health Connect as the first integration. Apple Health requires an iOS app. Garmin/Fitbit direct integrations require separate vendor API approval, OAuth/service architecture, terms review, rate-limit handling, and likely a backend; they are not implied by Health Connect support.

Health Connect requires supported Android/Google Play environments and is unavailable in some contexts such as unsupported OS/devices or work profiles. The app must remain fully usable manually.

## Initial read-only data set

Start with the smallest approved set: weight, height, resting heart rate, heart rate, steps, sleep sessions, and optionally blood pressure. Each type needs a visible user benefit, Play declaration, permission rationale, and retention rule. Do not request broad/history/background permissions by default.

## Work packages

1. Produce a per-data-type purpose/permission matrix and complete MG-01 privacy/Play review.
2. Implement availability/version checks and education states: unavailable, update/install required, permissions missing, partial access, revoked, paused.
3. Request permissions just in time and only for enabled data types; provide Manage access and Sync on/off controls.
4. Build a `HealthDataSource` adapter with pagination, time windows, changes/deletion tokens where supported, external IDs, client versions, origin metadata, and idempotent upsert.
5. Use bounded foreground sync first. Background/history access is a separate opt-in with platform/Play eligibility and battery/privacy justification.
6. Map records to MG-10 observations preserving source, device, recording method, original unit, external ID, and last modified time.
7. Define deduplication and deletion: do not merge different origins blindly; remove/update imported copies when source deletion/change is observed according to user policy.
8. Add sync status, last successful time, per-type counts/errors, retry/cancel, and local purge without revoking Health Connect source data.
9. Evaluate direct wearable providers as independent later ADRs and threat models.

## Tests

- Fake-client contract tests for pagination, partial permissions, duplicates, updates, deletions, revocation, token reset, large history, time zones, and intermittent failures.
- Instrumented tests on supported OS variants plus unavailable/work-profile-like states where feasible.
- Verify profile association: imported data cannot switch profiles implicitly.
- Data deletion/accounting tests for local purge and consent revocation.

## Acceptance criteria

- [ ] App requests only approved data-type permissions with a visible purpose.
- [ ] Manual vault features work when Health Connect is absent or revoked.
- [ ] Sync is idempotent and preserves origin/provenance.
- [ ] Partial access and stale/failing sync never appear as complete/current data.
- [ ] User can pause, manage permissions, and delete imported local copies.
- [ ] Play Health Apps and Data safety declarations match actual behavior.

## Exit gate

Permission UX, sync correctness, provenance, and revocation/deletion pass privacy and platform review for each enabled type.

