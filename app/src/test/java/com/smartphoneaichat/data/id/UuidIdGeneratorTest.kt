package com.smartphoneaichat.data.id

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class FakeIdGeneratorTest {

    private val generator = FakeIdGenerator()

    @Test
    fun generateMessageId_returnsNonNull() {
        val id = generator.generateMessageId()
        assertNotNull(id)
    }

    @Test
    fun generateMessageId_returnsUniqueIdsOnSuccessiveCalls() {
        val ids = (1..10).map { generator.generateMessageId() }.toSet()
        assert(ids.size == 10) { "Expected 10 unique IDs but got ${ids.size}" }
    }

    @Test
    fun generateConversationId_returnsNonNull() {
        val id = generator.generateConversationId()
        assertNotNull(id)
    }

    @Test
    fun generateConversationId_returnsUniqueIdsOnSuccessiveCalls() {
        val ids = (1..10).map { generator.generateConversationId() }.toSet()
        assert(ids.size == 10) { "Expected 10 unique IDs but got ${ids.size}" }
    }
}

class UuidIdGeneratorTest {

    private val generator = UuidIdGenerator()

    @Test
    fun generateMessageId_returnsNonNull() {
        val id = generator.generateMessageId()
        assertNotNull(id)
    }

    @Test
    fun generateMessageId_returnsUniqueIdsOnSuccessiveCalls() {
        val ids = (1..10).map { generator.generateMessageId() }.toSet()
        assert(ids.size == 10) { "Expected 10 unique IDs but got ${ids.size}" }
    }

    @Test
    fun generateConversationId_returnsNonNull() {
        val id = generator.generateConversationId()
        assertNotNull(id)
    }

    @Test
    fun generateConversationId_returnsUniqueIdsOnSuccessiveCalls() {
        val ids = (1..10).map { generator.generateConversationId() }.toSet()
        assert(ids.size == 10) { "Expected 10 unique IDs but got ${ids.size}" }
    }
}