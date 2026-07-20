package com.smartphoneaichat.data.security

import com.smartphoneaichat.domain.model.VaultSessionState
import com.smartphoneaichat.domain.repository.VaultSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DefaultVaultSession : VaultSession {
    private var dataEncryptionKey: ByteArray? = null
    private val _state = MutableStateFlow(VaultSessionState.Absent)
    override val state: StateFlow<VaultSessionState> = _state.asStateFlow()

    internal fun markVaultPresent() {
        if (_state.value == VaultSessionState.Absent) {
            _state.value = VaultSessionState.Locked
        }
    }

    internal fun unlock(key: ByteArray) {
        clearKey()
        dataEncryptionKey = key.copyOf()
        _state.value = VaultSessionState.Unlocked
    }

    internal fun <T> withDataEncryptionKey(block: (ByteArray) -> T): T? {
        val keyCopy = dataEncryptionKey?.copyOf() ?: return null
        return try {
            block(keyCopy)
        } finally {
            keyCopy.fill(0)
        }
    }

    override fun lock() {
        clearKey()
        if (_state.value != VaultSessionState.Absent && _state.value != VaultSessionState.Destroyed) {
            _state.value = VaultSessionState.Locked
        }
    }

    internal fun invalidate() {
        clearKey()
        _state.value = VaultSessionState.Invalidated
    }

    internal fun destroy() {
        clearKey()
        _state.value = VaultSessionState.Destroyed
    }

    private fun clearKey() {
        dataEncryptionKey?.fill(0)
        dataEncryptionKey = null
    }
}
