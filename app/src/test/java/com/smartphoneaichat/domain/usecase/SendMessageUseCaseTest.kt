package com.smartphoneaichat.domain.usecase

import com.smartphoneaichat.data.engine.FakeInferenceEngine
import com.smartphoneaichat.data.id.FakeIdGenerator
import com.smartphoneaichat.domain.model.ChatRole
import com.smartphoneaichat.domain.model.Conversation
import com.smartphoneaichat.domain.model.Message
import com.smartphoneaichat.domain.model.value.MessageText
import kotlinx.coroutines.test.runTest
import app.cash.turbine.test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SendMessageUseCaseTest {

    private val idGen = FakeIdGenerator()
    private val fakeEngine = FakeInferenceEngine()
    private val useCase = SendMessageUseCase(fakeEngine, idGen)

    @Test
    fun emitsInitialStateWithUserAndAiMessages() = runTest {
        val conv = Conversation.create(idGen.generateConversationId())
        val flow = useCase(conv, MessageText("Hello"))

        flow.test {
            val first = awaitItem()
            assertEquals(2, first.messages.size)
            assertEquals(ChatRole.USER, first.messages[0].role)
            assertEquals("Hello", first.messages[0].text.value)
            assertEquals(ChatRole.AI, first.messages[1].role)
            assertTrue(first.messages[1].isStreaming)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun initialEmissionAiMessageHasEmptyText() = runTest {
        val conv = Conversation.create(idGen.generateConversationId())
        val flow = useCase(conv, MessageText("Hi"))

        flow.test {
            val first = awaitItem()
            val aiMsg = first.messages.last()
            assertTrue(aiMsg.text.value.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun initialEmissionAiMessageHasThinkingText() = runTest {
        val conv = Conversation.create(idGen.generateConversationId())
        val flow = useCase(conv, MessageText("Hi"))

        flow.test {
            val first = awaitItem()
            val aiMsg = first.messages.last()
            assertTrue(aiMsg.thinkingText.isNotBlank())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun accumulatesTokensInAiMessageText() = runTest {
        val engine = FakeInferenceEngine(tokens = listOf("A", "B", "C"))
        val useCase = SendMessageUseCase(engine, idGen)
        val conv = Conversation.create(idGen.generateConversationId())

        useCase(conv, MessageText("Hi")).test {
            val first = awaitItem()
            assertEquals("", first.messages[1].text.value)

            assertEquals("A", awaitItem().messages[1].text.value)
            assertEquals("AB", awaitItem().messages[1].text.value)
            assertEquals("ABC", awaitItem().messages[1].text.value)
            val final = awaitItem()
            assertFalse(final.messages.last().isStreaming)
            awaitComplete()
        }
    }

    @Test
    fun finalEmissionMarksAiMessageNotStreaming() = runTest {
        val engine = FakeInferenceEngine(tokens = listOf("X"))
        val useCase = SendMessageUseCase(engine, idGen)
        val conv = Conversation.create(idGen.generateConversationId())

        useCase(conv, MessageText("Hi")).test {
            awaitItem()
            awaitItem()
            val final = awaitItem()
            val aiMsg = final.messages.find { it.role == ChatRole.AI }!!
            assertFalse(aiMsg.isStreaming)
            awaitComplete()
        }
    }

    @Test
    fun finalEmissionHasAllTokensAccumulated() = runTest {
        val engine = FakeInferenceEngine(tokens = listOf("Hello", " ", "World"))
        val useCase = SendMessageUseCase(engine, idGen)
        val conv = Conversation.create(idGen.generateConversationId())

        useCase(conv, MessageText("Hi")).test {
            awaitItem()
            awaitItem()
            awaitItem()
            awaitItem()
            val final = awaitItem()
            assertEquals("Hello World", final.messages.last().text.value)
            assertFalse(final.messages.last().isStreaming)
            awaitComplete()
        }
    }

    @Test
    fun autoTitlesConversationOnFirstMessageWhenTitleIsNewChat() = runTest {
        val conv = Conversation.create(idGen.generateConversationId())
        val flow = useCase(conv, MessageText("Tell me about Kotlin"))

        flow.test {
            val first = awaitItem()
            assertEquals("Tell me about Kotlin", first.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun autoTitle_truncatesLongFirstMessage() = runTest {
        val conv = Conversation.create(idGen.generateConversationId())
        val longText = "a".repeat(100)
        val flow = useCase(conv, MessageText(longText))

        flow.test {
            val first = awaitItem()
            assertEquals("a".repeat(40) + "\u2026", first.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun doesNotAutoTitleWhenTitleIsNotNewChat() = runTest {
        val conv = Conversation.create(idGen.generateConversationId(), "Existing Title")
        val flow = useCase(conv, MessageText("Hello"))

        flow.test {
            val first = awaitItem()
            assertEquals("Existing Title", first.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun doesNotAutoTitleWhenConversationHasMultipleMessages() = runTest {
        val msg1 = Message(id = idGen.generateMessageId(), role = ChatRole.USER, text = MessageText("A"))
        val msg2 = Message(id = idGen.generateMessageId(), role = ChatRole.AI, text = MessageText("B"))
        val conv = Conversation.create(idGen.generateConversationId())
            .addMessage(msg1)
            .addMessage(msg2)

        val flow = useCase(conv, MessageText("Third message"))

        flow.test {
            val first = awaitItem()
            assertEquals("New Chat", first.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun propagatesInferenceException_toCaller() = runTest {
        val engine = FakeInferenceEngine(shouldThrow = true, throwAfterTokens = 0)
        val useCase = SendMessageUseCase(engine, idGen)
        val conv = Conversation.create(idGen.generateConversationId())

        val items = mutableListOf<Conversation>()
        var caught: RuntimeException? = null
        try {
            useCase(conv, MessageText("Hi")).collect { items.add(it) }
        } catch (e: RuntimeException) {
            caught = e
        }
        assertNotNull(caught)
        assertEquals("Simulated inference failure", caught!!.message)
    }

    @Test
    fun userAndAiMessagesHaveDifferentIds() = runTest {
        val conv = Conversation.create(idGen.generateConversationId())
        useCase(conv, MessageText("Hi")).test {
            val initial = awaitItem()
            val ids = initial.messages.map { it.id }.toSet()
            assertEquals(2, ids.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun originalConversationIsNotMutated() = runTest {
        val conv = Conversation.create(idGen.generateConversationId())
        val originalMessageCount = conv.messages.size
        val originalTitle = conv.title

        useCase(conv, MessageText("Hi")).test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(originalMessageCount, conv.messages.size)
        assertEquals(originalTitle, conv.title)
    }

    @Test
    fun userMessagePreservesInputText() = runTest {
        val conv = Conversation.create(idGen.generateConversationId())
        useCase(conv, MessageText("What is Kotlin?")).test {
            val initial = awaitItem()
            val userMsg = initial.messages.find { it.role == ChatRole.USER }!!
            assertEquals("What is Kotlin?", userMsg.text.value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aiMessageHasNoAttachment() = runTest {
        val conv = Conversation.create(idGen.generateConversationId())
        useCase(conv, MessageText("Hi")).test {
            val initial = awaitItem()
            val aiMsg = initial.messages.find { it.role == ChatRole.AI }!!
            assertNull(aiMsg.attachment)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun marksStreamingFalse_evenWhenInferenceThrows() = runTest {
        val engine = FakeInferenceEngine(
            tokens = listOf("A"),
            shouldThrow = true,
            throwAfterTokens = 1
        )
        val useCase = SendMessageUseCase(engine, idGen)
        val conv = Conversation.create(idGen.generateConversationId())

        var finalConv: Conversation? = null
        try {
            useCase(conv, MessageText("Hi")).collect { finalConv = it }
        } catch (_: Exception) {
        }
        assertNotNull(finalConv)
        val aiMsg = finalConv!!.messages.last()
        assertEquals("A", aiMsg.text.value)
        assertFalse(aiMsg.isStreaming)
    }
}