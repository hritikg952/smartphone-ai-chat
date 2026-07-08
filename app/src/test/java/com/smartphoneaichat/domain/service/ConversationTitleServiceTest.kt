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
        assertEquals(41, result.length)
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
        val text = "a".repeat(39) + "\uD83D\uDE00"
        val result = ConversationTitleService.generateTitle(MessageText(text))
        assertEquals(41, result.length)
    }
}