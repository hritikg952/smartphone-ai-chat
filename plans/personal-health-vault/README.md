# Personal Health Vault — Master Implementation Planning Set

**Status:** Finalized planning + prototype brief  
**Created:** 2026-07-18  
**Product direction:** Pivot the current Android local-AI chat prototype into an Android-first, local-first personal health vault.

This folder is the source of truth for the future implementation. It deliberately makes no production-code changes. Every mini-goal has an independent plan, an exit gate, and explicit dependencies so work can later be converted into tickets without rediscovering architecture or safety constraints.

## Non-negotiable product principles

1. Health data is private by default, encrypted at rest, never logged, and never sent off device without explicit, purpose-specific consent.
2. “Zero knowledge” applies only when a server exists and its inability to decrypt is proven by the key design. Local-only storage is not marketed as zero knowledge.
3. Biometrics authorize key use; they are not a replacement for encryption, recovery, or account identity.
4. Emergency access is a deliberately minimized, user-approved projection of vault data. It is not a back door into the encrypted vault.
5. OCR and AI output are untrusted drafts until the user confirms them. The app must not diagnose, prescribe, or silently modify clinical records.
6. Deterministic rules and authoritative drug-data sources must drive safety-critical alerts. A generative model may explain or summarize but must not invent interactions.
7. The Android app is the first client. Apple Health, Face ID, Apple Wallet, and iOS widgets require a separate iOS client/workstream and are not Android implementation tasks.
8. No advertising SDKs or unrelated analytics are permitted in health-data processes.

## Delivery sequence

| Wave | Mini-goals | Outcome |
|---|---|---|
| 0 — Definition | MG-01 | Intended use, safety, privacy, and jurisdiction decisions are signed off. |
| 1 — Foundation | MG-02 to MG-06, MG-19 | Modular shell, security boundary, encrypted persistence, profiles, navigation, and a future-service boundary exist. |
| 2 — Safe MVP | MG-07, MG-08, MG-10 | Emergency card, medication/provider records, and core vitals/allergy/immunization records work offline. |
| 3 — Full vault | MG-09, MG-11, MG-13, MG-14 | Documents/OCR, daily logs, insurance, and search are usable. |
| 4 — Ecosystem | MG-12, MG-15 | Health Connect ingestion and granular exports/sharing are controlled and auditable. |
| 5 — Intelligence | MG-16 | Grounded, reviewable AI assistance is added behind safety gates. |
| 6 — Release | MG-17, MG-18 | Quality/compliance gates pass and legacy chat behavior is retired or migrated. |

Do not begin a wave while a required exit gate from an earlier wave is open. Within a wave, parallel work is allowed only where plan dependencies permit it.

## Mini-goal index

| ID | Plan | Depends on |
|---|---|---|
| MG-01 | [Product, safety, privacy, and regulatory baseline](01-product-safety-and-regulatory-baseline.md) | None |
| MG-02 | [Architecture pivot and modular application shell](02-architecture-pivot-and-app-shell.md) | MG-01 |
| MG-03 | [Vault cryptography, key lifecycle, and biometric access](03-vault-security-keys-and-biometrics.md) | MG-01, MG-02 |
| MG-04 | [Encrypted persistence, file vault, backup, and recovery](04-encrypted-persistence-and-recovery.md) | MG-02, MG-03 |
| MG-05 | [Profiles, dependents, consent, and audit](05-profiles-dependents-consent-and-audit.md) | MG-01, MG-03, MG-04 |
| MG-06 | [Navigation, home dashboard, and design system](06-navigation-home-and-design-system.md) | MG-02, MG-05 |
| MG-07 | [Emergency card and external emergency access](07-emergency-card-and-external-access.md) | MG-03 to MG-06 |
| MG-08 | [Medication, prescriptions, and provider history](08-medications-prescriptions-and-providers.md) | MG-04 to MG-06 |
| MG-09 | [Medical reports, document timeline, and OCR](09-reports-document-timeline-and-ocr.md) | MG-04 to MG-06 |
| MG-10 | [Vitals, allergies, immunizations, and trends](10-vitals-allergies-immunizations-and-trends.md) | MG-04 to MG-06 |
| MG-11 | [Symptoms, mood, appointments, and visit preparation](11-symptoms-mood-appointments-and-visit-prep.md) | MG-05, MG-06, MG-10 |
| MG-12 | [Health Connect and wearable integrations](12-health-connect-and-wearables.md) | MG-01, MG-04, MG-05, MG-10 |
| MG-13 | [Insurance and billing vault](13-insurance-and-billing-vault.md) | MG-04 to MG-06 |
| MG-14 | [Private global search](14-private-global-search.md) | MG-08 to MG-11, MG-13 |
| MG-15 | [Granular sharing, PDFs, and expiring links](15-granular-sharing-and-exports.md) | MG-03 to MG-05, relevant feature data |
| MG-16 | [AI assistance, grounding, and clinical safety](16-ai-assistance-and-clinical-safety.md) | MG-08 to MG-12, MG-14, MG-17 test harness |
| MG-17 | [Quality, privacy, compliance, and operations](17-quality-privacy-compliance-and-operations.md) | Starts at MG-01; release gate for all |
| MG-18 | [Legacy migration and staged rollout](18-legacy-migration-and-staged-rollout.md) | MG-02 to MG-17 |
| MG-19 | [Local-first data, optional cloud, sync, and recovery boundary](19-local-first-data-sync-and-recovery-boundary.md) | MG-02 to MG-05 |
| MG-20 | [Android verification, emulator, and UI-regression workflow](20-android-verification-emulator-and-ui-regression.md) | MG-02, MG-06 |

## Supporting control documents

- [Vision baseline and current-state gap](00-vision-baseline-and-current-state.md)
- [Questions for owner review](REVIEW_QUESTIONS.md)
- [Prototype fast track and its explicit boundaries](PROTOTYPE_FAST_TRACK.md)
- [Copy-ready dark UI design prompt](UI_DESIGN_PROMPT.md)
- [Figma design context and implementation alignment](FIGMA_DESIGN_CONTEXT.md)
- [Requirement traceability matrix](TRACEABILITY.md)
- [Decision register](DECISIONS.md)
- [Official reference baseline](REFERENCES.md)

## How to use each plan later

1. Resolve every item marked `Decision required` before implementation begins.
2. Turn the work packages into small tickets while preserving acceptance criteria and security tests.
3. Record architecture decisions in `DECISIONS.md`; do not silently change cross-cutting assumptions.
4. Update `TRACEABILITY.md` when a requirement, goal, or release scope changes.
5. Treat the exit gate as the definition of complete. A UI demo alone is not completion.

## Legacy ticket disposition

The existing `tickets/TICKET-1` through `TICKET-9` describe the older medicine-scanning/chat direction. They are not implementation-ready for health data.

| Legacy ticket | Disposition |
|---|---|
| TICKET-1 navigation | Re-plan under MG-02 and MG-06; retain the single-activity Compose concept. |
| TICKET-2 camera and TICKET-3 capture | Re-plan under MG-09 with encrypted files, URI permission hygiene, metadata stripping, and document lifecycle. |
| TICKET-4 multimodal and TICKET-5 vision backend | Defer to MG-16; not a prerequisite for deterministic OCR or vault storage. |
| TICKET-6 medicine prompt | Replace with schema-constrained extraction, confidence, provenance, and user confirmation in MG-09/MG-16. |
| TICKET-7 medicine database | Replace with the encrypted, profile-scoped schema in MG-04/MG-08. Never store health records in the proposed plaintext Room design. |
| TICKET-8 medicine table | Reframe as responsive medication schedule/history/detail UI in MG-08. |
| TICKET-9 chat on medicine data | Replace with consented, grounded retrieval and safety policy in MG-16. |
