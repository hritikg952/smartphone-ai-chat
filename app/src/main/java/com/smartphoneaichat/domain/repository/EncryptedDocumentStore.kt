package com.smartphoneaichat.domain.repository

import com.smartphoneaichat.domain.model.AuthorizedSessionContext

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
        context: AuthorizedSessionContext,
        documentId: String,
        mimeType: String,
        bytes: ByteArray,
    ): DocumentImportResult

    fun read(context: AuthorizedSessionContext, documentId: String): ByteArray?
    fun delete(context: AuthorizedSessionContext, documentId: String): Boolean
}
