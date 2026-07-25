package com.smartphoneaichat.data.persistence

import com.smartphoneaichat.domain.repository.VaultBackupExportResult
import com.smartphoneaichat.domain.repository.VaultRestoreResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class PrototypeVaultBackupPolicyTest {

    @Test
    fun prototypeBackupPolicy_disablesBackupExportAndRestoreUntilReviewed() {
        val policy = PrototypeVaultBackupPolicy

        assertFalse(policy.isPlatformBackupAllowed)
        assertEquals(VaultBackupExportResult.Disabled, policy.export())
        assertEquals(VaultRestoreResult.Unavailable, policy.restore(ByteArray(0)))
    }
}
