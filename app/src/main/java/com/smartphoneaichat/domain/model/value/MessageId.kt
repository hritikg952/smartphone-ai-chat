package com.smartphoneaichat.domain.model.value

@JvmInline
value class MessageId(val value: String) {
    init {
        require(value.isNotBlank()) { "MessageId must not be blank" }
    }
}
