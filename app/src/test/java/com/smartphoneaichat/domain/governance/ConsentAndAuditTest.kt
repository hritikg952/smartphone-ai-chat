package com.smartphoneaichat.domain.governance

import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.ProfileCapability
import com.smartphoneaichat.domain.model.ProfileRole
import com.smartphoneaichat.data.governance.InMemoryAuditRepository
import com.smartphoneaichat.data.governance.InMemoryConsentRepository
import com.smartphoneaichat.domain.model.AuditEvent
import com.smartphoneaichat.domain.model.AuditEventType
import com.smartphoneaichat.domain.model.AuditOutcome
import com.smartphoneaichat.domain.model.ConsentDataCategory
import com.smartphoneaichat.domain.model.ConsentPurpose
import com.smartphoneaichat.domain.model.ConsentRecipient
import com.smartphoneaichat.domain.usecase.ConsentService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConsentAndAuditTest {

    @Test
    fun grantedConsentAllowsOnlyTheApprovedPurposeCategoryAndRecipient() {
        val audit = InMemoryAuditRepository()
        val service = ConsentService(
            repository = InMemoryConsentRepository(),
            audit = audit,
            nowEpochMillis = { 100L },
        )
        val context = context()

        service.grant(
            context = context,
            consentId = "consent-1",
            purpose = ConsentPurpose.HealthConnectRead,
            categories = setOf(ConsentDataCategory.Vitals),
            recipient = ConsentRecipient.HealthConnect,
            policyVersion = "policy-1",
            expiresAtEpochMillis = 200L,
        )

        assertTrue(
            service.isAllowed(
                context,
                ConsentPurpose.HealthConnectRead,
                ConsentDataCategory.Vitals,
                ConsentRecipient.HealthConnect,
                nowEpochMillis = 150L,
            ),
        )
        assertFalse(
            service.isAllowed(
                context,
                ConsentPurpose.AiAssistance,
                ConsentDataCategory.Vitals,
                ConsentRecipient.HealthConnect,
                nowEpochMillis = 150L,
            ),
        )
        assertFalse(
            service.isAllowed(
                context,
                ConsentPurpose.HealthConnectRead,
                ConsentDataCategory.Documents,
                ConsentRecipient.HealthConnect,
                nowEpochMillis = 150L,
            ),
        )
        assertEquals(AuditEventType.ConsentChanged, audit.events.single().type)
    }

    @Test
    fun revocationImmediatelyStopsFutureUseAndIsAuditedWithoutHealthContent() {
        val audit = InMemoryAuditRepository()
        val service = ConsentService(
            repository = InMemoryConsentRepository(),
            audit = audit,
            nowEpochMillis = { 100L },
        )
        val context = context()
        service.grant(
            context,
            "consent-1",
            ConsentPurpose.DocumentProcessing,
            setOf(ConsentDataCategory.Documents),
            ConsentRecipient.LocalApp,
            "policy-1",
            null,
        )

        assertTrue(service.revoke(context, "consent-1"))
        assertFalse(
            service.isAllowed(
                context,
                ConsentPurpose.DocumentProcessing,
                ConsentDataCategory.Documents,
                ConsentRecipient.LocalApp,
                nowEpochMillis = 100L,
            ),
        )
        assertEquals(2, audit.events.size)
        assertTrue(audit.events.all { it.detailCode != "document text" })
    }

    @Test
    fun supportExportContainsSafeMetadataOnly() {
        val audit = InMemoryAuditRepository()
        val context = context()
        audit.append(
            AuditEvent(
                eventId = "event-1",
                actorId = context.actorId,
                profileId = context.profileId,
                type = AuditEventType.SensitiveRead,
                outcome = AuditOutcome.Success,
                occurredAtEpochMillis = 123L,
                targetType = "record",
                targetId = "record-1",
                detailCode = "viewed",
            ),
        )

        val export = audit.redactedSupportExport(context)

        assertTrue(export.contains("SensitiveRead"))
        assertTrue(export.contains("record-1"))
        assertFalse(export.contains("medication"))
        assertFalse(export.contains("password"))
        assertFalse(export.contains("token"))
    }

    private fun context(): AuthorizedSessionContext = AuthorizedSessionContext(
        actorId = "owner",
        profileId = "self",
        sessionId = "session-1",
        role = ProfileRole.Self,
        capabilities = ProfileCapability.entries.toSet(),
    )
}
