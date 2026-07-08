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
        repo.delete(idGen.generateConversationId())
    }

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