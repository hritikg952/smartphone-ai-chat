# Feature Risk and Clinical-Safety Register

## Classification rule

Features are classified by what they actually do and claim, not by their menu
name. A feature must be escalated before implementation when it interprets data
for a medical purpose, prioritizes clinical action, recommends treatment,
changes an authoritative record without review, or could foreseeably delay
professional or emergency care.

## Initial register

| Feature | Initial class | Primary harms | Required controls | Prototype disposition |
|---|---|---|---|---|
| Manual record entry/view | Record management | Wrong value, wrong profile, missing provenance | Validation, units, source, edit history, clear non-clinical framing | Allowed after persistence gates |
| Document storage | Record management | Disclosure, stale/wrong document, metadata leak | Private storage, type/size checks, provenance, deletion cascade | Deferred until file boundary exists |
| OCR extraction | Record-management assistance | Misread value becomes trusted record | On-device engine, confidence/provenance, field-by-field review, no silent commit | Deferred; review gate mandatory |
| Vitals charts/trends | Wellness/reference | Misleading scale/unit/time range or implied diagnosis | Canonical units, source/time zone, accessible axes, no diagnostic labels | Later feature review |
| Medication schedule/history | Record management | Wrong dose/timing, missed care | User-confirmed source, chronology rules, no prescribing language | Later feature review |
| Drug interaction alert | Potential clinical decision support | False negative/positive, harmful action | Authoritative licensed data, version/freshness, deterministic rules, clinician fixtures, escalation copy | Not in prototype/v1 unless separately approved |
| Symptom patterns | Potential clinical decision support | False reassurance/alarm, delayed care | Reproducible method, uncertainty, source traceability, clinical review | Deferred |
| Emergency card | Safety-critical reference | Stale or overexposed data, reliance during emergency | Explicit field opt-in, freshness, locked-state exposure review, emergency-services limitation | Deferred |
| AI chat isolated from vault | General assistant | Hallucination or medical advice | Archived entry point, no record access, prohibited-claim copy | Preserve only; not main journey |
| Grounded AI over records | Medical-reference assistance / possible CDS depending output | Hallucination, source mismatch, privacy leak | User-selected source packet, citations, deterministic retrieval, review, safety evaluation, no record mutation | Deferred to MG-16 |
| Sharing/export | Record management | Wrong recipient or excessive disclosure | Granular selection, exact preview, bounded URI, audit/deletion | Deferred |

## Safety escalation matrix

| Severity | Example | Immediate action | Review required |
|---|---|---|---|
| Critical | Advice or data error could plausibly contribute to death or serious harm; emergency data exposed broadly | Disable affected path, preserve content-free evidence, initiate incident runbook | Product, security/privacy, clinical, legal |
| High | Incorrect medication, allergy, identity/profile, or clinical value; health content leaves device unexpectedly | Stop release/rollout, contain, assess affected records and deletion | Product, security/privacy, clinical |
| Medium | Misleading non-urgent copy/chart, incomplete deletion, accessibility barrier to important information | Block affected feature release; correct and verify | Product plus relevant specialist |
| Low | Cosmetic or informational issue without health-decision impact | Normal backlog with documented rationale | Product/engineering |

## Review ownership

| Responsibility | Named owner | Minimum duty |
|---|---|---|
| Product safety | TBD | Own intended use, feature scope, and go/no-go |
| Clinical safety | TBD | Approve medical limitations, safety fixtures, and escalation outcomes |
| Privacy/legal | TBD | Assess jurisdiction, notices, rights, vendors, and reportability |
| Security | TBD | Own threat model, containment, deletion, and security gates |
| Engineering | TBD | Prove runtime behavior matches declared controls |

## Release blockers

- A medical or emergency claim without clinical/regulatory approval.
- AI/OCR content committed as a confirmed record without user review.
- Generative output used as the source of a medication interaction or other
  safety-critical alert.
- Health content in logs, analytics, crash reports, notifications, backups, or
  unintended network traffic.
- A high/critical risk without a named owner, due date, mitigation, and accepted
  residual risk.
