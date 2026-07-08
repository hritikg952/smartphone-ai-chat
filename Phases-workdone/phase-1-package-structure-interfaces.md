# Phase 1 — Package Structure + Interfaces

> **Source:** ARCHITECTURE.md §9 Phase 1  
> **Principle:** Structural reorganization + interface definitions. **No logic changes.**  
> **Constraint:** Every source line that moves keeps its original logic. Only package declarations, imports, and class signatures change.  
> **Quality Gate:** `./gradlew assembleDebug` compiles. `./gradlew test` passes (all Phase 0 tests still green).

---

## Overview

Reorganize the entire project into Clean Architecture package structure (`domain/`, `data/`, `presentation/`, `ui/`). Define the three ISP-compliant repository interfaces. Split the monolithic `LiteRtLmRepository` into `HuggingFaceModelFileManager` (model file lifecycle) and `LiteRtInferenceEngine` (inference). Extract `ChatUiState` and `AppNotificationManager` into the `presentation/` package.

## Current State (Post-Phase 0)

```
com.smartphoneaichat/
├── MainActivity.kt
├── data/id/UuidIdGenerator.kt
├── domain/
│   ├── model/value/{MessageId,ConversationId,MessageText}.kt
│   └── repository/IdGenerator.kt
├── litertlm/
│   ├── LiteRtLmRepository.kt        ← ISP violation: 10 methods, 2 concerns
│   └── ModelInfo.kt                  ← domain entity in infrastructure package
├── model/
│   ├── Message.kt                    ← in wrong package
│   └── Conversation.kt               ← in wrong package
├── notification/
│   └── AppNotificationManager.kt     ← in wrong package
├── viewmodel/
│   └── ChatViewModel.kt              ← ChatUiState co-located (line ~350+)
└── ui/...                            ← unchanged
```

## Target State (Post-Phase 1)

```
com.smartphoneaichat/
├── MainActivity.kt                   ← imports updated
├── data/
│   ├── id/UuidIdGenerator.kt         ← unchanged
│   ├── engine/LiteRtInferenceEngine.kt   ← NEW: InferenceEngine impl
│   └── model/HuggingFaceModelFileManager.kt  ← NEW: ModelFileManager impl
├── domain/
│   ├── model/
│   │   ├── Message.kt                ← MOVED from model/
│   │   ├── Conversation.kt           ← MOVED from model/
│   │   ├── ModelInfo.kt              ← MOVED from litertlm/
│   │   └── value/{MessageId,ConversationId,MessageText}.kt  ← unchanged
│   └── repository/
│       ├── IdGenerator.kt            ← unchanged
│       ├── InferenceEngine.kt        ← NEW interface
│       ├── ModelFileManager.kt       ← NEW interface
│       └── ConversationRepository.kt ← NEW interface
├── presentation/
│   ├── state/ChatUiState.kt          ← EXTRACTED from ChatViewModel.kt
│   ├── viewmodel/ChatViewModel.kt    ← MOVED from viewmodel/
│   └── notification/AppNotificationManager.kt  ← MOVED from notification/
└── ui/...                            ← imports updated only
```

---

## Tasks (execute in order)

### Task 1: Create target directory structure

Create empty directories (using `mkdir -p`):

```
app/src/main/java/com/smartphoneaichat/presentation/state/
app/src/main/java/com/smartphoneaichat/presentation/viewmodel/
app/src/main/java/com/smartphoneaichat/presentation/notification/
app/src/main/java/com/smartphoneaichat/data/engine/
app/src/main/java/com/smartphoneaichat/data/model/
```

**Manual verification checklist:**
- [x] All 5 directories exist
- [x] `domain/model/` already exists (from Phase 0)
- [x] `domain/repository/` already exists (from Phase 0)
- [x] `data/id/` already exists (from Phase 0)

---

### Task 2: Create domain repository interfaces

Create THREE new interface files in `domain/repository/`. Write them with full KDoc explaining the business rationale for each.

#### Task 2a: `InferenceEngine.kt`

```kotlin
package com.smartphoneaichat.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over the LLM inference backend.
 *
 * Business rationale: Decouples chat logic from the specific on-device
 * runtime. Enables testing with fake engines and future migration to
 * cloud or other local APIs without changing domain code.
 *
 * ISP note: Contains ONLY inference concerns. Download and file
 * management are separated into [ModelFileManager].
 */
interface InferenceEngine {
    fun sendMessage(text: String): Flow<String>
    fun stopGeneration()
    val isReady: Boolean
    val activeModelId: String?
}
```

#### Task 2b: `ModelFileManager.kt`

```kotlin
package com.smartphoneaichat.domain.repository

import com.smartphoneaichat.domain.model.ModelInfo

/**
 * Manages model file lifecycle: download, storage, deletion, loading.
 *
 * Business rationale: Model file management is a distinct concern from
 * inference. Separating them follows ISP — a client that only sends
 * messages should not depend on download or file deletion methods.
 */
interface ModelFileManager {
    suspend fun downloadModel(modelInfo: ModelInfo, onProgress: (Float) -> Unit = {}): Result<Unit>
    fun cancelDownload()
    fun isDownloaded(modelInfo: ModelInfo): Boolean
    fun listDownloadedModelIds(): List<String>
    fun deleteModelFile(modelInfo: ModelInfo): Boolean
    suspend fun loadModel(modelInfo: ModelInfo, onProgress: (Float) -> Unit = {}): Result<Unit>
    fun unloadModel()
}
```

#### Task 2c: `ConversationRepository.kt`

```kotlin
package com.smartphoneaichat.domain.repository

import com.smartphoneaichat.domain.model.Conversation
import com.smartphoneaichat.domain.model.value.ConversationId

/**
 * Persistence abstraction for conversation threads.
 *
 * Business rationale: Conversations are currently in-memory but the
 * codebase has explicit plans for Room/DataStore persistence.
 * Abstracting this now avoids a second refactoring later.
 *
 * The ViewModel never touches persistence details — it works through
 * this interface whether the implementation is in-memory or SQLite.
 */
interface ConversationRepository {
    suspend fun getAll(): List<Conversation>
    suspend fun getById(id: ConversationId): Conversation?
    suspend fun save(conversation: Conversation)
    suspend fun delete(id: ConversationId)
}
```

**Manual verification checklist:**
- [x] All 3 interface files created in `domain/repository/`
- [x] All have KDoc explaining business rationale
- [x] No Android, Compose, or SDK imports (only `kotlinx.coroutines.flow.Flow` + domain types)
- [x] Each interface follows ISP (no unused methods per client type)

---

### Task 3: Move domain model files

Move files to `domain/model/` and update their package declarations. This is a pure file move — zero logic changes.

#### Task 3a: Move `model/Message.kt` → `domain/model/Message.kt`

1. Read the current file
2. Write to `domain/model/Message.kt` with package changed to `com.smartphoneaichat.domain.model`
3. Delete `model/Message.kt`
4. The imports of `MessageId` and `MessageText` need path update:
   - `import com.smartphoneaichat.domain.model.value.MessageId`
   - `import com.smartphoneaichat.domain.model.value.MessageText`

Since Message.kt is already in `domain.model` after the move, these imports might simplify to relative/package-level. Verify: `MessageId` and `MessageText` are in `com.smartphoneaichat.domain.model.value` — Message.kt will be in `com.smartphoneaichat.domain.model`. So import stays: `import com.smartphoneaichat.domain.model.value.MessageId`.

#### Task 3b: Move `model/Conversation.kt` → `domain/model/Conversation.kt`

Same process. Package → `com.smartphoneaichat.domain.model`. Import `ConversationId` from `com.smartphoneaichat.domain.model.value`.

#### Task 3c: Move `litertlm/ModelInfo.kt` → `domain/model/ModelInfo.kt`

Package → `com.smartphoneaichat.domain.model`. This file has no imports of other app types. Delete original in `litertlm/`.

**Manual verification checklist:**
- [x] `model/Message.kt` deleted
- [x] `model/Conversation.kt` deleted
- [x] `litertlm/ModelInfo.kt` deleted
- [x] Three files exist in `domain/model/` with correct packages
- [x] `model/` directory is now empty (the old `com.smartphoneaichat.model` package has no files)
- [x] Verify no other files were in `model/` before deleting the directory (optional: remove empty directory)

---

### Task 4: Move `AppNotificationManager` to `presentation/notification/`

1. Read `notification/AppNotificationManager.kt`
2. Write to `presentation/notification/AppNotificationManager.kt` with package changed to `com.smartphoneaichat.presentation.notification`
3. Delete `notification/AppNotificationManager.kt`
4. Remove empty `notification/` directory

**Manual verification checklist:**
- [x] File exists at `presentation/notification/AppNotificationManager.kt`
- [x] Package is `com.smartphoneaichat.presentation.notification`
- [x] Old file deleted, old directory removed

---

### Task 5: Extract `ChatUiState` from ViewModel

1. Read the current `ChatUiState` data class from `viewmodel/ChatViewModel.kt` (the class starts around line 350 with `data class ChatUiState(...)`)
2. Create `presentation/state/ChatUiState.kt` with:
   - Package: `com.smartphoneaichat.presentation.state`
   - Copy the entire `ChatUiState` data class
   - Add imports: `ConversationId`, `MessageId` (value objects), `Conversation`, `ModelInfo`
3. Remove the `ChatUiState` data class from `ChatViewModel.kt` (but keep the `replaceConversation` extension function — it stays in the ViewModel file since it uses `_state`)
4. Add `import com.smartphoneaichat.presentation.state.ChatUiState` to ChatViewModel.kt

**Important:** The `replaceConversation` extension function is defined as:
```kotlin
private fun ChatUiState.replaceConversation(updated: Conversation): ChatUiState { ... }
```
This extension is private to `ChatViewModel` and uses ViewModel state. It stays in `ChatViewModel.kt`. It will still compile since `ChatUiState` is imported.

**Manual verification checklist:**
- [x] `presentation/state/ChatUiState.kt` exists with only the `ChatUiState` data class
- [x] `ChatViewModel.kt` no longer contains `ChatUiState` class definition
- [x] `ChatViewModel.kt` imports `ChatUiState` from `com.smartphoneaichat.presentation.state`
- [x] All existing functionality maintained

---

### Task 6: Split `LiteRtLmRepository` into two classes + implement interfaces

This is the core structural work. Split the single 153-line class into two classes, each implementing a dedicated interface.

#### What `LiteRtLmRepository.kt` currently contains:

| Method/Property | Belongs To (after split) | Interface Method |
|---|---|---|
| `engine` (private) | `HuggingFaceModelFileManager` | (internal) |
| `activeModelId` | `HuggingFaceModelFileManager` | `InferenceEngine.activeModelId` via delegation |
| `modelDir` (private) | `HuggingFaceModelFileManager` | (internal) |
| `isInitialized` | `HuggingFaceModelFileManager` | `InferenceEngine.isReady` via delegation |
| `getModelFile()` | `HuggingFaceModelFileManager` | (internal helper) |
| `isDownloaded()` | `HuggingFaceModelFileManager` | `ModelFileManager.isDownloaded()` |
| `listDownloadedModels()` | `HuggingFaceModelFileManager` | `ModelFileManager.listDownloadedModelIds()` |
| `downloadModel()` | `HuggingFaceModelFileManager` | `ModelFileManager.downloadModel()` |
| `cancelDownload()` | `HuggingFaceModelFileManager` | `ModelFileManager.cancelDownload()` |
| `initialize()` | `HuggingFaceModelFileManager` | `ModelFileManager.loadModel()` |
| `sendMessageAsync()` | `LiteRtInferenceEngine` | `InferenceEngine.sendMessage()` |
| `closeEngine()` | `HuggingFaceModelFileManager` | `ModelFileManager.unloadModel()` |
| `deleteModelFile()` | `HuggingFaceModelFileManager` | `ModelFileManager.deleteModelFile()` |

#### Task 6a: Create `data/model/HuggingFaceModelFileManager.kt`

This class handles ALL model file lifecycle concerns. It owns the `Engine` instance.

```kotlin
package com.smartphoneaichat.data.model

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.smartphoneaichat.BuildConfig
import com.smartphoneaichat.domain.model.ModelInfo
import com.smartphoneaichat.domain.repository.ModelFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class HuggingFaceModelFileManager(private val context: Context) : ModelFileManager {

    @Volatile
    private var isDownloadCancelled = false

    private var engine: Engine? = null

    var activeModelId: String? = null
        private set

    private val modelDir: File
        get() = File(context.filesDir, "models")

    val isInitialized: Boolean
        get() = engine != null

    fun getModelFile(modelInfo: ModelInfo): File =
        File(modelDir, modelInfo.fileName)

    override fun isDownloaded(modelInfo: ModelInfo): Boolean =
        getModelFile(modelInfo).exists()

    override fun listDownloadedModelIds(): List<String> =
        com.smartphoneaichat.domain.model.AVAILABLE_MODELS
            .asSequence()
            .filter { isDownloaded(it) }
            .map { it.id }
            .toList()

    override suspend fun downloadModel(modelInfo: ModelInfo, onProgress: (Float) -> Unit): Result<Unit> {
        // SAME LOGIC as current LiteRtLmRepository.downloadModel()
        // Copy exactly from the current file, line for line.
        // Only changes:
        //   - class name in error messages (if any)
        //   - BuildConfig import path (com.smartphoneaichat.BuildConfig)
    }

    override fun cancelDownload() { /* same logic */ }

    override suspend fun loadModel(modelInfo: ModelInfo, onProgress: (Float) -> Unit): Result<Unit> {
        // SAME LOGIC as current LiteRtLmRepository.initialize()
        // Rename: initialize → loadModel
    }

    override fun unloadModel() {
        // SAME LOGIC as current LiteRtLmRepository.closeEngine()
        // Rename: closeEngine → unloadModel
    }

    override fun deleteModelFile(modelInfo: ModelInfo): Boolean {
        // SAME LOGIC as current LiteRtLmRepository.deleteModelFile()
    }

    /**
     * Provides the underlying LiteRT-LM Engine to [LiteRtInferenceEngine].
     * Returns null if no model has been loaded.
     */
    internal fun getEngine(): Engine? = engine
}
```

**Key implementation notes:**
- Copy ALL method bodies verbatim from `LiteRtLmRepository.kt` — no logic changes
- `initialize()` is renamed to `loadModel()` but keeps the same body
- `closeEngine()` is renamed to `unloadModel()` but keeps the same body
- `listDownloadedModels()` is renamed to `listDownloadedModelIds()` in the interface — same body
- Add `internal fun getEngine(): Engine?` — used by `LiteRtInferenceEngine` (internal visibility, not exposed to domain)
- `isInitialized` remains as a convenience property (used by ViewModel to check `isReady`)

#### Task 6b: Create `data/engine/LiteRtInferenceEngine.kt`

This class handles ONLY inference — sending messages and stopping generation.

```kotlin
package com.smartphoneaichat.data.engine

import com.google.ai.edge.litertlm.Content
import com.smartphoneaichat.data.model.HuggingFaceModelFileManager
import com.smartphoneaichat.domain.repository.InferenceEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LiteRtInferenceEngine(
    private val modelFileManager: HuggingFaceModelFileManager
) : InferenceEngine {

    override val isReady: Boolean
        get() = modelFileManager.isInitialized

    override val activeModelId: String?
        get() = modelFileManager.activeModelId

    override fun sendMessage(text: String): Flow<String> {
        // SAME LOGIC as current LiteRtLmRepository.sendMessageAsync()
        // But get engine from modelFileManager.getEngine()
        val eng = modelFileManager.getEngine()
            ?: throw IllegalStateException("Engine not initialized. Call loadModel() first.")

        val conversation = eng.createConversation()

        return conversation.sendMessageAsync(text).map { liteRtMsg ->
            liteRtMsg.contents.contents.filterIsInstance<Content.Text>().joinToString("") { it.text }
        }
    }

    override fun stopGeneration() {
        // LiteRT-LM SDK: cancel the latest conversation's streaming
        // For now, ViewModel handles this via streamingJob?.cancel()
        // This is a placeholder that can be extended when SDK supports it.
        // Current behavior: cancelling the coroutine stops collection.
    }
}
```

**Key implementation notes:**
- `sendMessage()` contains the same logic as `sendMessageAsync()` from the old class
- `stopGeneration()` — the current codebase cancels via `streamingJob?.cancel()` in the ViewModel. Keep this behavior. The method body can be a no-op or close the engine conversation. The ViewModel continues to cancel via its coroutine job.
- `isReady` delegates to `modelFileManager.isInitialized`
- `activeModelId` delegates to `modelFileManager.activeModelId`
- Note: `sendMessage()` returns `Flow<String>` (interface method name), NOT `sendMessageAsync()` (old name). The ViewModel will call `inferenceEngine.sendMessage(text)` instead of `litertLmRepository.sendMessageAsync(text)`.

#### Task 6c: Delete `litertlm/LiteRtLmRepository.kt`

After confirming both new classes are created, delete the old file and remove the empty `litertlm/` directory.

**Manual verification checklist:**
- [x] `data/model/HuggingFaceModelFileManager.kt` exists, implements `ModelFileManager`
- [x] `data/engine/LiteRtInferenceEngine.kt` exists, implements `InferenceEngine`
- [x] `litertlm/LiteRtLmRepository.kt` deleted
- [x] `litertlm/` directory removed (since ModelInfo.kt was moved in Task 3c)
- [x] All method bodies match the original logic (can diff against git history)

---

### Task 7: Update `ChatViewModel` package + dependencies

#### Task 7a: Move `viewmodel/ChatViewModel.kt` → `presentation/viewmodel/ChatViewModel.kt`

1. Read current file
2. Write to `presentation/viewmodel/ChatViewModel.kt` with package → `com.smartphoneaichat.presentation.viewmodel`
3. Delete old file
4. Remove empty `viewmodel/` directory

#### Task 7b: Update all imports in `ChatViewModel.kt`

Replace old imports with new paths. The ViewModel now depends on domain interfaces + data implementations.

**Old imports to replace:**

| Old Import | New Import |
|---|---|
| `com.smartphoneaichat.litertlm.LiteRtLmRepository` | `com.smartphoneaichat.data.model.HuggingFaceModelFileManager` + `com.smartphoneaichat.data.engine.LiteRtInferenceEngine` |
| `com.smartphoneaichat.litertlm.modelInfoById` | `com.smartphoneaichat.domain.model.modelInfoById` |
| `com.smartphoneaichat.model.Attachment` | `com.smartphoneaichat.domain.model.Attachment` |
| `com.smartphoneaichat.model.ChatRole` | `com.smartphoneaichat.domain.model.ChatRole` |
| `com.smartphoneaichat.model.Conversation` | `com.smartphoneaichat.domain.model.Conversation` |
| `com.smartphoneaichat.model.Message` | `com.smartphoneaichat.domain.model.Message` |
| `com.smartphoneaichat.notification.AppNotificationEvent` | `com.smartphoneaichat.presentation.notification.AppNotificationEvent` |
| `com.smartphoneaichat.notification.AppNotificationManager` | `com.smartphoneaichat.presentation.notification.AppNotificationManager` |
| `com.smartphoneaichat.domain.repository.IdGenerator` | (unchanged) |
| `com.smartphoneaichat.data.id.UuidIdGenerator` | (unchanged) |
| `com.smartphoneaichat.domain.model.value.ConversationId` | (unchanged) |
| `com.smartphoneaichat.domain.model.value.MessageId` | (unchanged) |
| `com.smartphoneaichat.domain.model.value.MessageText` | (unchanged) |
| Add: | `com.smartphoneaichat.domain.repository.InferenceEngine` |
| Add: | `com.smartphoneaichat.domain.repository.ModelFileManager` |
| Add: | `com.smartphoneaichat.domain.model.ModelInfo` |

Also add `import com.smartphoneaichat.presentation.state.ChatUiState`.

#### Task 7c: Replace `litertLmRepository` with separated dependencies

Replace the single property:
```kotlin
val litertLmRepository = LiteRtLmRepository(application)
```

With two properties:
```kotlin
private val modelFileManager: ModelFileManager = HuggingFaceModelFileManager(application)
private val inferenceEngine: InferenceEngine = LiteRtInferenceEngine(modelFileManager as HuggingFaceModelFileManager)
```

> Note: The cast `as HuggingFaceModelFileManager` is needed because `LiteRtInferenceEngine` takes the concrete type (to access `getEngine()`). This coupling will be cleaned up in Phase 4 (DI).

#### Task 7d: Rename all method call sites

Update every call site in `ChatViewModel.kt`:

| Old Call | New Call |
|---|---|
| `litertLmRepository.sendMessageAsync(text)` | `inferenceEngine.sendMessage(text)` |
| `litertLmRepository.isInitialized` | `inferenceEngine.isReady` |
| `litertLmRepository.activeModelId` | `inferenceEngine.activeModelId` |
| `litertLmRepository.initialize(modelInfo) { ... }` | `modelFileManager.loadModel(modelInfo) { ... }` |
| `litertLmRepository.closeEngine()` | `modelFileManager.unloadModel()` |
| `litertLmRepository.downloadModel(modelInfo) { ... }` | `modelFileManager.downloadModel(modelInfo) { ... }` |
| `litertLmRepository.cancelDownload()` | `modelFileManager.cancelDownload()` |
| `litertLmRepository.deleteModelFile(modelInfo)` | `modelFileManager.deleteModelFile(modelInfo)` |
| `litertLmRepository.listDownloadedModels()` | `modelFileManager.listDownloadedModelIds()` |
| `litertLmRepository.isDownloaded(modelInfo)` | `modelFileManager.isDownloaded(modelInfo)` |

**Verification:** Search for any remaining `litertLmRepository` references in the file — there should be zero.

#### Task 7e: Update `ChatUiState` internal references

In `ChatUiState`, the field:
```kotlin
val modelSelectorModels: List<com.smartphoneaichat.litertlm.ModelInfo> = emptyList()
```

Becomes:
```kotlin
val modelSelectorModels: List<com.smartphoneaichat.domain.model.ModelInfo> = emptyList()
```

Better yet, add the import and use:
```kotlin
val modelSelectorModels: List<ModelInfo> = emptyList()
```

#### Task 7f: Update `modelInfoById()` and `AVAILABLE_MODELS` references

Since `ModelInfo.kt` moved to `domain/model/`, update any inline references:
- `com.smartphoneaichat.litertlm.modelInfoById` → `com.smartphoneaichat.domain.model.modelInfoById`
- `com.smartphoneaichat.litertlm.AVAILABLE_MODELS` → `com.smartphoneaichat.domain.model.AVAILABLE_MODELS`

**Manual verification checklist:**
- [x] `presentation/viewmodel/ChatViewModel.kt` exists
- [x] `viewmodel/ChatViewModel.kt` deleted
- [x] All imports updated to new package paths
- [x] Zero references to `litertLmRepository` (search the file)
- [x] Zero references to old `com.smartphoneaichat.model.*` imports
- [x] Zero references to old `com.smartphoneaichat.litertlm.*` imports
- [x] Zero references to old `com.smartphoneaichat.notification.*` imports
- [x] `inferenceEngine` and `modelFileManager` properties declared correctly

---

### Task 8: Update all UI component imports

Every file that imports from moved packages must be updated.

#### Files to update and their import changes:

| File | Old Import | New Import |
|---|---|---|
| `MainActivity.kt` | `com.smartphoneaichat.viewmodel.ChatViewModel` | `com.smartphoneaichat.presentation.viewmodel.ChatViewModel` |
| `ui/screens/ChatScreen.kt` | `com.smartphoneaichat.viewmodel.ChatViewModel` | `com.smartphoneaichat.presentation.viewmodel.ChatViewModel` |
| `ui/screens/ChatScreen.kt` | `com.smartphoneaichat.model.ChatRole` | `com.smartphoneaichat.domain.model.ChatRole` |
| `ui/screens/ChatScreen.kt` | `com.smartphoneaichat.model.Conversation` | `com.smartphoneaichat.domain.model.Conversation` |
| `ui/screens/ChatScreen.kt` | `com.smartphoneaichat.model.Message` | `com.smartphoneaichat.domain.model.Message` |
| `ui/screens/ChatScreen.kt` | `com.smartphoneaichat.model.Attachment` | `com.smartphoneaichat.domain.model.Attachment` |
| `ui/screens/ChatScreen.kt` | `com.smartphoneaichat.litertlm.AVAILABLE_MODELS` | `com.smartphoneaichat.domain.model.AVAILABLE_MODELS` |
| `ui/screens/ChatScreen.kt` | `com.smartphoneaichat.litertlm.modelInfoById` | `com.smartphoneaichat.domain.model.modelInfoById` |
| `ui/screens/ChatScreen.kt` | `com.smartphoneaichat.litertlm.ModelInfo` | `com.smartphoneaichat.domain.model.ModelInfo` |
| `ui/components/ChatBubble.kt` | `com.smartphoneaichat.model.ChatRole` | `com.smartphoneaichat.domain.model.ChatRole` |
| `ui/components/ChatBubble.kt` | `com.smartphoneaichat.model.Message` | `com.smartphoneaichat.domain.model.Message` |
| `ui/components/Sidebar.kt` | `com.smartphoneaichat.model.Conversation` | `com.smartphoneaichat.domain.model.Conversation` |
| `ui/components/ChatInput.kt` | (check if it imports any moved types) | (update if needed) |
| `ui/components/ThinkingSection.kt` | (likely no imports to update) | (verify) |
| `ui/components/ModelLoaderDialog.kt` | (check for model imports) | (update if needed) |
| `ui/components/ModelSelectorDialog.kt` | (check for ModelInfo import) | `com.smartphoneaichat.domain.model.ModelInfo` |
| `ui/components/NotificationHost.kt` | `com.smartphoneaichat.notification.*` | `com.smartphoneaichat.presentation.notification.*` |

#### Update approach for each file:
1. Read the file
2. Replace all outdated import statements
3. Confirm no other changes needed (no logic changes)
4. Save

**Manual verification checklist:**
- [x] Every `.kt` file in the project reviewed for import updates
- [x] No remaining imports from `com.smartphoneaichat.model.*` (old package)
- [x] No remaining imports from `com.smartphoneaichat.litertlm.*` (old package)
- [x] No remaining imports from `com.smartphoneaichat.notification.*` (old package)
- [x] No remaining imports from `com.smartphoneaichat.viewmodel.*` (old package)

---

### Task 9: Clean up empty directories

After all moves, these directories should be empty. Delete them:

```
app/src/main/java/com/smartphoneaichat/model/
app/src/main/java/com/smartphoneaichat/litertlm/
app/src/main/java/com/smartphoneaichat/notification/
app/src/main/java/com/smartphoneaichat/viewmodel/
```

**Manual verification checklist:**
- [x] All 4 old directories deleted
- [x] `ls` confirms they no longer exist

---

### Task 10: Verify build compiles and tests pass

```bash
./gradlew assembleDebug
./gradlew test
```

**Troubleshooting common issues:**

| Error | Fix |
|---|---|
| "Unresolved reference: ModelInfo" | Check that `ChatUiState.kt` imports `com.smartphoneaichat.domain.model.ModelInfo` |
| "Unresolved reference: AVAILABLE_MODELS" | `AVAILABLE_MODELS` is now in `com.smartphoneaichat.domain.model` — update imports |
| "Unresolved reference: modelInfoById" | Same as above |
| "Type mismatch: InferenceEngine vs LiteRtInferenceEngine" | In ViewModel, cast needed: `modelFileManager as HuggingFaceModelFileManager` |
| "Cannot access 'getEngine': it is internal" | Only `LiteRtInferenceEngine` (same module) should access it — if error in ViewModel, you're passing wrong type |
| "sendMessageAsync unresolved" | Method renamed to `sendMessage` — update call sites |
| "initialize unresolved" | Method renamed to `loadModel` — update call sites |
| "closeEngine unresolved" | Method renamed to `unloadModel` — update call sites |

**Quality Gate validation:**
- [ ] `./gradlew assembleDebug` passes with zero errors
- [ ] `./gradlew test` passes with all Phase 0 tests green
- [ ] No files remain in `com.smartphoneaichat.model` (old package)
- [ ] No files remain in `com.smartphoneaichat.litertlm` (old package)
- [ ] No files remain in `com.smartphoneaichat.notification` (old package)
- [ ] No files remain in `com.smartphoneaichat.viewmodel` (old package)
- [ ] `domain/repository/` contains: `IdGenerator.kt`, `InferenceEngine.kt`, `ModelFileManager.kt`, `ConversationRepository.kt`
- [ ] `domain/model/` contains: `Message.kt`, `Conversation.kt`, `ModelInfo.kt` + `value/` subdirectory
- [ ] `data/` contains: `id/UuidIdGenerator.kt`, `engine/LiteRtInferenceEngine.kt`, `model/HuggingFaceModelFileManager.kt`
- [ ] `presentation/` contains: `viewmodel/ChatViewModel.kt`, `state/ChatUiState.kt`, `notification/AppNotificationManager.kt`
- [ ] `LiteRtLmRepository.kt` no longer exists
- [ ] `ChatUiState` is no longer co-located with `ChatViewModel`

---

### Task 11: Dependency direction audit

Verify Clean Architecture dependency rules hold after Phase 1.

| Package | Forbidden imports |
|---|---|
| `domain/` | `android.*`, `com.google.ai.edge.litertlm.*`, `androidx.compose.*`, `com.smartphoneaichat.data.*`, `com.smartphoneaichat.presentation.*`, `com.smartphoneaichat.ui.*` |
| `data/` | `androidx.compose.*`, `com.smartphoneaichat.presentation.*`, `com.smartphoneaichat.ui.*` |
| `presentation/` | `com.google.ai.edge.litertlm.*` |
| `ui/` | `com.google.ai.edge.litertlm.*`, `com.smartphoneaichat.data.*` |

Run this check:
```bash
# Check domain for Android/SDK imports (should return nothing)
grep -r "import android" app/src/main/java/com/smartphoneaichat/domain/ || echo "domain/ clean of android"
grep -r "import com.google.ai.edge.litertlm" app/src/main/java/com/smartphoneaichat/domain/ || echo "domain/ clean of litertlm SDK"
grep -r "import androidx.compose" app/src/main/java/com/smartphoneaichat/domain/ || echo "domain/ clean of compose"
grep -r "import com.smartphoneaichat.data" app/src/main/java/com/smartphoneaichat/domain/ || echo "domain/ clean of data"
grep -r "import com.smartphoneaichat.presentation" app/src/main/java/com/smartphoneaichat/domain/ || echo "domain/ clean of presentation"
grep -r "import com.smartphoneaichat.ui" app/src/main/java/com/smartphoneaichat/domain/ || echo "domain/ clean of ui"

# Check presentation for SDK imports
grep -r "import com.google.ai.edge.litertlm" app/src/main/java/com/smartphoneaichat/presentation/ || echo "presentation/ clean of litertlm SDK"
grep -r "import com.smartphoneaichat.data" app/src/main/java/com/smartphoneaichat/presentation/ || echo "presentation/ clean of data"

# Check UI for SDK/data imports
grep -r "import com.google.ai.edge.litertlm" app/src/main/java/com/smartphoneaichat/ui/ || echo "ui/ clean of litertlm SDK"
grep -r "import com.smartphoneaichat.data" app/src/main/java/com/smartphoneaichat/ui/ || echo "ui/ clean of data"
```

> **Expected exception:** `presentation/viewmodel/ChatViewModel.kt` currently imports `com.smartphoneaichat.data.id.UuidIdGenerator` and `com.smartphoneaichat.data.model.HuggingFaceModelFileManager` — this is a Phase 4 problem (DI will resolve it). For Phase 1, this is acceptable since the ViewModel still instantiates its own dependencies. The architecture doc lists this as a Phase 4 cleanup item.

**Quality gate (dependency rules):**
- [x] `domain/` has zero forbidden imports
- [x] `data/` has zero forbidden imports (except Android, which is allowed in data layer)
- [x] `ui/` has zero `com.smartphoneaichat.data.*` imports
- [x] All KDoc on public interfaces explains business rationale, not implementation details

---

## Files to CREATE

| File | Purpose |
|------|---------|
| `domain/repository/InferenceEngine.kt` | ISP interface — inference only |
| `domain/repository/ModelFileManager.kt` | ISP interface — file lifecycle |
| `domain/repository/ConversationRepository.kt` | ISP interface — conversation CRUD |
| `domain/model/Message.kt` | MOVED — Message, ChatRole, Attachment |
| `domain/model/Conversation.kt` | MOVED — Conversation data class |
| `domain/model/ModelInfo.kt` | MOVED — ModelInfo + AVAILABLE_MODELS + modelInfoById |
| `presentation/state/ChatUiState.kt` | EXTRACTED — ChatUiState data class |
| `presentation/viewmodel/ChatViewModel.kt` | MOVED — ChatViewModel |
| `presentation/notification/AppNotificationManager.kt` | MOVED — AppNotificationManager |
| `data/engine/LiteRtInferenceEngine.kt` | EXTRACTED — InferenceEngine implementation |
| `data/model/HuggingFaceModelFileManager.kt` | EXTRACTED — ModelFileManager implementation |

## Files to DELETE

| File | Reason |
|------|--------|
| `litertlm/LiteRtLmRepository.kt` | Split into `HuggingFaceModelFileManager` + `LiteRtInferenceEngine` |
| `litertlm/ModelInfo.kt` | Moved to `domain/model/ModelInfo.kt` |
| `model/Message.kt` | Moved to `domain/model/Message.kt` |
| `model/Conversation.kt` | Moved to `domain/model/Conversation.kt` |
| `notification/AppNotificationManager.kt` | Moved to `presentation/notification/AppNotificationManager.kt` |
| `viewmodel/ChatViewModel.kt` | Moved to `presentation/viewmodel/ChatViewModel.kt` |

## Files to MODIFY (imports only)

| File | Changes |
|------|---------|
| `MainActivity.kt` | Update ViewModel import path |
| `ui/screens/ChatScreen.kt` | Update all moved-type imports |
| `ui/components/ChatBubble.kt` | Update ChatRole, Message imports |
| `ui/components/Sidebar.kt` | Update Conversation import |
| `ui/components/ModelSelectorDialog.kt` | Update ModelInfo import |
| `ui/components/NotificationHost.kt` | Update AppNotificationManager import |
| `ui/components/ChatInput.kt` | Review for any moved imports |
| `ui/components/ModelLoaderDialog.kt` | Review for any moved imports |
| `ui/components/ThinkingSection.kt` | Review for any moved imports |

## Architectural violations FIXED by this phase

| # | Violation | Status |
|---|-----------|--------|
| 2 | No repository interface | FIXED — `InferenceEngine`, `ModelFileManager`, `ConversationRepository` created |
| 3 | ISP violation (10 methods on one class) | FIXED — Split into `LiteRtInferenceEngine` (2 methods) + `HuggingFaceModelFileManager` (7 methods) |
| 9 | ChatUiState co-located | FIXED — Extracted to `presentation/state/` |
| 10 | ModelInfo in infrastructure package | FIXED — Moved to `domain/model/` |

## Architectural violations NOT YET fixed (deferred to later phases)

| # | Violation | Deferred To |
|---|-----------|-------------|
| 1 | God ViewModel (still ~400+ lines) | Phase 3 (Use Cases) |
| 4 | Anemic domain model | Phase 2 (Aggregate) |
| 5 | Nondeterministic IDs | Phase 0 (already fixed) |
| 6 | No DI | Phase 4 (DI) |
| 7 | SDK leak into ViewModel | NOT FIXED — ViewModel still calls `inferenceEngine.sendMessage()` which returns `Flow<String>`. The Flow type itself is pure Kotlin. SDK leak is addressed in Phase 3 via `SendMessageUseCase`. |
| 8 | No ConversationRepository | NOT FIXED — Interface created, implementation deferred to Phase 5 |

---

## Estimated effort

~1.5-2 hours. Most time spent on:
1. Carefully splitting `LiteRtLmRepository` into two classes (Task 6)
2. Updating imports across 10+ files (Tasks 7, 8)
3. Debugging compilation errors from missed import updates