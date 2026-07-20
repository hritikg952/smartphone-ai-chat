package com.smartphoneaichat.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.GeneralSecurityException
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Wraps the vault DEK with a non-exportable AES key held by Android Keystore. */
class AndroidKeystoreVaultKeyEnvelopeCipher(
    private val alias: String = DEFAULT_ALIAS,
) : VaultKeyEnvelopeCipher {

    override fun wrap(dataEncryptionKey: ByteArray): StoredKeyEnvelope {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateWrappingKey())
            cipher.updateAAD(ENVELOPE_AAD)
            StoredKeyEnvelope(
                version = CURRENT_VERSION,
                nonceBase64 = Base64.getEncoder().encodeToString(cipher.iv),
                ciphertextBase64 = Base64.getEncoder().encodeToString(
                    cipher.doFinal(dataEncryptionKey),
                ),
            )
        } catch (error: GeneralSecurityException) {
            throw VaultKeyEnvelopeUnavailableException(error)
        }
    }

    override fun unwrap(envelope: StoredKeyEnvelope): ByteArray {
        return try {
            if (envelope.version != CURRENT_VERSION) {
                throw IllegalArgumentException("Unsupported vault key envelope version")
            }
            val key = loadWrappingKey() ?: throw VaultWrappingKeyInvalidatedException()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                key,
                GCMParameterSpec(
                    TAG_BITS,
                    Base64.getDecoder().decode(envelope.nonceBase64),
                ),
            )
            cipher.updateAAD(ENVELOPE_AAD)
            cipher.doFinal(Base64.getDecoder().decode(envelope.ciphertextBase64))
        } catch (error: KeyPermanentlyInvalidatedException) {
            throw VaultWrappingKeyInvalidatedException(error)
        } catch (error: GeneralSecurityException) {
            throw VaultKeyEnvelopeUnavailableException(error)
        } catch (error: IllegalArgumentException) {
            throw VaultKeyEnvelopeUnavailableException(error)
        }
    }

    override fun destroyWrappingKey() {
        keyStore().deleteEntry(alias)
    }

    fun hasWrappingKey(): Boolean = keyStore().containsAlias(alias)

    private fun getOrCreateWrappingKey(): SecretKey = loadWrappingKey() ?: run {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_BITS)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        generator.generateKey()
    }

    private fun loadWrappingKey(): SecretKey? =
        keyStore().getKey(alias, null) as? SecretKey

    private fun keyStore(): KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val DEFAULT_ALIAS = "personal_health_vault_wrapping_key_v1"
        const val CURRENT_VERSION = 1
        const val KEY_BITS = 256
        const val TAG_BITS = 128
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        val ENVELOPE_AAD = "personal-health-vault:key-envelope:v1".encodeToByteArray()
    }
}
