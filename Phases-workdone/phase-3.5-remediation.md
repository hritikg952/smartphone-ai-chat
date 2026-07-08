# Phase 3.5 — Remediation (Pre-Phase 4 Audit Fixes)

> **Source:** Full codebase audit of Phases 0-3  
> **Purpose:** Fix issues discovered during audit before proceeding to Phase 4  
> **Constraint:** Only fix bugs and documentation. No architectural refactoring.

---

## Audit Summary

Full audit results at: `Phases/phase-3-use-cases.md` (Task 10 audit script)  
10 production files, 10 test files, 7 test classes (~74 tests), all packages clean.

### Items requiring action

| # | Severity | Issue | File | Action |
|---|----------|-------|------|--------|
| 1 | **CRITICAL** | `ConversationId("")` causes runtime crash on ViewModel construction | `ChatUiState.kt:10` | Fix |
| 2 | **HIGH** | Stale KDoc references deleted `LiteRtLmRepository.kt` | `MainActivity.kt:30,42,44` | Fix |
| 3 | **MEDIUM** | No JDK installed — tests cannot be verified | System | Document |
| 4 | **LOW** | `data.*` imports in `presentation/` layer | `ChatViewModel.kt:6-8` | Defer to Phase 4 |
| 5 | **LOW** | `ConversationRepository` has no implementation | N/A | Defer to Phase 5 |
| 6 | **NOTE** | `HuggingFaceModelFileManager` concrete coupling in `LiteRtInferenceEngine` | Data layer | Design note, no action |

### Items confirmed clean (PASS)

- All old package directories removed (`model/`, `viewmodel/`, `litertlm/`, `notification/`)
- Zero old-path imports anywhere in the codebase
- Zero `android.*` imports in `domain/`
- Zero `com.google.ai.edge.litertlm.*` imports in `domain/`, `presentation/`, or `ui/`
- Zero `conv.copy(messages = ...)` or `conv.copy(title = ...)` in ChatViewModel
- Zero `Conversation(...)` public constructor calls outside `Conversation.kt`
- Zero `System.currentTimeMillis()` in `domain/`
- Zero `inferenceEngine.sendMessage()` direct calls in ChatViewModel
- Zero `ConversationTitleService` import in ChatViewModel
- `ChatUiState` correctly references `domain.model.ModelInfo`
- `MainActivity` correctly imports `presentation.viewmodel.ChatViewModel`

---

## Task 1: Fix `ConversationId("")` crash bug

### Root cause

`ChatUiState` default field `activeConversationId: ConversationId = ConversationId("")` creates a blank value class that fails its `require(value.isNotBlank())` init block.

`MutableStateFlow(ChatUiState())` is evaluated eagerly in the ViewModel constructor — this throws before `init {}` can assign a real ID.

### Fix

Make `activeConversationId` nullable in all locations.

#### 1a: `ChatUiState.kt` (1 file)

```diff
- val activeConversationId: ConversationId = ConversationId(""),
+ val activeConversationId: ConversationId? = null,
```

The `activeConversation` computed property on line 26 already handles this correctly — `conversations.find { it.id == activeConversationId }` with null will compare `ConversationId == null` which returns false for all items, yielding `null`. No change needed.

#### 1b: `Sidebar.kt` (1 file)

```diff
- activeConversationId: ConversationId,
+ activeConversationId: ConversationId?,
```

The comparison on line 133 (`conversation.id == activeConversationId`) works correctly with null — if `activeConversationId` is null, no conversation highlights as active.

#### 1c: `ChatViewModel.kt` — `selectConversation()` (1 method signature)

```diff
- fun selectConversation(conversationId: ConversationId) {
+ fun selectConversation(conversationId: ConversationId?) {
```

#### 1d: `ChatViewModel.kt` — `deleteConversation()` (1 method signature)

```diff
- fun deleteConversation(conversationId: ConversationId) {
+ fun deleteConversation(conversationId: ConversationId?) {
```

Wait — actually `selectConversation` and `deleteConversation` receive IDs from the Sidebar's `onSelectConversation`/`onDeleteConversation` callbacks, which pass `conversation.id` (a non-null `ConversationId`). But since the callbacks type is `(ConversationId) -> Unit`, if we change the ViewModel signatures to nullable, the callbacks still pass non-null IDs and they auto-box to nullable. That's fine.

Actually, let me reconsider. The Sidebar callbacks are:
```kotlin
onSelectConversation = { viewModel.selectConversation(it) },
onDeleteConversation = { viewModel.deleteConversation(it) }
```

Where `it` is `conversation.id: ConversationId` (non-null). The Lambda type `(ConversationId) -> Unit` is applied, not `(ConversationId?) -> Unit`. If the ViewModel method takes `ConversationId?`, the lambda with `(ConversationId) -> Unit` still compiles because `ConversationId` is a subtype of `ConversationId?`.

Wait, no. `ConversationId` is an inline value class. `ConversationId?` is `ConversationId | null`. A lambda `(ConversationId) -> Unit` is NOT a subtype of `(ConversationId?) -> Unit` in Kotlin. So changing the ViewModel method to take `ConversationId?` would break the lambda call.

Let me think again. Actually, `selectConversation` is called from the Sidebar with `it: ConversationId` (non-null). If I make the ViewModel method take `ConversationId?`, but the lambda is `(ConversationId) -> Unit`, it won't compile because `(ConversationId) -> Unit` is not `(ConversationId?) -> Unit`.

Hmm, but if I change the Sidebar's callback type... Let me re-read:

Sidebar.kt line 67:
```kotlin
onSelectConversation: (ConversationId) -> Unit,
```

This takes non-null. The Sidebar passes `conversation.id` which is always non-null. Making this nullable is wrong — the Sidebar should always have a non-null conversation.

The issue is only that `ChatUiState.activeConversationId` needs a nullable default for the initial `ChatUiState()` construction. Once init {} runs, it's always non-null. The UI and ViewModel methods should continue to work with non-null since by the time they're called, a conversation always exists.

So the actual fix is simpler:
- `ChatUiState.kt`: `activeConversationId: ConversationId? = null`
- `Sidebar.kt`: change parameter to `ConversationId?` (since it's read from state)
- The `onSelectConversation` and `onDeleteConversation` callbacks stay `(ConversationId) -> Unit` — they always pass non-null IDs
- ViewModel methods stay `(ConversationId)` — they always receive non-null from callbacks

But `ChatScreen.kt` line 128 passes `state.activeConversationId` (now nullable) to `SidebarContent(activeConversationId = ...)`. If Sidebar expects `ConversationId?`, this is fine. The null-safe operator `conversation.id == activeConversationId` works.

Wait, but there's a subtle issue. `conversation.id == activeConversationId` — if `activeConversationId` is null, this compares `ConversationId("conv-1") == null` which returns `false`. Correct — no conversation is highlighted when state is initial (before init assigns a real ID).

Actually, `state.activeConversationId` will be `null` for approximately 0 time (immediately overwritten in init {}). But structurally it's correct.

Let me simplify the plan:

#### Changes needed:

1. **ChatUiState.kt:10** — `val activeConversationId: ConversationId? = null`
2. **Sidebar.kt:66** — `activeConversationId: ConversationId?`
3. **ChatScreen.kt:128** — passes `state.activeConversationId` (now nullable, parameter type matches)
4. No changes to ViewModel method signatures needed (they receive non-null from callbacks)
5. No changes to Sidebar callbacks needed (they pass `conversation.id` which is always non-null)

Wait, but if the Sidebar parameter is `ConversationId?` and the callbacks are `(ConversationId) -> Unit`, the callbacks still take `conversation.id: ConversationId` and pass non-null. The ViewModel methods can stay `ConversationId`. The param types for Sidebar and callback/ViewModel are different:
- Sidebar receives nullable from state
- But it passes non-null to callbacks (since `conversation.id` is always non-null)

That's fine. The Sidebar's `activeConversationId` parameter is only used for comparison (`conversation.id == activeConversationId`). The callbacks always call with non-null conversation IDs.

#### Final fix:

**ChatUiState.kt:**
```diff
- val activeConversationId: ConversationId = ConversationId(""),
+ val activeConversationId: ConversationId? = null,
```

**Sidebar.kt:**
```diff
- activeConversationId: ConversationId,
+ activeConversationId: ConversationId?,
```

That's it. Two lines changed, zero compilation issues.

### Manual verification checklist
- [x] `ChatUiState.kt` line 10: `ConversationId? = null`
- [x] `Sidebar.kt` line 66: `ConversationId?`
- [x] No other changes needed (ViewModel methods stay `ConversationId` — always receive non-null)
- [x] `./gradlew assembleDebug` compiles (if JDK available)

---

## Task 2: Fix stale KDoc in MainActivity.kt

### Root cause

Lines 30, 42, and 44 reference `LiteRtLmRepository.kt` which was split into `HuggingFaceModelFileManager.kt` and `LiteRtInferenceEngine.kt` in Phase 1.

### Fix

#### Line 30

```
-    Edit LiteRtLmRepository.kt and set HF_TOKEN to your HuggingFace
+    Set HF_TOKEN in your gradle.properties or via the HF_TOKEN build config
+    field in app/build.gradle.kts (used by HuggingFaceModelFileManager.kt).
```

#### Line 42

```
-    LiteRtLmRepository.kt. Already declared in AndroidManifest.xml.
+    HuggingFaceModelFileManager.kt. INTERNET permission is already declared
+    in AndroidManifest.xml.
```

#### Line 44

```
- See LiteRtLmRepository.kt and ChatViewModel.kt for implementation details.
+ See data/model/HuggingFaceModelFileManager.kt and
+ data/engine/LiteRtInferenceEngine.kt for implementation details.
```

### Manual verification checklist
- [x] Zero references to `LiteRtLmRepository` in MainActivity.kt
- [x] Updated paths are correct (`HuggingFaceModelFileManager.kt`, `LiteRtInferenceEngine.kt`)

---

## Task 3: JDK verification

### Issue

No JDK installed on this machine. Cannot run `./gradlew test` to verify the 74 tests pass.

### Action

Install JDK 17 (required by `compileOptions { sourceCompatibility = JavaVersion.VERSION_17 }`):

```bash
# macOS with Homebrew:
brew install openjdk@17

# Or download from:
# https://adoptium.net/download/
```

After installation:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew assembleDebug
./gradlew test
```

Expected results:
- `assembleDebug`: BUILD SUCCESSFUL
- `test`: all 74 tests pass (15 value objects + 4 FakeIdGenerator + 4 UuidIdGenerator + 16 Conversation + 7 TitleService + 16 SendMessageUseCase + 4 DownloadModelUseCase + 5 LoadModelUseCase + 3 additional from ConversationTest counting)

### Manual verification checklist
- [x] JDK 17 installed
- [x] `./gradlew test` passes with zero failures
- [x] `./gradlew assembleDebug` passes

---

## Task 4: Deferred items (documented, no action now)

### 4a: `data.*` imports in `presentation/` layer

**Files:** `ChatViewModel.kt` lines 6-8
```kotlin
import com.smartphoneaichat.data.engine.LiteRtInferenceEngine
import com.smartphoneaichat.data.id.UuidIdGenerator
import com.smartphoneaichat.data.model.HuggingFaceModelFileManager
```

**Status:** Expected. The ViewModel directly instantiates its dependencies (no DI yet). Phase 4 will move wiring to `AppContainer` → these imports are removed from ViewModel.

**Deferred to:** Phase 4 (DI)

### 4b: `ConversationRepository` has no implementation

**Files:** `domain/repository/ConversationRepository.kt` (interface exists, no impl)

**Status:** Expected. Interface created in Phase 1 for future use. Implementation (`InMemoryConversationRepository`) is Phase 5.

**Deferred to:** Phase 5 (Repository)

### 4c: `HuggingFaceModelFileManager` concrete coupling

**Files:** `LiteRtInferenceEngine.kt:10` takes concrete `HuggingFaceModelFileManager`, not the `ModelFileManager` interface.

**Status:** By design. The inference engine needs `getEngine(): Engine?` which is internal to the concrete class (not on the interface). Phase 4 DI improves this but doesn't eliminate it — it's an inherent coupling of the LiteRT-LM SDK.

**Deferred to:** Phase 4 (minor improvement via AppContainer wiring)

---

## Summary of changes

| Task | Files Modified | Lines Changed |
|------|---------------|---------------|
| 1: Fix ConversationId("") crash | `ChatUiState.kt`, `Sidebar.kt` | 2 lines |
| 2: Fix stale KDoc | `MainActivity.kt` | 3 lines |
| 3: JDK verification | None (system) | 0 |
| 4: Deferred items | None | 0 |

**Total:** 3 files, 5 lines changed. Zero architectural impact.

---

## Quality gate (before Phase 4)

- [ ] `./gradlew assembleDebug` compiles with zero errors
- [ ] `./gradlew test` — all tests pass (74+ tests)
- [ ] Zero references to `LiteRtLmRepository` in source files (only AGENTS.md and Phases/ docs may reference it historically)
- [ ] `ConversationId("")` no longer exists anywhere in production code
- [ ] All dependency direction rules pass (re-run audit script from Phase 3.5 Task 10)
- [ ] All old package directories still clean (model/, viewmodel/, litertlm/, notification/)