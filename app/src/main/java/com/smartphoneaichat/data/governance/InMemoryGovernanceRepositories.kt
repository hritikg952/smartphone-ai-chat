package com.smartphoneaichat.data.governance

import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.AuditEvent
import com.smartphoneaichat.domain.model.ConsentReceipt
import com.smartphoneaichat.domain.model.ProfileCapability
import com.smartphoneaichat.domain.model.Profile
import com.smartphoneaichat.domain.model.ProfileRole
import com.smartphoneaichat.domain.model.ProfileStatus
import com.smartphoneaichat.domain.repository.AuditRepository
import com.smartphoneaichat.domain.repository.ConsentRepository
import com.smartphoneaichat.domain.repository.ProfileRepository

class InMemoryProfileRepository : ProfileRepository {
    private val profiles = linkedMapOf<String, Profile>()

    override fun findSelfProfile(): Profile? = profiles.values.firstOrNull { it.relationship == ProfileRole.Self }

    override fun initializeSelfProfile(profile: Profile): Profile {
        require(profile.relationship == ProfileRole.Self) {
            "The current prototype supports a self profile only."
        }
        require(profiles.values.none { it.relationship == ProfileRole.Self }) {
            "A self profile already exists."
        }
        profiles[profile.id] = profile
        return profile
    }

    override fun get(context: AuthorizedSessionContext): Profile? {
        context.requireAccess(context.profileId, ProfileCapability.Read)
        return profiles[context.profileId]?.takeIf { it.actorId == context.actorId }
    }

    override fun update(context: AuthorizedSessionContext, profile: Profile): Profile {
        context.requireAccess(profile.id, ProfileCapability.ManageProfile)
        require(profile.relationship == ProfileRole.Self)
        require(profile.actorId == context.actorId)
        profiles[profile.id] = profile
        return profile
    }

    override fun archive(context: AuthorizedSessionContext): Boolean {
        context.requireAccess(context.profileId, ProfileCapability.ManageProfile)
        val current = get(context) ?: return false
        profiles[current.id] = current.copy(status = ProfileStatus.Archived)
        return true
    }

    override fun delete(context: AuthorizedSessionContext): Boolean {
        context.requireAccess(context.profileId, ProfileCapability.Delete)
        return profiles.remove(context.profileId) != null
    }
}

/** Prototype repository; persistence will move behind the encrypted vault database boundary. */
class InMemoryConsentRepository : ConsentRepository {
    private val receipts = linkedMapOf<String, ConsentReceipt>()

    override fun save(context: AuthorizedSessionContext, receipt: ConsentReceipt): ConsentReceipt {
        context.requireAccess(receipt.profileId, ProfileCapability.ManageConsent)
        receipts[receipt.id] = receipt
        return receipt
    }

    override fun list(context: AuthorizedSessionContext): List<ConsentReceipt> {
        context.requireAccess(context.profileId, ProfileCapability.Read)
        return receipts.values.filter { it.profileId == context.profileId }
    }

    override fun revoke(
        context: AuthorizedSessionContext,
        consentId: String,
        revokedAtEpochMillis: Long,
    ): Boolean {
        context.requireAccess(context.profileId, ProfileCapability.ManageConsent)
        val existing = receipts[consentId] ?: return false
        if (existing.profileId != context.profileId || existing.revokedAtEpochMillis != null) return false
        receipts[consentId] = existing.copy(revokedAtEpochMillis = revokedAtEpochMillis)
        return true
    }
}

class InMemoryAuditRepository : AuditRepository {
    val events = mutableListOf<AuditEvent>()

    override fun append(event: AuditEvent) {
        events += event
    }

    override fun list(context: AuthorizedSessionContext, limit: Int): List<AuditEvent> {
        context.requireAccess(context.profileId, ProfileCapability.ViewAudit)
        require(limit > 0)
        return events.asSequence()
            .filter { it.profileId == context.profileId || (it.profileId == null && it.actorId == context.actorId) }
            .sortedByDescending { it.occurredAtEpochMillis }
            .take(limit)
            .toList()
    }

    override fun redactedSupportExport(context: AuthorizedSessionContext): String =
        list(context, AuditRepository.DEFAULT_AUDIT_LIMIT).joinToString(separator = "\n") { event ->
            listOf(
                event.occurredAtEpochMillis,
                event.type.name,
                event.outcome.name,
                event.targetType.orEmpty(),
                event.targetId.orEmpty(),
                event.detailCode.orEmpty(),
            ).joinToString("\t")
        }
}
