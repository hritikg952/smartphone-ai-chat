package com.smartphoneaichat.domain.model

/** The relationship represented by a profile. Dependent roles are reserved for a later milestone. */
enum class ProfileRole {
    VaultOwner,
    Self,
    CaregiverEditor,
    ViewerExporter,
    EmergencyProjectionManager,
}

/** Actions that must be granted explicitly by an authorized session. */
enum class ProfileCapability {
    Read,
    Write,
    Delete,
    Export,
    ManageProfile,
    ManageConsent,
    ViewAudit,
    ManageEmergencyProjection,
}

/**
 * The selected profile plus the actor's permissions for the current unlocked session.
 * A profile ID from the UI is never sufficient to authorize a repository operation.
 */
data class AuthorizedSessionContext(
    val actorId: String,
    val profileId: String,
    val sessionId: String,
    val role: ProfileRole,
    val capabilities: Set<ProfileCapability>,
) {
    init {
        require(actorId.isNotBlank())
        require(profileId.isNotBlank())
        require(sessionId.isNotBlank())
        require(capabilities.isNotEmpty())
    }

    fun can(capability: ProfileCapability): Boolean = capability in capabilities

    fun includesProfile(profileId: String): Boolean = this.profileId == profileId

    fun requireAccess(profileId: String, capability: ProfileCapability) {
        check(includesProfile(profileId)) { "The selected session profile does not match the requested profile." }
        check(can(capability)) { "The current session does not have the requested capability." }
    }
}

/** Invalidates feature state before a different profile becomes visible. */
fun interface ProfileStateInvalidator {
    fun clearForProfileSwitch()
}
