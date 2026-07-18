# MG-10 — Vitals, Allergies, Immunizations, and Trends

## Outcome

Offer trustworthy manual/imported health observations, interactive trends, and dedicated allergy/immunization records with source and uncertainty visible.

## Dependencies

MG-04 through MG-06.

## Domain model

- `Observation`: typed code, original/canonical value and unit, effective time/date, source, method/device, verification, notes, optional source document/Health Connect ID.
- Initial types: weight, height, BMI as derived display, blood pressure pair, heart rate/resting heart rate, oxygen saturation, temperature, respiratory rate, glucose only if intended-use review approves.
- `AllergyIntolerance`: substance, category, status, criticality, reaction manifestations/severity/date, verification, source, notes.
- `Immunization`: vaccine/product, dose/series, date precision, lot/manufacturer/site/provider, status, source document, verification.
- `ReferenceRange`: source-specific and versioned; not hard-coded as universal medical truth.

## Work packages

1. Define supported types, canonical units, validation ranges as input sanity checks (not clinical diagnosis), date precision, and correction/audit behavior.
2. Build quick/manual entry with unit preferences, locale-aware decimal parsing, source selection, and duplicate warning.
3. Build list/detail/edit/history screens for observations, allergies, reactions, and immunizations.
4. Build accessible charts with selected range, aggregation policy, missing-data display, raw-point inspection, source filters, and text/table alternative.
5. Derive BMI or other calculations only from explicit formulas with version/source and appropriate limitations; never persist a derived value as user-entered.
6. Merge OCR/Health Connect/manual sources using stable external IDs, provenance, deterministic deduplication, and conflict display—not last-write-wins.
7. Expose selected verified allergies/vitals to emergency projection through explicit user selection.
8. Prepare immunization/travel export without asserting validity requirements for a destination.

## Safety rules

- Charts do not diagnose, predict, or label trends as dangerous without an approved clinical rule.
- A validation-range rejection should catch impossible input while plausible outliers require confirmation rather than deletion.
- Display source, unit, timestamp, and time zone context.
- Never average incompatible methods/contexts silently.

## Tests

- Unit conversion, locale, DST/time-zone, date precision, duplicate/conflict, and derived-formula tests.
- Chart aggregation/downsampling/missing values/large dataset tests.
- Profile isolation and provenance tests across manual, OCR, and Health Connect data.
- Accessibility tests with color-blind palettes, screen reader, and data-table alternative.

## Acceptance criteria

- [ ] Users can record, correct, and inspect provenance for all supported records offline.
- [ ] Original and canonical units remain recoverable.
- [ ] Charts accurately reflect selected data/range and have non-visual alternatives.
- [ ] Allergy reactions and immunization series/history are not flattened into ambiguous strings.
- [ ] Cross-source duplicates/conflicts are visible and deterministic.
- [ ] Only verified/approved fields can reach emergency or AI features.

## Exit gate

Golden conversion/chart fixtures and cross-source conflict scenarios pass clinical-content review.

