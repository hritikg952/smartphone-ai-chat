package com.smartphoneaichat.domain.repository

import com.smartphoneaichat.domain.model.HealthRecord
import com.smartphoneaichat.domain.model.HealthRecordWrite
import com.smartphoneaichat.domain.model.AuthorizedSessionContext

sealed interface HealthRecordSaveResult {
    data object Saved : HealthRecordSaveResult
    data object Locked : HealthRecordSaveResult
    data object Unavailable : HealthRecordSaveResult
}

sealed interface HealthRecordDeleteResult {
    data object Deleted : HealthRecordDeleteResult
    data object NotFound : HealthRecordDeleteResult
    data object Unavailable : HealthRecordDeleteResult
}

/** Profile-scoped persistence contract for encrypted structured health data. */
interface HealthRecordRepository {
    fun save(context: AuthorizedSessionContext, record: HealthRecordWrite): HealthRecordSaveResult
    fun get(context: AuthorizedSessionContext, id: String): HealthRecord?
    fun list(context: AuthorizedSessionContext, limit: Int, offset: Int = 0): List<HealthRecord>
    fun delete(context: AuthorizedSessionContext, id: String): HealthRecordDeleteResult
}
