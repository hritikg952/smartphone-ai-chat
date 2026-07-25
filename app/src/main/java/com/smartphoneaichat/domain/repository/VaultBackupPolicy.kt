package com.smartphoneaichat.domain.repository

sealed interface VaultBackupExportResult {
    data object Disabled : VaultBackupExportResult
    data object Unavailable : VaultBackupExportResult
    data class Exported(val bytes: ByteArray) : VaultBackupExportResult
}

sealed interface VaultRestoreResult {
    data object Restored : VaultRestoreResult
    data object Disabled : VaultRestoreResult
    data object InvalidBackup : VaultRestoreResult
    data object Unavailable : VaultRestoreResult
}

interface VaultBackupPolicy {
    val isPlatformBackupAllowed: Boolean
    fun export(): VaultBackupExportResult
    fun restore(bytes: ByteArray): VaultRestoreResult
}
