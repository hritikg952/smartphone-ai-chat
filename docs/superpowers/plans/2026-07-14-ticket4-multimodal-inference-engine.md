# TICKET-4: Multimodal InferenceEngine Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add multimodal (text + image) support to the InferenceEngine interface, implementations, use case, and ViewModel wiring.

**Architecture:** Add `sendMultimodalMessage(text, imageBytes)` to `InferenceEngine` as a separate method. `SendMessageUseCase` accepts optional `attachmentUri`, reads bytes from disk, and branches between `sendMessage`/`sendMultimodalMessage`. `ChatViewModel` passes `pendingAttachmentUri` from state.

**Tech Stack:** Kotlin, Kotlin Coroutines/Flow, LiteRT-LM SDK (`com.google.ai.edge.litertlm`), JUnit 5

## Global Constraints

- No comments in production code (except KDoc where already present)
- No emojis in UI code
- Follow existing Clean Architecture patterns (domain/data/presentation layers)
- `Attachment` model stays lean (URI, not bytes) — bytes read at engine boundary
- `Content.Text` must come before `Content.Image` in contents list
- Text-only messages remain fully backward-compatible

---

### Task 1: Add `sendMultimodalMessage` to InferenceEngine interface

**Files:**
- Modify: `app/src/main/java/com/smartphoneaichat/domain/repository/InferenceEngine.kt`

**Interfaces:**
- Consumes: nothing
- Produces: `fun sendMultimodalMessage(text: String, imageBytes: ByteArray): Flow<String>`

- [ ] **Step 1: Add the method to the interface**

```kotlin
package com.smartphoneaichat.domain.repository

import kotlinx.coroutines.flow.Flow

interface InferenceEngine {
    fun sendMessage(text: String): Flow<String>
    fun sendMultimodalMessage(text: String, imageBytes: ByteArray): Flow<String>
    fun stopGeneration()
    val isReady: Boolean
    val activeModelId: String?
}
```

- [ ] **Step 2: Verify compilation fails** (both implementations now missing the method)

Run: `./gradlew :app:compileDebugKotlin 2>&1 | tail -20`
Expected: compilation errors in `LiteRtInferenceEngine` and `FakeInferenceEngine`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/smartphoneaichat/domain/repository/InferenceEngine.kt
git commit -m "feat: add sendMultimodalMessage to InferenceEngine interface"
```

---

### Task 2: Update FakeInferenceEngine

**Files:**
- Modify: `app/src/test/java/com/smartphoneaichat/data/engine/FakeInferenceEngine.kt`

**Interfaces:**
- Consumes: `InferenceEngine.sendMultimodalMessage(text: String, imageBytes: ByteArray): Flow<String>`
- Produces: `FakeInferenceEngine.lastImageBytes: ByteArray?` (testable field)

- [ ] **Step 1: Implement sendMultimodalMessage**

```kotlin
package com.smartphoneaichat.data.engine

import com.smartphoneaichat.domain.repository.InferenceEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeInferenceEngine(
    private val tokens: List<String> = listOf("Hello", " ", "World"),
    private val shouldThrow: Boolean = false,
    private val throwAfterTokens: Int = Int.MAX_VALUE,
    override val isReady: Boolean = true,
    override val activeModelId: String? = "gemma3-1b",
) : InferenceEngine {

    var lastImageBytes: ByteArray? = null
        private set

    private var emittedCount = 0

    override fun sendMessage(text: String): Flow<String> = flow {
        emittedCount = 0
        for (token in tokens) {
            if (shouldThrow && emittedCount >= throwAfterTokens) {
                throw RuntimeException("Simulated inference failure")
            }
            emit(token)
            emittedCount++
        }
    }

    override fun sendMultimodalMessage(text: String, imageBytes: ByteArray): Flow<String> = flow {
        lastImageBytes = imageBytes
        emittedCount = 0
        for (token in tokens) {
            if (shouldThrow && emittedCount >= throwAfterTokens) {
                throw RuntimeException("Simulated inference failure")
            }
            emit(token)
            emittedCount++
        }
    }

    override fun stopGeneration() {}
}
```

- [ ] **Step 2: Verify FakeInferenceEngine compiles**

Run: `./gradlew :app:testClasses 2>&1 | tail -10`
Expected: BUILD SUCCESSFUL (or only LiteRtInferenceEngine still failing)

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/com/smartphoneaichat/data/engine/FakeInferenceEngine.kt
git commit -m "feat: add sendMultimodalMessage to FakeInferenceEngine"
```

---

### Task 3: Write failing tests for multimodal SendMessageUseCase

**Files:**
- Modify: `app/src/test/java/com/smartphoneaichat/domain/usecase/SendMessageUseCaseTest.kt`

**Interfaces:**
- Consumes: `SendMessageUseCase.invoke(conversation, text, attachmentUri: String? = null)`, `FakeInferenceEngine.lastImageBytes`
- Produces: test failures that validate multimodal flow

- [ ] **Step 1: Add test for user message having attachment when image URI provided**

```kotlin
@Test
fun `userMessageHasAttachmentWhenImageUriProvided`() = runTest {
    val imageUri = "/captures/test-image.jpg"
    val results = mutableListOf<Conversation>()
    sendMessageUseCase(conversation, MessageText("Describe this"), imageUri)
        .collect { results.add(it) }

    val userMessage = results.first().messages.first { it.role == ChatRole.USER }
    val attachment = userMessage.attachment
    assertNotNull(attachment)
    assertEquals("test-image.jpg", attachment!!.fileName)
    assertEquals("image/jpeg", attachment.mimeType)
    assertEquals(imageUri, attachment.imageUri)
}
```

- [ ] **Step 2: Add test for multimodal path calling sendMultimodalMessage**

```kotlin
@Test
fun `callsSendMultimodalMessageWhenImageUriProvided`() = runTest {
    val imageUri = "/captures/test-image.jpg"
    sendMessageUseCase(conversation, MessageText("Describe this"), imageUri).collect()

    assertNotNull(inferenceEngine.lastImageBytes)
}
```

- [ ] **Step 3: Add test for text-only path NOT calling sendMultimodalMessage**

```kotlin
@Test
fun `doesNotCallSendMultimodalMessageWhenNoImageUri`() = runTest {
    sendMessageUseCase(conversation, MessageText("Hello")).collect()

    assertNull(inferenceEngine.lastImageBytes)
}
```

- [ ] **Step 4: Add test for token accumulation in multimodal mode**

```kotlin
@Test
fun `accumulatesTokensInMultimodalMode`() = runTest {
    val imageUri = "/captures/test-image.jpg"
    val results = mutableListOf<Conversation>()
    sendMessageUseCase(conversation, MessageText("Describe this"), imageUri)
        .collect { results.add(it) }

    val lastEmission = results.last()
    val aiMessage = lastEmission.messages.last { it.role == ChatRole.AI }
    assertFalse(aiMessage.isStreaming)
    assertTrue(aiMessage.text.value.isNotEmpty())
}
```

- [ ] **Step 5: Create temporary test JPEG file in setup for tests that need real file reads**

```kotlin
// Add to test class:
private lateinit var tempImageFile: File

@BeforeEach
fun setUpImage() {
    tempImageFile = File.createTempFile("test-image", ".jpg")
    tempImageFile.writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte())) // JPEG header
}

@AfterEach
fun tearDownImage() {
    tempImageFile.delete()
}
```

Then update tests that use an image URI to use `tempImageFile.absolutePath` instead of `"/captures/test-image.jpg"`.

- [ ] **Step 6: Run tests to verify they fail**

Run: `./gradlew :app:test --tests "com.smartphoneaichat.domain.usecase.SendMessageUseCaseTest" 2>&1 | tail -20`
Expected: FAIL — tests calling `sendMessageUseCase` with 3 args don't compile, or runtime NPE on missing file

- [ ] **Step 7: Commit**

```bash
git add app/src/test/java/com/smartphoneaichat/domain/usecase/SendMessageUseCaseTest.kt
git commit -m "test: add multimodal tests for SendMessageUseCase"
```

---

### Task 4: Implement SendMessageUseCase multimodal support

**Files:**
- Modify: `app/src/main/java/com/smartphoneaichat/domain/usecase/SendMessageUseCase.kt`

**Interfaces:**
- Consumes: `InferenceEngine.sendMultimodalMessage(text, imageBytes)`, `File(path).readBytes()`
- Produces: `operator fun invoke(conversation: Conversation, text: MessageText, attachmentUri: String? = null): Flow<Conversation>`

- [ ] **Step 1: Add `attachmentUri` parameter and image path handling**

```kotlin
package com.smartphoneaichat.domain.usecase

import com.smartphoneaichat.domain.model.Attachment
import com.smartphoneaichat.domain.model.ChatRole
import com.smartphoneaichat.domain.model.Conversation
import com.smartphoneaichat.domain.model.Message
import com.smartphoneaichat.domain.model.value.MessageText
import com.smartphoneaichat.domain.repository.IdGenerator
import com.smartphoneaichat.domain.repository.InferenceEngine
import com.smartphoneaichat.domain.service.ConversationTitleService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class SendMessageUseCase(
    private val inferenceEngine: InferenceEngine,
    private val idGenerator: IdGenerator,
    private val titleService: ConversationTitleService = ConversationTitleService,
) {
    operator fun invoke(
        conversation: Conversation,
        text: MessageText,
        attachmentUri: String? = null,
    ): Flow<Conversation> = flow {
        val attachment = attachmentUri?.let { uri ->
            Attachment(
                fileName = File(uri).name,
                mimeType = "image/jpeg",
                imageUri = uri,
            )
        }
        val userMessage = Message(
            id = idGenerator.generateMessageId(),
            role = ChatRole.USER,
            text = text,
            attachment = attachment,
        )
        val aiMessageId = idGenerator.generateMessageId()
        val aiMessage = Message(
            id = aiMessageId,
            role = ChatRole.AI,
            text = MessageText(""),
            thinkingText = "Analyzing your request...\n" +
                    "Identifying key concepts...\n" +
                    "Formulating response...",
            isStreaming = true
        )

        var currentConv = conversation
            .addMessage(userMessage)
            .addMessage(aiMessage)

        val shouldAutoTitle = conversation.title == "New Chat" && conversation.messages.size <= 1
        if (shouldAutoTitle) {
            currentConv = currentConv.withTitle(titleService.generateTitle(text))
        }

        emit(currentConv)

        try {
            val tokenFlow = if (attachmentUri != null) {
                val imageBytes = File(attachmentUri).readBytes()
                inferenceEngine.sendMultimodalMessage(text.value, imageBytes)
            } else {
                inferenceEngine.sendMessage(text.value)
            }
            tokenFlow.collect { token ->
                currentConv = currentConv.updateMessage(aiMessageId) { msg ->
                    msg.copy(text = MessageText(msg.text.value + token))
                }
                emit(currentConv)
            }
        } finally {
            currentConv = currentConv.updateMessage(aiMessageId) { msg ->
                msg.copy(isStreaming = false)
            }
            try {
                emit(currentConv)
            } catch (_: CancellationException) {
            }
        }
    }
}
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `./gradlew :app:test --tests "com.smartphoneaichat.domain.usecase.SendMessageUseCaseTest" 2>&1 | tail -20`
Expected: all tests pass (including new multimodal tests)

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/smartphoneaichat/domain/usecase/SendMessageUseCase.kt
git commit -m "feat: add multimodal attachment support to SendMessageUseCase"
```

---

### Task 5: Add pendingAttachmentUri to ChatUiState

**Files:**
- Modify: `app/src/main/java/com/smartphoneaichat/presentation/state/ChatUiState.kt`

**Interfaces:**
- Produces: `pendingAttachmentUri: String?`

- [ ] **Step 1: Add the field**

```kotlin
data class ChatUiState(
    val conversations: List<Conversation> = emptyList(),
    val activeConversationId: ConversationId? = null,
    val isSidebarOpen: Boolean = false,
    val thinkingExpandedIds: Set<MessageId> = emptySet(),
    val loadingModelId: String? = null,
    val isModelLoading: Boolean = false,
    val modelLoadProgress: Float = 0f,
    val modelLoadPhase: String = "",
    val activeModelId: String? = null,
    val activeModelDisplayName: String? = null,
    val downloadedModelIds: Set<String> = emptySet(),
    val showDeleteConfirmation: Boolean = false,
    val deleteTargetModelId: String? = null,
    val showModelSelector: Boolean = false,
    val modelSelectorModels: List<ModelInfo> = emptyList(),
    val pendingAttachmentUri: String? = null,
) {
    val activeConversation: Conversation?
        get() = conversations.find { it.id == activeConversationId }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :app:compileDebugKotlin 2>&1 | tail -10`
Expected: compiles (all ChatUiState usage sites with defaults should still work)

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/smartphoneaichat/presentation/state/ChatUiState.kt
git commit -m "feat: add pendingAttachmentUri to ChatUiState"
```

---

### Task 6: Wire pendingAttachmentUri in ChatViewModel.sendMessage

**Files:**
- Modify: `app/src/main/java/com/smartphoneaichat/presentation/viewmodel/ChatViewModel.kt`

**Interfaces:**
- Consumes: `ChatUiState.pendingAttachmentUri`, `SendMessageUseCase(conversation, text, attachmentUri)`

- [ ] **Step 1: Update sendMessage() to pass attachment URI and clear it on send**

Find the `sendMessage` method and update the coroutine block:

```kotlin
fun sendMessage(text: String) {
    val trimmedInput = text.trim()
    if (trimmedInput.isEmpty()) return

    if (trimmedInput.length > MAX_INPUT_LENGTH) {
        notifications.show(AppNotificationEvent.Error("Message too long (max $MAX_INPUT_LENGTH characters)"))
        return
    }

    streamingJob?.cancel()

    val currentConv = _state.value.activeConversation ?: return
    val imageUri = _state.value.pendingAttachmentUri

    streamingJob = viewModelScope.launch {
        try {
            sendMessageUseCase(currentConv, MessageText(trimmedInput), imageUri)
                .flowOn(Dispatchers.IO)
                .collect { updatedConversation ->
                    _state.update {
                        replaceConversation(updatedConversation).copy(pendingAttachmentUri = null)
                    }
                    conversationRepository.save(updatedConversation)
                }
        } catch (e: Exception) {
            notifications.show(AppNotificationEvent.Error("AI error: ${e.message}"))
        }
    }
}
```

The only changes are:
1. `val imageUri = _state.value.pendingAttachmentUri` — snapshot before coroutine
2. Pass `imageUri` as third argument to `sendMessageUseCase()`
3. `.copy(pendingAttachmentUri = null)` — clear after first emission

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :app:compileDebugKotlin 2>&1 | tail -10`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Run all existing tests to verify nothing broke**

Run: `./gradlew :app:test 2>&1 | tail -10`
Expected: all tests pass

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/smartphoneaichat/presentation/viewmodel/ChatViewModel.kt
git commit -m "feat: wire pendingAttachmentUri through ChatViewModel to use case"
```

---

### Task 7: Implement LiteRtInferenceEngine multimodal

**Files:**
- Modify: `app/src/main/java/com/smartphoneaichat/data/engine/LiteRtInferenceEngine.kt`

**Interfaces:**
- Consumes: `engine.createConversation().sendMessage(Contents.of(...))` or `sendMessageAsync(Contents.of(...))`
- Produces: `sendMultimodalMessage(text, imageBytes): Flow<String>`

- [ ] **Step 1: Add sendMultimodalMessage using Contents.of**

```kotlin
package com.smartphoneaichat.data.engine

import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
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
        val eng = modelFileManager.getEngine()
            ?: throw IllegalStateException("Engine not initialized. Call loadModel() first.")

        val conversation = eng.createConversation()

        return conversation.sendMessageAsync(text).map { liteRtMsg ->
            liteRtMsg.contents.contents.filterIsInstance<Content.Text>().joinToString("") { it.text }
        }
    }

    override fun sendMultimodalMessage(text: String, imageBytes: ByteArray): Flow<String> {
        val eng = modelFileManager.getEngine()
            ?: throw IllegalStateException("Engine not initialized. Call loadModel() first.")

        val conversation = eng.createConversation()

        return conversation.sendMessageAsync(Contents.of(Content.Text(text), Content.Image(imageBytes)))
            .map { liteRtMsg ->
                liteRtMsg.contents.contents.filterIsInstance<Content.Text>().joinToString("") { it.text }
            }
    }

    override fun stopGeneration() {
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :app:compileDebugKotlin 2>&1 | tail -20`
Expected: BUILD SUCCESSFUL. If `sendMessageAsync(Contents)` doesn't exist, try `sendMessage(Contents.of(...))` and wrap as `flow { emit(extractText(response)) }` if needed.

- [ ] **Step 3: Run all tests**

Run: `./gradlew :app:test 2>&1 | tail -10`
Expected: all tests pass

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/smartphoneaichat/data/engine/LiteRtInferenceEngine.kt
git commit -m "feat: implement multimodal send in LiteRtInferenceEngine"
```

---

## Self-Review Checklist

- [x] All 7 tasks have explicit file create/modify paths
- [x] All code changes shown as complete blocks (no TBDs)
- [x] Type consistency: `sendMultimodalMessage(text: String, imageBytes: ByteArray): Flow<String>` matches everywhere
- [x] `attachmentUri: String? = null` default parameter ensures backward compatibility
- [x] Tests written before implementation (Task 3 before Task 4)
- [x] All compile and test run commands specified
- [x] Each task ends with a commit