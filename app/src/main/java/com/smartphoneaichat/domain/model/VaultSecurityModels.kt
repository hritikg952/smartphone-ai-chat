package com.smartphoneaichat.domain.model

/** Observable lifecycle of the process-local plaintext vault key. */
enum class VaultSessionState {
    Absent,
    Locked,
    Unlocked,
    Invalidated,
    Destroyed,
}

/** Typed result for vault provisioning and authentication operations. */
sealed interface VaultAccessResult {
    data object Success : VaultAccessResult
    data object AlreadyExists : VaultAccessResult
    data object InvalidCredentials : VaultAccessResult
    data object KeyInvalidated : VaultAccessResult
    data object Unavailable : VaultAccessResult
}

/** Stable metadata authenticated with every encrypted record. */
data class VaultAssociatedData(
    val profileId: String,
    val recordId: String,
    val schemaVersion: Int,
) {
    init {
        require(profileId.isNotBlank())
        require(recordId.isNotBlank())
        require(schemaVersion > 0)
    }
}

/** Versioned encrypted payload safe to persist outside the unlocked session. */
data class VaultEncryptedPayload(
    val version: Int,
    val nonceBase64: String,
    val ciphertextBase64: String,
)

sealed interface VaultCryptoResult {
    data class Encrypted(val payload: VaultEncryptedPayload) : VaultCryptoResult
    data class Plaintext(val bytes: ByteArray) : VaultCryptoResult
    data object Locked : VaultCryptoResult
    data object InvalidCiphertext : VaultCryptoResult
    data object UnsupportedVersion : VaultCryptoResult
    data object Unavailable : VaultCryptoResult
}
