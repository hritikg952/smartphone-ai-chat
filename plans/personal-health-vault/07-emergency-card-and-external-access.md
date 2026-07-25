# MG-07 — Emergency Card and External Emergency Access

## Outcome

Give first responders fast offline access to a user-approved minimum emergency dataset without unlocking or weakening the main vault.

## Dependencies

MG-03 through MG-06; decisions D-010 and D-011.

## Current prototype status — 2026-07-25

The first in-app slice is implemented pending Gradle verification: an unlocked
self profile can explicitly publish or revoke a name-only snapshot, and the
public `Emergency` route reads that independently authenticated, no-backup
projection while the vault is locked. Blood group, allergies, conditions,
contacts, clinical medications, calling, QR/share, Wallet, widgets, and
dependent profiles remain deferred until their verified source or platform work
exists. The local Java runtime is unavailable, so the new test suite has not
run in this environment.

## Security model

Emergency data is a derived projection copied from selected verified vault fields. It has its own minimal store and integrity protection because it may be intentionally viewable without vault authentication. The user must understand that anyone with device access may see enabled fields.

## Candidate fields

Preferred name, age/date-of-birth choice, photo choice, blood group and verification status, severe allergies/reactions, critical conditions, critical medications, organ-donor statement where lawful, emergency contacts, clinician contact, last refreshed time, owner-written note, and “call local emergency services” action.

Never include full reports, identifiers, insurance policy numbers, addresses, detailed medication history, AI-generated conclusions, or unverified OCR values by default.

## Delivery surfaces

1. In-app emergency card reachable from the locked screen through a deliberate action.
2. Offline printable/shareable card or QR payload only after a privacy review; QR must not expose a reusable vault token.
3. Google Wallet generic private pass only after issuer eligibility/approval, service/JWT design, jurisdiction review, revoke/update plan, and user consent.
4. Lock-screen widget/OS surface only after a device/version feasibility spike; do not promise universal lock-screen availability.
5. Apple Wallet/iOS lock-screen delivery belongs to a separate iOS plan.

## Work packages

1. Approve the emergency field allowlist, verification badges, disclaimer, exposure warning, and dependent-profile rules.
2. Build `EmergencyProfile` as a user-managed aggregate referencing source records and storing an explicit projection snapshot.
3. Add preview/confirm/publish/revoke flows; never auto-publish new health values.
4. Render a large-type, high-contrast, one-handed offline UI; defer emergency
   calling and contacts until a supported jurisdiction/number and verified
   contact sources exist.
5. Detect source changes and show “update available”; retain last-published timestamp and stale warning.
6. Add abuse controls: no navigation into vault, no sensitive notifications, rate-limited contact actions, and no secret-bearing QR/deep links.
7. Run Wallet/widget feasibility spikes and document platform approval, backend, signing, privacy, and maintenance obligations.
8. Test after reboot, airplane mode, locale change, large fonts, process death, and vault key invalidation.

## Acceptance criteria

- [x] User explicitly confirms the currently externally visible name field and sees an exposure warning.
- [ ] Locked emergency access cannot traverse into any other profile or vault record.
- [x] Card persistence and integrity/restart behavior are covered by the repository seam; device verification remains pending.
- [ ] Unverified/OCR/AI values are excluded or clearly identified and require confirmation.
- [ ] Stale source data is visible and refresh is explicit.
- [ ] Disable/revoke removes all app-controlled copies; external pass limitations are disclosed.
- [x] Wallet/widget claims are absent; those surfaces remain deferred.

## Exit gate

Security/privacy review approves the minimal projection and verifies there is no path from the emergency surface to the vault key or full records.
