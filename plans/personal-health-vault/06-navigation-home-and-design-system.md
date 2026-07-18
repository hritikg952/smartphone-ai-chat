# MG-06 — Navigation, Home Dashboard, and Design System

## Outcome

Replace the chat-first interface with an accessible, adaptive health-vault shell centered on rapid retrieval, today’s tasks, and emergency information.

## Dependencies

MG-02, MG-05.

## Information architecture

Primary destinations: Home, Records, Add, Insights, and Profile/Settings. Records groups medications, reports, vitals, allergies, immunizations, providers, journal, and insurance. Search is globally reachable. Assistant is a secondary capability, never the landing screen or sole way to access data.

## Home content

- Selected profile with unmistakable dependent indicator.
- Compact emergency card preview.
- Today’s medication schedule and adherence state.
- Recent records/reports and quick add actions.
- Latest selected vitals/trends with source and timestamp.
- Upcoming appointments and consent/integration warnings.
- AI insight cards only after MG-16, clearly labeled and dismissible.

## Work packages

1. Validate information architecture and critical tasks with low-fidelity prototypes and representative users/caregivers.
2. Implement typed navigation, adaptive navigation rail/bar, deep-link guards, and predictable back behavior.
3. Create health-vault color, typography, spacing, iconography, chart, form, table/list, empty/loading/error, verification, and source/provenance components.
4. Build dashboard sections from independent feature state; one failed section must not blank the home screen.
5. Standardize destructive confirmation, unsaved changes, offline state, locked content, permission education, and notification severity.
6. Add dynamic type, screen-reader semantics, keyboard/switch access, touch targets, color-independent status, reduced motion, high contrast, and locale-aware units/dates.
7. Define screenshot/recents redaction by screen sensitivity and user setting.
8. Add Compose previews and screenshot tests for phone/tablet, font scales, long translations, RTL, empty/large data, and dependent mode.

## UX safety rules

- Never encode abnormal/critical solely by red/green color.
- Always display unit, measurement time, and source beside clinical values.
- Separate “recorded,” “OCR draft,” “AI suggestion,” and “verified” visually and semantically.
- Do not use alarming language without a defined clinical rule and next step.
- Emergency features must remain accessible without exposing the rest of the vault.

## Acceptance criteria

- [ ] Core records are reachable without chat/AI.
- [ ] Selected profile is visible on every sensitive destination.
- [ ] Locked/deep-linked routes reveal no health content.
- [ ] Home sections fail independently and support offline use.
- [ ] Accessibility testing passes agreed WCAG/Android targets at large font scale.
- [ ] Design system documents verification, provenance, warning, and destructive-action patterns.

## Exit gate

The shell can host every feature plan without duplicating navigation, security, accessibility, or status semantics.

