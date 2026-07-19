# Data Processing Inventory

## Processing boundary

The prototype's health-record boundary is the Android device. Health data must
not be transmitted off device. The current legacy model downloader may contact
Hugging Face for model files, but it must remain outside vault workflows and
must never receive health content, prompts derived from records, identifiers,
or diagnostics containing user content.

“Deferred” means the category may appear in the roadmap but must not be
collected or processed in the prototype until its dependency gates are closed
and this inventory is updated.

## Field-level inventory

| Category | Status | Source | Purpose / transformation | Storage / destination | Sharing / processor | Retention | Deletion path |
|---|---|---|---|---|---|---|---|
| Local username | Planned prototype | User entry | Local access-flow identification; not cloud identity | App-private local auth store | None | Until reset or app data removal | Settings reset; uninstall/app-data clear |
| Local password verifier | Planned prototype | User entry | Verify local access; never retain plaintext password | App-private salted password-verifier record | None | Until password reset or app data removal | Replace on reset; erase with app data |
| Self-profile identity | Planned prototype | User entry | Label the single local profile | Local repository | None | Until profile deletion | In-app profile deletion plus derived-data cascade |
| Health record fields | Planned by feature | User entry | Organize medications, vitals, allergies, immunizations, providers, reports, symptoms, or insurance data | Local profile-scoped repository | None | User controlled; no automatic expiry by default | Delete record and linked provenance/derived artifacts |
| Date, time, units, provenance | Planned by feature | User/device | Normalize values while preserving original input and source | Stored with owning record | None | Same as owning record | Cascade with owning record |
| Camera/gallery document | Deferred until document workflow | User-selected URI or capture | Import a source document; strip unnecessary metadata where feasible | App-private document store | Android system picker/camera only; no cloud OCR | Until document deletion | Delete source, thumbnails, temp copies, URI grants, OCR derivatives |
| OCR text and structured draft | Deferred | On-device OCR from document | Produce untrusted, reviewable draft | Local draft/derived record linked to source and engine version | On-device OCR engine only | Delete after rejection or retain with confirmed record according to product rule | Delete draft and indexes; cascade from source deletion |
| AI prompt/output | Archived; no vault access | Manual legacy chat only | Optional local chat | Existing in-memory conversation; local model runtime | No health-record processor; model file download only | Current conversation lifetime | Clear conversation/process; later policy required before persistence |
| Model file and download metadata | Existing legacy | Hugging Face | Run optional on-device model | App-private model directory | Hugging Face receives ordinary network request metadata, not health content | Until user deletes model/app data | Existing model delete flow or app-data clear |
| Emergency projection | Deferred | User selection from confirmed records | Create a minimum, explicitly approved locked-state view | Separate local projection | Visible to a person with device access when enabled | Until user disables/changes it | Disable and erase projection/cache |
| Local diagnostic event | Planned | App-generated | Debug app health without health content | Bounded app-private log | None | Proposed 7 days or size-bounded rotation; approval open | In-app clear, rotation, or app-data clear |
| Crash report / remote telemetry | Prohibited in prototype | — | — | — | None | None | Not collected |
| Exported file/share URI | Deferred | User-selected records | Produce user-reviewed packet | Temporary app-private export, then user-selected destination | Only explicit recipient/app after preview | App copy deleted immediately after handoff or at bounded expiry | Delete temp export and revoke URI grant |
| Android system backup | Disabled | Android OS | No vault backup until designed | Excluded from backup | None | None | Not created |
| Cloud backup/sync/account | Deferred | — | — | — | None | None | Not collected |
| Health Connect/wearables/ABDM | Deferred | — | — | — | None | None | Not collected |
| Consent receipt / acknowledgement | Planned when purpose requires it | User action plus app version | Prove purpose-specific notice and user choice | Local consent ledger | None | Through relevant record lifecycle plus approved audit period | Revoke future processing; retain minimal revocation evidence if approved |
| Audit event | Deferred until audited workflows | App-generated | Record security/privacy-significant actions without health content | Local bounded audit store | None | Schedule requires security/privacy approval | User-visible deletion rule; never silently retain health payloads |

## Required invariants

1. Every health record is scoped to one profile, even while only one profile is
   exposed.
2. Logs, analytics, crash reports, filenames, notifications, and screenshots
   must not contain record content, credentials, tokens, or document text.
3. Temporary files and derived data are part of deletion, not an exception to
   it.
4. No SDK or permission enters a release merely because it is convenient. Its
   data behavior must be added here and pass vendor review first.
5. Any off-device transmission of health or identity data reopens this baseline
   and requires purpose, consent, processor, transfer, security, retention, and
   deletion review.

## External processor register

| Processor | Prototype role | Health/personal data allowed | Approval |
|---|---|---|---|
| Hugging Face | Legacy model-file download only | No health content, prompts, identity, or diagnostics | Existing temporary dependency; remove client token before public release |
| Google Play | App distribution if/when used | Store/account/device information governed by Play; no app health-record upload by design | Public/closed distribution review required |
| OCR vendor | None; on-device engine not yet selected | No network transmission | Must pass vendor gate before inclusion |
| Crash/analytics vendor | None | None | Prohibited for prototype |
| Cloud/storage/share provider | None | None | Deferred; new MG-01 review required |
