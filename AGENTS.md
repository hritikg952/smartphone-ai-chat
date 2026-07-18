# Smartphone AI Chat — Agent Guide

## Overview

Single-activity Android chat app built with Kotlin and Jetpack Compose. It has a Gemini-inspired, strictly dark UI and runs supported Gemma models locally through LiteRT-LM. Models are downloaded from Hugging Face to app-private storage at runtime.

The app uses Clean Architecture with MVVM and small repository interfaces:

```
UI → Presentation → Domain ← Data
```

Package: `com.smartphoneaichat`. Production sources are under `app/src/main/java/com/smartphoneaichat/`.

Conversations are kept in memory through `InMemoryConversationRepository`; they do not survive an app-process restart.

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
| `MainActivity.kt` | Enables edge-to-edge UI, creates the manual `AppContainer`, creates `ChatViewModel` through its factory, and renders `ChatScreen`. |
| `di/AppContainer.kt` | Composes concrete repositories, the LiteRT engine, title service, and use cases. |
| `presentation/viewmodel/ChatViewModel.kt` | Owns UI state, coordinates conversations, model actions, notifications, and use cases. |
| `presentation/state/ChatUiState.kt` | Flat immutable state consumed by Compose; exposes the computed `activeConversation`. |
| `presentation/notification/` | SharedFlow-backed snackbar event bus and its event types. |
| `domain/model/` | Conversation aggregate, messages, attachments, model metadata, and validated ID/text value objects. |
| `domain/repository/` | ISP contracts for inference, model files, conversations, and ID generation. |
| `domain/usecase/` | Message streaming, model download, and model loading orchestration. |
| `data/` | LiteRT engine adapter, Hugging Face file manager, in-memory conversations, and UUID IDs. |
| `ui/` | Root chat screen, reusable Compose components, and dark Material 3 theme. |

Use domain interfaces from presentation/domain code. Keep LiteRT-LM, HTTP, Android file I/O, and concrete storage details in `data`.

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

`ChatScreen` owns Compose-only state such as drawer/list/snackbar state and wires the sidebar, message list, `ChatInput`, model menu, dialogs, and notification host to `ChatViewModel` actions.

- Keep the UI strictly dark through `SmartphoneAIChatTheme`, colors in `ui/theme/Color.kt`, and typography in `ui/theme/Type.kt`.
- Public reusable composables accept a `Modifier` parameter when they expose layout customization; `ChatScreen` is the root exception because it fills the app surface.
- Prefer `remember` for Compose-local state. `ChatInput` intentionally uses `rememberSaveable` for draft text across configuration changes.
- Keep UI components declarative; state mutations and side effects belong in `ChatViewModel` or use cases.
- Production code contains explanatory KDoc and integration notes where useful; keep comments accurate and concise.

## Testing

### Test-driven development requirement

For every feature, bug fix, or behavior change, use the `/tdd` skill and follow its red → green loop. Before writing a test, state the public seam(s) to be tested and confirm them with the user. Work in vertical slices: write one failing behavior-focused test, implement only enough to make it pass, then continue to the next slice. Do not write implementation-coupled or tautological tests, and keep refactoring separate from the red → green loop.

### Personal Health Vault wrap-up

When the user explicitly invokes `/health-vault-wrap-up` or `$health-vault-wrap-up`, read and follow [`plans/personal-health-vault/skills/health-vault-wrap-up/SKILL.md`](plans/personal-health-vault/skills/health-vault-wrap-up/SKILL.md). Do not update the Personal Health Vault dashboard automatically outside that manual workflow.

Unit tests live in `app/src/test/java/com/smartphoneaichat/` and use JUnit 5, MockK, `kotlinx-coroutines-test`, and Turbine.

Coverage currently includes:

- Domain value-object validation, conversation aggregate operations, and conversation title generation.
- Send, download, and load use-case behavior, including token streaming, failures, and cancellation/finalization paths.
- `ChatUiState` and `ChatViewModel` state transitions.
- In-memory conversation repository and UUID ID generator behavior.

Use `FakeInferenceEngine` and `FakeIdGenerator` for deterministic domain/presentation tests; prefer them over wiring LiteRT or network/file I/O into unit tests. Add or update focused tests whenever changing state transitions, aggregate behavior, or use-case contracts.
