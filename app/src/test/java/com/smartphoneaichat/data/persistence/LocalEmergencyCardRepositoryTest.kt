package com.smartphoneaichat.data.persistence

import com.smartphoneaichat.data.security.EmergencyCardIntegritySigner
import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.EmergencyCardProjection
import com.smartphoneaichat.domain.model.ProfileCapability
import com.smartphoneaichat.domain.model.ProfileRole
import com.smartphoneaichat.domain.repository.EmergencyCardReadResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.junit.jupiter.api.Test

class LocalEmergencyCardRepositoryTest {
    @TempDir lateinit var tempDir: Path

    @Test
    fun publicProjection_survivesRestartRejectsTamperingAndIsRemovedByRevoke() {
        val signer = TestSigner()
        val projection = EmergencyCardProjection("self", "Avery", 1, 10, 10)
        LocalEmergencyCardRepository(tempDir, signer).publish(context(), projection)

        val restarted = LocalEmergencyCardRepository(tempDir, signer)
        assertEquals(EmergencyCardReadResult.Available(projection), restarted.publicCard())

        Files.writeString(tempDir.resolve("emergency-card.v1"), "tampered")
        assertEquals(EmergencyCardReadResult.Unavailable, restarted.publicCard())

        restarted.publish(context(), projection)
        restarted.revoke(context())
        assertEquals(EmergencyCardReadResult.NotPublished, restarted.publicCard())
        assertEquals(false, Files.exists(tempDir.resolve("emergency-card.v1")))
    }

    private fun context() = AuthorizedSessionContext(
        actorId = "vault-owner", profileId = "self", sessionId = "session", role = ProfileRole.Self,
        capabilities = setOf(ProfileCapability.ManageEmergencyProjection),
    )

    private class TestSigner : EmergencyCardIntegritySigner {
        private val key = SecretKeySpec(ByteArray(32) { 9 }, "HmacSHA256")
        override fun sign(bytes: ByteArray): ByteArray = Mac.getInstance("HmacSHA256").run { init(key); doFinal(bytes) }
        override fun verify(bytes: ByteArray, signature: ByteArray): Boolean = sign(bytes).contentEquals(signature)
    }
}
