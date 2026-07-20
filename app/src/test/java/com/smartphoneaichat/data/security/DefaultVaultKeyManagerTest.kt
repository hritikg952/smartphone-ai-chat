package com.smartphoneaichat.data.security

import com.smartphoneaichat.domain.model.VaultAccessResult
import com.smartphoneaichat.domain.model.VaultSessionState
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class DefaultVaultKeyManagerTest {

    @Test
    fun rejectedAccess_attemptAlwaysClearsCallerPassword() {
        val fixture = createProvisionedFixture()
        val duplicatePassword = "duplicate-pass".toCharArray()
        val missingVaultPassword = "missing-pass".toCharArray()

        assertEquals(
            VaultAccessResult.AlreadyExists,
            fixture.manager.createVault("owner", duplicatePassword),
        )
        fixture.manager.destroy()
        assertEquals(
            VaultAccessResult.Unavailable,
            fixture.manager.unlock("owner", missingVaultPassword),
        )

        assertArrayEquals(CharArray(duplicatePassword.size) { '\u0000' }, duplicatePassword)
        assertArrayEquals(CharArray(missingVaultPassword.size) { '\u0000' }, missingVaultPassword)
    }

    @Test
    fun createVault_wrapsRandomDataKeyAndUnlocksSession() {
        val generatedKey = ByteArray(32) { index -> (index + 1).toByte() }
        val storage = TestVaultSecurityStorage()
        val keyEnvelopeCipher = RecordingKeyEnvelopeCipher()
        val session = DefaultVaultSession()
        val manager = DefaultVaultKeyManager(
            storage = storage,
            authenticationGateway = Pbkdf2AuthenticationGateway(
                randomBytes = FixedRandomBytes(ByteArray(16) { 7 }),
                iterations = 10,
            ),
            keyEnvelopeCipher = keyEnvelopeCipher,
            randomBytes = FixedRandomBytes(generatedKey),
            session = session,
        )

        val result = manager.createVault("owner", "vault-pass".toCharArray())

        assertEquals(VaultAccessResult.Success, result)
        assertEquals(VaultSessionState.Unlocked, session.state.value)
        assertArrayEquals(generatedKey, keyEnvelopeCipher.lastPlaintextKey)
        val storedVault = storage.load()
        assertNotNull(storedVault)
        assertEquals("wrapped-key", storedVault?.keyEnvelope?.ciphertextBase64)
        assertFalse(storedVault?.credential?.verifierBase64.orEmpty().contains("vault-pass"))
    }

    @Test
    fun unlock_rejectsWrongCredentialsAndAcceptsCorrectCredentialsAfterRestart() {
        val generatedKey = ByteArray(32) { index -> (index + 1).toByte() }
        val storage = TestVaultSecurityStorage()
        val keyEnvelopeCipher = RecordingKeyEnvelopeCipher()
        val authenticationGateway = Pbkdf2AuthenticationGateway(
            randomBytes = FixedRandomBytes(ByteArray(16) { 7 }),
            iterations = 10,
        )
        DefaultVaultKeyManager(
            storage = storage,
            authenticationGateway = authenticationGateway,
            keyEnvelopeCipher = keyEnvelopeCipher,
            randomBytes = FixedRandomBytes(generatedKey),
            session = DefaultVaultSession(),
        ).createVault("owner", "vault-pass".toCharArray())
        val restartedSession = DefaultVaultSession()
        val restartedManager = DefaultVaultKeyManager(
            storage = storage,
            authenticationGateway = authenticationGateway,
            keyEnvelopeCipher = keyEnvelopeCipher,
            randomBytes = FixedRandomBytes(generatedKey),
            session = restartedSession,
        )

        assertEquals(VaultSessionState.Locked, restartedSession.state.value)
        assertEquals(
            VaultAccessResult.InvalidCredentials,
            restartedManager.unlock("owner", "wrong-pass".toCharArray()),
        )
        assertEquals(VaultSessionState.Locked, restartedSession.state.value)

        assertEquals(
            VaultAccessResult.Success,
            restartedManager.unlock("owner", "vault-pass".toCharArray()),
        )
        assertEquals(VaultSessionState.Unlocked, restartedSession.state.value)
    }

    @Test
    fun destroy_clearsEnvelopeWrappingKeyAndSession() {
        val fixture = createProvisionedFixture()

        fixture.manager.destroy()

        assertEquals(null, fixture.storage.load())
        assertEquals(true, fixture.keyEnvelopeCipher.wasDestroyed)
        assertEquals(VaultSessionState.Destroyed, fixture.session.state.value)
        assertEquals(
            VaultAccessResult.Unavailable,
            fixture.manager.unlock("owner", "vault-pass".toCharArray()),
        )
    }

    @Test
    fun unlock_whenWrappingKeyIsInvalidated_failsClosedWithoutDeletingEnvelope() {
        val fixture = createProvisionedFixture()
        fixture.session.lock()
        fixture.keyEnvelopeCipher.failUnwrapWithInvalidation = true

        val result = fixture.manager.unlock("owner", "vault-pass".toCharArray())

        assertEquals(VaultAccessResult.KeyInvalidated, result)
        assertEquals(VaultSessionState.Invalidated, fixture.session.state.value)
        assertNotNull(fixture.storage.load())
    }

    @Test
    fun unlock_whenEnvelopeCannotBeAuthenticated_returnsUnavailableAndStaysLocked() {
        val fixture = createProvisionedFixture()
        fixture.session.lock()
        fixture.keyEnvelopeCipher.failUnwrapAsUnavailable = true

        val result = fixture.manager.unlock("owner", "vault-pass".toCharArray())

        assertEquals(VaultAccessResult.Unavailable, result)
        assertEquals(VaultSessionState.Locked, fixture.session.state.value)
        assertNotNull(fixture.storage.load())
    }

    private fun createProvisionedFixture(): ProvisionedFixture {
        val generatedKey = ByteArray(32) { index -> (index + 1).toByte() }
        val storage = TestVaultSecurityStorage()
        val keyEnvelopeCipher = RecordingKeyEnvelopeCipher()
        val session = DefaultVaultSession()
        val manager = DefaultVaultKeyManager(
            storage = storage,
            authenticationGateway = Pbkdf2AuthenticationGateway(
                randomBytes = FixedRandomBytes(ByteArray(16) { 7 }),
                iterations = 10,
            ),
            keyEnvelopeCipher = keyEnvelopeCipher,
            randomBytes = FixedRandomBytes(generatedKey),
            session = session,
        )
        manager.createVault("owner", "vault-pass".toCharArray())
        return ProvisionedFixture(manager, storage, keyEnvelopeCipher, session)
    }

    private data class ProvisionedFixture(
        val manager: DefaultVaultKeyManager,
        val storage: TestVaultSecurityStorage,
        val keyEnvelopeCipher: RecordingKeyEnvelopeCipher,
        val session: DefaultVaultSession,
    )

    private class FixedRandomBytes(
        private val bytes: ByteArray,
    ) : RandomBytes {
        override fun next(size: Int): ByteArray {
            assertEquals(size, bytes.size)
            return bytes.copyOf()
        }
    }

    private class RecordingKeyEnvelopeCipher : VaultKeyEnvelopeCipher {
        var lastPlaintextKey: ByteArray? = null
        var wasDestroyed: Boolean = false
        var failUnwrapWithInvalidation: Boolean = false
        var failUnwrapAsUnavailable: Boolean = false

        override fun wrap(dataEncryptionKey: ByteArray): StoredKeyEnvelope {
            lastPlaintextKey = dataEncryptionKey.copyOf()
            return StoredKeyEnvelope(
                version = 1,
                nonceBase64 = "wrapped-nonce",
                ciphertextBase64 = "wrapped-key",
            )
        }

        override fun unwrap(envelope: StoredKeyEnvelope): ByteArray {
            if (failUnwrapWithInvalidation) throw VaultWrappingKeyInvalidatedException()
            if (failUnwrapAsUnavailable) throw VaultKeyEnvelopeUnavailableException()
            return requireNotNull(lastPlaintextKey).copyOf()
        }

        override fun destroyWrappingKey() {
            wasDestroyed = true
        }
    }

    private class TestVaultSecurityStorage : VaultSecurityStorage {
        private var storedVault: StoredVault? = null

        override fun load(): StoredVault? = storedVault

        override fun save(vault: StoredVault) {
            storedVault = vault
        }

        override fun clear() {
            storedVault = null
        }
    }
}
