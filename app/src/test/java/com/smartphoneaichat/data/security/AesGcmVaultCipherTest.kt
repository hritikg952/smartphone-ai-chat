package com.smartphoneaichat.data.security

import com.smartphoneaichat.domain.model.VaultAssociatedData
import com.smartphoneaichat.domain.model.VaultCryptoResult
import com.smartphoneaichat.domain.model.VaultEncryptedPayload
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.util.Base64

class AesGcmVaultCipherTest {

    private val associatedData = VaultAssociatedData(
        profileId = "profile-1",
        recordId = "record-1",
        schemaVersion = 1,
    )

    @Test
    fun encryptAndDecrypt_requireUnlockedSessionAndRoundTripWithMatchingMetadata() {
        val session = DefaultVaultSession()
        val cipher = AesGcmVaultCipher(
            session = session,
            randomBytes = FixedRandomBytes(ByteArray(12) { 5 }),
        )

        assertEquals(
            VaultCryptoResult.Locked,
            cipher.encrypt("sensitive".encodeToByteArray(), associatedData),
        )

        session.unlock(ByteArray(32) { 9 })
        val encrypted = assertInstanceOf(
            VaultCryptoResult.Encrypted::class.java,
            cipher.encrypt("sensitive".encodeToByteArray(), associatedData),
        ).payload
        val decrypted = assertInstanceOf(
            VaultCryptoResult.Plaintext::class.java,
            cipher.decrypt(encrypted, associatedData),
        )

        assertArrayEquals("sensitive".encodeToByteArray(), decrypted.bytes)
    }

    @Test
    fun decrypt_rejectsWrongMetadataTamperingAndUnsupportedVersion() {
        val session = DefaultVaultSession().apply { unlock(ByteArray(32) { 9 }) }
        val cipher = AesGcmVaultCipher(
            session = session,
            randomBytes = FixedRandomBytes(ByteArray(12) { 5 }),
        )
        val encrypted = (cipher.encrypt(
            "sensitive".encodeToByteArray(),
            associatedData,
        ) as VaultCryptoResult.Encrypted).payload

        assertEquals(
            VaultCryptoResult.InvalidCiphertext,
            cipher.decrypt(encrypted, associatedData.copy(recordId = "record-2")),
        )
        val tamperedBytes = Base64.getDecoder().decode(encrypted.ciphertextBase64)
        tamperedBytes[0] = (tamperedBytes[0].toInt() xor 1).toByte()
        val tampered = encrypted.copy(
            ciphertextBase64 = Base64.getEncoder().encodeToString(tamperedBytes),
        )
        assertEquals(
            VaultCryptoResult.InvalidCiphertext,
            cipher.decrypt(tampered, associatedData),
        )
        assertEquals(
            VaultCryptoResult.UnsupportedVersion,
            cipher.decrypt(
                VaultEncryptedPayload(
                    version = 99,
                    nonceBase64 = encrypted.nonceBase64,
                    ciphertextBase64 = encrypted.ciphertextBase64,
                ),
                associatedData,
            ),
        )
    }

    private class FixedRandomBytes(
        private val bytes: ByteArray,
    ) : RandomBytes {
        override fun next(size: Int): ByteArray {
            assertEquals(size, bytes.size)
            return bytes.copyOf()
        }
    }
}
