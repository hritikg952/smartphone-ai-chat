package com.smartphoneaichat.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey

/** Authenticates the public emergency projection without participating in vault-key lifecycle. */
interface EmergencyCardIntegritySigner {
    fun sign(bytes: ByteArray): ByteArray
    fun verify(bytes: ByteArray, signature: ByteArray): Boolean
}

class AndroidKeystoreEmergencyCardIntegritySigner : EmergencyCardIntegritySigner {
    override fun sign(bytes: ByteArray): ByteArray = mac().doFinal(bytes)

    override fun verify(bytes: ByteArray, signature: ByteArray): Boolean =
        java.security.MessageDigest.isEqual(sign(bytes), signature)

    private fun mac(): Mac = Mac.getInstance(MAC_ALGORITHM).apply { init(loadOrCreateKey()) }

    private fun loadOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(MAC_ALGORITHM, ANDROID_KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY,
                )
                    .setDigests(KeyProperties.DIGEST_SHA256)
                    .setUserAuthenticationRequired(false)
                    .build(),
            )
        }.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val MAC_ALGORITHM = "HmacSHA256"
        const val KEY_ALIAS = "personal_health_vault_emergency_card_integrity_v1"
    }
}
