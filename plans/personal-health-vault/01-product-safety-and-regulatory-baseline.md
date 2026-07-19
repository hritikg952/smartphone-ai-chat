# MG-01 — Product, Safety, Privacy, and Regulatory Baseline

**Mini-goal status:** Development baseline recorded — legal, privacy, clinical,
and store review deferred and non-blocking for private prototype development.
See the
[vision baseline](00-vision-baseline-and-current-state.md) and
[MG-01 governance baseline packet](mg-01/README.md).

## Outcome

Keep a product-intent and data-governance draft that informs development without
blocking the private prototype. It is not an approved legal, privacy, clinical,
or release baseline. Those reviews are deferred until the owner chooses to
prepare the application for real-world use or distribution.

## Scope

- Define target users, age limits, dependent use, launch countries, supported languages, and accessibility baseline.
- State intended use and prohibited use in product, store, onboarding, and AI copy.
- Inventory every health/personal/financial data category, source, transformation, destination, retention rule, and deletion path.
- Classify features as record management, wellness, medical reference, clinical decision support, or potential medical-device functionality.
- Establish consent, privacy-notice, incident-response, user-rights, and vendor-review requirements.
- Define clinical safety ownership and content-review process.

## Not in scope

- Legal conclusions made by engineers.
- Production privacy policy text without counsel approval.
- Feature implementation.

## Required decisions

The owner-selected direction is recorded for personal record organization,
private local-only development, self-profile only, no minimum age restriction,
English-only UI, and no drug-interaction alerts in the prototype/v1. Launch
jurisdictions and legal, privacy, clinical, and store constraints are deferred.

## Owner direction recorded 2026-07-19

- No minimum age restriction is enforced in the prototype.
- The prototype UI is English only.
- Named governance owners and specialist reviewers are deferred.
- MG-01 does not gate private prototype implementation.
- Legal and regulatory applicability will be addressed later.

These are development instructions, not legal or clinical approvals. Until the
deferred review occurs, the application and documentation must not claim legal
compliance, medical approval, clinical validation, or suitability for real
patient care.

## Work packages

1. **Product intent:** one-page intended-use statement, claims register, prohibited claims, and emergency disclaimers.
2. **Data map:** field-level processing inventory covering user entry, camera/gallery, documents, Health Connect, model prompts, exports, logs, backups, and deletion.
3. **Risk classification:** regulatory counsel reviews AI, OCR, interaction alerts, symptom patterns, and visit-prep outputs for each launch jurisdiction.
4. **Consent model:** define consent receipts with version, purpose, profile, data categories, grantee, timestamp, expiry, and revocation.
5. **Retention/deletion:** define defaults and exceptions for primary records, documents, derived values, audit events, exports, caches, model context, and backups.
6. **Vendor gate:** create a security/privacy checklist for OCR, drug knowledge, crash reporting, cloud storage, and link-sharing providers.
7. **Incident plan:** roles, severity, containment, evidence preservation, user/regulator notification assessment, and postmortem process.
8. **Store compliance:** map Play Health Apps declaration, Data safety form, permission disclosures, privacy policy, and medical disclaimer requirements.

## Required artifacts

- [Intended-use and prohibited-use statement](mg-01/01-intended-use-and-claims.md).
- [Data-flow/data-processing inventory](mg-01/02-data-processing-inventory.md)
  and [retention schedule](mg-01/04-consent-privacy-retention-and-rights.md).
- [Feature risk register and clinical-safety escalation matrix](mg-01/03-feature-risk-and-clinical-safety.md).
- [Consent taxonomy and privacy-copy checklist](mg-01/04-consent-privacy-retention-and-rights.md).
- [Vendor assessment template](mg-01/05-vendor-assessment-template.md) and
  [incident-response runbook](mg-01/06-incident-response-runbook.md).
- [Jurisdiction-specific counsel sign-off record](mg-01/07-store-compliance-and-signoff.md).

## Verification

- Trace every master-plan requirement to a declared data purpose and deletion path.
- Threat-model reviewers can identify all external data processors from the inventory.
- Store declarations can be produced without reading source code.
- UX review confirms medical limitations are visible at decision points, not hidden only in terms.

## Acceptance criteria

- [ ] Product owner, security owner, privacy/legal reviewer, and clinical safety reviewer are named. Deferred; non-blocking for private development.
- [x] Development scope is recorded: personal records, self-profile, no minimum age restriction, English only, local-only private prototype, and no clinical claims.
- [ ] Launch jurisdiction and legal/clinical/store approval are complete. Deferred until distribution or real-world use is planned.
- [x] Every planned data category has source, purpose, storage, sharing, retention, and deletion rules in the draft baseline.
- [x] AI/OCR claims are bounded and review requirements are explicit.
- [x] Incident and user-data request procedures exist as reviewable runbooks.
- [x] MG-02 through MG-20 have been checked at planning level and deferred capabilities remain behind later gates.

## Exit gate

MG-01 does not block private prototype implementation. Revisit this baseline
before external distribution, use with real health data, medical or clinical
claims, off-device health-data processing, or any claim of compliance or
approval.
