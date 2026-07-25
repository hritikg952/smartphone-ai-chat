package com.smartphoneaichat.domain.repository

sealed interface VaultDocumentImportResult {
    data object Imported : VaultDocumentImportResult
    data object Locked : VaultDocumentImportResult
    data object UnsupportedType : VaultDocumentImportResult
    data object TooLarge : VaultDocumentImportResult
    data object Unavailable : VaultDocumentImportResult
}
