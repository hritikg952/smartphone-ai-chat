# Consent, Privacy, Retention, and User-Rights Baseline

## Consent taxonomy

Consent is purpose-specific. A general acceptance of terms must not silently
authorize camera access, health-data import, AI processing, analytics, sharing,
or a future cloud service.

| Consent / notice | Prototype state | Required choice |
|---|---|---|
| Prototype-risk acknowledgement | Required before entering health data | Acknowledge storage is not yet production-encrypted and use test data |
| Camera/gallery import | Deferred | Just-in-time explanation and Android permission/system picker choice |
| OCR processing | Deferred | Explain on-device processing, draft status, and source retention |
| Health Connect data type | Deferred | Separate purpose and platform permission per data type |
| AI use of selected records | Deferred | Explicit source selection, purpose, model destination, and save policy |
| Export/share | Deferred | Exact record/field/recipient preview immediately before disclosure |
| Remote diagnostics/analytics | Prohibited | New opt-in decision and schema review required |
| Cloud backup/sync | Deferred | Separate service, payload, key, retention, deletion, and recovery consent |

## Consent receipt schema

When a workflow depends on consent, store a local receipt containing:

- receipt ID and schema version;
- notice/policy version and locale;
- purpose ID and plain-language description;
- profile ID;
- data categories and sources;
- grantee/processor/destination;
- granted, denied, or withdrawn state;
- timestamp and source time zone;
- optional expiry;
- revocation timestamp and effect;
- app version and evidence of the UI action.

The receipt must not duplicate health content. Withdrawal stops future
processing and triggers the declared deletion/invalidation actions; it does not
pretend that an already delivered export can be recalled.

## Privacy-copy checklist

- Identify the product/operator and a privacy contact before distribution.
- Explain each data category, purpose, on-device storage, and any external
  recipient in clear language.
- State that the private prototype lacks production vault encryption.
- Separate required processing from optional features.
- Put important limitations at the decision point, not only in terms.
- Explain retention, deletion, derived artifacts, backups, and exports.
- Explain how to access, correct, export, and erase local records.
- State that uninstall/app-data clear removes the local copy but cannot erase a
  file the user exported elsewhere.
- Describe complaint and incident-contact routes before public distribution.
- Never claim legal compliance, zero knowledge, or medical approval without
  evidence and reviewer sign-off.
- Reconcile the copy with the runtime data map, Android permissions, bundled
  SDKs, Play Health declaration, and Play Data safety form for every release.

## Retention schedule

| Data | Default | Exception / review | Deletion proof |
|---|---|---|---|
| Primary health record | Until user deletes profile/record | Legal hold is not assumed for a personal local app | Repository and UI no longer return it; linked derivatives removed |
| Imported document | Until user deletes it | None by default | Source, temp copy, thumbnail, indexes, OCR/AI derivatives removed |
| Rejected OCR draft | Delete immediately after rejection or bounded draft expiry | Product may offer explicit draft save later | Draft and cache absent |
| Confirmed OCR provenance | Same as confirmed record/source | Keep engine/version metadata without unnecessary extracted content | Cascades with owner |
| AI prompt/output | No persistence by default | Explicitly saved, clearly labeled note only after MG-16 | Conversation/cache/citations removed with source/profile |
| Local diagnostics | Proposed 7-day or size-bounded rotation | Final duration requires security approval | Clear action and rotation test |
| Consent receipt | While purpose/record is active plus approved audit period | Withdrawal evidence may be minimized rather than fully erased | No health payload; state marked withdrawn |
| Temporary export/share file | Delete immediately after handoff or short bounded expiry | Destination copy is controlled by recipient/user | Temp file absent and URI grant revoked |
| Backup/sync data | None in prototype | Requires separate approved schedule | No backup produced |

## Local user-rights procedure

1. **Access:** show all records and their provenance in-app; do not require a
   support request for local data.
2. **Correction:** edit through domain rules and preserve only the minimum
   history required by the approved audit model.
3. **Deletion:** present scope, require confirmation, delete primary and derived
   local artifacts, and report any user-controlled exports that cannot be
   recalled.
4. **Withdrawal:** stop future optional processing and invalidate derived access
   or queued work.
5. **Grievance/contact:** before external distribution, publish the responsible
   operator and contact route approved by India counsel.
6. **Incident request:** authenticate the requester proportionately without
   collecting unnecessary identity documents.

India-specific applicability, notice form, rights handling, grievance duties,
and transition dates under the DPDP Act and final 2025 Rules require counsel
confirmation for the actual operator and release date.
