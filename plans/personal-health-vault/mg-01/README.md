# MG-01 Governance Baseline Packet

**Status:** Development direction recorded; governance review deferred

**Prepared:** 2026-07-19

**Applies to:** English-only, private, local-only Personal Health Vault prototype

This packet is a product and engineering reference, not legal advice, a privacy
assessment, or a medical-device classification. Its governance review is
deferred and it does not block private prototype implementation. Deferred work
must not be represented as approval or compliance.

## Owner-selected planning direction

These choices are recorded in the decision register and constrain the draft.
They are not a substitute for the versioned approval evidence below.

- Individuals managing only their own personal records; the prototype enforces
  no minimum age restriction.
- Personal record organization and wellness support only.
- No diagnosis, treatment, dosage, clinical decision support, or emergency
  triage claims.
- The prototype UI is English only. Launch jurisdiction is not selected for
  development and must be reviewed before distribution.
- Private prototype; no public Play Store release in this phase.
- Local-only health records. No health-data backend, cloud OCR, remote
  analytics, sharing service, Health Connect, or wearable integration.
- Existing on-device AI chat is preserved outside the main vault journey and
  must not read or modify health records.
- Prototype storage is not yet encrypted. Synthetic or developer-controlled
  data is required for development and demonstrations.

## Required artifacts

1. [Vision baseline and verified current-state gap](../00-vision-baseline-and-current-state.md)
2. [Intended use and claims register](01-intended-use-and-claims.md)
3. [Data processing inventory](02-data-processing-inventory.md)
4. [Feature risk and clinical-safety register](03-feature-risk-and-clinical-safety.md)
5. [Consent, privacy, retention, and user-rights baseline](04-consent-privacy-retention-and-rights.md)
6. [Vendor assessment template](05-vendor-assessment-template.md)
7. [Incident-response runbook](06-incident-response-runbook.md)
8. [Store compliance and reviewer sign-off](07-store-compliance-and-signoff.md)

## Approval record

Names must identify accountable people, not only teams. A person may hold more
than one role for a private prototype, but each role must be explicitly
accepted.

| Role | Name | Status | Date | Evidence |
|---|---|---|---|---|
| Product owner | TBD | Deferred | — | Intended use and scope approval |
| Security owner | TBD | Deferred | — | Data map, vendor gate, incident plan |
| Privacy/legal reviewer | TBD | Deferred | — | Applicable law, rights, and copy review |
| Clinical safety reviewer | TBD | Deferred | — | Claims, feature risk, escalation review |

## Decision and approval status

| ID | Decision | Owner | Due date | Severity | Status |
|---|---|---|---|---|---|
| MG01-O01 | Prototype minimum age restriction | Product/privacy | 2026-07-19 | High | Resolved for development: none; legal implications deferred |
| MG01-O02 | Prototype language and localization | Product | 2026-07-19 | Medium | Resolved: English only; localization deferred |
| MG01-O03 | Confirm named reviewers and versioned approval evidence | Product | Later | High | Deferred; non-blocking for private development |
| MG01-O04 | Obtain applicable counsel assessment before any external/public distribution | Privacy/legal | Before distribution | High | Deferred |
| MG01-O05 | Exclude drug-interaction alerts from prototype/v1 | Product/clinical | 2026-07-19 | High | Resolved by D-021; later reintroduction requires separate approval |
| MG01-O06 | MG-01 implementation gate | Product/security | 2026-07-19 | High | Resolved: no MG-01 gate for private prototype development |

## Exit-gate status

- Documentation work packages: complete as a reviewable draft.
- Vision/current-state baseline: verified against the repository on 2026-07-19.
- Cross-plan conflict review: complete at planning level; deferred capabilities
  remain behind their later gates.
- Product direction: recorded in the decision register.
- Human approvals and specialist review: deferred.
- Development dependency: satisfied for private prototype work.
- **MG-01 governance status: DEFERRED; NON-BLOCKING FOR DEVELOPMENT.**
