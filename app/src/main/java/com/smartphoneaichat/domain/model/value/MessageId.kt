package com.smartphoneaichat.domain.model.value

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class MessageId(val value: String) : Parcelable {
    init {
        require(value.isNotBlank()) { "MessageId must not be blank" }
    }
}
