# Vision Baseline and Current-State Gap

## Normalized product vision

Build a secure personal health vault that consolidates medications, prescriptions, providers, reports, lab values, vitals, allergies, immunizations, wearable context, symptoms, mood, insurance, billing, emergency information, sharing, and carefully bounded intelligent assistance.

The product should remain useful offline and make user-controlled health records easier to retrieve and understand. It is not initially a clinical system of record, a diagnostic tool, a treatment recommender, an emergency dispatch service, or a substitute for professional care.

## Current repository baseline

The repository is a Kotlin/Jetpack Compose, single-activity Android app using Clean Architecture-style package boundaries and manual dependency injection. Its current product behavior is a Gemini-inspired chat interface backed by on-device LiteRT-LM models downloaded from Hugging Face.

### Assets worth retaining

- Kotlin, coroutines, `StateFlow`, Compose, Material 3, and the single-activity approach.
- Domain interfaces and constructor injection as architectural habits.
- Value-object and aggregate patterns, UUID generation, notification events, fakes, JUnit 5, MockK, Turbine, and coroutine tests.
- LiteRT-LM as an optional on-device assistant implementation after safety and grounding work.
- Dark theme assets where they meet the new accessibility requirements.

### Components to replace or radically narrow

- `ChatScreen`, `ChatUiState`, `ChatViewModel`, sidebar, conversation aggregate, and in-memory conversation repository as the application shell/state model.
- Runtime Hugging Face token embedded through `BuildConfig`; gated credentials must never ship in a client build.
- Model download as a primary onboarding path. AI must be optional and the vault must work without a model.
- Placeholder image attachments and unstructured chat as a record-ingestion path.
- App-wide ViewModel ownership. Each feature requires its own state holder and use cases.

### Missing foundations

- Persistent database and schema migration strategy.
- Encrypted structured storage and encrypted document storage.
- Key lifecycle, biometric gate, recovery, backup rules, and threat model.
- Profile scoping, dependent authorization, consent ledger, audit history, and provenance.
- Navigation, feature modules, deep links, accessibility, and adaptive layouts.
- Health-specific models, validation, units, time zones, source attribution, conflict handling, and deletion semantics.
- Health Connect permissions/sync, share/export controls, search, OCR validation, and release compliance.

## Product and technical assumptions

1. Android is the only implementation target in this repository.
2. Initial release is local-first and useful without an account or server.
3. Cloud sync and expiring web links require a separately planned service; they do not enter the MVP by implication.
4. `minSdk 26` can remain for the local vault, but Health Connect is available only on supported Android/Google Play devices; the integration must degrade gracefully.
5. Every clinical record belongs to exactly one profile and carries provenance, creation time, update time, and soft-delete/tombstone state where sync may later exist.
6. All dates use an instant plus source zone when time matters; date-only clinical concepts remain date-only.
7. Measurements store canonical units and the original entered value/unit.
8. Documents and derived OCR values are separate records linked by provenance.
9. “Delete” includes derived search/OCR/AI artifacts, cached thumbnails, exports, and queued jobs.
10. A legal/security review is required before public release; this planning set does not make legal or medical-device determinations.

## Target architecture direction

```text
Compose UI → feature state holders → use cases → domain contracts
                                            ↓
                encrypted repositories / document vault / integrations
                                            ↓
                  Room-compatible DB, files, Health Connect, optional AI
```

Suggested Gradle/module direction after MG-02 proves build cost:

```text
:app
:core:model        :core:domain       :core:database
:core:security     :core:files        :core:ui
:core:testing      :core:search       :core:ai
:feature:home      :feature:profiles  :feature:emergency
:feature:medications  :feature:reports  :feature:vitals
:feature:journal   :feature:insurance :feature:sharing
:integration:healthconnect
```

The exact split is a decision, not a requirement. Dependency direction and feature ownership are required.

## Success definition

The pivot is successful when a user can create an encrypted local vault, unlock it safely, manage multiple authorized profiles, record and retrieve core health data offline, expose only chosen emergency fields, verify imported/OCR data, search privately, export a granular packet, and use optional grounded assistance without the AI becoming the source of truth.

