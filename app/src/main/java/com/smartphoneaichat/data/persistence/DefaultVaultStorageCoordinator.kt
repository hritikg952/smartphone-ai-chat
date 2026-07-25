package com.smartphoneaichat.data.persistence

import com.smartphoneaichat.domain.model.HealthRecordWrite
import com.smartphoneaichat.domain.repository.DocumentImportResult
import com.smartphoneaichat.domain.repository.EncryptedDocumentStore
import com.smartphoneaichat.domain.repository.HealthRecordRepository
import com.smartphoneaichat.domain.repository.HealthRecordSaveResult
import com.smartphoneaichat.domain.repository.VaultDocumentImportResult

/** Coordinates multi-artifact writes so callers do not publish partial vault state. */
class VaultStorageCoordinator(
    private val records: HealthRecordRepository,
    private val documents: EncryptedDocumentStore,
) {
    fun importDocumentWithRecord(
        record: HealthRecordWrite,
        documentId: String,
        mimeType: String,
        bytes: ByteArray,
    ): VaultDocumentImportResult {
        return when (documents.import(record.profileId, documentId, mimeType, bytes)) {
            DocumentImportResult.Locked -> VaultDocumentImportResult.Locked
            DocumentImportResult.UnsupportedType -> VaultDocumentImportResult.UnsupportedType
            DocumentImportResult.TooLarge -> VaultDocumentImportResult.TooLarge
            DocumentImportResult.Unavailable -> VaultDocumentImportResult.Unavailable
            DocumentImportResult.Imported -> when (records.save(record)) {
                HealthRecordSaveResult.Saved -> VaultDocumentImportResult.Imported
                HealthRecordSaveResult.Locked -> {
                    documents.delete(record.profileId, documentId)
                    VaultDocumentImportResult.Locked
                }
                HealthRecordSaveResult.Unavailable -> {
                    documents.delete(record.profileId, documentId)
                    VaultDocumentImportResult.Unavailable
                }
            }
        }
    }
}
