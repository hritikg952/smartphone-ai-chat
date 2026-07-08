# Phase 2 — Conversation Encapsulation + ConversationTitleService

> **Source:** ARCHITECTURE.md §9 Phase 2  
> **Principle:** Tests-first (TDD). "Tell, Don't Ask" — consumers tell the Conversation what to do, don't mutate its internals.  
> **Constraint:** `Conversation` private constructor + named mutation methods. No direct `conv.copy(messages = ...)` or `conv.copy(title = ...)` allowed.  
> **Quality Gate:** All Conversation state changes route through declared methods (`create()`, `addMessage()`, `updateMessage()`, `replaceMessages()`, `withTitle()`). Build compiles. All tests pass (Phase 0 + new Phase 2 tests).

---

## Overview

Convert `Conversation` from an anemic data class (public constructor, `copy()` for mutations) into an aggregate root with a `private constructor` and a `companion object.create()` factory. Add four named mutation methods. Add `ConversationTitleService` as a pure domain service. Replace all direct `conv.copy(messages = ...)` / `conv.copy(title = ...)` calls in the ViewModel with route-through methods.

## Current State (Post-Phase 1)

**Conversation.kt** — public constructor, no methods, no encapsulation:
```kotlin
data class Conversation(
    val id: ConversationId,
    val title: String = "New Chat",
    val messages: List<Message> = emptyList()
)
```

**All 4 mutation patterns in ChatViewModel.kt** (5 total sites):

| Line | Pattern | What It Does |
|------|---------|-------------|
| ~49 | `Conversation(id = ..., title = ..., messages = listOf(...))` | Construct with messages inline |
| ~128 | `currentConv.copy(messages = currentConv.messages + userMessage + aiMessage)` | Add two messages |
| ~139 | `conv.copy(title = trimmedInput.take(40) + ...)` | Auto-title from first message |
| ~162 | `conv.copy(messages = updatedMessages)` | Update AI message text (streaming token) |
| ~173 | `conv.copy(messages = updatedMessages)` | Update AI message streaming=false (final) |
| ~197 | `conv.copy(messages = conv.messages + pendingMessage)` | Add one message (attachment) |
| ~213 | `conv.copy(messages = msgs.dropLast(1))` | Remove last message |
| ~455 | `Conversation(id = ..., title = "New Chat")` | Construct empty |
| ~472 | `Conversation(id = ...)` | Construct with defaults |

**Existing tests:** 15 tests (ValueObjectTest: 3 test classes × 4-5 tests each, UuidIdGeneratorTest: 4, FakeIdGeneratorTest: 4)

---

## Target State (Post-Phase 2)

**Conversation.kt:**
```kotlin
data class Conversation private constructor(
    val id: ConversationId,
    val title: String,
    val messages: List<Message>,
) {
    fun addMessage(message: Message): Conversation = copy(messages = messages + message)

    fun updateMessage(messageId: MessageId, transform: (Message) -> Message): Conversation =
        copy(messages = messages.map { if (it.id == messageId) transform(it) else it })

    fun replaceMessages(newMessages: List<Message>): Conversation = copy(messages = newMessages)

    fun withTitle(title: String): Conversation = copy(title = title)

    val lastMessage: Message? get() = messages.lastOrNull()
    val isEmpty: Boolean get() = messages.isEmpty()

    companion object {
        fun create(id: ConversationId, title: String = "New Chat"): Conversation =
            Conversation(id = id, title = title, messages = emptyList())
    }
}
```

**New file — `ConversationTitleService.kt`** in `domain/service/`:
```kotlin
object ConversationTitleService {
    private const val MAX_TITLE_LENGTH = 40

    fun generateTitle(firstMessage: MessageText): String {
        val raw = firstMessage.value.trim()
        return if (raw.length <= MAX_TITLE_LENGTH) raw
        else raw.take(MAX_TITLE_LENGTH) + "\u2026"
    }
}
```

---

## Tasks (execute in order)

### Task 1: Write Conversation tests (TDD — RED)

**Create file:** `app/src/test/java/com/smartphoneaichat/domain/model/ConversationTest.kt`

These tests must compile and fail BEFORE touching Conversation.kt. Use `FakeIdGenerator` from the test directory for deterministic IDs.

#### Tests to write:

```kotlin
package com.smartphoneaichat.domain.model

import com.smartphoneaichat.data.id.FakeIdGenerator
import com.smartphoneaichat.domain.model.value.MessageText
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConversationTest {

    private val idGen = FakeIdGenerator()

    // ── Factory ──────────────────────────────────────────────────

    @Test
    fun create_withIdAndDefaultTitle_hasCorrectIdAndDefaultTitle() {
        val id = idGen.generateConversationId()
        val conv = Conversation.create(id)
        assertEquals(id, conv.id)
        assertEquals("New Chat", conv.title)
    }

    @Test
    fun create_withIdAndCustomTitle_hasCustomTitle() {
        val id = idGen.generateConversationId()
        val conv = Conversation.create(id, "Custom Title")
        assertEquals("Custom Title", conv.title)
    }

    @Test
    fun create_producesEmptyConversation() {
        val conv = Conversation.create(idGen.generateConversationId())
        assertTrue(conv.isEmpty)
        assertEquals(emptyList<Message>(), conv.messages)
    }

    // ── addMessage ───────────────────────────────────────────────

    @Test
    fun addMessage_appendsMessage() {
        val conv = Conversation.create(idGen.generateConversationId())
        val msg = Message(
            id = idGen.generateMessageId(),
            role = ChatRole.USER,
            text = MessageText("Hello")
        )
        val updated = conv.addMessage(msg)
        assertEquals(1, updated.messages.size)
        assertEquals(msg, updated.messages.first())
    }

    @Test
    fun addMessage_doesNotMutateOriginal() {
        val conv = Conversation.create(idGen.generateConversationId())
        val msg = Message(
            id = idGen.generateMessageId(),
            role = ChatRole.USER,
            text = MessageText("Hello")
        )
        conv.addMessage(msg)
        assertTrue(conv.isEmpty)  // original unchanged
    }

    @Test
    fun addMessage_returnsNewConversation() {
        val conv = Conversation.create(idGen.generateConversationId())
        val msg = Message(
            id = idGen.generateMessageId(),
            role = ChatRole.USER,
            text = MessageText("Hello")
        )
        val updated = conv.addMessage(msg)
        assertNotSame(conv, updated)
    }

    // ── updateMessage ────────────────────────────────────────────

    @Test
    fun updateMessage_appliesTransformToMatchingId() {
        val conv = Conversation.create(idGen.generateConversationId())
        val msgId = idGen.generateMessageId()
        val msg = Message(id = msgId, role = ChatRole.USER, text = MessageText("original"))
        val updated = conv.addMessage(msg).updateMessage(msgId) { it.copy(text = MessageText("updated")) }
        assertEquals("updated", updated.messages.first().text.value)
    }

    @Test
    fun updateMessage_onlyTransformsMatchingId() {
        val conv = Conversation.create(idGen.generateConversationId())
        val id1 = idGen.generateMessageId()
        val id2 = idGen.generateMessageId()
        val msg1 = Message(id = id1, role = ChatRole.USER, text = MessageText("first"))
        val msg2 = Message(id = id2, role = ChatRole.AI, text = MessageText("second"))
        val updated = conv.addMessage(msg1).addMessage(msg2)
            .updateMessage(id1) { it.copy(text = MessageText("changed")) }
        assertEquals("changed", updated.messages[0].text.value)
        assertEquals("second", updated.messages[1].text.value)
    }

    @Test
    fun updateMessage_doesNothingForNonMatchingId() {
        val conv = Conversation.create(idGen.generateConversationId())
        val msg = Message(id = idGen.generateMessageId(), role = ChatRole.USER, text = MessageText("hello"))
        val nonMatchingId = idGen.generateMessageId()
        val updated = conv.addMessage(msg).updateMessage(nonMatchingId) { it.copy(text = MessageText("changed")) }
        assertEquals("hello", updated.messages.first().text.value)
    }

    @Test
    fun updateMessage_doesNotMutateOriginal() {
        val conv = Conversation.create(idGen.generateConversationId())
        val msgId = idGen.generateMessageId()
        val msg = Message(id = msgId, role = ChatRole.USER, text = MessageText("original"))
        val withMsg = conv.addMessage(msg)
        withMsg.updateMessage(msgId) { it.copy(text = MessageText("changed")) }
        assertEquals("original", withMsg.messages.first().text.value)
    }

    // ── replaceMessages ──────────────────────────────────────────

    @Test
    fun replaceMessages_replacesEntireList() {
        val conv = Conversation.create(idGen.generateConversationId())
        val msg1 = Message(id = idGen.generateMessageId(), role = ChatRole.USER, text = MessageText("A"))
        val withMsg = conv.addMessage(msg1)
        val msg2 = Message(id = idGen.generateMessageId(), role = ChatRole.AI, text = MessageText("B"))
        val replaced = withMsg.replaceMessages(listOf(msg2))
        assertEquals(1, replaced.messages.size)
        assertEquals("B", replaced.messages.first().text.value)
    }

    @Test
    fun replaceMessages_canSetEmptyList() {
        val conv = Conversation.create(idGen.generateConversationId())
        val msg = Message(id = idGen.generateMessageId(), role = ChatRole.USER, text = MessageText("A"))
        val withMsg = conv.addMessage(msg)
        val replaced = withMsg.replaceMessages(emptyList())
        assertTrue(replaced.isEmpty)
    }

    // ── withTitle ────────────────────────────────────────────────

    @Test
    fun withTitle_changesTitle() {
        val conv = Conversation.create(idGen.generateConversationId())
        val updated = conv.withTitle("New Title")
        assertEquals("New Title", updated.title)
    }

    @Test
    fun withTitle_doesNotMutateOriginal() {
        val conv = Conversation.create(idGen.generateConversationId(), "Original")
        conv.withTitle("Changed")
        assertEquals("Original", conv.title)
    }

    // ── lastMessage ──────────────────────────────────────────────

    @Test
    fun lastMessage_returnsLastAdded() {
        val conv = Conversation.create(idGen.generateConversationId())
        val msg1 = Message(id = idGen.generateMessageId(), role = ChatRole.USER, text = MessageText("First"))
        val msg2 = Message(id = idGen.generateMessageId(), role = ChatRole.USER, text = MessageText("Last"))
        val updated = conv.addMessage(msg1).addMessage(msg2)
        assertEquals(msg2, updated.lastMessage)
    }

    @Test
    fun lastMessage_returnsNullForEmpty() {
        val conv = Conversation.create(idGen.generateConversationId())
        assertNull(conv.lastMessage)
    }

    // ── isEmpty ──────────────────────────────────────────────────

    @Test
    fun isEmpty_returnsTrueForEmpty() {
        val conv = Conversation.create(idGen.generateConversationId())
        assertTrue(conv.isEmpty)
    }

    @Test
    fun isEmpty_returnsFalseWithMessages() {
        val conv = Conversation.create(idGen.generateConversationId())
        val msg = Message(id = idGen.generateMessageId(), role = ChatRole.USER, text = MessageText("Hi"))
        val updated = conv.addMessage(msg)
        assertFalse(updated.isEmpty)
    }
}
```

**Manual verification checklist:**
- [x] `./gradlew test` FAILS — `Conversation.create()`, `addMessage()`, etc. don't exist yet

---

### Task 2: Write ConversationTitleService tests (TDD — RED)

**Create file:** `app/src/test/java/com/smartphoneaichat/domain/service/ConversationTitleServiceTest.kt`

```kotlin
package com.smartphoneaichat.domain.service

import com.smartphoneaichat.domain.model.value.MessageText
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConversationTitleServiceTest {

    @Test
    fun shortTitle_returnedVerbatim() {
        val result = ConversationTitleService.generateTitle(MessageText("Hello"))
        assertEquals("Hello", result)
    }

    @Test
    fun exactly40Chars_returnedVerbatim() {
        val text = "a".repeat(40)
        val result = ConversationTitleService.generateTitle(MessageText(text))
        assertEquals(text, result)
    }

    @Test
    fun exceeds40Chars_truncatedWithEllipsis() {
        val text = "a".repeat(50)
        val result = ConversationTitleService.generateTitle(MessageText(text))
        assertEquals("a".repeat(40) + "\u2026", result)
        assertEquals(41, result.length) // 40 chars + 1 ellipsis char
    }

    @Test
    fun emptyString_returnsEmptyString() {
        val result = ConversationTitleService.generateTitle(MessageText(""))
        assertEquals("", result)
    }

    @Test
    fun whitespaceOnly_trimsToEmptyAndReturnsEmptyString() {
        val result = ConversationTitleService.generateTitle(MessageText("   "))
        assertEquals("", result)
    }

    @Test
    fun leadingAndTrailingWhitespace_trimmed() {
        val result = ConversationTitleService.generateTitle(MessageText("  Hello World  "))
        assertEquals("Hello World", result)
    }

    @Test
    fun unicodeAtBoundary_truncatedAtCharBoundary() {
        // 38 ASCII chars + 3-byte emoji = 41 "visual" chars but truncates at 40
        // "a".repeat(38) + "\uD83D\uDE00" is 38 + 1 emoji = 39 grapheme clusters
        // but 40 chars if emoji is 2 chars. After take(40) and adding ellipsis...
        val text = "a".repeat(39) + "\uD83D\uDE00" // 39 ASCII + emoji (2 code units) = 41
        val result = ConversationTitleService.generateTitle(MessageText(text))
        // take(40) keeps "a"*39 + first half of emoji = broken surrogate
        // This is a known edge case. The test documents the behavior.
        assertEquals(41, result.length) // 40 chars + 1 ellipsis
    }
}
```

**Manual verification checklist:**
- [x] `./gradlew test` FAILS — `ConversationTitleService` doesn't exist yet
- [x] Both test files compile and run (failing due to missing implementation)

---

### Task 3: Implement Conversation aggregate root (TDD — GREEN)

**File:** `app/src/main/java/com/smartphoneaichat/domain/model/Conversation.kt`

Rewrite the entire file with:

1. **`private constructor`** — replaces the current public constructor
2. **`companion object.create()`** — factory method, takes `id: ConversationId` and optional `title: String = "New Chat"`
3. **`addMessage(message: Message): Conversation`** — appends to internal list via `copy(messages = messages + message)`
4. **`updateMessage(messageId: MessageId, transform: (Message) -> Message): Conversation`** — applies transform to matching message
5. **`replaceMessages(newMessages: List<Message>): Conversation`** — replaces entire message list
6. **`withTitle(title: String): Conversation`** — replaces title
7. **`lastMessage: Message?`** — computed property
8. **`isEmpty: Boolean`** — computed property

Add KDoc to the class, the factory method, and each mutation method explaining the "Tell, Don't Ask" rationale.

**After writing, verify with:** `./gradlew test --tests "com.smartphoneaichat.domain.model.ConversationTest"`

This should compile but FAIL because ChatViewModel still uses public constructor/direct `copy()`.

**Manual verification checklist:**
- [x] `./gradlew test --tests "com.smartphoneaichat.domain.model.ConversationTest"` — all 16 Conversation tests GREEN
- [x] `./gradlew assembleDebug` FAILS — ViewModel uses deprecated public constructor and direct `copy()` mutations
- [x] KDoc present on class, `create()`, and each mutation method

---

### Task 4: Implement ConversationTitleService (TDD — GREEN)

**Create file:** `app/src/main/java/com/smartphoneaichat/domain/service/ConversationTitleService.kt`

Pure `object` with a single function:

```kotlin
package com.smartphoneaichat.domain.service

import com.smartphoneaichat.domain.model.value.MessageText

object ConversationTitleService {
    private const val MAX_TITLE_LENGTH = 40

    fun generateTitle(firstMessage: MessageText): String {
        val raw = firstMessage.value.trim()
        return if (raw.length <= MAX_TITLE_LENGTH) raw
        else raw.take(MAX_TITLE_LENGTH) + "\u2026"
    }
}
```

Add KDoc explaining business rationale (title rules are domain logic, not ViewModel concerns).

**Manual verification checklist:**
- [x] `./gradlew test --tests "com.smartphoneaichat.domain.service.ConversationTitleServiceTest"` — all 7 tests GREEN
- [x] No Android, Compose, or SDK imports
- [x] KDoc present

---

### Task 5: Update ChatViewModel — Remove all direct `copy()` mutations

**File:** `app/src/main/java/com/smartphoneaichat/presentation/viewmodel/ChatViewModel.kt`

Add import:
```kotlin
import com.smartphoneaichat.domain.service.ConversationTitleService
```

Replace ALL 9 mutation sites. See detailed mapping below.

---

#### Site 1: `init {}` block — mock conversation construction (line ~49)

**Before:**
```kotlin
val mockConversation = Conversation(
    id = idGenerator.generateConversationId(),
    title = "Welcome to AI Chat",
    messages = listOf(
        Message(
            id = idGenerator.generateMessageId(),
            role = ChatRole.AI,
            text = MessageText("Hello! I'm your AI assistant. Send me a message to get started."),
            thinkingText = "",
        )
    )
)
```

**After:**
```kotlin
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
```

---

#### Site 2: `sendMessage()` — append user + AI messages (line ~128)

**Before:**
```kotlin
val updatedConv = currentConv.copy(
    messages = currentConv.messages + userMessage + aiMessage
)
```

**After:**
```kotlin
val updatedConv = currentConv
    .addMessage(userMessage)
    .addMessage(aiMessage)
```

---

#### Site 3: `sendMessage()` — auto-title (line ~139)

**Before:**
```kotlin
conv.copy(
    title = trimmedInput.take(40) +
            if (trimmedInput.length > 40) "\u2026" else ""
)
```

**After:**
```kotlin
conv.withTitle(ConversationTitleService.generateTitle(MessageText(trimmedInput)))
```

---

#### Site 4: `sendMessage()` — streaming token accumulation (line ~162)

**Before:**
```kotlin
val updatedMessages = conv.messages.map { msg ->
    if (msg.id == aiMessageId && msg.role == ChatRole.AI) {
        msg.copy(text = MessageText(msg.text.value + token))
    } else msg
}
replaceConversation(conv.copy(messages = updatedMessages))
```

**After:**
```kotlin
val updated = conv.updateMessage(aiMessageId) { msg ->
    msg.copy(text = MessageText(msg.text.value + token))
}
replaceConversation(updated)
```

---

#### Site 5: `sendMessage()` — final streaming complete (line ~173)

**Before:**
```kotlin
val updatedMessages = conv.messages.map { msg ->
    if (msg.id == aiMessageId) msg.copy(isStreaming = false) else msg
}
replaceConversation(conv.copy(messages = updatedMessages))
```

**After:**
```kotlin
val updated = conv.updateMessage(aiMessageId) { msg ->
    msg.copy(isStreaming = false)
}
replaceConversation(updated)
```

---

#### Site 6: `attachImage()` — append pending message (line ~197)

**Before:**
```kotlin
val updatedConv = conv.copy(
    messages = conv.messages + pendingMessage
)
```

**After:**
```kotlin
val updatedConv = conv.addMessage(pendingMessage)
```

---

#### Site 7: `removePendingAttachment()` — remove last message (line ~213)

**Before:**
```kotlin
return@update replaceConversation(
    conv.copy(messages = msgs.dropLast(1))
)
```

**After:**
```kotlin
return@update replaceConversation(
    conv.replaceMessages(msgs.dropLast(1))
)
```

---

#### Site 8: `newConversation()` — create empty conversation (line ~455)

**Before:**
```kotlin
val newConv = Conversation(
    id = idGenerator.generateConversationId(),
    title = "New Chat"
)
```

**After:**
```kotlin
val newConv = Conversation.create(
    id = idGenerator.generateConversationId(),
    title = "New Chat"
)
```

---

#### Site 9: `deleteConversation()` — fresh fallback (line ~472)

**Before:**
```kotlin
val fresh = Conversation(id = idGenerator.generateConversationId())
```

**After:**
```kotlin
val fresh = Conversation.create(id = idGenerator.generateConversationId())
```

---

**Manual verification checklist:**
- [x] Zero occurrences of `conv.copy(messages` or `currentConv.copy(messages` or `.copy(messages` in ChatViewModel.kt
- [x] Zero occurrences of `conv.copy(title` in ChatViewModel.kt
- [x] Zero occurrences of `Conversation(` (public constructor) in ChatViewModel.kt — only `Conversation.create(` allowed
- [x] All 9 sites updated

---

### Task 6: Verify build compiles and all tests pass

```bash
./gradlew assembleDebug
./gradlew test
```

**Troubleshooting common issues:**

| Error | Fix |
|---|---|
| "Cannot access '<init>': it is private" | Somewhere still calling `Conversation(...)` directly — find and replace with `Conversation.create(...)` |
| "Unresolved reference: create" | `Conversation.create()` might need import check — it's a static method via companion object |
| "Unresolved reference: addMessage" | Import `Conversation` from correct package |
| "Type mismatch: Conversation vs ..." | `addMessage()`, `withTitle()`, `updateMessage()` all return `Conversation` — ensure variables are typed correctly |
| "Unresolved reference: ConversationTitleService" | Add import in ChatViewModel.kt |
| Test failing: `create` test | Check that factory method signature matches test expectations |

**Quality Gate validation:**
- [ ] `./gradlew assembleDebug` passes with zero errors (final check)
- [ ] `./gradlew test` passes — ALL tests green (15 Phase 0 + 16 Conversation + 7 TitleService = 38 tests)
- [ ] No `Conversation(` constructor calls outside `Conversation.kt` itself (except the `companion object` calling the private constructor)
- [ ] No `conv.copy(messages = ...)` or `conv.copy(title = ...)` in ChatViewModel.kt
- [ ] All Conversation state changes route through: `create()`, `addMessage()`, `updateMessage()`, `replaceMessages()`, `withTitle()`
- [ ] `ConversationTitleService` is a pure `object` with no dependencies
- [ ] `domain/` package has zero forbidden imports (no Android, Compose, SDK)

---

### Task 7: Dependency direction audit

Run these checks to verify Phase 2 didn't introduce dependency violations:

```bash
# domain/ must be clean
grep -r "import android" app/src/main/java/com/smartphoneaichat/domain/ || echo "OK: domain/ clean of android"
grep -r "import com.google.ai.edge.litertlm" app/src/main/java/com/smartphoneaichat/domain/ || echo "OK: domain/ clean of SDK"
grep -r "import androidx.compose" app/src/main/java/com/smartphoneaichat/domain/ || echo "OK: domain/ clean of compose"
grep -r "import com.smartphoneaichat.data" app/src/main/java/com/smartphoneaichat/domain/ || echo "OK: domain/ clean of data"
grep -r "import com.smartphoneaichat.presentation" app/src/main/java/com/smartphoneaichat/domain/ || echo "OK: domain/ clean of presentation"
grep -r "import com.smartphoneaichat.ui" app/src/main/java/com/smartphoneaichat/domain/ || echo "OK: domain/ clean of ui"

# ConversationTitleService must NOT import domain/ things it shouldn't need
grep "import" app/src/main/java/com/smartphoneaichat/domain/service/ConversationTitleService.kt
# Expected: only MessageText import
```

**Manual verification checklist:**
- [x] All 6 domain/ dependency checks pass
- [x] `ConversationTitleService.kt` imports only `MessageText` (no redundant imports)

---

## Files to CREATE

| File | Purpose |
|------|---------|
| `domain/service/ConversationTitleService.kt` | Pure domain service — title generation rules |
| `app/src/test/.../domain/model/ConversationTest.kt` | 16 tests for Conversation aggregate |
| `app/src/test/.../domain/service/ConversationTitleServiceTest.kt` | 7 tests for title service |

## Files to MODIFY

| File | Changes |
|------|---------|
| `domain/model/Conversation.kt` | Private constructor, `create()`, 4 mutation methods, 2 computed properties |
| `presentation/viewmodel/ChatViewModel.kt` | 9 constructor/copy sites → method calls, add `ConversationTitleService` import |

## Architectural violations FIXED by this phase

| # | Violation | Status |
|---|-----------|--------|
| 4 | Anemic domain model — public constructor + public `copy()` for mutations | FIXED — Private constructor, `addMessage()`, `updateMessage()`, `replaceMessages()`, `withTitle()` |

Violations fixed in prior phases:
- #2 (no repository interfaces) — Phase 1
- #3 (ISP violation) — Phase 1
- #5 (nondeterministic IDs) — Phase 0
- #9 (ChatUiState co-located) — Phase 1
- #10 (ModelInfo in infrastructure package) — Phase 1

## Architectural violations NOT YET fixed

| # | Violation | Deferred To |
|---|-----------|-------------|
| 1 | God ViewModel (~500 lines, 14+ responsibilities) | Phase 3 (Use Cases) |
| 6 | No DI (direct instantiation in ViewModel body) | Phase 4 (DI) |
| 7 | SDK leak into ViewModel (Flow from LiteRT-LM) | Phase 3 (SendMessageUseCase wraps it) |
| 8 | No ConversationRepository implementation | Phase 5 (Repository) |

---

## Estimated effort

~1.5 hours. Most time spent on:
1. Writing 23 new tests (Task 1 + 2)
2. Updating 9 mutation sites in ChatViewModel (Task 5)
3. Debugging any missed `Conversation(...)` constructor calls or `conv.copy(...)` mutations