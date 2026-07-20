package com.smartphoneaichat.domain.repository

import com.smartphoneaichat.domain.model.VaultAccessResult
import com.smartphoneaichat.domain.model.VaultAssociatedData
import com.smartphoneaichat.domain.model.VaultCryptoResult
import com.smartphoneaichat.domain.model.VaultEncryptedPayload
import com.smartphoneaichat.domain.model.VaultSessionState
import kotlinx.coroutines.flow.StateFlow

/** Creates and unlocks the vault's data-encryption key without exposing it. */
interface VaultKeyManager {
    fun createVault(username: String, password: CharArray): VaultAccessResult
    fun unlock(username: String, password: CharArray): VaultAccessResult
    fun destroy()
}

/** Process-local authorization boundary for access to plaintext vault data. */
interface VaultSession {
    val state: StateFlow<VaultSessionState>
    fun lock()
}

/** Authenticated encryption boundary for profile-scoped vault content. */
interface VaultCipher {
    fun encrypt(plaintext: ByteArray, associatedData: VaultAssociatedData): VaultCryptoResult
    fun decrypt(payload: VaultEncryptedPayload, associatedData: VaultAssociatedData): VaultCryptoResult
}
