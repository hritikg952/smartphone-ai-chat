# MG-08 — Medication, Prescription, and Provider History

## Outcome

Provide an auditable medication regimen with current schedule, chronological changes, reference information, prescriptions, and provider history.

## Dependencies

MG-04 through MG-06; D-012 before authoritative drug monographs/interactions.

## Current prototype implementation — 2026-07-26

Implemented offline, encrypted slice:

- Manual, self-profile medication regimens with dose, route, form, indication,
  dates, status, notes, source, and an optional provider link.
- Manual providers with specialty, facility, and contact details; both record
  types are persisted through typed repositories over encrypted health records.
- Daily, weekly, as-needed, and explicit unsupported-complex instructions.
  Today materializes active daily/weekly occurrences only, using device time
  zone rules including DST gap normalization.
- Editable medication and provider forms, a Today summary on Home, save-failure
  feedback, profile-switch-safe state publication, and duplicate-save guards.
- Unit verification: `assembleDebug` and `testDebugUnitTest` pass.

This is intentionally not the MG-08 exit gate. Direct replacement is used for
edits; there is no append-only regimen event history, dose/adherence logging,
prescription-document link, drug reference/interactions, export/search index,
or clinician-reviewed scenario suite.

## Domain model

- `MedicationConcept`: normalized ingredient/product identifiers plus user-entered label.
- `MedicationRegimen`: profile, medication, indication (optional), route, form, start/end, status, prescriber, source, notes.
- `DosageInstruction`: amount/unit, frequency or recurrence rule, timing window, food instructions, as-needed rules, maximum constraints as recorded—not calculated advice.
- `RegimenEvent`: prescribed, started, dose changed, paused, resumed, discontinued, corrected; append-only chronology with effective time.
- `DoseEvent`: scheduled occurrence, taken/skipped/snoozed/unknown, actual time, optional note.
- `PrescriptionDocumentLink`, `Provider`, `Facility`, and contact/specialty history.
- `DrugReferenceSnapshot`: source, jurisdiction, version/date, therapeutic use, precautions, side effects; never treated as the user’s prescription.

## Work packages

1. Define status state machine and correction semantics; historical events are not overwritten silently.
2. Build manual medication search/add with duplicate detection, free-text fallback, unit validation, and explicit profile/source.
3. Build regimen editor for common, interval, specific-time, weekly, cyclical, and PRN schedules; separate unsupported complex schedules.
4. Materialize a bounded schedule window deterministically with time-zone/daylight-saving tests.
5. Build Today/list/detail/history UIs and accessible table/list adaptation.
6. Add dose logging and correction; reminders/alarms require a separate notification-permission and exact-alarm policy spike.
7. Link providers and encrypted prescription documents without duplicating files.
8. Integrate an approved drug knowledge source with attribution, versioning, offline cache, update policy, and jurisdiction coverage.
9. Add interaction-check service interface; deterministic result includes severity, evidence/source, affected items, timestamp, uncertainty, and next step. No generative-only alerts.
10. Support export/search/index invalidation and emergency-field selection.

## Safety rules

- Never recommend starting, stopping, or changing dosage.
- Distinguish user-entered regimen from general monograph/reference content.
- Alerts say what was checked and what was not; high-risk language is clinically reviewed.
- Data-source outage or stale knowledge cannot silently report “no interaction.”
- Keep original prescription/OCR image and user-confirmed structured values linked by provenance.

## Tests

- State-machine, recurrence/time-zone/DST, duplicate, edit-history, and profile-isolation tests.
- Property tests for schedule materialization and boundary dates.
- Reference version/stale/offline/failure tests and curated interaction fixtures reviewed by a pharmacist/clinician.
- UI tests for empty/large regimens, complex instructions, discontinued records, and accessibility.

## Acceptance criteria

- [ ] Active, past, paused, and discontinued regimens are chronologically accurate.
- [ ] Today view shows dose, route, timing, status, and source without losing instruction detail.
- [ ] Corrections remain auditable.
- [ ] Prescriptions/providers link to records without unencrypted copies.
- [ ] Monographs carry authoritative source/version and do not masquerade as medical advice.
- [ ] Interaction results fail safely and are not produced solely by an LLM.

## Exit gate

A clinician/pharmacist-reviewed scenario suite and security tests pass; no known schedule rule silently produces unsafe occurrences.
