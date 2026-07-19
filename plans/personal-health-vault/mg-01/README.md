# MG-01 Governance Baseline Packet

**Status:** In progress — draft complete; human approvals open

**Prepared:** 2026-07-19

**Applies to:** India-first, private, local-only Personal Health Vault prototype

This packet implements the documentation work packages in MG-01. It is a
product and engineering baseline, not legal advice or a medical-device
classification. No later mini-goal may treat MG-01 as closed until the named
reviewers approve the packet and any high-severity question has an owner and
due date.

## Approved planning direction

- Adults managing only their own personal records.
- Personal record organization and wellness support only.
- No diagnosis, treatment, dosage, clinical decision support, or emergency
  triage claims.
- India is the only initial jurisdiction.
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
| Product owner | TBD | Open | — | Intended use and scope approval |
| Security owner | TBD | Open | — | Data map, vendor gate, incident plan |
| Privacy/legal reviewer | TBD | Open | — | India DPDP applicability and copy review |
| Clinical safety reviewer | TBD | Open | — | Claims, feature risk, escalation review |

## Open decisions blocking the exit gate

| ID | Decision | Owner | Due date | Severity |
|---|---|---|---|---|
| MG01-O01 | Confirm minimum age (proposed: 18+) | Product/privacy | TBD | High |
| MG01-O02 | Confirm prototype language (proposed: English) and localization plan | Product | TBD | Medium |
| MG01-O03 | Confirm named reviewers and approval evidence | Product | TBD | High |
| MG01-O04 | Obtain India-specific counsel assessment before any external/public distribution | Privacy/legal | Before distribution | High |
| MG01-O05 | Confirm whether any drug-interaction feature is in v1 (proposed: no) | Product/clinical | TBD | High |

## Exit-gate status

- Documentation work packages: complete as a reviewable draft.
- Vision/current-state baseline: verified against the repository on 2026-07-19.
- Cross-plan conflict review: complete at planning level; deferred capabilities
  remain behind their later gates.
- Human approvals: open.
- High-severity questions with named owner and due date: open.
- **MG-01 exit gate: OPEN.**
