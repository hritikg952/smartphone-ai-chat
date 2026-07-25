# Prototype Fast Track — Personal Health Vault

**Status:** Approved direction from owner review on 2026-07-18.  
**Purpose:** Deliver a quick, private-development prototype while clearly
separating implemented local encryption from unreleased production hardening.

## Prototype objective

Turn the existing chat prototype into the beginning of **Personal Health Vault**: a single-user Android app for organizing personal health records locally. Preserve the current local AI chat/model code in an archived feature area for later work.

## Included in the fast track

- Rename the visible product to Personal Health Vault.
- Single self profile only; no dependent/caregiver profiles.
- Local username/password screen with no inactivity timeout for this prototype.
- Prototype vault encryption using Android Keystore-wrapped local keys and
  encrypted local record/document stores.
- Modular app shell and navigation, designed so future integrations can be added without rewriting feature boundaries.
- Manual personal-record entry and local display for the first selected health modules.
- On-device, plug-and-play OCR adapter with separate reusable documentation; no cloud OCR fallback.
- Basic local, content-free diagnostic logs.
- Preserve/archived current AI chat code and model download flow; do not integrate it into vault records yet.

## Explicitly deferred

- Biometric access, password recovery mechanism, encrypted export/backup, and cloud synchronization.
- Android backup/restore feature.
- Health Connect, ABHA/ABDM, Garmin, Fitbit, iOS, Google Wallet, sharing/export, reminders, interaction checking, and advanced AI.
- Dependent profiles and any public-release or medical claims.

## Prototype constraints

- The username/password is local vault access for the prototype, not a cloud
  identity or recovery mechanism.
- Do not describe the prototype as zero-knowledge or as a production secure
  medical-record service.
- Keep Android system backup disabled while there is no designed, tested backup/restore flow.
- Use synthetic or developer-controlled test data during development and
  demonstrations. MG-03/MG-04 provide local encrypted storage, but production
  hardening, recovery, migration, and independent security review are still
  incomplete.
- Do not add remote analytics, cloud OCR, or any health-data upload.
- Store recovery metadata only as optional/reserved schema/configuration. It cannot recover a forgotten existing password until an actual recovery-key design is implemented.

## Future-ready seams required now

| Seam | Prototype implementation | Later upgrade |
|---|---|---|
| Authentication | Local username/password unlock with Android Keystore-wrapped DEK | Optional biometric/device credential gate, recovery envelope, rate limiting |
| Data repository | Encrypted local record/document stores behind repository contracts | Normalized encrypted database, migration fixtures, quotas, crash recovery |
| Documents/OCR | On-device `OcrEngine` interface | Replace/add engine without changing report UI/domain workflow |
| AI chat | Archived module/feature boundary | Grounded, profile-scoped assistant |
| Integrations | No-op interface/adapters | Health Connect, ABHA/ABDM, wearables, sync |
| Backup | Disabled | User-initiated encrypted backup and recovery restore |

## Definition of a successful prototype

The app’s visible product identity and navigation have pivoted to Personal Health Vault; a single user can exercise the selected local health-record workflow; OCR can be swapped independently; the old chat remains intact but out of the main journey; and no deferred security/integration capability is implied or advertised.

## Handoff rule

Before moving a prototype build to public distribution or positioning it for
real sensitive health records, complete the remaining MG-03/MG-04 hardening:
recovery, password policy/rate limiting, biometric/device-credential decision,
database migration fixtures, full derivative deletion, crash/power-loss testing,
backup/restore design, and independent security review.
