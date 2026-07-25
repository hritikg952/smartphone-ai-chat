package com.smartphoneaichat.data.persistence

import com.smartphoneaichat.data.security.AesGcmVaultCipher
import com.smartphoneaichat.data.security.DefaultVaultSession
import com.smartphoneaichat.data.security.RandomBytes
import com.smartphoneaichat.domain.model.HealthRecord
import com.smartphoneaichat.domain.model.HealthRecordProvenance
import com.smartphoneaichat.domain.model.HealthRecordWrite
import com.smartphoneaichat.domain.repository.HealthRecordDeleteResult
import com.smartphoneaichat.domain.repository.HealthRecordRepository
import com.smartphoneaichat.domain.repository.HealthRecordSaveResult
import com.smartphoneaichat.domain.repository.VaultDocumentImportResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class VaultStorageCoordinatorTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun importDocumentWithRecord_rollsBackDocumentWhenRecordSaveFails() {
        val documentStore = LocalEncryptedDocumentStore(
            rootDirectory = tempDir,
            cipher = createCipher(),
        )
        val coordinator = VaultStorageCoordinator(
            records = FailingHealthRecordRepository,
            documents = documentStore,
        )

        val result = coordinator.importDocumentWithRecord(
            record = HealthRecordWrite(
                id = "doc-record-1",
                profileId = "profile-a",
                type = "document",
                schemaVersion = 1,
                createdAtEpochMillis = 100,
                updatedAtEpochMillis = 100,
                provenance = HealthRecordProvenance.Import,
                plaintext = "document metadata".encodeToByteArray(),
            ),
            documentId = "document-1",
            mimeType = "application/pdf",
            bytes = "sensitive pdf body".encodeToByteArray(),
        )

        assertEquals(VaultDocumentImportResult.Unavailable, result)
        assertNull(documentStore.read(profileId = "profile-a", documentId = "document-1"))
    }

    private fun createCipher(): AesGcmVaultCipher {
        val session = DefaultVaultSession().apply { unlock(ByteArray(32) { 12 }) }
        return AesGcmVaultCipher(
            session = session,
            randomBytes = IncrementingRandomBytes(),
        )
    }

    private object FailingHealthRecordRepository : HealthRecordRepository {
        override fun save(record: HealthRecordWrite): HealthRecordSaveResult =
            HealthRecordSaveResult.Unavailable

        override fun get(profileId: String, id: String): HealthRecord? = null

        override fun list(profileId: String, limit: Int, offset: Int): List<HealthRecord> =
            emptyList()

        override fun delete(profileId: String, id: String): HealthRecordDeleteResult =
            HealthRecordDeleteResult.NotFound
    }

    private class IncrementingRandomBytes : RandomBytes {
        private var value = 61

        override fun next(size: Int): ByteArray = ByteArray(size) { value++.toByte() }
    }
}
