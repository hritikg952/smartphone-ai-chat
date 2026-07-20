package com.smartphoneaichat.data.security

import com.smartphoneaichat.domain.model.VaultSessionState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DefaultVaultSessionTest {

    @Test
    fun lockBeforeVaultCreation_preservesAbsentState() {
        val session = DefaultVaultSession()

        session.lock()

        assertEquals(VaultSessionState.Absent, session.state.value)
    }
}
