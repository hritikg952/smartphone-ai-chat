# Personal Health Vault — Agent Guide

## Overview

Single-activity Android app built with Kotlin and Jetpack Compose. The active product direction is a strictly dark, local-first Personal Health Vault. Startup now enters a health-vault onboarding flow; the former local Gemma chat implementation remains in the repository as legacy code pending isolation and migration work.

The app uses Clean Architecture with MVVM and small repository interfaces:

```
UI → Presentation → Domain ← Data
```

Package: `com.smartphoneaichat`. Production sources are under `app/src/main/java/com/smartphoneaichat/`.

The current health-vault shell has real vault unlock state plus prototype
encrypted local persistence for structured health records and document bytes.
Legacy conversations remain in memory through `InMemoryConversationRepository`;
they do not survive an app-process restart.

## Project Setup

- Android Gradle Plugin 8.5.0, Kotlin 2.2.0, Java 17
- `compileSdk` / `targetSdk`: 34; `minSdk`: 26
- Jetpack Compose with Material 3; one `MainActivity`
- LiteRT-LM dependency: `com.google.ai.edge.litertlm:litertlm-android`
- Internet permission is required for model downloads.
- Set `HF_TOKEN` in `gradle.properties` to download gated Hugging Face models. Users must also accept the relevant Gemma license.

Build commands:

```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew testDebugUnitTest
```

`.github/workflows/release.yml` builds a release APK and uploads it as an artifact on pushes to `main`.

## Architecture and Key Ownership

| Area | Responsibility |
|---|---|
| `MainActivity.kt` | Enables secure-window rendering, edge-to-edge UI, creates app/session/access ViewModels, renders `PersonalHealthVaultApp`, and locks the vault on backgrounding. |
| `ui/app/PersonalHealthVaultApp.kt` | Resolves safe destinations for onboarding, unlock, and the unlocked vault shell from session state. |
| `ui/app/VaultShell.kt` | Owns the adaptive primary navigation bar/rail, selected-profile indicator, and lock affordance for unlocked routes. |
| `presentation/session/` | Defines startup session state and the pure onboarding/unlock/home destination resolver. |
| `presentation/onboarding/` | Owns the welcome-to-credentials onboarding transition. |
| `di/AppContainer.kt` | Composes concrete repositories, the LiteRT engine, title service, and use cases. |
| `presentation/viewmodel/ChatViewModel.kt` | Owns UI state, coordinates conversations, model actions, notifications, and use cases. |
| `presentation/state/ChatUiState.kt` | Flat immutable state consumed by Compose; exposes the computed `activeConversation`. |
| `presentation/notification/` | SharedFlow-backed snackbar event bus and its event types. |
| `domain/model/` | Health record models, vault security models, conversation aggregate, messages, attachments, model metadata, and validated ID/text value objects. |
| `domain/repository/` | ISP contracts for vault access/ciphering, encrypted health records, encrypted documents, backup policy, inference, model files, conversations, and ID generation. |
| `domain/usecase/` | Message streaming, model download, and model loading orchestration. |
| `data/security/` | Local password verifier, Android Keystore DEK envelope, process-local vault session, and AES-GCM content cipher. |
| `data/persistence/` | Prototype encrypted health-record file store, encrypted document blob store, record/document coordinator, and disabled backup policy. |
| `data/` | LiteRT engine adapter, Hugging Face file manager, in-memory conversations, UUID IDs, session storage, security, and persistence implementations. |
| `ui/` | Health-vault app root and onboarding screens plus the retained legacy chat screen/components and dark Material 3 theme. |

Use domain interfaces from presentation/domain code. Keep LiteRT-LM, HTTP,
Android file I/O, Android Keystore, concrete vault storage, and concrete
encrypted persistence details in `data`.

## Vault Storage

MG-03 owns the key lifecycle. MG-04 owns prototype encrypted persistence. Feature
code should depend on `HealthRecordRepository`, `EncryptedDocumentStore`,
`VaultBackupPolicy`, and future feature-specific repositories rather than
reading files directly.

- `HealthRecordRepository` stores profile-scoped structured record bodies via
  `VaultCipher`; reads/writes require the vault to be unlocked.
- `EncryptedDocumentStore` stores document bytes as encrypted blobs with opaque
  filenames and an app-private index.
- `VaultStorageCoordinator` coordinates a document import with its companion
  record write and rolls back the document if the record save fails.
- `PrototypeVaultBackupPolicy` keeps export disabled and restore unavailable
  until an authenticated backup/recovery format is reviewed.
- D-007 intentionally defers Room/SQLCipher. Do not add plaintext Room tables
  for health records; replace the prototype store only with an approved
  encrypted database/migration plan.

## State, Conversations, and Notifications

`ChatViewModel` exposes `StateFlow<ChatUiState>` backed by a private `MutableStateFlow`. Update state immutably with `_state.update { ... }`; Compose reads it from `ChatScreen`.

The ViewModel initializes the repository with existing conversations or a welcome conversation. It owns one active conversation, sidebar state, expanded thinking sections, model lifecycle/progress state, and selector/delete dialog state. It cancels active streaming when switching or creating conversations and unloads the model when cleared.

`Conversation` has a private constructor. Create it with `Conversation.create()` and evolve it with `addMessage`, `updateMessage`, `replaceMessages`, or `withTitle`; do not bypass these aggregate methods.

Show user-facing transient feedback through `viewModel.notifications` using `AppNotificationEvent.Success` or `.Error`. `NotificationHost` collects the event flow and renders Material snackbars.

## Message and Inference Flow

`ChatViewModel.sendMessage()` trims input, rejects blank text, enforces the 4,096-character limit, cancels the previous stream, and collects `SendMessageUseCase` on `Dispatchers.IO`.

`SendMessageUseCase`:

1. Creates user and streaming AI messages with `IdGenerator`.
2. Appends them to the `Conversation` and auto-titles a new chat from its first message.
3. Streams text tokens from `InferenceEngine` into the AI message.
4. Always marks the AI message as no longer streaming when the flow finishes or is cancelled.

`LiteRtInferenceEngine` creates a fresh LiteRT conversation per sent message. `stopGeneration()` is currently a no-op, so cancellation is handled by the coroutine that collects the flow.

Image attachments are UI placeholders only: they contain a generated filename and `image/jpeg` MIME type, are not persisted by the ViewModel at attachment time, and are not sent to LiteRT inference.

## Model Lifecycle

Available models are declared in `domain/model/ModelInfo.kt`:

| ID | Display name | Hugging Face repository | File |
|---|---|---|---|
| `gemma3-1b` | Gemma 3 1B | `litert-community/Gemma3-1B-IT` | `gemma3-1b-it-int4.litertlm` |
| `gemma4-e2b` | Gemma 4 E2B | `litert-community/gemma-4-E2B-it-litert-lm` | `gemma-4-E2B-it.litertlm` |

`HuggingFaceModelFileManager` stores model files in `<filesDir>/models`, downloads them with an authenticated `HttpURLConnection`, reports progress, supports cancellation, and deletes partial files after errors or cancellation.

`LoadModelUseCase` unloads any existing model before loading the selected file. The file manager creates and initializes LiteRT `Engine` with the CPU backend and the app cache directory. Deleting an active model unloads it first. When only one model is downloaded, the ViewModel automatically loads it; when two or more are available, it shows `ModelSelectorDialog` for the user to choose.

## UI and Compose Conventions

`PersonalHealthVaultApp` is the application root. It consumes session state plus `OnboardingUiState` and keeps navigation decisions outside individual screens. `VaultShell` is rendered only after a route has passed the session guard; it exposes the typed Home, Records, Add, Insights, and Profile destinations as a bottom bar on compact screens and a rail at 600 dp or wider. Settings is nested under Profile. The retained `ChatScreen` still owns its legacy Compose-only state but is no longer the startup destination.

Home must keep feature sections independent: an unavailable medication, records, vitals, or appointment section cannot blank the rest of Home. Until its owning feature exists, render a safe empty/loading/error state rather than invented clinical data. Show the selected profile relationship in the shell on every protected destination; current storage supports only the self profile despite the future-ready UI contract.

- Keep the UI strictly dark through `SmartphoneAIChatTheme`, colors in `ui/theme/Color.kt`, and typography in `ui/theme/Type.kt`.
- Public reusable composables accept a `Modifier` parameter when they expose layout customization; application-root composables that fill the surface are exceptions.
- Prefer `remember` for Compose-local state. `ChatInput` intentionally uses `rememberSaveable` for draft text across configuration changes.
- Keep UI components declarative; state mutations and side effects belong in `ChatViewModel` or use cases.
- Production code contains explanatory KDoc and integration notes where useful; keep comments accurate and concise.

## Testing

### Test-driven development requirement

For every feature, bug fix, or behavior change, use the `/tdd` skill and follow its red → green loop. Before writing a test, state the public seam(s) to be tested and confirm them with the user. Work in vertical slices: write one failing behavior-focused test, implement only enough to make it pass, then continue to the next slice. Do not write implementation-coupled or tautological tests, and keep refactoring separate from the red → green loop.

### Personal Health Vault wrap-up

When the user explicitly invokes `/health-vault-wrap-up` or `$health-vault-wrap-up`, read and follow [`.codex/skills/health-vault-wrap-up/SKILL.md`](.codex/skills/health-vault-wrap-up/SKILL.md). Do not update the Personal Health Vault dashboard automatically outside that manual workflow.

Unit tests live in `app/src/test/java/com/smartphoneaichat/` and use JUnit 5, MockK, `kotlinx-coroutines-test`, and Turbine.

Coverage currently includes:

- Domain value-object validation, conversation aggregate operations, and conversation title generation.
- Send, download, and load use-case behavior, including token streaming, failures, and cancellation/finalization paths.
- `ChatUiState` and `ChatViewModel` state transitions.
- In-memory conversation repository and UUID ID generator behavior.
- Vault key/session/cipher behavior, encrypted record/document persistence,
  record/document rollback, disabled backup policy, and app session locking.
- Typed vault navigation, primary-destination semantics, selected-profile visibility,
  and safe Home/destination rendering.

Use `FakeInferenceEngine` and `FakeIdGenerator` for deterministic domain/presentation tests; prefer them over wiring LiteRT or network/file I/O into unit tests. Add or update focused tests whenever changing state transitions, aggregate behavior, or use-case contracts.
