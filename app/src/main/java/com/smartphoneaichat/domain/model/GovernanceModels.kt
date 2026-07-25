package com.smartphoneaichat.domain.model

enum class ConsentPurpose {
    HealthConnectRead,
    DocumentProcessing,
    CloudOcr,
    AiAssistance,
    Export,
    Sharing,
}

enum class ConsentDataCategory {
    Profile,
    Documents,
    Medications,
    Vitals,
    Allergies,
    Immunizations,
    Journal,
    Insurance,
}

enum class ConsentRecipient {
    LocalApp,
    HealthConnect,
    CloudService,
    ExternalRecipient,
    AiModel,
}

data class ConsentReceipt(
    val id: String,
    val profileId: String,
    val purpose: ConsentPurpose,
    val categories: Set<ConsentDataCategory>,
    val recipient: ConsentRecipient,
    val policyVersion: String,
    val grantedAtEpochMillis: Long,
    val expiresAtEpochMillis: Long? = null,
    val revokedAtEpochMillis: Long? = null,
) {
    init {
        require(id.isNotBlank())
        require(profileId.isNotBlank())
        require(categories.isNotEmpty())
        require(policyVersion.isNotBlank())
        require(grantedAtEpochMillis >= 0)
        require(expiresAtEpochMillis == null || expiresAtEpochMillis > grantedAtEpochMillis)
        require(revokedAtEpochMillis == null || revokedAtEpochMillis >= grantedAtEpochMillis)
    }

    fun isActive(nowEpochMillis: Long): Boolean =
        revokedAtEpochMillis == null &&
            nowEpochMillis >= grantedAtEpochMillis &&
            (expiresAtEpochMillis == null || nowEpochMillis < expiresAtEpochMillis)
}

enum class AuditEventType {
    UnlockFailure,
    ProfileChanged,
    SensitiveRead,
    Import,
    Edit,
    Delete,
    ExportOrShare,
    ConsentChanged,
    KeyEvent,
    AdministrativeAction,
}

enum class AuditOutcome {
    Success,
    Failure,
}

/** Audit metadata is intentionally structured; it has no field for clinical text or secrets. */
data class AuditEvent(
    val eventId: String,
    val actorId: String,
    val profileId: String?,
    val type: AuditEventType,
    val outcome: AuditOutcome,
    val occurredAtEpochMillis: Long,
    val targetType: String? = null,
    val targetId: String? = null,
    val detailCode: String? = null,
) {
    init {
        require(eventId.isNotBlank())
        require(actorId.isNotBlank())
        require(profileId == null || profileId.isNotBlank())
        require(occurredAtEpochMillis >= 0)
        require(targetType == null || targetType.isNotBlank())
        require(targetId == null || targetId.isNotBlank())
        require(detailCode == null || detailCode.isNotBlank())
    }
}
