package com.smartphoneaichat.data.security

import java.util.Base64
import java.security.MessageDigest
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Password verifier using a unique salt and versioned PBKDF2 work factor. */
class Pbkdf2AuthenticationGateway(
    private val randomBytes: RandomBytes,
    private val iterations: Int = DEFAULT_ITERATIONS,
) : AuthenticationGateway {
    init {
        require(iterations > 0)
    }

    override fun createCredential(username: String, password: CharArray): StoredCredential {
        val salt = randomBytes.next(SALT_BYTES)
        val verifier = derive(password, salt, iterations)
        return try {
            StoredCredential(
                username = username,
                saltBase64 = Base64.getEncoder().encodeToString(salt),
                verifierBase64 = Base64.getEncoder().encodeToString(verifier),
                iterations = iterations,
            )
        } finally {
            salt.fill(0)
            verifier.fill(0)
        }
    }

    override fun authenticate(
        username: String,
        password: CharArray,
        credential: StoredCredential,
    ): Boolean {
        if (username != credential.username) return false
        val salt = Base64.getDecoder().decode(credential.saltBase64)
        val expectedVerifier = Base64.getDecoder().decode(credential.verifierBase64)
        val actualVerifier = derive(password, salt, credential.iterations)
        return try {
            MessageDigest.isEqual(expectedVerifier, actualVerifier)
        } finally {
            salt.fill(0)
            expectedVerifier.fill(0)
            actualVerifier.fill(0)
        }
    }

    private fun derive(password: CharArray, salt: ByteArray, workFactor: Int): ByteArray {
        val specification = PBEKeySpec(password, salt, workFactor, VERIFIER_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(specification)
                .encoded
        } finally {
            specification.clearPassword()
        }
    }

    private companion object {
        const val DEFAULT_ITERATIONS = 310_000
        const val SALT_BYTES = 16
        const val VERIFIER_BITS = 256
    }
}
