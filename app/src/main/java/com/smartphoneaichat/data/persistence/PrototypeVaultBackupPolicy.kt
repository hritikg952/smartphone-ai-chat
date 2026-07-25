package com.smartphoneaichat.data.persistence

import com.smartphoneaichat.domain.repository.VaultBackupExportResult
import com.smartphoneaichat.domain.repository.VaultBackupPolicy
import com.smartphoneaichat.domain.repository.VaultRestoreResult

/** Prototype policy: no backup or recovery path is active until its format is reviewed. */
object PrototypeVaultBackupPolicy : VaultBackupPolicy {
    override val isPlatformBackupAllowed: Boolean = false

    override fun export(): VaultBackupExportResult = VaultBackupExportResult.Disabled

    override fun restore(bytes: ByteArray): VaultRestoreResult = VaultRestoreResult.Unavailable
}
