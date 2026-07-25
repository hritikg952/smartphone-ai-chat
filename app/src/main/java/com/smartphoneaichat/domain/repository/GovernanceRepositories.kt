package com.smartphoneaichat.domain.repository

import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.AuditEvent
import com.smartphoneaichat.domain.model.ConsentReceipt
import com.smartphoneaichat.domain.model.Profile

interface ConsentRepository {
    fun save(context: AuthorizedSessionContext, receipt: ConsentReceipt): ConsentReceipt
    fun list(context: AuthorizedSessionContext): List<ConsentReceipt>
    fun revoke(context: AuthorizedSessionContext, consentId: String, revokedAtEpochMillis: Long): Boolean
}

interface ProfileRepository {
    fun findSelfProfile(): Profile?
    fun initializeSelfProfile(profile: Profile): Profile
    fun get(context: AuthorizedSessionContext): Profile?
    fun update(context: AuthorizedSessionContext, profile: Profile): Profile
    fun archive(context: AuthorizedSessionContext): Boolean
    fun delete(context: AuthorizedSessionContext): Boolean
}

interface AuditRepository {
    fun append(event: AuditEvent)
    fun list(context: AuthorizedSessionContext, limit: Int = DEFAULT_AUDIT_LIMIT): List<AuditEvent>
    fun redactedSupportExport(context: AuthorizedSessionContext): String

    companion object {
        const val DEFAULT_AUDIT_LIMIT = 100
    }
}
