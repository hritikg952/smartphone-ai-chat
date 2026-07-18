# MG-17 — Quality, Privacy, Compliance, and Operations

## Outcome

Create continuous evidence that the vault is correct, secure, accessible, supportable, and compliant with declared behavior.

## Dependencies

Starts with MG-01 and evolves with every mini-goal; decision D-017.

## Test strategy

- **Domain:** aggregates, state machines, units, time, recurrence, provenance, policy decisions.
- **Repository/database:** encryption open/lock, transactions, migrations, profile isolation, deletes, concurrency.
- **File/security:** key invalidation, corruption, tamper, import limits, backup/restore, plaintext leakage.
- **Integration:** Health Connect, OCR, model runtime, Android permissions, share URI, optional services with fakes/contract tests.
- **UI:** Compose behavior, navigation, process recreation, accessibility, screenshots, adaptive layouts.
- **End-to-end:** onboarding → unlock → profile → record/import → search → export → delete → restore.
- **Safety evaluation:** clinician-approved fixtures and AI red-team suites.

## Work packages

1. Define release risk tiers and required test/review evidence for each tier.
2. Add static analysis, formatting, dependency/secret/license/vulnerability scanning, SBOM, architecture checks, and reproducible/signing controls to CI.
3. Add database/crypto migration fixtures and block release without forward migration tests.
4. Create synthetic/de-identified test-data generators; prohibit real user health data in development, screenshots, fixtures, and issue trackers.
5. Add accessibility checks plus manual screen-reader/switch/large-font/color review.
6. Implement privacy-preserving diagnostics: local health-free logs, redaction tests, user-generated support bundle, and opt-in telemetry only if D-017 approves exact events.
7. Define SLOs for startup/unlock, query, import, sync, OCR, charts, exports, crashes/ANRs, memory, battery, and model thermal impact.
8. Build incident, vulnerability disclosure, dependency update, key compromise, bad drug-data, model rollback, and store-policy response runbooks.
9. Maintain Play Health Apps/Data safety/permissions/privacy-policy evidence from the MG-01 data map.
10. Schedule independent mobile penetration testing, crypto review, privacy review, clinical safety review, and release sign-off.

## Release blocking conditions

- Any plaintext health data/secret in logs, backups, temp files, reports, or analytics.
- Cross-profile access, unlock bypass, corrupt migration/data loss, or unrecoverable key regression.
- Unreviewed medical claim or deterministic safety-data failure.
- Critical/high exploitable security finding.
- Store declarations inconsistent with runtime behavior.
- AI critical-harm evaluation failure or missing source traceability.

## Acceptance criteria

- [ ] CI produces test, scan, dependency, SBOM, and migration evidence for release commits.
- [ ] Real health data is banned from non-production environments and support channels.
- [ ] Privacy/logging tests prove the approved event schema contains no content fields.
- [ ] Incident and rollback exercises have been run, not merely documented.
- [ ] Accessibility and performance budgets are measured on representative devices.
- [ ] Required external reviews have no unresolved release blockers.
- [ ] Play declarations and in-app privacy controls match observed data flows.

## Exit gate

The release evidence bundle is complete and signed by product, engineering, security/privacy, quality, and clinical safety owners.

