package com.smartphoneaichat.data.security

import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class AndroidVaultSecurityAdaptersTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val suffix = UUID.randomUUID().toString()
    private val alias = "health-vault-test-$suffix"
    private val preferencesName = "health-vault-test-$suffix"
    private val keyCipher = AndroidKeystoreVaultKeyEnvelopeCipher(alias)
    private val storage = SharedPreferencesVaultSecurityStorage(context, preferencesName)

    @After
    fun cleanUp() {
        keyCipher.destroyWrappingKey()
        storage.clear()
    }

    @Test
    fun keystoreEnvelopeAndStorage_roundTripWithoutPersistingPlaintextKey() {
        val plaintextKey = ByteArray(32) { index -> (index + 1).toByte() }
        val envelope = keyCipher.wrap(plaintextKey)
        val vault = StoredVault(
            credential = StoredCredential(
                username = "owner",
                saltBase64 = "salt",
                verifierBase64 = "verifier",
                iterations = 310_000,
            ),
            keyEnvelope = envelope,
        )

        storage.save(vault)

        assertEquals(vault, storage.load())
        assertArrayEquals(plaintextKey, keyCipher.unwrap(envelope))
        val rawPreferences = context.getSharedPreferences(preferencesName, 0).all.values
            .joinToString(separator = "|")
        assertFalse(rawPreferences.contains(plaintextKey.joinToString(separator = ",")))

        keyCipher.destroyWrappingKey()
        assertFalse(keyCipher.hasWrappingKey())
        storage.clear()
        assertNull(storage.load())
    }
}
