package com.smartphoneaichat.domain.model

/** Source marker carried with every persisted health record. */
enum class HealthRecordProvenance {
    ManualEntry,
    Import,
    Derived,
}

/** Write request for one structured health record body. */
data class HealthRecordWrite(
    val id: String,
    val profileId: String,
    val type: String,
    val schemaVersion: Int,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val provenance: HealthRecordProvenance,
    val plaintext: ByteArray,
) {
    init {
        require(id.isNotBlank())
        require(profileId.isNotBlank())
        require(type.isNotBlank())
        require(schemaVersion > 0)
        require(createdAtEpochMillis >= 0)
        require(updatedAtEpochMillis >= createdAtEpochMillis)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HealthRecordWrite) return false
        return id == other.id &&
            profileId == other.profileId &&
            type == other.type &&
            schemaVersion == other.schemaVersion &&
            createdAtEpochMillis == other.createdAtEpochMillis &&
            updatedAtEpochMillis == other.updatedAtEpochMillis &&
            provenance == other.provenance &&
            plaintext.contentEquals(other.plaintext)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + profileId.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + schemaVersion
        result = 31 * result + createdAtEpochMillis.hashCode()
        result = 31 * result + updatedAtEpochMillis.hashCode()
        result = 31 * result + provenance.hashCode()
        result = 31 * result + plaintext.contentHashCode()
        return result
    }
}

/** Decrypted structured health record returned only after successful vault access. */
data class HealthRecord(
    val id: String,
    val profileId: String,
    val type: String,
    val schemaVersion: Int,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val provenance: HealthRecordProvenance,
    val plaintext: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HealthRecord) return false
        return id == other.id &&
            profileId == other.profileId &&
            type == other.type &&
            schemaVersion == other.schemaVersion &&
            createdAtEpochMillis == other.createdAtEpochMillis &&
            updatedAtEpochMillis == other.updatedAtEpochMillis &&
            provenance == other.provenance &&
            plaintext.contentEquals(other.plaintext)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + profileId.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + schemaVersion
        result = 31 * result + createdAtEpochMillis.hashCode()
        result = 31 * result + updatedAtEpochMillis.hashCode()
        result = 31 * result + provenance.hashCode()
        result = 31 * result + plaintext.contentHashCode()
        return result
    }
}
