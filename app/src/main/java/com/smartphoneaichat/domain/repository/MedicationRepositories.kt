package com.smartphoneaichat.domain.repository

import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.MedicationRegimen
import com.smartphoneaichat.domain.model.Provider

interface MedicationRepository {
    fun save(context: AuthorizedSessionContext, regimen: MedicationRegimen): HealthRecordSaveResult
    fun get(context: AuthorizedSessionContext, id: String): MedicationRegimen?
    fun list(context: AuthorizedSessionContext): List<MedicationRegimen>
    fun delete(context: AuthorizedSessionContext, id: String): HealthRecordDeleteResult
}

interface ProviderRepository {
    fun save(context: AuthorizedSessionContext, provider: Provider): HealthRecordSaveResult
    fun get(context: AuthorizedSessionContext, id: String): Provider?
    fun list(context: AuthorizedSessionContext): List<Provider>
    fun delete(context: AuthorizedSessionContext, id: String): HealthRecordDeleteResult
}
