# MG-15 — Granular Sharing, PDFs, and Expiring Links

## Outcome

Let users compile a minimum, purpose-specific health packet, preview every disclosed field, and control its protection and lifecycle.

## Dependencies

MG-03 through MG-05 plus the source feature plans; decision D-015 for web links.

## Share manifest

Every export starts with an immutable `ShareManifest`: profile, purpose/recipient label, selected record/document IDs and versions, date range, fields/redactions, generated time, expiry policy, watermark choice, format, and disclosure warning. The manifest is auditable without storing unnecessary exported plaintext.

## Delivery modes

1. In-app preview only.
2. Password-protected encrypted package for machine/app import, using an approved standard/design.
3. Human-readable PDF. Be explicit: ordinary PDF password protection and OS share sheets may not meet zero-knowledge guarantees; minimize data and warn users.
4. Direct Android share via short-lived content URI with exact read permission, expiry, no broad file path, and cleanup.
5. Expiring web link only after a separate zero-knowledge service design, authenticated encryption, opaque capability, password/key separation, recipient authentication choice, rate limits, revocation, access logs, expiry, regional/storage/vendor review, and deletion verification.

## Work packages

1. Define export templates: emergency summary, medication list, visit packet, reports bundle, vitals trend, immunization record, insurance packet, and custom.
2. Build record/field picker with safe defaults, date range, dependent authorization check, unverified-data exclusion, and estimated size.
3. Build preview that clearly marks omitted, stale, draft, AI-generated, and externally sourced content.
4. Generate accessible PDFs with profile/date/source, page numbers, disclaimer, and optional watermark; never include hidden metadata or unintended attachments.
5. Implement encrypted package format with versioned manifest, authenticated contents, integrity, password KDF, and recovery/error UX.
6. Use ephemeral encrypted temp files or streaming where possible; revoke URI grants and schedule reliable cleanup.
7. Add share audit event, active-share list, revoke/cleanup, and recipient-purpose label.
8. Add re-authentication for export/reveal and block while selected profile/authority is invalid.
9. Threat-model screenshots, print spooler, recipient apps, clipboard, email, and user misunderstanding; disclose limits.

## Tests

- Golden PDF accessibility/layout/redaction tests and package interoperability/corruption/wrong-password tests.
- URI grant scope/expiry, process death, cancellation, storage-full, large bundle, malicious filename/content, and cleanup tests.
- Verify unselected fields and other profiles are absent at byte/content level.
- Link mode adds service abuse, revoke/expiry, clock, enumeration, brute force, cache/CDN, backup, and deletion tests.

## Acceptance criteria

- [ ] User previews and explicitly confirms exactly what is disclosed.
- [ ] Exports are profile/authority scoped and default to verified minimum data.
- [ ] Temporary plaintext exposure is eliminated or explicitly documented/minimized where platform sharing requires it.
- [ ] Direct-share grants are narrow and cleaned up.
- [ ] Web-link mode cannot ship before separate security/privacy/service approval.
- [ ] Audit and revocation behavior matches the selected delivery mode’s real capabilities.

## Exit gate

Byte-level disclosure tests and a security/privacy review verify that no unselected record, hidden metadata, or reusable vault credential leaves the app.

