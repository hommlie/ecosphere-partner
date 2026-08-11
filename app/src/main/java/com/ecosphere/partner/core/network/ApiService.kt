package com.ecosphere.partner.core.network

import com.ecosphere.partner.feature.jobs.model.PickUpDataByQR
import com.ecosphere.partner.feature.jobs.model.PickupUi
import com.ecosphere.partner.feature.jobs.model.SubmitWasteCollectionRequest
import com.ecosphere.partner.feature.login.model.LoginData
import com.ecosphere.partner.model.CommonResponse
import com.ecosphere.partner.feature.patients.model.BranchesData
import com.ecosphere.partner.feature.patients.model.PatientsData
import com.ecosphere.partner.feature.setting.model.CmsPageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.HashMap

interface ApiService {

    @GET("v1/patient")
    suspend fun getPatients(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<CommonResponse<PatientsData>>

    @GET("v1/branch")
    suspend fun getBranch(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("searchKey") searchKey : String
    ): Response<CommonResponse<BranchesData>>

    @POST("driver/login")
    suspend fun login(
        @Body hashMap: HashMap<String, String>
    ) : Response<CommonResponse<LoginData>>

    @POST("driver/pickups")
    suspend fun getPickups() : Response<CommonResponse<List<PickupUi>>>

    @POST("cms")
    suspend fun getCms() : Response<CommonResponse<CmsPageResponse>>

    @POST("v1/driver/fetch-order-details")
    suspend fun getPickUpDataByQr(
        @Body hashMap: HashMap<String, String>
    ) : Response<CommonResponse<PickUpDataByQR>>

    @POST("v1/driver/create-trip-summary")
    suspend fun submitPickUpCollection(
        @Body hashMap: SubmitWasteCollectionRequest
    ) : Response<CommonResponse<Any?>>

}