package com.smartphoneaichat.domain.repository

sealed interface DocumentImportResult {
    data object Imported : DocumentImportResult
    data object Locked : DocumentImportResult
    data object UnsupportedType : DocumentImportResult
    data object TooLarge : DocumentImportResult
    data object Unavailable : DocumentImportResult
}

/** Stores large document bytes outside structured records without exposing plaintext files. */
interface EncryptedDocumentStore {
    fun import(
        profileId: String,
        documentId: String,
        mimeType: String,
        bytes: ByteArray,
    ): DocumentImportResult

    fun read(profileId: String, documentId: String): ByteArray?
    fun delete(profileId: String, documentId: String): Boolean
}
