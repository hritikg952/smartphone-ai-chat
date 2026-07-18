# Prototype Fast Track — Personal Health Vault

**Status:** Approved direction from owner review on 2026-07-18.  
**Purpose:** Deliver a quick, private-development prototype before the encrypted production vault work begins.

## Prototype objective

Turn the existing chat prototype into the beginning of **Personal Health Vault**: a single-user Android app for organizing personal health records locally. Preserve the current local AI chat/model code in an archived feature area for later work.

## Included in the fast track

- Rename the visible product to Personal Health Vault.
- Single self profile only; no dependent/caregiver profiles.
- Local username/password screen with no inactivity timeout for this prototype.
- Modular app shell and navigation, designed so future integrations can be added without rewriting feature boundaries.
- Manual personal-record entry and local display for the first selected health modules.
- On-device, plug-and-play OCR adapter with separate reusable documentation; no cloud OCR fallback.
- Basic local, content-free diagnostic logs.
- Preserve/archived current AI chat code and model download flow; do not integrate it into vault records yet.

## Explicitly deferred

- Vault encryption, biometric access, password recovery mechanism, encrypted export/backup, and cloud synchronization.
- Android backup/restore feature.
- Health Connect, ABHA/ABDM, Garmin, Fitbit, iOS, Google Wallet, sharing/export, reminders, interaction checking, and advanced AI.
- Dependent profiles and any public-release or medical claims.

## Prototype constraints

- The username/password is an access-flow prototype, **not** a substitute for encrypted storage.
- Do not describe the prototype as an encrypted vault, zero-knowledge product, or secure medical-record service.
- Keep Android system backup disabled while there is no designed, tested backup/restore flow.
- Use synthetic or developer-controlled test data during development and demonstrations. If real personal records are entered, the owner accepts that the planned encryption guarantees do not yet exist.
- Do not add remote analytics, cloud OCR, or any health-data upload.
- Store recovery metadata only as optional/reserved schema/configuration. It cannot recover a forgotten existing password until an actual recovery-key design is implemented.

## Future-ready seams required now

| Seam | Prototype implementation | Later upgrade |
|---|---|---|
| Authentication | Local username/password session | Password-derived vault-key unwrap, optional biometric gate, recovery envelope |
| Data repository | Local repository contracts | Encrypted database and encrypted document store behind the same contracts |
| Documents/OCR | On-device `OcrEngine` interface | Replace/add engine without changing report UI/domain workflow |
| AI chat | Archived module/feature boundary | Grounded, profile-scoped assistant |
| Integrations | No-op interface/adapters | Health Connect, ABHA/ABDM, wearables, sync |
| Backup | Disabled | User-initiated encrypted backup and recovery restore |

## Definition of a successful prototype

The app’s visible product identity and navigation have pivoted to Personal Health Vault; a single user can exercise the selected local health-record workflow; OCR can be swapped independently; the old chat remains intact but out of the main journey; and no deferred security/integration capability is implied or advertised.

## Handoff rule

Before moving a prototype build to public distribution or positioning it for real sensitive health records, resume MG-03 and MG-04: vault keys, encrypted persistence, document encryption, recovery, deletion, and backup design must be completed first.

