# MG-13 — Insurance and Billing Vault

## Outcome

Archive insurance cards, policies, claims, bills, and related contacts next to clinical records while applying equally strong privacy controls to financial identifiers.

## Dependencies

MG-04 through MG-06.

## Domain model

- `InsurancePolicy`: insurer, plan/type, member/group identifiers, policyholder/profile relationship, effective/expiry date, contacts, encrypted card images.
- `Claim`: provider, service dates, claim/control number, submitted/processed state, billed/allowed/paid/user-responsibility amounts and currency, linked documents.
- `Bill`: issuer, dates, amount/currency, status, due date, account/reference, linked claim/document, notes.
- `InsuranceContact`: role and contact channels.

Sensitive identifiers need masked display, deliberate reveal, screenshot/clipboard policy, and exclusion from notifications/search snippets/emergency surfaces.

## Work packages

1. Define supported jurisdictions and vocabulary; avoid pretending insurance workflows are universal.
2. Build policy/card manual entry and encrypted camera/gallery/document import via MG-09 infrastructure.
3. Build claim/bill timelines, status transitions, links to providers/reports, and attachment management.
4. Store money as integer minor units plus currency; preserve source statement values and do not calculate coverage promises.
5. Add expiring-policy and due-date local reminders with discreet content.
6. Add masked search/index fields and explicit reveal/share controls.
7. Build export selection under MG-15 with warnings for identifiers and card images.
8. Treat OCR as a draft; require confirmation for all identifiers and amounts.

## Tests

- Currency/minor-unit, date/status, masking/reveal, duplicate, attachment, and profile-isolation tests.
- Screenshot, clipboard, notifications, logs, search snippet, export preview, and deletion privacy tests.
- OCR correction and malformed/unsupported statement tests.

## Acceptance criteria

- [ ] Policy, card, claim, and bill records persist encrypted and offline.
- [ ] Member/policy/account identifiers are masked by default.
- [ ] Amounts preserve source currency and are not presented as coverage guarantees.
- [ ] OCR values cannot become authoritative without user confirmation.
- [ ] Sharing defaults exclude sensitive identifiers/images unless explicitly selected.
- [ ] Deletion covers linked copies, derivatives, search, and reminders.

## Exit gate

Financial/privacy review approves display, notification, search, sharing, and deletion behavior.

