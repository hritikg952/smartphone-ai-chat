package com.smartphoneaichat.data.persistence

import com.smartphoneaichat.data.security.AesGcmVaultCipher
import com.smartphoneaichat.data.security.DefaultVaultSession
import com.smartphoneaichat.data.security.RandomBytes
import com.smartphoneaichat.domain.model.HealthRecord
import com.smartphoneaichat.domain.model.HealthRecordProvenance
import com.smartphoneaichat.domain.model.HealthRecordWrite
import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.ProfileCapability
import com.smartphoneaichat.domain.model.ProfileRole
import com.smartphoneaichat.domain.repository.HealthRecordDeleteResult
import com.smartphoneaichat.domain.repository.HealthRecordSaveResult
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class EncryptedHealthRecordRepositoryTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun saveAndGet_requireUnlockedVaultAndPersistOnlyCiphertextAcrossRestart() {
        val record = HealthRecordWrite(
            id = "record-1",
            profileId = "profile-a",
            type = "allergy",
            schemaVersion = 1,
            createdAtEpochMillis = 100,
            updatedAtEpochMillis = 100,
            provenance = HealthRecordProvenance.ManualEntry,
            plaintext = "peanut allergy".encodeToByteArray(),
        )
        val lockedRepository = EncryptedHealthRecordRepository(
            rootDirectory = tempDir,
            cipher = createCipher(unlocked = false),
        )

        assertEquals(HealthRecordSaveResult.Locked, lockedRepository.save(context("profile-a"), record))

        val unlockedRepository = EncryptedHealthRecordRepository(
            rootDirectory = tempDir,
            cipher = createCipher(unlocked = true),
        )
        assertEquals(HealthRecordSaveResult.Saved, unlockedRepository.save(context("profile-a"), record))

        val restartedRepository = EncryptedHealthRecordRepository(
            rootDirectory = tempDir,
            cipher = createCipher(unlocked = true),
        )
        val restored = restartedRepository.get(context("profile-a"), id = "record-1")

        assertEquals("record-1", restored?.id)
        assertEquals("profile-a", restored?.profileId)
        assertEquals("allergy", restored?.type)
        assertEquals(1, restored?.schemaVersion)
        assertEquals(100, restored?.createdAtEpochMillis)
        assertEquals(100, restored?.updatedAtEpochMillis)
        assertEquals(HealthRecordProvenance.ManualEntry, restored?.provenance)
        assertArrayEquals("peanut allergy".encodeToByteArray(), restored?.plaintext)
        assertFalse(
            String(Files.readAllBytes(tempDir.resolve("health-records.v1"))).contains("peanut allergy"),
        )
    }

    @Test
    fun listAndDelete_areProfileScopedAndStableAcrossRecords() {
        val repository = EncryptedHealthRecordRepository(
            rootDirectory = tempDir,
            cipher = createCipher(unlocked = true),
        )
        repository.save(context("profile-a"), record("record-1", "profile-a", 100, "alpha"))
        repository.save(context("profile-b"), record("record-2", "profile-b", 200, "bravo"))
        repository.save(context("profile-a"), record("record-3", "profile-a", 300, "charlie"))

        val listed = repository.list(context("profile-a"), limit = 1, offset = 1)

        assertEquals(listOf("record-1"), listed.map { it.id })
        assertEquals(HealthRecordDeleteResult.Deleted, repository.delete(context("profile-a"), "record-3"))
        assertEquals(null, repository.get(context("profile-a"), "record-3"))
        assertEquals("record-2", repository.get(context("profile-b"), "record-2")?.id)
    }

    @Test
    fun saveRejectsARecordWhoseProfileDoesNotMatchTheAuthorizedSession() {
        val repository = EncryptedHealthRecordRepository(
            rootDirectory = tempDir,
            cipher = createCipher(unlocked = true),
        )

        assertThrows(IllegalStateException::class.java) {
            repository.save(
                context("profile-a"),
                record("record-2", "profile-b", 100, "secret from another profile"),
            )
        }
    }

    @Test
    fun save_whenRecordStoreIsMalformed_failsClosedWithoutOverwriting() {
        Files.write(tempDir.resolve("health-records.v1"), "not-a-valid-record".encodeToByteArray())
        val repository = EncryptedHealthRecordRepository(
            rootDirectory = tempDir,
            cipher = createCipher(unlocked = true),
        )

        assertEquals(
            HealthRecordSaveResult.Unavailable,
            repository.save(context("profile-a"), record("record-1", "profile-a", 100, "alpha")),
        )
        assertEquals("not-a-valid-record", String(Files.readAllBytes(tempDir.resolve("health-records.v1"))))
    }

    private fun createCipher(unlocked: Boolean): AesGcmVaultCipher {
        val session = DefaultVaultSession()
        if (unlocked) session.unlock(ByteArray(32) { 4 })
        return AesGcmVaultCipher(
            session = session,
            randomBytes = IncrementingRandomBytes(),
        )
    }

    private fun record(
        id: String,
        profileId: String,
        updatedAt: Long,
        text: String,
    ): HealthRecordWrite = HealthRecordWrite(
        id = id,
        profileId = profileId,
        type = "note",
        schemaVersion = 1,
        createdAtEpochMillis = 100,
        updatedAtEpochMillis = updatedAt,
        provenance = HealthRecordProvenance.ManualEntry,
        plaintext = text.encodeToByteArray(),
    )

    private fun context(profileId: String): AuthorizedSessionContext = AuthorizedSessionContext(
        actorId = "owner",
        profileId = profileId,
        sessionId = "session-$profileId",
        role = ProfileRole.Self,
        capabilities = ProfileCapability.entries.toSet(),
    )

    private class IncrementingRandomBytes : RandomBytes {
        private var value = 1

        override fun next(size: Int): ByteArray = ByteArray(size) { value++.toByte() }
    }
}
