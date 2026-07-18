# MG-09 — Medical Reports, Document Timeline, and OCR

## Outcome

Store medical documents chronologically and convert selected report content into reviewable, provenance-linked structured observations.

## Dependencies

MG-04 through MG-06; D-013.

## Domain model

- `MedicalDocument`: profile, type, title, service/issued date precision, provider, encrypted asset IDs, source, hash, import metadata, tags.
- `DocumentPage` and encrypted thumbnail references.
- `ExtractionRun`: engine/version, input hash, start/end, status, language, quality, errors.
- `ExtractedObservationDraft`: test code/name, value/text, unit, reference range, flag, specimen/effective date, page/bounding region, confidence.
- `VerifiedObservation`: user-confirmed/corrected structured record with immutable link to draft/document.

## Ingestion paths

System photo picker, Storage Access Framework, camera document capture, multi-page scan, and supported PDF/image import. Avoid broad media/storage permission. Persist URI content into the encrypted vault promptly and release temporary grants.

## Work packages

1. Approve file/MIME/page/size limits, malware/parser strategy, EXIF stripping, duplicate policy, and corrupt-file UX.
2. Build transactional encrypted import with progress, cancellation, storage checks, hash-based duplicate warning, and cleanup.
3. Build chronological timeline, filters, preview, metadata editor, tags, provider link, and original-file export controls.
4. Evaluate on-device OCR first for supported scripts, tables, handwriting, latency, size, and offline performance; cloud OCR requires MG-01 vendor/consent approval.
5. Separate OCR text extraction from clinical field parsing. Use schema-constrained parsers and validation; generative extraction, if used, remains untrusted.
6. Create review UI showing value/unit/range/confidence beside the exact source page/region. Require confirmation before analytics, alerts, emergency, search snippets, or AI use.
7. Normalize known tests through versioned mappings while preserving original labels/units. Never infer a unit when absent.
8. Invalidate/re-run derivatives when source, parser, mappings, or verification changes.
9. Support delete across original pages, thumbnails, OCR text, observations, search, AI caches, and queued work.

## Safety and privacy rules

- Raw OCR text is health data and encrypted.
- Never send a document to a network service through hidden fallback.
- Display abnormal flags from the report/source range, not a universal guessed range.
- AI summaries include source references and cannot replace the original report.
- Document previews must redact app switcher/screenshots according to policy.

## Tests

- Golden corpus covering clear/blurred/rotated/multi-page/table/handwritten/multilingual reports with synthetic/de-identified data.
- Precision/recall and field-level unit/date/reference-range metrics with release thresholds.
- Parser fuzzing, malformed PDF/image, decompression/size limits, cancellation, storage-full, and process-death tests.
- Provenance and reprocessing tests proving no draft becomes verified implicitly.

## Acceptance criteria

- [ ] Original documents remain encrypted, retrievable offline, and correctly ordered by clinical date.
- [ ] Import leaves no plaintext cache or orphan after failure.
- [ ] OCR/parser output is visibly draft with confidence and exact source location.
- [ ] Only user-confirmed observations enter trends, alerts, or assistant grounding.
- [ ] Units/original values and parser versions are preserved.
- [ ] Delete and reprocess semantics cover every derivative.

## Exit gate

The approved evaluation corpus meets documented quality thresholds, and users can correct every extracted field before it affects another feature.

