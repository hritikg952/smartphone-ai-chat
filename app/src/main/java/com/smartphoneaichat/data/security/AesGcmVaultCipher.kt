package com.smartphoneaichat.data.security

import com.smartphoneaichat.domain.model.VaultAssociatedData
import com.smartphoneaichat.domain.model.VaultCryptoResult
import com.smartphoneaichat.domain.model.VaultEncryptedPayload
import com.smartphoneaichat.domain.repository.VaultCipher
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/** AES-256-GCM record cipher with versioned, profile-scoped associated data. */
class AesGcmVaultCipher(
    private val session: DefaultVaultSession,
    private val randomBytes: RandomBytes,
) : VaultCipher {

    override fun encrypt(
        plaintext: ByteArray,
        associatedData: VaultAssociatedData,
    ): VaultCryptoResult = session.withDataEncryptionKey { key ->
        val nonce = randomBytes.next(NONCE_BYTES)
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.ENCRYPT_MODE,
                SecretKeySpec(key, "AES"),
                GCMParameterSpec(TAG_BITS, nonce),
            )
            cipher.updateAAD(encodeAssociatedData(associatedData))
            VaultCryptoResult.Encrypted(
                VaultEncryptedPayload(
                    version = CURRENT_VERSION,
                    nonceBase64 = Base64.getEncoder().encodeToString(nonce),
                    ciphertextBase64 = Base64.getEncoder().encodeToString(cipher.doFinal(plaintext)),
                ),
            )
        } catch (_: GeneralSecurityException) {
            VaultCryptoResult.Unavailable
        } finally {
            nonce.fill(0)
        }
    } ?: VaultCryptoResult.Locked

    override fun decrypt(
        payload: VaultEncryptedPayload,
        associatedData: VaultAssociatedData,
    ): VaultCryptoResult {
        if (payload.version != CURRENT_VERSION) return VaultCryptoResult.UnsupportedVersion
        return session.withDataEncryptionKey { key ->
            try {
                val nonce = Base64.getDecoder().decode(payload.nonceBase64)
                if (nonce.size != NONCE_BYTES) return@withDataEncryptionKey VaultCryptoResult.InvalidCiphertext
                try {
                    val cipher = Cipher.getInstance(TRANSFORMATION)
                    cipher.init(
                        Cipher.DECRYPT_MODE,
                        SecretKeySpec(key, "AES"),
                        GCMParameterSpec(TAG_BITS, nonce),
                    )
                    cipher.updateAAD(encodeAssociatedData(associatedData))
                    VaultCryptoResult.Plaintext(
                        cipher.doFinal(Base64.getDecoder().decode(payload.ciphertextBase64)),
                    )
                } finally {
                    nonce.fill(0)
                }
            } catch (_: IllegalArgumentException) {
                VaultCryptoResult.InvalidCiphertext
            } catch (_: GeneralSecurityException) {
                VaultCryptoResult.InvalidCiphertext
            }
        } ?: VaultCryptoResult.Locked
    }

    private fun encodeAssociatedData(value: VaultAssociatedData): ByteArray {
        val profileBytes = value.profileId.toByteArray(StandardCharsets.UTF_8)
        val recordBytes = value.recordId.toByteArray(StandardCharsets.UTF_8)
        return ByteBuffer.allocate(
            Int.SIZE_BYTES * 4 + profileBytes.size + recordBytes.size,
        )
            .putInt(CURRENT_VERSION)
            .putInt(profileBytes.size)
            .put(profileBytes)
            .putInt(recordBytes.size)
            .put(recordBytes)
            .putInt(value.schemaVersion)
            .array()
    }

    private companion object {
        const val CURRENT_VERSION = 1
        const val NONCE_BYTES = 12
        const val TAG_BITS = 128
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
