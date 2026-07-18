# MG-02 — Architecture Pivot and Modular Application Shell

## Outcome

Transform the chat-centric application structure into a health-vault shell while retaining useful Kotlin/Compose/Clean Architecture practices and avoiding an all-at-once rewrite.

## Dependencies

MG-01; decisions D-002 and D-003.

## Scope

- Product/application naming, package strategy, navigation/deep-link model, feature ownership, DI lifetime, background-work boundary, and build conventions.
- Separation of reusable core code from legacy chat/model code.
- Architecture tests and a migration seam that keeps the app buildable throughout the pivot.

## Target boundaries

- `core:model`: identifiers, time/unit primitives, result/error types, provenance.
- `core:domain`: repository contracts and cross-feature use cases without Android imports.
- `core:security`, `core:database`, `core:files`: infrastructure behind domain contracts.
- `core:ui`: theme, adaptive scaffolds, accessibility helpers, shared components.
- One feature owner/state holder per top-level area; no replacement “god ViewModel.”
- Integration adapters for Health Connect, OCR, model runtime, and future services.

## Work packages

1. Capture ADRs for module strategy, navigation, DI, background jobs, error handling, and event delivery.
2. Move `AppContainer` ownership to an `Application`-lifetime root or adopt an approved DI framework; enforce scopes explicitly.
3. Add Compose Navigation with typed routes, saved-state rules, deep-link allowlist, and locked-vault redirect behavior.
4. Create the app shell and placeholder routes: onboarding/unlock, home, profiles, emergency, medications, reports, vitals, journal, insurance, search, sharing, settings, assistant.
5. Define `AppSessionState`: onboarding status, vault lock state, selected profile, migration state, and safe global notifications only.
6. Add time, UUID, dispatcher, and file abstractions so tests remain deterministic.
7. Establish feature package/module dependency rules and automated forbidden-import checks.
8. Isolate legacy chat/model download behind a build flag until MG-18 decides its final disposition.
9. Rename user-facing resources, application icon/labels, and documentation only after D-002.

## Migration notes

- Keep `MainActivity`, Compose, theme primitives, notification event pattern, UUID generator, and test fakes where appropriate.
- Do not copy chat concepts (`Conversation`, `Message`, `ChatUiState`) into health models.
- Do not add Room, camera, OCR, or Health Connect directly to UI modules.

## Tests

- Unit tests for session transitions and locked-route redirection.
- Navigation tests for every top-level destination, back behavior, process recreation, and invalid deep links.
- Architecture tests for dependency direction.
- Smoke test with AI/model components disabled.

## Acceptance criteria

- [ ] App launches into onboarding/unlock/home based on session state.
- [ ] All feature routes are addressable without exposing data while locked.
- [ ] Dependency lifetimes survive configuration change and do not leak an Activity.
- [ ] Domain/core modules have no Compose, LiteRT, Room, or platform imports except explicitly platform-owned modules.
- [ ] Legacy chat is isolated and removable.
- [ ] Build and focused test suite pass after every migration step.

## Exit gate

The new shell owns application startup and navigation, is testable without AI, and supplies stable boundaries for MG-03 through MG-16.

