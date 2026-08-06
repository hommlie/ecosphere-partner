package com.ecosphere.partner.feature.patients

import com.ecosphere.partner.core.network.ApiService
import com.ecosphere.partner.core.network.SafeApiCall
import javax.inject.Inject

class PatientRepository @Inject constructor(
    private val api: ApiService,
    private val safeApiCall: SafeApiCall
) {

    suspend fun getPatients(
        page:Int,
        limit:Int
    ) =
        safeApiCall {
            api.getPatients(page,limit)
        }

    suspend fun getBranch(
        page: Int,
        limit: Int,
        search: String
    ) = safeApiCall {

        api.getBranch(page, limit, search)

    }

//    suspend fun addPatient(
//        body: HashMap<String, String>
//    ) = safeApiCall {
//        api.addPatient(body)
//    }
}