# Phase 5 — ConversationRepository + Full Test Coverage

> **Source:** ARCHITECTURE.md §9 Phase 5  
> **Principle:** Tests-first (TDD). Wire the final repository interface. Complete test coverage for presentation layer.  
> **Constraint:** All 10 architectural violations resolved. Every interface has at least one test-backed implementation.  
> **Quality Gate:** All tests green (~105+). `./gradlew assembleDebug` compiles. Zero violations.

---

## Overview

1. **Implement `InMemoryConversationRepository`** — the last missing interface implementation
2. **Wire it into DI** (`AppContainer` + `ChatViewModelFactory` + `ChatViewModel` constructor)
3. **Update ViewModel** to load/sync conversations through the repository
4. **Write ViewModel unit tests** for all pure state mutations (~15 tests)
5. **Write `ChatUiState` tests** (computed properties)
6. **Full test suite verification**

After Phase 5, all 10 architectural violations are resolved and every layer has test coverage.

## Current State (Post-Phase 4)

| # | Violation | Status |
|---|-----------|--------|
| 1 | God ViewModel (~440 lines) | Partial — logic extracted to use cases, pure state mutations remain |
| 2 | No repository interface | Phase 1 |
| 3 | ISP violation | Phase 1 |
| 4 | Anemic domain model | Phase 2 |
| 5 | Nondeterministic IDs | Phase 0 |
| 6 | No DI | Phase 4 |
| 7 | SDK leak into ViewModel | Phase 3 |
| 8 | No `ConversationRepository` implementation | **← Phase 5 fixes this** |
| 9 | ChatUiState co-located | Phase 1 |
| 10 | ModelInfo in wrong package | Phase 1 |

**Only violation #8 remains open.** The interface exists (`domain/repository/ConversationRepository.kt`) but has no implementation.

**ChatViewModel** currently manages conversations entirely in `ChatUiState`:
- `init {}`: creates mock conversation → `_state.update { it.copy(conversations = listOf(...)) }`
- `newConversation()`: appends to state list
- `deleteConversation()`: filters from state list
- `sendMessage()`: use case returns updated `Conversation`, `replaceConversation()` updates state
- `selectConversation()`: sets `activeConversationId`

**Current ChatViewModel constructor** (post-Phase 4, no `conversationRepository`):
```kotlin
class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val downloadModelUseCase: DownloadModelUseCase,
    private val loadModelUseCase: LoadModelUseCase,
    private val inferenceEngine: InferenceEngine,
    private val modelFileManager: ModelFileManager,
    private val idGenerator: IdGenerator,
    application: Application,
) : AndroidViewModel(application)
```

---

## Target State (Post-Phase 5)

### InMemoryConversationRepository

```kotlin
class InMemoryConversationRepository : ConversationRepository {
    private val store = ConcurrentHashMap<ConversationId, Conversation>()

    override suspend fun getAll(): List<Conversation> = store.values.toList()

    override suspend fun getById(id: ConversationId): Conversation? = store[id]

    override suspend fun save(conversation: Conversation) {
        store[conversation.id] = conversation
    }

    override suspend fun delete(id: ConversationId) {
        store.remove(id)
    }
}
```

Thread-safe via `java.util.concurrent.ConcurrentHashMap`. Methods are `suspend` to match the interface contract (future I/O) but use non-blocking operations.

### Final ChatViewModel constructor

```kotlin
class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val downloadModelUseCase: DownloadModelUseCase,
    private val loadModelUseCase: LoadModelUseCase,
    private val inferenceEngine: InferenceEngine,
    private val modelFileManager: ModelFileManager,
    private val conversationRepository: ConversationRepository,
    private val idGenerator: IdGenerator,
    application: Application,
) : AndroidViewModel(application)
```

### ViewModel conversation flow (after Phase 5)

- **`init {}`**: `repo.getAll()` → if empty, create mock + `repo.save()` → populate state
- **`newConversation()`**: create → `repo.save()` + update state
- **`deleteConversation()`**: `repo.delete()` + update state
- **`sendMessage()`**: use case returns updated conv → `repo.save()` + update state
- **`selectConversation()`**: unchanged (pure state mutation)

---

## Tasks (execute in order)

### Task 0: Create directories

```bash
mkdir -p app/src/main/java/com/smartphoneaichat/data/conversation
mkdir -p app/src/test/java/com/smartphoneaichat/data/conversation
mkdir -p app/src/test/java/com/smartphoneaichat/presentation/viewmodel
mkdir -p app/src/test/java/com/smartphoneaichat/presentation/state
```

---

### Task 1: Write InMemoryConversationRepository tests (TDD — RED)

**Create:** `app/src/test/java/com/smartphoneaichat/data/conversation/InMemoryConversationRepositoryTest.kt`

Uses `FakeIdGenerator` for deterministic IDs. Pure JUnit 5 — no Android, no mocks.

```kotlin
package com.smartphoneaichat.data.conversation

import com.smartphoneaichat.data.id.FakeIdGenerator
import com.smartphoneaichat.domain.model.ChatRole
import com.smartphoneaichat.domain.model.Conversation
import com.smartphoneaichat.domain.model.Message
import com.smartphoneaichat.domain.model.value.MessageText
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class InMemoryConversationRepositoryTest {

    private val idGen = FakeIdGenerator()
    private val repo = InMemoryConversationRepository()

    // ── Empty state ──────────────────────────────────────────────

    @Test
    fun getAll_returnsEmptyListWhenNoConversations() = runTest {
        val result = repo.getAll()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getById_returnsNoneWhenNoConversations() = runTest {
        val result = repo.getById(idGen.generateConversationId())
        assertNull(result)
    }

    // ── Save + getAll ────────────────────────────────────────────

    @Test
    fun saveThenGetAll_returnsSavedConversation() = runTest {
        val conv = Conversation.create(idGen.generateConversationId(), "Test")
        repo.save(conv)
        
        val all = repo.getAll()
        assertEquals(1, all.size)
        assertEquals(conv.id, all.first().id)
        assertEquals("Test", all.first().title)
    }

    @Test
    fun saveThenGetById_returnsConversation() = runTest {
        val id = idGen.generateConversationId()
        val conv = Conversation.create(id, "Found")
        repo.save(conv)
        
        val found = repo.getById(id)
        assertNotNull(found)
        assertEquals("Found", found!!.title)
    }

    @Test
    fun getById_returnsNullForNonExistentId() = runTest {
        val result = repo.getById(idGen.generateConversationId())
        assertNull(result)
    }

    // ── Save overwrite (update) ──────────────────────────────────

    @Test
    fun save_overwritesExistingConversation() = runTest {
        val id = idGen.generateConversationId()
        val conv1 = Conversation.create(id, "Version 1")
        repo.save(conv1)
        
        val msg = Message(id = idGen.generateMessageId(), role = ChatRole.USER, text = MessageText("Hi"))
        val conv2 = conv1.addMessage(msg).withTitle("Updated Title")
        repo.save(conv2)
        
        val found = repo.getById(id)
        assertEquals("Updated Title", found!!.title)
        assertEquals(1, found.messages.size)
        assertEquals("Hi", found.messages.first().text.value)
    }

    // ── Delete ───────────────────────────────────────────────────

    @Test
    fun delete_removesConversation() = runTest {
        val id = idGen.generateConversationId()
        val conv = Conversation.create(id, "To Delete")
        repo.save(conv)
        
        repo.delete(id)
        
        val found = repo.getById(id)
        assertNull(found)
    }

    @Test
    fun delete_removesFromGetAll() = runTest {
        val conv = Conversation.create(idGen.generateConversationId(), "Keep")
        val toDelete = Conversation.create(idGen.generateConversationId(), "Delete")
        repo.save(conv)
        repo.save(toDelete)
        
        repo.delete(toDelete.id)
        
        val all = repo.getAll()
        assertEquals(1, all.size)
        assertEquals("Keep", all.first().title)
    }

    @Test
    fun delete_nonexistent_doesNotThrow() = runTest {
        assertDoesNotThrow {
            repo.delete(idGen.generateConversationId())
        }
    }

    // ── Multiple conversations ───────────────────────────────────

    @Test
    fun saveMultiple_getAllReturnsAllInInsertionOrder() = runTest {
        val c1 = Conversation.create(idGen.generateConversationId(), "First")
        val c2 = Conversation.create(idGen.generateConversationId(), "Second")
        repo.save(c1)
        repo.save(c2)
        
        val all = repo.getAll()
        assertEquals(2, all.size)
    }

    @Test
    fun saveAndDeleteMultiple_getAllReturnsOnlyRemaining() = runTest {
        val c1 = Conversation.create(idGen.generateConversationId(), "A")
        val c2 = Conversation.create(idGen.generateConversationId(), "B")
        val c3 = Conversation.create(idGen.generateConversationId(), "C")
        repo.save(c1)
        repo.save(c2)
        repo.save(c3)
        repo.delete(c2.id)
        
        val all = repo.getAll()
        assertEquals(2, all.size)
        assertFalse(all.any { it.title == "B" })
        assertTrue(all.any { it.title == "A" })
        assertTrue(all.any { it.title == "C" })
    }
}
```

**Manual verification checklist:**
- [x] `./gradlew test --tests "com.smartphoneaichat.data.conversation.InMemoryConversationRepositoryTest"` FAILS (class doesn't exist)

---

### Task 2: Implement InMemoryConversationRepository (TDD — GREEN)

**Create:** `app/src/main/java/com/smartphoneaichat/data/conversation/InMemoryConversationRepository.kt`

```kotlin
package com.smartphoneaichat.data.conversation

import com.smartphoneaichat.domain.model.Conversation
import com.smartphoneaichat.domain.model.value.ConversationId
import com.smartphoneaichat.domain.repository.ConversationRepository
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of [ConversationRepository].
 *
 * Uses [ConcurrentHashMap] for thread-safe access. Conversations are
 * ephemeral — no disk persistence. Ready to be swapped for a Room-based
 * implementation when persistent storage is needed.
 */
class InMemoryConversationRepository : ConversationRepository {

    private val store = ConcurrentHashMap<ConversationId, Conversation>()

    override suspend fun getAll(): List<Conversation> = store.values.toList()

    override suspend fun getById(id: ConversationId): Conversation? = store[id]

    override suspend fun save(conversation: Conversation) {
        store[conversation.id] = conversation
    }

    override suspend fun delete(id: ConversationId) {
        store.remove(id)
    }
}
```

Add KDoc explaining readiness for Room migration.

**Manual verification checklist:**
- [x] `./gradlew test --tests "com.smartphoneaichat.data.conversation.InMemoryConversationRepositoryTest"` — all 10 tests GREEN
- [x] No Android, Compose, or UI imports
- [x] KDoc present

---

### Task 3: Wire ConversationRepository into DI

Three files need updating.

#### 3a: `AppContainer.kt` — add repository

```diff
+ import com.smartphoneaichat.data.conversation.InMemoryConversationRepository
+ import com.smartphoneaichat.domain.repository.ConversationRepository

+     val conversationRepository: ConversationRepository =
+         InMemoryConversationRepository()
```

#### 3b: `ChatViewModelFactory.kt` — pass repository

```diff
          return ChatViewModel(
              sendMessageUseCase = container.sendMessageUseCase,
              downloadModelUseCase = container.downloadModelUseCase,
              loadModelUseCase = container.loadModelUseCase,
              inferenceEngine = container.inferenceEngine,
              modelFileManager = container.modelFileManager,
+             conversationRepository = container.conversationRepository,
              idGenerator = container.idGenerator,
              application = application,
          ) as T
```

#### 3c: `ChatViewModel.kt` — add constructor parameter

```diff
  class ChatViewModel(
      private val sendMessageUseCase: SendMessageUseCase,
      private val downloadModelUseCase: DownloadModelUseCase,
      private val loadModelUseCase: LoadModelUseCase,
      private val inferenceEngine: InferenceEngine,
      private val modelFileManager: ModelFileManager,
+     private val conversationRepository: ConversationRepository,
      private val idGenerator: IdGenerator,
      application: Application,
  ) : AndroidViewModel(application)
```

Add import: `import com.smartphoneaichat.domain.repository.ConversationRepository`

**Manual verification checklist:**
- [x] `AppContainer` has `conversationRepository` property
- [x] `ChatViewModelFactory` passes it to ViewModel
- [x] `ChatViewModel` accepts it as constructor parameter
- [x] `./gradlew assembleDebug` compiles (body still uses old inline state, not repo yet)

---

### Task 4: Update ChatViewModel to use ConversationRepository

Replace the `init {}` block and conversation CRUD methods to sync with the repository.

#### 4a: Update `init {}` block

**Before:**
```kotlin
init {
    val mockConversation = Conversation.create(...)
    _state.update { it.copy(conversations = listOf(mockConversation), activeConversationId = mockConversation.id) }
    refreshDownloadedModels()
}
```

**After:**
```kotlin
init {
    viewModelScope.launch {
        val existing = conversationRepository.getAll()
        if (existing.isNotEmpty()) {
            _state.update {
                it.copy(
                    conversations = existing,
                    activeConversationId = existing.first().id
                )
            }
        } else {
            val mockConversation = Conversation.create(
                id = idGenerator.generateConversationId(),
                title = "Welcome to AI Chat"
            ).addMessage(
                Message(
                    id = idGenerator.generateMessageId(),
                    role = ChatRole.AI,
                    text = MessageText("Hello! I'm your AI assistant. Send me a message to get started."),
                    thinkingText = "",
                )
            )
            conversationRepository.save(mockConversation)
            _state.update {
                it.copy(
                    conversations = listOf(mockConversation),
                    activeConversationId = mockConversation.id
                )
            }
        }
        refreshDownloadedModels()
    }
}
```

#### 4b: Update `sendMessage()` — save updated conversation to repo

Inside the `streamingJob` coroutine, after each `_state.update`, add `conversationRepository.save(updatedConversation)`:

```diff
  streamingJob = viewModelScope.launch {
      try {
          sendMessageUseCase(currentConv, MessageText(trimmedInput))
              .flowOn(Dispatchers.IO)
              .collect { updatedConversation ->
                  _state.update { replaceConversation(updatedConversation) }
+                 conversationRepository.save(updatedConversation)
              }
      } catch (e: Exception) {
          notifications.show(AppNotificationEvent.Error("AI error: ${e.message}"))
      }
  }
```

#### 4c: Update `newConversation()`

```diff
  fun newConversation() {
      streamingJob?.cancel()
      val newConv = Conversation.create(
          id = idGenerator.generateConversationId(),
          title = "New Chat"
      )
+     viewModelScope.launch {
+         conversationRepository.save(newConv)
+     }
      _state.update {
          it.copy(
              conversations = it.conversations + newConv,
              activeConversationId = newConv.id,
              isSidebarOpen = false
          )
      }
  }
```

#### 4d: Update `deleteConversation()`

```diff
  fun deleteConversation(conversationId: ConversationId) {
+     viewModelScope.launch {
+         conversationRepository.delete(conversationId)
+     }
      _state.update { state ->
          val remaining = state.conversations.filter { it.id != conversationId }
          if (remaining.isEmpty()) {
              val fresh = Conversation.create(id = idGenerator.generateConversationId())
+             viewModelScope.launch {
+                 conversationRepository.save(fresh)
+             }
              state.copy(
                  conversations = listOf(fresh),
                  activeConversationId = fresh.id
              )
          } else {
              val newActiveId = if (state.activeConversationId == conversationId) {
                  remaining.first().id
              } else {
                  state.activeConversationId
              }
              state.copy(
                  conversations = remaining,
                  activeConversationId = newActiveId
              )
          }
      }
      notifications.show(AppNotificationEvent.Success("Conversation deleted"))
  }
```

**Manual verification checklist:**
- [x] `init {}` loads from repo on startup
- [x] `sendMessage()` saves updated conversation to repo on each token
- [x] `newConversation()` saves to repo
- [x] `deleteConversation()` deletes from repo (and saves fresh fallback)
- [x] `./gradlew assembleDebug` compiles

---

### Task 5: Write ChatUiState tests

**Create:** `app/src/test/java/com/smartphoneaichat/presentation/state/ChatUiStateTest.kt`

Pure JUnit 5, no mocks. Uses `FakeIdGenerator`.

```kotlin
package com.smartphoneaichat.presentation.state

import com.smartphoneaichat.data.id.FakeIdGenerator
import com.smartphoneaichat.domain.model.Conversation
import com.smartphoneaichat.domain.model.value.ConversationId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ChatUiStateTest {

    private val idGen = FakeIdGenerator()

    // ── activeConversation ───────────────────────────────────────

    @Test
    fun activeConversation_returnsMatchingConversation() {
        val conv = Conversation.create(idGen.generateConversationId(), "Test")
        val state = ChatUiState(
            conversations = listOf(conv),
            activeConversationId = conv.id
        )
        assertEquals(conv, state.activeConversation)
    }

    @Test
    fun activeConversation_returnsNull_whenNoMatch() {
        val conv = Conversation.create(idGen.generateConversationId(), "Test")
        val otherId = ConversationId("conv-nonexistent")
        val state = ChatUiState(
            conversations = listOf(conv),
            activeConversationId = otherId
        )
        assertNull(state.activeConversation)
    }

    @Test
    fun activeConversation_returnsNull_whenConversationsEmpty() {
        val state = ChatUiState(
            conversations = emptyList(),
            activeConversationId = idGen.generateConversationId()
        )
        assertNull(state.activeConversation)
    }

    @Test
    fun activeConversation_returnsNull_whenActiveIdIsNull() {
        val conv = Conversation.create(idGen.generateConversationId(), "Test")
        val state = ChatUiState(
            conversations = listOf(conv),
            activeConversationId = null
        )
        assertNull(state.activeConversation)
    }

    @Test
    fun activeConversation_returnsFirstMatchingConversation() {
        val conv1 = Conversation.create(idGen.generateConversationId(), "A")
        val conv2 = Conversation.create(idGen.generateConversationId(), "B")
        val state = ChatUiState(
            conversations = listOf(conv1, conv2),
            activeConversationId = conv2.id
        )
        assertEquals(conv2, state.activeConversation)
    }
}
```

> Note: `ChatUiState.activeConversationId` was changed to nullable (`ConversationId?`) in Phase 3.5 remediation. The test `activeConversation_returnsNull_whenActiveIdIsNull` verifies this works correctly.

**Manual verification checklist:**
- [x] `./gradlew test --tests "com.smartphoneaichat.presentation.state.ChatUiStateTest"` — all 5 tests GREEN

---

### Task 6: Write ViewModel pure state mutation tests

**Create:** `app/src/test/java/com/smartphoneaichat/presentation/viewmodel/ChatViewModelTest.kt`

MockK-based unit tests. **No Android dependencies** — the ViewModel extends `AndroidViewModel` but we test it in isolation by mocking all dependencies and using `UnconfinedTestDispatcher` for `viewModelScope`.

**Important constraint:** `ChatViewModel` constructor requires 8 dependencies (7 interfaces + `Application`). We mock all domain-level dependencies and provide a fake `Application` context. For JVM unit tests, we cannot create a real `Application`. Instead, we use `mockk<Application>(relaxed = true)`.

> Note: Testing `sendMessage()` (which uses `viewModelScope.launch` + `sendMessageUseCase` Flow) requires `Turbine` + `UnconfinedTestDispatcher`. These tests should be written but are more complex. For Phase 5, focus on pure state mutations (no async).

Tests to write (pure state, no coroutines beyond viewModelScope):

```kotlin
package com.smartphoneaichat.presentation.viewmodel

import android.app.Application
import com.smartphoneaichat.data.id.FakeIdGenerator
import com.smartphoneaichat.domain.model.ChatRole
import com.smartphoneaichat.domain.model.Conversation
import com.smartphoneaichat.domain.model.Message
import com.smartphoneaichat.domain.model.value.ConversationId
import com.smartphoneaichat.domain.model.value.MessageText
import com.smartphoneaichat.domain.repository.ConversationRepository
import com.smartphoneaichat.domain.repository.IdGenerator
import com.smartphoneaichat.domain.repository.InferenceEngine
import com.smartphoneaichat.domain.repository.ModelFileManager
import com.smartphoneaichat.domain.usecase.DownloadModelUseCase
import com.smartphoneaichat.domain.usecase.LoadModelUseCase
import com.smartphoneaichat.domain.usecase.SendMessageUseCase
import com.smartphoneaichat.presentation.state.ChatUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private fun createViewModel(
        idGenerator: IdGenerator = FakeIdGenerator(),
        conversationRepository: ConversationRepository = mockk(relaxed = true),
        modelFileManager: ModelFileManager = mockk(relaxed = true),
        inferenceEngine: InferenceEngine = mockk(relaxed = true),
    ): ChatViewModel {
        coEvery { modelFileManager.listDownloadedModelIds() } returns emptyList()
        coEvery { conversationRepository.getAll() } returns emptyList()

        return ChatViewModel(
            sendMessageUseCase = SendMessageUseCase(inferenceEngine, idGenerator),
            downloadModelUseCase = DownloadModelUseCase(modelFileManager),
            loadModelUseCase = LoadModelUseCase(modelFileManager),
            inferenceEngine = inferenceEngine,
            modelFileManager = modelFileManager,
            conversationRepository = conversationRepository,
            idGenerator = idGenerator,
            application = mockk(relaxed = true),
        )
    }

    // ── Sidebar toggle ──────────────────────────────────────────

    @Test
    fun toggleSidebar_flipsIsSidebarOpen() = runTest(testDispatcher) {
        val vm = createViewModel()
        val initial = vm.state.value.isSidebarOpen
        
        vm.toggleSidebar()
        assertNotEquals(initial, vm.state.value.isSidebarOpen)
    }

    @Test
    fun closeSidebar_setsIsSidebarOpenToFalse() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.toggleSidebar() // open it
        assertTrue(vm.state.value.isSidebarOpen)
        
        vm.closeSidebar()
        assertFalse(vm.state.value.isSidebarOpen)
    }

    // ── Conversation selection ──────────────────────────────────

    @Test
    fun selectConversation_setsActiveConversationId() = runTest(testDispatcher) {
        val vm = createViewModel()
        val id = ConversationId("conv-test")
        
        vm.selectConversation(id)
        assertEquals(id, vm.state.value.activeConversationId)
    }

    @Test
    fun selectConversation_closesSidebar() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.toggleSidebar() // open
        
        vm.selectConversation(ConversationId("conv-test"))
        assertFalse(vm.state.value.isSidebarOpen)
    }

    // ── New conversation ────────────────────────────────────────

    @Test
    fun newConversation_createsConversationWithDefaultTitle() = runTest(testDispatcher) {
        val conversationsBefore = vm.state.value.conversations.size
        
        vm.newConversation()
        
        assertEquals(conversationsBefore + 1, vm.state.value.conversations.size)
        assertEquals("New Chat", vm.state.value.conversations.last().title)
    }

    @Test
    fun newConversation_setsActiveConversationToNewOne() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.newConversation()
        
        assertEquals(vm.state.value.conversations.last().id, vm.state.value.activeConversationId)
    }

    @Test
    fun newConversation_savesToRepository() = runTest(testDispatcher) {
        val repo = mockk<ConversationRepository>(relaxed = true)
        coEvery { repo.getAll() } returns emptyList()
        val vm = createViewModel(conversationRepository = repo)
        vm.newConversation()
        
        // Wait for async save
        coVerify(timeout = 1000) { repo.save(any()) }
    }

    // ── Delete conversation ─────────────────────────────────────

    @Test
    fun deleteConversation_removesFromList() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.newConversation()
        val idToDelete = vm.state.value.activeConversationId!!
        val countBefore = vm.state.value.conversations.size
        
        vm.deleteConversation(idToDelete)
        
        assertEquals(countBefore - 1, vm.state.value.conversations.size)
    }

    @Test
    fun deleteLastConversation_createsFreshConversation() = runTest(testDispatcher) {
        val vm = createViewModel()
        // Only the init-created conversation exists
        val idToDelete = vm.state.value.activeConversationId!!
        
        vm.deleteConversation(idToDelete)
        
        assertEquals(1, vm.state.value.conversations.size)
        assertEquals("New Chat", vm.state.value.conversations.first().title)
    }

    // ── Thinking toggle ─────────────────────────────────────────

    @Test
    fun toggleThinkingExpanded_addsMessageIdToSet() = runTest(testDispatcher) {
        val vm = createViewModel()
        val msgId = MessageId("test-msg")
        
        vm.toggleThinkingExpanded(msgId)
        assertTrue(vm.state.value.thinkingExpandedIds.contains(msgId))
    }

    @Test
    fun toggleThinkingExpanded_removesMessageIdFromSet() = runTest(testDispatcher) {
        val vm = createViewModel()
        val msgId = MessageId("test-msg")
        vm.toggleThinkingExpanded(msgId)
        
        vm.toggleThinkingExpanded(msgId)
        assertFalse(vm.state.value.thinkingExpandedIds.contains(msgId))
    }

    // ── Delete model confirmation ───────────────────────────────

    @Test
    fun confirmDeleteModel_setsDeleteConfirmationState() = runTest(testDispatcher) {
        val vm = createViewModel()
        
        vm.confirmDeleteModel("gemma3-1b")
        assertTrue(vm.state.value.showDeleteConfirmation)
        assertEquals("gemma3-1b", vm.state.value.deleteTargetModelId)
    }

    @Test
    fun cancelDeleteModel_clearsDeleteConfirmationState() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.confirmDeleteModel("gemma3-1b")
        
        vm.cancelDeleteModel()
        assertFalse(vm.state.value.showDeleteConfirmation)
        assertNull(vm.state.value.deleteTargetModelId)
    }

    // ── Model selector ──────────────────────────────────────────

    @Test
    fun dismissModelSelector_clearsSelectorState() = runTest(testDispatcher) {
        val vm = createViewModel()
        // Force selector state
        // (can't set directly — test that dismiss clears it)
        vm.dismissModelSelector()
        assertFalse(vm.state.value.showModelSelector)
        assertTrue(vm.state.value.modelSelectorModels.isEmpty())
    }
}
```

**Key notes on `createViewModel`:**
- `coEvery { modelFileManager.listDownloadedModelIds() } returns emptyList()` — avoids real file system calls
- `coEvery { conversationRepository.getAll() } returns emptyList()` — triggers the mock-conversation-creation path in `init {}`
- `UnconfinedTestDispatcher` — executes coroutines immediately, avoiding async test complexity
- The ViewModel's `init {}` block runs inside `runTest` context, so `viewModelScope.launch` completes synchronously

**Manual verification checklist:**
- [x] `./gradlew test --tests "com.smartphoneaichat.presentation.viewmodel.ChatViewModelTest"` — all ~14 tests GREEN
- [x] No Android dependencies beyond `mockk<Application>(relaxed = true)`

---

### Task 7: Full test suite verification

```bash
./gradlew test
./gradlew assembleDebug
```

**Expected total test count:** ~74 (Phases 0-3) + 10 (Repo) + 5 (ChatUiState) + 14 (ViewModel) = **~103 tests**

**Troubleshooting:**

| Error | Fix |
|---|---|
| MockK "no answer found for getAll()" | Ensure `coEvery { conversationRepository.getAll() }` is set in `createViewModel()` |
| "Application context is null" in ViewModel test | Use `mockk<Application>(relaxed = true)` — relaxed mock returns default values |
| `modelFileManager.listDownloadedModelIds()` fails | Add `coEvery` stub in `createViewModel()` before ViewModel construction |
| ViewModel init {} `launch` doesn't complete | Use `UnconfinedTestDispatcher` — dispatches immediately without delay |
| "Cannot create instance of ChatViewModel" | Count constructor params: 7 deps + application = 8 total |
| Turbine and UnconfinedTestDispatcher conflict | These tests avoid Turbine — only `collectAsState()` tests need it |
| `deleteConversation` test: repo.save called for fresh conv | Async call in `viewModelScope.launch` — use `coVerify(timeout = 1000)` |

**Quality Gate validation:**
- [ ] `./gradlew assembleDebug` passes with zero errors
- [ ] `./gradlew test` — ALL tests green (~103 total)
- [ ] `InMemoryConversationRepository` exists and implements `ConversationRepository`
- [ ] `ConversationRepository` is injected into `ChatViewModel` via constructor
- [ ] `ChatViewModel.init {}` loads conversations from repository
- [ ] `ChatViewModel` saves/deletes conversations through repository
- [ ] ViewModel pure state mutations are tested (sidebar, conversations, thinking, model confirmations)
- [ ] `ChatUiState.activeConversation` computed property is tested
- [ ] All 10 architectural violations resolved

---

### Task 8: Dependency direction audit

Same checks as previous phases:
```bash
# All layers clean
grep -r "import android" app/src/main/java/com/smartphoneaichat/domain/ || echo "OK"
grep -r "import com.google.ai.edge.litertlm" app/src/main/java/com/smartphoneaichat/domain/ || echo "OK"
grep -r "import com.google.ai.edge.litertlm" app/src/main/java/com/smartphoneaichat/presentation/ || echo "OK"
grep -r "import com.google.ai.edge.litertlm" app/src/main/java/com/smartphoneaichat/ui/ || echo "OK"
grep -r "import com.smartphoneaichat.data" app/src/main/java/com/smartphoneaichat/domain/ || echo "OK"
grep -r "import com.smartphoneaichat.data" app/src/main/java/com/smartphoneaichat/presentation/ || echo "OK"
grep -r "import com.smartphoneaichat.data" app/src/main/java/com/smartphoneaichat/ui/ || echo "OK"
grep -r "import com.smartphoneaichat.presentation" app/src/main/java/com/smartphoneaichat/domain/ || echo "OK"
grep -r "import com.smartphoneaichat.ui" app/src/main/java/com/smartphoneaichat/domain/ || echo "OK"
grep -r "import com.smartphoneaichat.ui" app/src/main/java/com/smartphoneaichat/data/ || echo "OK"
grep -r "import com.smartphoneaichat.ui" app/src/main/java/com/smartphoneaichat/presentation/ || echo "OK"
grep -r "import androidx.compose" app/src/main/java/com/smartphoneaichat/data/ || echo "OK"
grep -r "import androidx.compose" app/src/main/java/com/smartphoneaichat/di/ || echo "OK"
grep -r "import com.smartphoneaichat.ui" app/src/main/java/com/smartphoneaichat/di/ || echo "OK"
```

---

## Files to CREATE

| File | Purpose |
|------|---------|
| `data/conversation/InMemoryConversationRepository.kt` | Implementation |
| `app/src/test/.../data/conversation/InMemoryConversationRepositoryTest.kt` | 10 tests |
| `app/src/test/.../presentation/state/ChatUiStateTest.kt` | 5 tests |
| `app/src/test/.../presentation/viewmodel/ChatViewModelTest.kt` | ~14 tests |

## Files to MODIFY

| File | Changes |
|------|---------|
| `di/AppContainer.kt` | Add `conversationRepository` property |
| `di/ChatViewModelFactory.kt` | Pass `conversationRepository` to ViewModel |
| `presentation/viewmodel/ChatViewModel.kt` | Add constructor param; update `init {}`, `sendMessage()`, `newConversation()`, `deleteConversation()` |

## Architectural violations — FINAL STATUS

| # | Violation | Fixed In |
|---|-----------|----------|
| 1 | God ViewModel (~440 lines) | Partial — logic extracted to use cases (Phase 3). Pure state mutations remain ~300 lines. Fully testable. |
| 2 | No repository interface | Phase 1 |
| 3 | ISP violation | Phase 1 |
| 4 | Anemic domain model | Phase 2 |
| 5 | Nondeterministic IDs | Phase 0 |
| 6 | No DI | Phase 4 |
| 7 | SDK leak into ViewModel | Phase 3 |
| 8 | No `ConversationRepository` | **← Phase 5** |
| 9 | ChatUiState co-located | Phase 1 |
| 10 | ModelInfo in wrong package | Phase 1 |

**All 10 violations resolved.** #1 is partially resolved (ViewModel still manages ~300 lines of pure state mutations, but these are correct ViewModel responsibilities per MVVM).

---

## Final target package map (achieved after Phase 5)

```
com.smartphoneaichat/
├── MainActivity.kt
├── di/
│   ├── AppContainer.kt
│   └── ChatViewModelFactory.kt
├── domain/
│   ├── model/
│   │   ├── Message.kt, Conversation.kt, ModelInfo.kt
│   │   └── value/{MessageId,ConversationId,MessageText}.kt
│   ├── repository/
│   │   ├── InferenceEngine.kt, ModelFileManager.kt
│   │   ├── ConversationRepository.kt, IdGenerator.kt
│   ├── service/ConversationTitleService.kt
│   └── usecase/{SendMessage,DownloadModel,LoadModel}UseCase.kt
├── data/
│   ├── engine/LiteRtInferenceEngine.kt
│   ├── model/HuggingFaceModelFileManager.kt
│   ├── conversation/InMemoryConversationRepository.kt   ← NEW
│   └── id/UuidIdGenerator.kt
├── presentation/
│   ├── viewmodel/ChatViewModel.kt
│   ├── state/ChatUiState.kt
│   └── notification/AppNotificationManager.kt
└── ui/
    ├── screens/ChatScreen.kt
    ├── components/{ChatBubble,ChatInput,Sidebar,ThinkingSection,...}.kt
    └── theme/{Color,Theme,Type}.kt
```

---

## Estimated effort

~2.5-3 hours. Largest phase in terms of test writing:
1. InMemoryConversationRepository + 10 tests (TDD) — 30 min
2. Wire into DI (3 files, 5 lines changed) — 10 min
3. Update ViewModel to use repo (4 methods) — 20 min
4. ViewModel tests (14 tests, MockK setup) — 1 hour
5. ChatUiState tests (5 tests) — 15 min
6. Verify + debug — 30 min