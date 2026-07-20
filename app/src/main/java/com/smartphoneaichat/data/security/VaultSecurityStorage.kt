package com.smartphoneaichat.data.security

data class StoredCredential(
    val username: String,
    val saltBase64: String,
    val verifierBase64: String,
    val iterations: Int,
)

data class StoredKeyEnvelope(
    val version: Int,
    val nonceBase64: String,
    val ciphertextBase64: String,
)

data class StoredVault(
    val credential: StoredCredential,
    val keyEnvelope: StoredKeyEnvelope,
)

/** Persists only credential verification material and a wrapped key envelope. */
interface VaultSecurityStorage {
    fun load(): StoredVault?
    fun save(vault: StoredVault)
    fun clear()
}

interface RandomBytes {
    fun next(size: Int): ByteArray
}

interface AuthenticationGateway {
    fun createCredential(username: String, password: CharArray): StoredCredential
    fun authenticate(username: String, password: CharArray, credential: StoredCredential): Boolean
}

interface VaultKeyEnvelopeCipher {
    fun wrap(dataEncryptionKey: ByteArray): StoredKeyEnvelope
    fun unwrap(envelope: StoredKeyEnvelope): ByteArray
    fun destroyWrappingKey()
}

class VaultWrappingKeyInvalidatedException(cause: Throwable? = null) : Exception(cause)
class VaultKeyEnvelopeUnavailableException(cause: Throwable? = null) : Exception(cause)
