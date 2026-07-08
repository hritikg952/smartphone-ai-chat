package com.smartphoneaichat.domain.model.value

@JvmInline
value class ConversationId(val value: String) {
    init {
        require(value.isNotBlank()) { "ConversationId must not be blank" }
    }
}
