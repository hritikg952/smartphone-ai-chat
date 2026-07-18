# MG-11 — Symptoms, Mood, Appointments, and Visit Preparation

## Outcome

Provide a genuinely fast daily check-in, longitudinal symptom context, appointment tracking, and a transparent provider-visit packet.

## Dependencies

MG-05, MG-06, MG-10. AI-generated visit preparation additionally depends on MG-16.

## Domain model

- `CheckIn`: profile, effective time, pain/fatigue/mood scales with named scale/version, optional symptoms/tags/notes, source.
- `SymptomEpisode`: symptom concept/free text, onset/end, severity, body location, triggers, associated events, notes.
- `Appointment`: provider/facility, start/end/time zone, purpose, status, preparation window, notes.
- `VisitPrepPacket`: time window, included record IDs/versions, generated sections, user edits, creation time, staleness marker.

## Work packages

1. User-test a five-second check-in: one-screen defaults, accessible scale semantics, optional detail, undo, and reminder controls.
2. Build journal/calendar/timeline views with edit history and clear distinction between daily check-in and symptom episode.
3. Add configurable local reminders using least privilege and quiet-time/time-zone rules; sensitive notification text is hidden by default.
4. Link entries to medication regimen events, vitals, sleep/activity context, and appointments by time without asserting causality.
5. Build appointments manually first; calendar integration requires separate permission/consent and must not be mandatory.
6. Build deterministic visit-prep base: recent changes, selected trend tables, active regimen, allergies, new reports, and user-pinned questions with source links.
7. Add printable/shareable preview through MG-15.
8. Under MG-16, optionally rewrite/summarize the deterministic packet; retain source list, allow editing, label AI content, and never invent questions/findings.

## Safety and privacy rules

- Mood/symptom data receives the same encryption and disclosure controls as medical records.
- No crisis detection or intervention claim unless separately designed, clinically governed, localized, and operationally supported.
- Correlation language must say “occurred around the same time,” not imply medication causation.
- Notifications and widgets show no symptom/mood content by default.

## Tests

- Scale/version, time-zone, recurring reminders, undo/edit, linked-event-window, and packet-staleness tests.
- Usability timing with accessibility users; no required path should depend on precise slider gestures.
- Privacy tests for notifications, lock screen, exports, search, and profile switching.
- Visit packets must reproduce from a frozen record/version set.

## Acceptance criteria

- [ ] Common check-in completes in the agreed five-second usability target.
- [ ] Scale labels/versions make longitudinal comparison meaningful.
- [ ] Reminders are opt-in, discreet, and resilient to reboot/time-zone changes.
- [ ] Timeline shows temporal associations without causal claims.
- [ ] Visit packet lists included period, sources, omissions, and stale state.
- [ ] User can edit/approve before any export or share.

## Exit gate

Usability, privacy, and clinical-language reviews approve the journaling and deterministic visit-prep flows.

