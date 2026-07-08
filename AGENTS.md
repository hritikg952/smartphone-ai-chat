# Smartphone AI Chat — Agent Guide

## Overview

Single-activity Kotlin/Compose chat app (Gemini-inspired). Dark theme, ephemeral in-memory state. Uses **LiteRT-LM** (Google's on-device LLM runtime) — model file downloaded from HuggingFace at runtime. Supports multiple models (Gemma 3 1B, Gemma 4 E2B).

**Refactoring complete** — all 6 migration phases (Clean Architecture + ISP + DI + ConversationRepository) are done as of July 2026.

## Architecture

**Clean Architecture + MVVM + ISP** — fully refactored from the original monolithic god-class ViewModel.

```
UI → Presentation → Domain ← Data
```

Package: `com.smartphoneaichat`. Sources under `app/src/main/java/com/smartphoneaichat/`.

## Key Files

| File | Purpose |
|---|---|
| `MainActivity.kt` | Entry point — applies theme, creates `AppContainer`, wires `ChatViewModelFactory` |
| `di/AppContainer.kt` | Manual DI container — instantiates all dependencies |
| `di/ChatViewModelFactory.kt` | `ViewModelProvider.Factory` for constructor injection |
| `presentation/viewmodel/ChatViewModel.kt` | [483 lines] Orchestrates use cases. Owns `ChatUiState`. Handles pure-state mutations. |
| `presentation/state/ChatUiState.kt` | Single flat `data class` — all state the UI reads |
| `presentation/notification/AppNotificationManager.kt` | SharedFlow-based snackbar event bus |
| `domain/model/Message.kt` | `Message`, `ChatRole`, `Attachment` — domain entities with typed IDs |
| `domain/model/Conversation.kt` | Aggregate root — private constructor, `addMessage()`/`updateMessage()` methods |
| `domain/model/ModelInfo.kt` | `ModelInfo` data class + `AVAILABLE_MODELS` list + `modelInfoById()` |
| `domain/model/value/MessageId.kt` | `@JvmInline value class` — validated non-blank |
| `domain/model/value/ConversationId.kt` | `@JvmInline value class` — validated non-blank |
| `domain/model/value/MessageText.kt` | `@JvmInline value class` — validated max 4096 chars |
| `domain/repository/InferenceEngine.kt` | ISP interface — `sendMessage()`, `stopGeneration()`, `isReady`, `activeModelId` |
| `domain/repository/ModelFileManager.kt` | ISP interface — download/load/delete/unload lifecycle |
| `domain/repository/ConversationRepository.kt` | ISP interface — CRUD for conversations |
| `domain/repository/IdGenerator.kt` | Factory interface — `generateMessageId()`, `generateConversationId()` |
| `domain/service/ConversationTitleService.kt` | Pure service — auto-titling from first user message |
| `domain/usecase/SendMessageUseCase.kt` | Orchestrates inference — token streaming, auto-title, error handling |
| `domain/usecase/DownloadModelUseCase.kt` | Orchestrates HuggingFace download |
| `domain/usecase/LoadModelUseCase.kt` | Orchestrates engine init with unload-before-load |
| `data/engine/LiteRtInferenceEngine.kt` | `InferenceEngine` impl — wraps LiteRT-LM `Engine` |
| `data/model/HuggingFaceModelFileManager.kt` | `ModelFileManager` impl — HTTP download, file I/O, Engine init |
| `data/conversation/InMemoryConversationRepository.kt` | `ConversationRepository` impl — `ConcurrentHashMap`-backed |
| `data/id/UuidIdGenerator.kt` | `IdGenerator` impl — `UUID.randomUUID()` |
| `ui/screens/ChatScreen.kt` | Root composable — wires sidebar, messages, input, model menu |
| `ui/components/` | `ChatBubble`, `ChatInput`, `Sidebar`, `ThinkingSection`, `ModelLoaderDialog`, `ModelSelectorDialog`, `NotificationHost` |
| `ui/theme/` | Dark palette, typography, Material 3 theme |

## State Pattern

`ChatViewModel._state: MutableStateFlow<ChatUiState>` → exposed as `state: StateFlow<ChatUiState>`. All mutations go through `_state.update { ... }`.

### ChatUiState fields

```kotlin
data class ChatUiState(
    conversations: List<Conversation>,
    activeConversationId: ConversationId?,
    isSidebarOpen: Boolean,
    thinkingExpandedIds: Set<MessageId>,
    loadingModelId: String?,
    isModelLoading: Boolean,
    modelLoadProgress: Float,
    modelLoadPhase: String,
    activeModelId: String?,       // null = no model loaded
    activeModelDisplayName: String?,
    downloadedModelIds: Set<String>,
    showDeleteConfirmation: Boolean,
    deleteTargetModelId: String?,
    showModelSelector: Boolean,
    modelSelectorModels: List<ModelInfo>
)
```

Helper: `activeConversation: Conversation?` computed property.

## Notifications

`viewModel.notifications.show(AppNotificationEvent.Success|Error("msg"))` → collected by `NotificationHost` → rendered as Snackbar.

## AI Inference (LiteRT-LM)

### Available Models

Defined in `ModelInfo.kt`:

| ID | Display Name | HF Repo | File |
|---|---|---|---|
| `gemma3-1b` | Gemma 3 1B | `litert-community/Gemma3-1B-IT` | `gemma3-1b-it-int4.litertlm` |
| `gemma4-e2b` | Gemma 4 E2B | `litert-community/gemma-4-E2B-it-litert-lm` | `gemma-4-E2B-it.litertlm` |

### Send Message

`ChatViewModel.sendMessage(text)` → trims text, validates length (max 4096), cancels previous stream → `SendMessageUseCase.invoke(conversation, text)` → `Flow<Conversation>` collected token-by-token → state updated on each emission.

`SendMessageUseCase` handles:
1. Create user `Message` via `IdGenerator`
2. Create placeholder AI `Message` (empty text, `isStreaming = true`, thinking text)
3. Append both to conversation
4. Auto-title conversation from first message via `ConversationTitleService`
5. Collect tokens from `InferenceEngine.sendMessage()` into AI message text
6. Mark `isStreaming = false` on completion/error

Also supports `attachImage()` (creates placeholder attachment) and `removePendingAttachment()`.

### Model Download / Load / Management

`ChatViewModel.downloadModel(modelId)` → `DownloadModelUseCase.invoke(modelId)` → `HuggingFaceModelFileManager.downloadModel()`. If only one model downloaded, auto-loads via `LoadModelUseCase`. If multiple, shows `ModelSelectorDialog`.

`LoadModelUseCase` unloads any previously loaded model first, then calls `HuggingFaceModelFileManager.loadModel()` which creates and initializes the `Engine`.

`HuggingFaceModelFileManager` handles all file I/O: download from HuggingFace (8192-byte buffer, `HF_TOKEN` auth, cancellation), file existence checks, engine lifecycle (`Engine(EngineConfig)` with CPU backend → `engine.initialize()`), and model file deletion.

### ViewModel Dependencies (constructor-injected)

- `SendMessageUseCase`, `DownloadModelUseCase`, `LoadModelUseCase`
- `InferenceEngine`, `ModelFileManager`, `ConversationRepository`, `IdGenerator`
- `Application` (for `filesDir` in `HuggingFaceModelFileManager`)

### ViewModel Fields

- `streamingJob: Job?` — tracks active streaming coroutine
- `downloadJob: Job?` — tracks active download coroutine
- `notifications: AppNotificationManager`

## Theme

Strictly dark (`SmartphoneAIChatTheme`). Colors defined in `Color.kt`. Typography in `Type.kt`.

## Build & Run

```bash
./gradlew assembleDebug          # Debug APK
./gradlew assembleRelease        # Release APK
```

CI: `.github/workflows/release.yml` builds on push to `main`.

## Conventions

- No comments in production code (except KDoc on `MainActivity` for HF setup instructions)
- No emojis in UI code
- Use `Modifier` parameter on all public composables
- Prefer `remember { }` over `rememberSaveable` (exception: `ChatInput` saves text across config changes)
- All composables are `@Composable` functions, not extension functions