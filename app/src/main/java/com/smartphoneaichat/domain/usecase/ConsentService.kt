package com.smartphoneaichat.domain.usecase

import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.AuditEvent
import com.smartphoneaichat.domain.model.AuditEventType
import com.smartphoneaichat.domain.model.AuditOutcome
import com.smartphoneaichat.domain.model.ConsentDataCategory
import com.smartphoneaichat.domain.model.ConsentPurpose
import com.smartphoneaichat.domain.model.ConsentRecipient
import com.smartphoneaichat.domain.model.ConsentReceipt
import com.smartphoneaichat.domain.model.ProfileCapability
import com.smartphoneaichat.domain.repository.AuditRepository
import com.smartphoneaichat.domain.repository.ConsentRepository

/** Captures and enforces purpose-specific consent before an integration may use data. */
class ConsentService(
    private val repository: ConsentRepository,
    private val audit: AuditRepository,
    private val nowEpochMillis: () -> Long,
) {
    fun grant(
        context: AuthorizedSessionContext,
        consentId: String,
        purpose: ConsentPurpose,
        categories: Set<ConsentDataCategory>,
        recipient: ConsentRecipient,
        policyVersion: String,
        expiresAtEpochMillis: Long?,
    ): ConsentReceipt {
        context.requireAccess(context.profileId, ProfileCapability.ManageConsent)
        val now = nowEpochMillis()
        val receipt = ConsentReceipt(
            id = consentId,
            profileId = context.profileId,
            purpose = purpose,
            categories = categories,
            recipient = recipient,
            policyVersion = policyVersion,
            grantedAtEpochMillis = now,
            expiresAtEpochMillis = expiresAtEpochMillis,
        )
        repository.save(context, receipt)
        audit.append(
            AuditEvent(
                eventId = "consent-granted:${receipt.id}:$now",
                actorId = context.actorId,
                profileId = context.profileId,
                type = AuditEventType.ConsentChanged,
                outcome = AuditOutcome.Success,
                occurredAtEpochMillis = now,
                targetType = "consent",
                targetId = receipt.id,
                detailCode = "granted",
            ),
        )
        return receipt
    }

    fun revoke(context: AuthorizedSessionContext, consentId: String): Boolean {
        context.requireAccess(context.profileId, ProfileCapability.ManageConsent)
        val now = nowEpochMillis()
        val revoked = repository.revoke(context, consentId, now)
        if (revoked) {
            audit.append(
                AuditEvent(
                    eventId = "consent-revoked:$consentId:$now",
                    actorId = context.actorId,
                    profileId = context.profileId,
                    type = AuditEventType.ConsentChanged,
                    outcome = AuditOutcome.Success,
                    occurredAtEpochMillis = now,
                    targetType = "consent",
                    targetId = consentId,
                    detailCode = "revoked",
                ),
            )
        }
        return revoked
    }

    fun isAllowed(
        context: AuthorizedSessionContext,
        purpose: ConsentPurpose,
        category: ConsentDataCategory,
        recipient: ConsentRecipient,
        nowEpochMillis: Long = nowEpochMillis(),
    ): Boolean {
        context.requireAccess(context.profileId, ProfileCapability.Read)
        return repository.list(context).any { receipt ->
            receipt.isActive(nowEpochMillis) &&
                receipt.purpose == purpose &&
                category in receipt.categories &&
                receipt.recipient == recipient
        }
    }
}
