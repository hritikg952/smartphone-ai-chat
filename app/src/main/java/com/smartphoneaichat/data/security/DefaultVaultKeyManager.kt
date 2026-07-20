package com.smartphoneaichat.data.security

import com.smartphoneaichat.domain.model.VaultAccessResult
import com.smartphoneaichat.domain.repository.VaultKeyManager

class DefaultVaultKeyManager(
    private val storage: VaultSecurityStorage,
    private val authenticationGateway: AuthenticationGateway,
    private val keyEnvelopeCipher: VaultKeyEnvelopeCipher,
    private val randomBytes: RandomBytes,
    private val session: DefaultVaultSession,
) : VaultKeyManager {

    init {
        if (storage.load() != null) session.markVaultPresent()
    }

    override fun createVault(username: String, password: CharArray): VaultAccessResult {
        return try {
            if (username.isBlank() || password.isEmpty()) return VaultAccessResult.InvalidCredentials
            if (storage.load() != null) return VaultAccessResult.AlreadyExists

            val dataEncryptionKey = randomBytes.next(DATA_ENCRYPTION_KEY_BYTES)
            try {
                try {
                    val storedVault = StoredVault(
                        credential = authenticationGateway.createCredential(username, password),
                        keyEnvelope = keyEnvelopeCipher.wrap(dataEncryptionKey),
                    )
                    storage.save(storedVault)
                    session.unlock(dataEncryptionKey)
                    VaultAccessResult.Success
                } catch (_: VaultKeyEnvelopeUnavailableException) {
                    keyEnvelopeCipher.destroyWrappingKey()
                    VaultAccessResult.Unavailable
                }
            } finally {
                dataEncryptionKey.fill(0)
            }
        } finally {
            password.fill('\u0000')
        }
    }

    override fun unlock(username: String, password: CharArray): VaultAccessResult {
        return try {
            val storedVault = storage.load() ?: return VaultAccessResult.Unavailable
            if (!authenticationGateway.authenticate(username, password, storedVault.credential)) {
                VaultAccessResult.InvalidCredentials
            } else {
                try {
                    val dataEncryptionKey = keyEnvelopeCipher.unwrap(storedVault.keyEnvelope)
                    try {
                        session.unlock(dataEncryptionKey)
                        VaultAccessResult.Success
                    } finally {
                        dataEncryptionKey.fill(0)
                    }
                } catch (_: VaultWrappingKeyInvalidatedException) {
                    session.invalidate()
                    VaultAccessResult.KeyInvalidated
                } catch (_: VaultKeyEnvelopeUnavailableException) {
                    VaultAccessResult.Unavailable
                }
            }
        } catch (_: IllegalArgumentException) {
            VaultAccessResult.Unavailable
        } finally {
            password.fill('\u0000')
        }
    }

    override fun destroy() {
        session.lock()
        keyEnvelopeCipher.destroyWrappingKey()
        storage.clear()
        session.destroy()
    }

    private companion object {
        const val DATA_ENCRYPTION_KEY_BYTES = 32
    }
}
