# Phase 0 — IdGenerator + Value Objects

> **Source:** ARCHITECTURE.md §9 Phase 0  
> **Principle:** Tests-first (TDD). All new domain code must have tests before implementation.  
> **Constraint:** This phase only changes constructors and ID types. No logic refactoring.  
> **Quality Gate:** All existing functionality still works. IDs are deterministic in tests. Build compiles. All tests pass.

---

## Overview

Replace auto-generated `String` IDs (`System.currentTimeMillis()` + random) in `Message` and `Conversation` with typed value objects (`MessageId`, `ConversationId`). Add `IdGenerator` interface for deterministic, testable ID creation. Add `MessageText` value object for message text validation.

## Current state

- No test infrastructure exists (no JUnit, no test directories, no test dependencies)
- `Message.id: String` — default-generated as `"msg_${System.currentTimeMillis()}_${random}"`
- `Conversation.id: String` — default-generated as `"conv_${System.currentTimeMillis()}"`
- `ChatUiState.activeConversationId: String` — plain string comparison throughout
- `Message.text: String` — no length validation at type level

---

## Tasks (execute in order)

### Task 1: Set up test infrastructure

**File:** `app/build.gradle.kts`

Add these dependencies:

```kotlin
testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.3")
testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.3")
testImplementation("io.mockk:mockk:1.13.12")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
testImplementation("app.cash.turbine:turbine:1.1.0")
```

Add this in the `android {}` block to enable JUnit 5:

```kotlin
testOptions {
    unitTests.all {
        it.useJUnitPlatform()
    }
}
```

**Create directories:**
- `app/src/test/java/com/smartphoneaichat/`
- `app/src/test/java/com/smartphoneaichat/domain/model/value/`
- `app/src/test/java/com/smartphoneaichat/data/id/`

**Manual verification checklist:**
- [x] `./gradlew test` runs and reports "0 tests" (no failure)
- [x] Dependencies resolve without error

---

### Task 2: Write value object tests (TDD — RED)

**Create:** `app/src/test/java/com/smartphoneaichat/domain/model/value/ValueObjectTest.kt`

Tests to write (write these FIRST before any implementation):

#### MessageId tests
- Create with non-blank string succeeds, `.value` returns the string
- Create with blank string `""` throws `IllegalArgumentException`
- Create with whitespace-only `"   "` throws `IllegalArgumentException`
- Two MessageIds with same value are equal (`==`)
- Two MessageIds with different values are not equal

#### ConversationId tests
- Create with non-blank string succeeds
- Create with blank string throws `IllegalArgumentException`
- Create with whitespace-only throws `IllegalArgumentException`
- Two ConversationIds with same value are equal
- Two ConversationIds with different values are not equal

#### MessageText tests
- Create with text under 4096 chars succeeds
- Create with text exactly 4096 chars succeeds
- Create with text exactly 4097 chars throws `IllegalArgumentException`
- Create with text of 10000 chars throws `IllegalArgumentException`
- Empty string `""` is valid (user may send empty message with attachment)

**Manual verification checklist:**
- [x] `./gradlew test` FAILS (all tests red — classes don't exist yet)
- [x] All test method names follow `thingUnderTest_condition_expectedResult` convention

---

### Task 3: Implement value objects (TDD — GREEN)

**Create files:**

```
domain/model/value/MessageId.kt
domain/model/value/ConversationId.kt
domain/model/value/MessageText.kt
```

All three are `@JvmInline value class` with a single `val value: String` property and an `init` block enforcing invariants.

- `MessageId`: `require(value.isNotBlank())`
- `ConversationId`: `require(value.isNotBlank())`
- `MessageText`: `require(value.length <= 4096)`

Add KDoc on each value class explaining its business rationale (why it exists, what invariant it enforces).

**Manual verification checklist:**
- [x] `./gradlew test` PASSES (all value object tests green)

---

### Task 4: Write IdGenerator tests (TDD — RED)

**Create:** `app/src/test/java/com/smartphoneaichat/data/id/UuidIdGeneratorTest.kt`

Tests to write FIRST:

- `generateMessageId()` returns non-null MessageId
- `generateMessageId()` returns unique IDs on successive calls (same millisecond, called 10x, all distinct)
- `generateConversationId()` returns non-null ConversationId
- `generateConversationId()` returns unique IDs on successive calls

Also create `app/src/test/java/com/smartphoneaichat/data/id/FakeIdGenerator.kt` (test helper):

A simple in-memory counter implementation of `IdGenerator` that returns sequential IDs (`"msg-1"`, `"msg-2"`, `"conv-1"`, etc.). This is NOT placed in `src/main` — it lives in `src/test` only. The architecture doc says to use it for ViewModel tests in later phases, but having it ready now is useful.

Test the FakeIdGenerator itself to verify it works correctly.

**Manual verification checklist:**
- [x] `./gradlew test` FAILS (IdGenerator/UuidIdGenerator don't exist yet)

---

### Task 5: Implement IdGenerator interface + UuidIdGenerator (TDD — GREEN)

**Create:** `domain/repository/IdGenerator.kt`

```kotlin
interface IdGenerator {
    fun generateMessageId(): MessageId
    fun generateConversationId(): ConversationId
}
```

Add KDoc explaining why ID generation is abstracted (deterministic testing).

**Create:** `data/id/UuidIdGenerator.kt`

Implementation using `UUID.randomUUID()`. Prefix with `"msg-"` for messages and `"conv-"` for conversations for readability.

Add KDoc.

**Manual verification checklist:**
- [x] `./gradlew test` PASSES (all IdGenerator tests green)

---

### Task 6: Update Message.kt

**File:** `app/src/main/java/com/smartphoneaichat/model/Message.kt`

Changes:
1. Import `MessageId`
2. Change `id: String = "msg_${...}"` to `id: MessageId` (no default — required parameter)
3. Change `text: String = ""` to `text: MessageText = MessageText("")`

The `id` parameter must now be passed explicitly by all callers.

**Manual verification checklist:**
- [x] `./gradlew assembleDebug` FAILS (because callers in ViewModel still don't pass `MessageId`)

---

### Task 7: Update Conversation.kt

**File:** `app/src/main/java/com/smartphoneaichat/model/Conversation.kt`

Changes:
1. Import `ConversationId`
2. Change `id: String = "conv_${...}"` to `id: ConversationId` (no default — required parameter)

**Manual verification checklist:**
- [x] `./gradlew assembleDebug` FAILS (callers don't pass `ConversationId`)

---

### Task 8: Update ChatUiState

**File:** `app/src/main/java/com/smartphoneaichat/viewmodel/ChatViewModel.kt`

Change `activeConversationId: String` to `activeConversationId: ConversationId` in `ChatUiState`.

Set a default value of `ConversationId("")` so that `ChatUiState()` can still be constructed without arguments (initially empty state).

**Manual verification checklist:**
- [x] Import added for `ConversationId`
- [ ] Callers that assign `activeConversationId` will need updating (see Task 9)

---

### Task 9: Update ChatViewModel to use IdGenerator + pass IDs

**File:** `app/src/main/java/com/smartphoneaichat/viewmodel/ChatViewModel.kt`

#### 9a: Add IdGenerator dependency

Add a property to `ChatViewModel`:
```kotlin
private val idGenerator: IdGenerator = UuidIdGenerator()
```

Note: Constructor injection happens in Phase 4. For now, instantiate directly (same pattern as `liteRtLmRepository` and others already in the class).

#### 9b: Update all Message constructors

Every `Message(...)` call must now pass `id = idGenerator.generateMessageId()` and `text = MessageText(...)`.

Locations (search for `Message(` in ChatViewModel.kt):
- `init {}` block — mock welcome message (line ~40)
- `sendMessage()` — user message and AI placeholder (lines ~90, ~100)
- `attachImage()` — pending message with attachment (line ~170)

#### 9c: Update all Conversation constructors

Every `Conversation(...)` call must now pass `id = idGenerator.generateConversationId()`.

Locations (search for `Conversation(` in ChatViewModel.kt):
- `init {}` block — mock conversation (line ~36)
- `newConversation()` (line ~433)
- `deleteConversation()` fallback (line ~456)

#### 9d: Update all ID comparisons

Wherever `conversation.id` is compared with `activeConversationId`, no change needed since both are now `ConversationId` (value class, structural equality). Same for `message.id` comparisons.

#### 9e: Update `MessageText` wrapping

Where user input text is used to create messages, wrap with `MessageText(...)`. Where message text is read for comparison/display, use `.value`.

Check all usages:
- `sendMessage()`: `val trimmed = text.trim()` — this now becomes `val trimmed = MessageText(text.trim().take(4096))` or similar. The validation happens in the value object constructor.
- Any place that reads `.text` and uses it as a String — if it's passed to UI or comparison, `.value` may be needed. However since `MessageText` is a string-backed value class, Kotlin auto-boxing may handle `toString()` — but explicit `.value` is safer for concatenation and string operations.

**Manual verification checklist:**
- [x] All `Message(` calls pass `id` and `text` as typed values
- [x] All `Conversation(` calls pass `id` as `ConversationId`
- [x] No compilation errors in ViewModel
- [ ] UI files (ChatScreen, composables) may need `.value` for string operations — verify during Task 10

---

### Task 10: Update UI composables and remaining files

**Search scope:** All `.kt` files under `app/src/main/java/com/smartphoneaichat/ui/` and `MainActivity.kt`

#### What to check:

1. **Where `conversation.id` is used as a String** — any `.id` access on a `Conversation` now returns `ConversationId`. If the value is used as a `key` in Compose or as a `tag`, wrap with `.value` if needed. Simple `toString()` may work but should be verified.

2. **Where `message.id` is used as a String** — same as above, `.id` now returns `MessageId`.

3. **Where `message.text` is used** — now returns `MessageText`. Any string interpolation like `"User said: ${message.text}"` needs `message.text.value`.

4. **`LiteRtLmRepository.kt`** — `sendMessageAsync()` returns `Flow<String>`. This `String` gets collected and concatenated to the AI message text in the ViewModel. The concatenation path now works with `MessageText`. Check the token accumulation logic in `sendMessage()` — it likely does `msg.copy(text = msg.text + token)` which now needs `msg.copy(text = MessageText(msg.text.value + token))`.

5. **`ChatInput.kt`** — `onSend(text: String)` callback. The text comes from user input as a raw `String`. In the ViewModel, it gets wrapped into `MessageText`. No change needed here.

6. **`Sidebar.kt`** — Likely uses `conversation.id` for keys and `conversation.title`. Check if `.id` needs `.value`.

7. **`ChatBubble.kt`** — Uses `message.text` to render. May need `.value`.

8. **`NotificationHost.kt`**, **`ThinkingSection.kt`**, **`ModelLoaderDialog.kt`**, **`ModelSelectorDialog.kt`** — unlikely to use message/conversation IDs, but verify.

#### Key files to read and update:

| File | What to check |
|------|--------------|
| `ui/screens/ChatScreen.kt` | `conversation.id` used for LazyColumn keys, `activeConversationId` comparison, `message.text` display |
| `ui/components/ChatBubble.kt` | `message.text` rendering, `message.thinkingText`, `message.isStreaming` |
| `ui/components/Sidebar.kt` | `conversation.id`, `conversation.title` |
| `ui/components/ChatInput.kt` | None expected (only `onSend(String)`) |
| `ui/components/ThinkingSection.kt` | `message.thinkingText`, `message.isStreaming` |
| `MainActivity.kt` | None expected |

**Manual verification checklist:**
- [x] `./gradlew assembleDebug` PASSES (build compiles cleanly)
- [x] No raw `String` ID comparisons remain that should use `ConversationId`/`MessageId`

---

### Task 11: Final verification

1. **Compile check:** `./gradlew assembleDebug`
2. **Test suite:** `./gradlew test`
3. **Lint check:** `./gradlew lintDebug` (if lint is configured)
4. **Manual check:** Verify all imports are correct — `domain/` packages must NOT import `android.*`, `com.google.ai.edge.litertlm.*`

**Quality Gate validation:**
- [ ] `./gradlew assembleDebug` passes with zero errors
- [ ] `./gradlew test` passes with all domain + data-id tests green
- [ ] `Message` no longer auto-generates its own ID (no `System.currentTimeMillis()` in `Message.kt`)
- [ ] `Conversation` no longer auto-generates its own ID (no `System.currentTimeMillis()` in `Conversation.kt`)
- [ ] `IdGenerator` is used for all ID creation
- [ ] Value objects enforce invariants (`MessageText` max 4096, IDs non-blank)
- [ ] No Android imports in `domain/` package

---

## Files that must NOT be created

- Do NOT move files between packages (that's Phase 1)
- Do NOT create the Full Architecture's package structure (`domain/model/`, `data/engine/`, etc.) beyond what's needed for value objects + IdGenerator
- Do NOT create interfaces for `InferenceEngine`, `ModelFileManager`, `ConversationRepository` (that's Phase 1)
- Do NOT convert `Conversation` to an aggregate root with private constructor (that's Phase 2)
- Do NOT extract use cases (that's Phase 3)
- Do NOT create `AppContainer` or `ChatViewModelFactory` (that's Phase 4)

## Files that MUST be created

| File | Purpose |
|------|---------|
| `domain/model/value/MessageId.kt` | Value object for message IDs |
| `domain/model/value/ConversationId.kt` | Value object for conversation IDs |
| `domain/model/value/MessageText.kt` | Value object for message text |
| `domain/repository/IdGenerator.kt` | ID generation interface |
| `data/id/UuidIdGenerator.kt` | UUID-based ID generator |
| `app/src/test/.../domain/model/value/ValueObjectTest.kt` | Tests for all 3 value objects |
| `app/src/test/.../data/id/UuidIdGeneratorTest.kt` | Tests for UuidIdGenerator |
| `app/src/test/.../data/id/FakeIdGenerator.kt` | Test fake (for future ViewModel tests) |

## Files that MUST be modified

| File | Changes |
|------|---------|
| `app/build.gradle.kts` | Add test dependencies + JUnit Platform config |
| `app/src/main/.../model/Message.kt` | `id: MessageId` (no default), `text: MessageText` |
| `app/src/main/.../model/Conversation.kt` | `id: ConversationId` (no default) |
| `app/src/main/.../viewmodel/ChatViewModel.kt` | Add `IdGenerator`, pass IDs to constructors, update `ChatUiState.activeConversationId` type, token accumulation with `MessageText` |
| `app/src/main/.../ui/screens/ChatScreen.kt` | Potentially `.value` on `.id`/`.text` accesses |
| `app/src/main/.../ui/components/ChatBubble.kt` | Potentially `.value` on `.text` |
| `app/src/main/.../ui/components/Sidebar.kt` | Potentially `.value` on `.id` |
| `app/src/main/.../ui/components/ThinkingSection.kt` | Potentially `.value` on `.thinkingText` |

## Dependency direction check

After completion, verify:
- `domain/*` imports contain **NO** `android.*`, `com.google.ai.edge.litertlm.*`, `androidx.compose.*`, `com.smartphoneaichat.data.*`, `com.smartphoneaichat.presentation.*`, `com.smartphoneaichat.ui.*`
- `data/*` imports contain **NO** `androidx.compose.*`, `com.smartphoneaichat.presentation.*`, `com.smartphoneaichat.ui.*`
- All value objects use `@JvmInline value class` (zero runtime overhead)

---

## Estimated effort

~2-3 hours of focused work. Most time spent on updating references across files and ensuring compilation.