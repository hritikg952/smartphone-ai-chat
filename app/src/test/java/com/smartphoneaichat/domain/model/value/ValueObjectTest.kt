package com.smartphoneaichat.domain.model.value

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class MessageIdTest {

    @Test
    fun createWithNonBlankString_succeeds_valueReturned() {
        val id = MessageId("msg-123")
        assertEquals("msg-123", id.value)
    }

    @Test
    fun createWithBlankString_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException::class.java) {
            MessageId("")
        }
    }

    @Test
    fun createWithWhitespaceOnly_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException::class.java) {
            MessageId("   ")
        }
    }

    @Test
    fun twoMessageIdsWithSameValue_areEqual() {
        val a = MessageId("abc")
        val b = MessageId("abc")
        assertEquals(a, b)
    }

    @Test
    fun twoMessageIdsWithDifferentValues_areNotEqual() {
        val a = MessageId("abc")
        val b = MessageId("xyz")
        assertNotEquals(a, b)
    }
}

class ConversationIdTest {

    @Test
    fun createWithNonBlankString_succeeds_valueReturned() {
        val id = ConversationId("conv-123")
        assertEquals("conv-123", id.value)
    }

    @Test
    fun createWithBlankString_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException::class.java) {
            ConversationId("")
        }
    }

    @Test
    fun createWithWhitespaceOnly_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException::class.java) {
            ConversationId("   ")
        }
    }

    @Test
    fun twoConversationIdsWithSameValue_areEqual() {
        val a = ConversationId("abc")
        val b = ConversationId("abc")
        assertEquals(a, b)
    }

    @Test
    fun twoConversationIdsWithDifferentValues_areNotEqual() {
        val a = ConversationId("abc")
        val b = ConversationId("xyz")
        assertNotEquals(a, b)
    }
}

class MessageTextTest {

    @Test
    fun createWithTextUnder4096Chars_succeeds() {
        val text = MessageText("Hello")
        assertEquals("Hello", text.value)
    }

    @Test
    fun createWithTextExactly4096Chars_succeeds() {
        val text = "a".repeat(4096)
        val msgText = MessageText(text)
        assertEquals(text, msgText.value)
    }

    @Test
    fun createWithTextExactly4097Chars_throwsIllegalArgumentException() {
        val text = "a".repeat(4097)
        assertThrows(IllegalArgumentException::class.java) {
            MessageText(text)
        }
    }

    @Test
    fun createWithTextOf10000Chars_throwsIllegalArgumentException() {
        val text = "a".repeat(10000)
        assertThrows(IllegalArgumentException::class.java) {
            MessageText(text)
        }
    }

    @Test
    fun emptyStringIsValid() {
        val text = MessageText("")
        assertEquals("", text.value)
    }
}