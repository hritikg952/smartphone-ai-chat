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
        assertTrue(conv.isEmpty)
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