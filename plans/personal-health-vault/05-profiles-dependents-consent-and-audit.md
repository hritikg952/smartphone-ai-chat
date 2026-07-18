# MG-05 — Profiles, Dependents, Consent, and Audit

## Outcome

Allow one vault owner to manage their own and authorized dependents’ records without cross-profile disclosure or ambiguous authorship.

## Dependencies

MG-01, MG-03, MG-04; decision D-009.

## Domain model

- `Profile`: ID, display name, relationship, date of birth/date precision, optional demographic fields, avatar reference, status.
- `AuthorityGrant`: actor, subject profile, role/capabilities, basis, start/end, revocation, evidence metadata.
- `ConsentReceipt`: purpose, data categories, recipient/integration, policy version, timestamps, expiry, revocation.
- `AuditEvent`: append-only security-relevant action metadata without unnecessary clinical content.

Roles/capabilities must be explicit: vault owner, self profile, caregiver editor, viewer/exporter, and emergency projection manager. Do not infer authority solely from a profile selector.

## Work packages

1. Define profile and authority aggregates, age transitions, archived/deceased profile handling, and profile deletion/export policy.
2. Add first-run self-profile creation and later dependent creation with clear representation/consent claims.
3. Make selected profile part of the authorized session context, not a free string passed from UI.
4. Require all repositories/use cases to take scoped profile context and reject cross-profile IDs.
5. Build atomic profile switching that clears feature caches, navigation back stacks, search results, assistant context, and pending edits.
6. Implement consent capture/revocation and integrate it with Health Connect, sharing, cloud/OCR, and AI purposes.
7. Record audit events for unlock failures, profile changes, sensitive views, imports, edits, deletions, exports/shares, consent changes, key events, and admin actions.
8. Provide a user-readable activity history and a redacted support/security export.
9. Define audit retention, integrity, and deletion exceptions with legal/privacy review.

## Security and privacy tests

- IDOR-style repository/use-case tests using valid IDs from another profile.
- Rapid profile switching during loading, OCR, search, export, and model generation.
- Revocation cancels queued work and prevents reuse of cached permissions/context.
- Audit events contain no document text, medication notes, or secret material unless explicitly approved.

## Acceptance criteria

- [ ] Every health aggregate is profile-scoped and enforced below the UI.
- [ ] Dependent authority and limitations are visible and auditable.
- [ ] Switching profiles cannot flash or retain the previous profile’s data.
- [ ] Consent receipts are versioned, inspectable, revocable, and enforced.
- [ ] Sensitive actions produce minimal audit events.
- [ ] Profile archive/delete/export behavior is complete and tested.

## Exit gate

No feature may use unscoped repository methods or global cached health state.

