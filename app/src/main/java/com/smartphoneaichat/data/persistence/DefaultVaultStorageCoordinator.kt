package com.smartphoneaichat.data.persistence

import com.smartphoneaichat.domain.model.HealthRecordWrite
import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.ProfileCapability
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
        context: AuthorizedSessionContext,
        record: HealthRecordWrite,
        documentId: String,
        mimeType: String,
        bytes: ByteArray,
    ): VaultDocumentImportResult {
        context.requireAccess(record.profileId, ProfileCapability.Write)
        return when (documents.import(context, documentId, mimeType, bytes)) {
            DocumentImportResult.Locked -> VaultDocumentImportResult.Locked
            DocumentImportResult.UnsupportedType -> VaultDocumentImportResult.UnsupportedType
            DocumentImportResult.TooLarge -> VaultDocumentImportResult.TooLarge
            DocumentImportResult.Unavailable -> VaultDocumentImportResult.Unavailable
            DocumentImportResult.Imported -> when (records.save(context, record)) {
                HealthRecordSaveResult.Saved -> VaultDocumentImportResult.Imported
                HealthRecordSaveResult.Locked -> {
                    documents.delete(context, documentId)
                    VaultDocumentImportResult.Locked
                }
                HealthRecordSaveResult.Unavailable -> {
                    documents.delete(context, documentId)
                    VaultDocumentImportResult.Unavailable
                }
            }
        }
    }
}
