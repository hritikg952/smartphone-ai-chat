package com.smartphoneaichat.data.security

import android.content.Context

/** App-private persistence for the versioned wrapped-key and password-verifier records. */
class SharedPreferencesVaultSecurityStorage(
    context: Context,
    preferencesName: String = FILE_NAME,
) : VaultSecurityStorage {
    private val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    override fun load(): StoredVault? {
        if (!preferences.getBoolean(KEY_PRESENT, false)) return null
        return runCatching {
            StoredVault(
                credential = StoredCredential(
                    username = requireNotNull(preferences.getString(KEY_USERNAME, null)),
                    saltBase64 = requireNotNull(preferences.getString(KEY_CREDENTIAL_SALT, null)),
                    verifierBase64 = requireNotNull(preferences.getString(KEY_CREDENTIAL_VERIFIER, null)),
                    iterations = preferences.getInt(KEY_CREDENTIAL_ITERATIONS, 0).also {
                        require(it > 0)
                    },
                ),
                keyEnvelope = StoredKeyEnvelope(
                    version = preferences.getInt(KEY_ENVELOPE_VERSION, 0).also {
                        require(it > 0)
                    },
                    nonceBase64 = requireNotNull(preferences.getString(KEY_ENVELOPE_NONCE, null)),
                    ciphertextBase64 = requireNotNull(
                        preferences.getString(KEY_ENVELOPE_CIPHERTEXT, null),
                    ),
                ),
            )
        }.getOrNull()
    }

    override fun save(vault: StoredVault) {
        check(
            preferences.edit()
                .clear()
                .putString(KEY_USERNAME, vault.credential.username)
                .putString(KEY_CREDENTIAL_SALT, vault.credential.saltBase64)
                .putString(KEY_CREDENTIAL_VERIFIER, vault.credential.verifierBase64)
                .putInt(KEY_CREDENTIAL_ITERATIONS, vault.credential.iterations)
                .putInt(KEY_ENVELOPE_VERSION, vault.keyEnvelope.version)
                .putString(KEY_ENVELOPE_NONCE, vault.keyEnvelope.nonceBase64)
                .putString(KEY_ENVELOPE_CIPHERTEXT, vault.keyEnvelope.ciphertextBase64)
                .putBoolean(KEY_PRESENT, true)
                .commit(),
        ) { "Unable to persist the vault security envelope" }
    }

    override fun clear() {
        check(preferences.edit().clear().commit()) {
            "Unable to clear the vault security envelope"
        }
    }

    private companion object {
        const val FILE_NAME = "health_vault_security"
        const val KEY_PRESENT = "present"
        const val KEY_USERNAME = "username"
        const val KEY_CREDENTIAL_SALT = "credential_salt"
        const val KEY_CREDENTIAL_VERIFIER = "credential_verifier"
        const val KEY_CREDENTIAL_ITERATIONS = "credential_iterations"
        const val KEY_ENVELOPE_VERSION = "envelope_version"
        const val KEY_ENVELOPE_NONCE = "envelope_nonce"
        const val KEY_ENVELOPE_CIPHERTEXT = "envelope_ciphertext"
    }
}
