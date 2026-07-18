# MG-01 — Product, Safety, Privacy, and Regulatory Baseline

## Outcome

Create an approved product-intent and data-governance baseline that constrains every later feature. This is the first release gate because security architecture and AI scope cannot be chosen until the product claims, users, jurisdictions, and data flows are known.

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

D-001, initial age/dependent policy, initial jurisdictions, offline-only launch versus any service, and whether drug interaction alerts ship in v1.

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

- Intended-use and prohibited-use statement.
- Data-flow/data-processing inventory and retention schedule.
- Feature risk register and clinical-safety escalation matrix.
- Consent taxonomy and privacy-copy checklist.
- Vendor assessment template and incident-response runbook.
- Jurisdiction-specific counsel sign-off record.

## Verification

- Trace every master-plan requirement to a declared data purpose and deletion path.
- Threat-model reviewers can identify all external data processors from the inventory.
- Store declarations can be produced without reading source code.
- UX review confirms medical limitations are visible at decision points, not hidden only in terms.

## Acceptance criteria

- [ ] Product owner, security owner, privacy/legal reviewer, and clinical safety reviewer are named.
- [ ] Intended use, prohibited use, launch jurisdiction, and user population are approved.
- [ ] Every planned data category has source, purpose, storage, sharing, retention, and deletion rules.
- [ ] AI/OCR claims are bounded and review requirements are explicit.
- [ ] Incident and user-data request procedures exist.
- [ ] MG-02 through MG-18 contain no conflict with this baseline.

## Exit gate

No later feature may enter implementation until the baseline is approved and open high-severity regulatory/safety questions have an owner and due date.

