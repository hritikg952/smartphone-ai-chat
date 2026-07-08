# Phase 4 — Dependency Injection (Constructor Injection)

> **Source:** ARCHITECTURE.md §9 Phase 4  
> **Principle:** Wiring only. No logic changes. Manual constructor injection via `AppContainer`.  
> **Constraint:** ViewModel receives ALL dependencies via constructor parameters. Zero direct instantiation (`XxxImpl()`) in ViewModel body.  
> **Quality Gate:** `./gradlew assembleDebug` compiles. All existing tests (~74) pass. Zero `data.*` imports in `presentation/` layer.

---

## Overview

Create `AppContainer` (object graph) and `ChatViewModelFactory` (ViewModelProvider.Factory). Update `ChatViewModel` to accept dependencies via constructor instead of instantiating them inline. Update `MainActivity` to use the factory. This eliminates the final `data.*` imports from the `presentation/` layer.

**Note:** `ConversationRepository` and `InMemoryConversationRepository` are deferred to Phase 5. The ViewModel constructor will add that parameter in Phase 5.

## Current State (Post-Phase 3.5)

**ChatViewModel** creates all dependencies inline (lines 44-53):
```kotlin
class ChatViewModel(application: Application) : AndroidViewModel(application) {
    ...
    val notifications = AppNotificationManager()                            // inline
    private val modelFileManager: ModelFileManager = HuggingFaceModelFileManager(application)  // inline
    private val inferenceEngine: InferenceEngine = LiteRtInferenceEngine(modelFileManager as HuggingFaceModelFileManager)  // inline
    private val idGenerator: IdGenerator = UuidIdGenerator()                // inline
    private val sendMessageUseCase = SendMessageUseCase(inferenceEngine, idGenerator)  // inline
    private val downloadModelUseCase = DownloadModelUseCase(modelFileManager)         // inline
    private val loadModelUseCase = LoadModelUseCase(modelFileManager)                 // inline
```

**Imports that will become removable:** lines 6-8:
```kotlin
import com.smartphoneaichat.data.engine.LiteRtInferenceEngine
import com.smartphoneaichat.data.id.UuidIdGenerator
import com.smartphoneaichat.data.model.HuggingFaceModelFileManager
```

**MainActivity** uses default `viewModel()` without factory:
```kotlin
val viewModel: ChatViewModel = viewModel()
```

---

## Target State (Post-Phase 4)

### AppContainer

```kotlin
class AppContainer(private val application: Application) {
    val idGenerator: IdGenerator = UuidIdGenerator()
    val modelFileManager: ModelFileManager = HuggingFaceModelFileManager(application)
    val inferenceEngine: InferenceEngine =
        LiteRtInferenceEngine(modelFileManager as HuggingFaceModelFileManager)
    val titleService = ConversationTitleService
    val sendMessageUseCase = SendMessageUseCase(inferenceEngine, idGenerator, titleService)
    val downloadModelUseCase = DownloadModelUseCase(modelFileManager)
    val loadModelUseCase = LoadModelUseCase(modelFileManager)
}
```

### ChatViewModelFactory

```kotlin
class ChatViewModelFactory(
    private val container: AppContainer,
    private val application: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(
            sendMessageUseCase = container.sendMessageUseCase,
            downloadModelUseCase = container.downloadModelUseCase,
            loadModelUseCase = container.loadModelUseCase,
            inferenceEngine = container.inferenceEngine,
            modelFileManager = container.modelFileManager,
            idGenerator = container.idGenerator,
            application = application,
        ) as T
    }
}
```

### ChatViewModel constructor (new)

```kotlin
class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val downloadModelUseCase: DownloadModelUseCase,
    private val loadModelUseCase: LoadModelUseCase,
    private val inferenceEngine: InferenceEngine,
    private val modelFileManager: ModelFileManager,
    private val idGenerator: IdGenerator,
    application: Application,
) : AndroidViewModel(application) {
    // body unchanged — all properties come from constructor now
    ...
}
```

### MainActivity wiring (new)

```kotlin
val context = LocalContext.current
val appContainer = remember { AppContainer(context.applicationContext as Application) }
val factory = remember { ChatViewModelFactory(appContainer, context.applicationContext as Application) }
val viewModel: ChatViewModel = viewModel(factory = factory)
```

---

## Tasks (execute in order)

### Task 1: Create directory

```bash
mkdir -p app/src/main/java/com/smartphoneaichat/di
```

---

### Task 2: Create `AppContainer`

**File:** `app/src/main/java/com/smartphoneaichat/di/AppContainer.kt`

```kotlin
package com.smartphoneaichat.di

import android.app.Application
import com.smartphoneaichat.data.engine.LiteRtInferenceEngine
import com.smartphoneaichat.data.id.UuidIdGenerator
import com.smartphoneaichat.data.model.HuggingFaceModelFileManager
import com.smartphoneaichat.domain.repository.IdGenerator
import com.smartphoneaichat.domain.repository.InferenceEngine
import com.smartphoneaichat.domain.repository.ModelFileManager
import com.smartphoneaichat.domain.service.ConversationTitleService
import com.smartphoneaichat.domain.usecase.DownloadModelUseCase
import com.smartphoneaichat.domain.usecase.LoadModelUseCase
import com.smartphoneaichat.domain.usecase.SendMessageUseCase

class AppContainer(private val application: Application) {

    val idGenerator: IdGenerator = UuidIdGenerator()

    val modelFileManager: ModelFileManager =
        HuggingFaceModelFileManager(application)

    val inferenceEngine: InferenceEngine =
        LiteRtInferenceEngine(modelFileManager as HuggingFaceModelFileManager)

    val titleService = ConversationTitleService

    val sendMessageUseCase = SendMessageUseCase(inferenceEngine, idGenerator, titleService)

    val downloadModelUseCase = DownloadModelUseCase(modelFileManager)

    val loadModelUseCase = LoadModelUseCase(modelFileManager)
}
```

**Key notes:**
- The cast `modelFileManager as HuggingFaceModelFileManager` is required because `LiteRtInferenceEngine` takes the concrete type (to access `getEngine()` which is internal to `HuggingFaceModelFileManager`). This cast is hidden in `AppContainer` — the ViewModel only sees `InferenceEngine` and `ModelFileManager` interfaces.
- `titleService` is `ConversationTitleService` (the singleton object) — passed explicitly to `SendMessageUseCase` to override its default parameter with the same value. This makes wiring explicit.
- `ConversationRepository` is NOT included — deferred to Phase 5.

**Manual verification checklist:**
- [x] `di/AppContainer.kt` created
- [x] All dependencies wired correctly
- [x] `data.*` imports only in `AppContainer` (which lives in `di/` package)

---

### Task 3: Create `ChatViewModelFactory`

**File:** `app/src/main/java/com/smartphoneaichat/di/ChatViewModelFactory.kt`

```kotlin
package com.smartphoneaichat.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smartphoneaichat.presentation.viewmodel.ChatViewModel

class ChatViewModelFactory(
    private val container: AppContainer,
    private val application: Application,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(
            sendMessageUseCase = container.sendMessageUseCase,
            downloadModelUseCase = container.downloadModelUseCase,
            loadModelUseCase = container.loadModelUseCase,
            inferenceEngine = container.inferenceEngine,
            modelFileManager = container.modelFileManager,
            idGenerator = container.idGenerator,
            application = application,
        ) as T
    }
}
```

**Manual verification checklist:**
- [x] `di/ChatViewModelFactory.kt` created
- [x] Extends `ViewModelProvider.Factory`
- [x] `@Suppress("UNCHECKED_CAST")` on the cast

---

### Task 4: Update `ChatViewModel` constructor

**File:** `app/src/main/java/com/smartphoneaichat/presentation/viewmodel/ChatViewModel.kt`

#### 4a: Change constructor signature

Replace lines 36-53:

```diff
- class ChatViewModel(application: Application) : AndroidViewModel(application) {
- 
-     private val _state = MutableStateFlow(ChatUiState())
-     val state: StateFlow<ChatUiState> = _state.asStateFlow()
- 
-     private var streamingJob: Job? = null
-     private var downloadJob: Job? = null
- 
-     val notifications = AppNotificationManager()
- 
-     private val modelFileManager: ModelFileManager = HuggingFaceModelFileManager(application)
-     private val inferenceEngine: InferenceEngine = LiteRtInferenceEngine(modelFileManager as HuggingFaceModelFileManager)
- 
-     private val idGenerator: IdGenerator = UuidIdGenerator()
- 
-     private val sendMessageUseCase = SendMessageUseCase(inferenceEngine, idGenerator)
-     private val downloadModelUseCase = DownloadModelUseCase(modelFileManager)
-     private val loadModelUseCase = LoadModelUseCase(modelFileManager)
+ class ChatViewModel(
+     private val sendMessageUseCase: SendMessageUseCase,
+     private val downloadModelUseCase: DownloadModelUseCase,
+     private val loadModelUseCase: LoadModelUseCase,
+     private val inferenceEngine: InferenceEngine,
+     private val modelFileManager: ModelFileManager,
+     private val idGenerator: IdGenerator,
+     application: Application,
+ ) : AndroidViewModel(application) {
+ 
+     private val _state = MutableStateFlow(ChatUiState())
+     val state: StateFlow<ChatUiState> = _state.asStateFlow()
+ 
+     private var streamingJob: Job? = null
+     private var downloadJob: Job? = null
+ 
+     val notifications = AppNotificationManager()
```

Note: Properties that stay inline (NOT injected):
- `_state` / `state` — ViewModel's own state, not a dependency
- `streamingJob` / `downloadJob` — coroutine lifecycle, ViewModel concern
- `notifications` — `AppNotificationManager` has no external dependencies, simple SharedFlow wrapper

#### 4b: Remove stale imports

Remove these 3 imports (lines 6-8) — they're no longer needed:
```kotlin
import com.smartphoneaichat.data.engine.LiteRtInferenceEngine
import com.smartphoneaichat.data.id.UuidIdGenerator
import com.smartphoneaichat.data.model.HuggingFaceModelFileManager
```

Verify that `IdGenerator` and `InferenceEngine` and `ModelFileManager` imports (lines 20-22) remain — they're still used for the constructor parameter types.

**Manual verification checklist:**
- [x] Constructor takes 7 dependencies + application
- [x] All dependencies are `private val` (encapsulated, read-only)
- [x] `application` parameter has no `val`/`private val` (only passed to super)
- [x] No `HuggingFaceModelFileManager`, `LiteRtInferenceEngine`, `UuidIdGenerator` imports
- [x] Body unchanged from line 38 onward (lines renumber but logic identical)
- [x] `_state`, `streamingJob`, `downloadJob`, `notifications` still initialized inline

---

### Task 5: Update `MainActivity` wiring

**File:** `app/src/main/java/com/smartphoneaichat/MainActivity.kt`

#### 5a: Add imports

```kotlin
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import com.smartphoneaichat.di.AppContainer
import com.smartphoneaichat.di.ChatViewModelFactory
```

#### 5b: Replace ViewModel creation

Replace line 55:
```diff
- val viewModel: ChatViewModel = viewModel()
+ val context = LocalContext.current
+ val appContainer = remember { AppContainer(context.applicationContext as Application) }
+ val factory = remember { ChatViewModelFactory(appContainer, context.applicationContext as Application) }
+ val viewModel: ChatViewModel = viewModel(factory = factory)
```

**Why this works:**
- `LocalContext.current.applicationContext as Application` — Compose's context cast to Application
- `remember { }` without key — `context.applicationContext` is application-scoped (stable reference), so the block runs once per composition lifetime
- `viewModel(factory = factory)` — passes the custom factory to `viewModel()`, which uses it instead of the default `AndroidViewModelFactory`

**Manual verification checklist:**
- [x] Three new imports added
- [x] `viewModel(factory = factory)` used instead of `viewModel()`
- [x] `AppContainer` created with application context
- [x] `remember { }` used for both container and factory (avoids re-creation on recomposition)

---

### Task 6: Verify build and tests

```bash
./gradlew assembleDebug
./gradlew test
```

**Expected:** BUILD SUCCESSFUL, all ~74 tests green.

**Troubleshooting:**

| Error | Fix |
|---|---|
| "Too many arguments for ChatViewModel" | Count matches — 7 dependencies + application = 8 params |
| "Unresolved reference: HuggingFaceModelFileManager" in ViewModel | You missed removing the import — it should only be in AppContainer |
| "Cannot create instance of ChatViewModel" | Factory might not be passing all params — check `ChatViewModelFactory.create()` |
| "Unresolved reference: LocalContext" | Add `import androidx.compose.ui.platform.LocalContext` |
| "viewModel() type mismatch" | The `viewModel(factory = ...)` overload returns the inferred type — should be `ChatViewModel` |
| "Unresolved reference: remember" | Add `import androidx.compose.runtime.remember` (already implicitly imported via compose BOM, but check) |
| Tests failing: SendMessageUseCaseTest | `SendMessageUseCase` now receives `ConversationTitleService` explicitly from AppContainer — defaults still work in tests |

**Quality Gate validation:**
- [ ] `./gradlew assembleDebug` passes
- [ ] `./gradlew test` — all tests pass
- [ ] Zero `data.*` imports in `presentation/viewmodel/ChatViewModel.kt`
- [ ] Zero `LiteRtInferenceEngine`, `HuggingFaceModelFileManager`, `UuidIdGenerator` references in `presentation/`
- [ ] ViewModel constructor takes all 7 dependencies + application (no prop init in body)
- [ ] `AppContainer` in `di/` package
- [ ] `ChatViewModelFactory` in `di/` package
- [ ] `MainActivity` uses factory for ViewModel creation
- [ ] No `val xxx = XxxImpl()` in ViewModel body (only `_state`, `streamingJob`, `downloadJob`, `notifications`)

---

### Task 7: Dependency direction audit

```bash
# presentation/ must NOT import data.*
grep -r "import com.smartphoneaichat.data" app/src/main/java/com/smartphoneaichat/presentation/ || echo "OK: presentation/ clean of data imports"

# di/ must NOT import compose.* or ui.* (per architecture rules)
grep -r "import androidx.compose" app/src/main/java/com/smartphoneaichat/di/ || echo "OK: di/ clean of compose"
grep -r "import com.smartphoneaichat.ui" app/src/main/java/com/smartphoneaichat/di/ || echo "OK: di/ clean of ui"
```

---

## Files to CREATE

| File | Purpose |
|------|---------|
| `di/AppContainer.kt` | Object graph — creates and wires all dependencies |
| `di/ChatViewModelFactory.kt` | ViewModelProvider.Factory — creates ChatViewModel with dependencies |

## Files to MODIFY

| File | Changes |
|------|---------|
| `presentation/viewmodel/ChatViewModel.kt` | New constructor (7 deps + application), remove inline instantiation, remove 3 data imports |
| `MainActivity.kt` | Add imports for AppContainer, ChatViewModelFactory, LocalContext; use factory with viewModel() |

## Architectural violations FIXED by this phase

| # | Violation | Status |
|---|-----------|--------|
| 6 | No DI — dependencies instantiated as ViewModel properties | FIXED — All dependencies injected via constructor. Zero `data.*` imports in `presentation/`. |

Violations fixed in prior phases: #2, #3, #4, #5, #7, #9, #10

## Architectural violations NOT YET fixed

| # | Violation | Deferred To |
|---|-----------|-------------|
| 1 | God ViewModel (~440 lines) | Residual; further reduced in Phase 5 with ConversationRepository |
| 8 | No ConversationRepository implementation | Phase 5 (Repository) |

---

## Dependency direction after Phase 4

```
MainActivity
  ├─→ AppContainer (di/)
  │     ├─→ UuidIdGenerator (data/)
  │     ├─→ HuggingFaceModelFileManager (data/)
  │     └─→ LiteRtInferenceEngine (data/)
  ├─→ ChatViewModelFactory (di/)
  │     └─→ ChatViewModel (presentation/)
  └─→ ChatScreen (ui/)

ChatViewModel (presentation/)
  ├─→ SendMessageUseCase (domain/)
  ├─→ DownloadModelUseCase (domain/)
  ├─→ LoadModelUseCase (domain/)
  ├─→ InferenceEngine (domain/ interface)
  ├─→ ModelFileManager (domain/ interface)
  └─→ IdGenerator (domain/ interface)

NO: presentation/ → data/
```

Wiring concern isolated to `di/` package. ViewModel depends only on domain abstractions.

---

## Estimated effort

~30-45 minutes. Purely mechanical:
1. Create 2 wiring files with boilerplate (AppContainer, Factory) — 10 min
2. Change ChatViewModel constructor + remove 3 imports — 10 min
3. Update MainActivity imports + wiring — 10 min
4. Verify build + tests — 10 min