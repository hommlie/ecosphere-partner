package com.ecosphere.partner.feature.jobs.repository

import com.ecosphere.partner.core.network.ApiResult
import com.ecosphere.partner.core.network.ApiService
import com.ecosphere.partner.core.network.SafeApiCall
import com.ecosphere.partner.feature.jobs.model.PickupStatus
import com.ecosphere.partner.feature.jobs.model.PickupUi
import com.ecosphere.partner.feature.jobs.model.SubmitWasteCollectionRequest
import com.ecosphere.partner.feature.jobs.model.WasteType
import kotlinx.coroutines.delay
import javax.inject.Inject

class JobsRepository @Inject constructor(
    private val safeApiCall: SafeApiCall,
    private val apiService: ApiService
) {

//    suspend fun getPickups() = safeApiCall {
//        apiService.getPickups()
//    }

    suspend fun getPickUpDataBy(hashMap: HashMap<String, String>) = safeApiCall {
        apiService.getPickUpDataByQr(hashMap)
    }
    suspend fun submitPickUpCollection(hashMap : SubmitWasteCollectionRequest) = safeApiCall {
        apiService.submitPickUpCollection(hashMap)
    }

    private val isMockEnabled = true

    suspend fun getPickups(): ApiResult<List<PickupUi>?> {

        if (isMockEnabled) {
            delay(1500)
            return ApiResult.Success(mockPickups)
        }
        return safeApiCall {
            apiService.getPickups()
        }
    }


    private val mockPickups = listOf(

        PickupUi(
            pickupId = "PK2026001",
            customerName = "Arjun Reddy",
            customerPhone = "9876543210",
            status = PickupStatus.PENDING,
            wasteType = WasteType.WET,
            estimatedWeightKg = 12.5f,
            distanceKm = 2.8,
            pickupTime = System.currentTimeMillis(),
            pickupAddress = "Prestige Shantiniketan, ITPL Main Road, Whitefield, Bengaluru - 560048",
            mapLink = "",
            latitude = 12.9963,
            longitude = 77.7301
        ),

        PickupUi(
            pickupId = "PK2026002",
            customerName = "Srinivas Rao",
            customerPhone = "9876543211",
            status = PickupStatus.STARTED,
            wasteType = WasteType.DRY,
            estimatedWeightKg = 8.0f,
            distanceKm = 1.5,
            pickupTime = System.currentTimeMillis(),
            pickupAddress = "Sobha Dream Acres, Panathur Main Road, Bellandur, Bengaluru - 560103",
            mapLink = "",
            latitude = 12.9279,
            longitude = 77.6836
        ),

        PickupUi(
            pickupId = "PK2026003",
            customerName = "Lakshmi Narayanan",
            customerPhone = "9876543212",
            status = PickupStatus.COMPLETED,
            wasteType = WasteType.SANITARY,
            estimatedWeightKg = 3.5f,
            distanceKm = 4.1,
            pickupTime = System.currentTimeMillis(),
            pickupAddress = "Brigade Gateway Apartments, Dr. Rajkumar Road, Rajajinagar, Bengaluru - 560010",
            mapLink = "",
            latitude = 13.0116,
            longitude = 77.5550
        ),

        PickupUi(
            pickupId = "PK2026004",
            customerName = "Harish Kumar",
            customerPhone = "9876543213",
            status = PickupStatus.PENDING,
            wasteType = WasteType.SPECIAL_CARE,
            estimatedWeightKg = 2.0f,
            distanceKm = 5.8,
            pickupTime = System.currentTimeMillis(),
            pickupAddress = "Purva Skywood, Silver County Road, Electronic City Phase 1, Bengaluru - 560100",
            mapLink = "",
            latitude = 12.8407,
            longitude = 77.6766
        ),

        PickupUi(
            pickupId = "PK2026005",
            customerName = "Pradeep Gowda",
            customerPhone = "9876543214",
            status = PickupStatus.COMPLETED,
            wasteType = WasteType.WET,
            estimatedWeightKg = 15.3f,
            distanceKm = 3.2,
            pickupTime = System.currentTimeMillis(),
            pickupAddress = "Embassy Lake Terraces, Hebbal Flyover, Hebbal, Bengaluru - 560024",
            mapLink = "",
            latitude = 13.0448,
            longitude = 77.5919
        ),

        PickupUi(
            pickupId = "PK2026006",
            customerName = "Manjunath Shetty",
            customerPhone = "9876543215",
            status = PickupStatus.STARTED,
            wasteType = WasteType.DRY,
            estimatedWeightKg = 6.2f,
            distanceKm = 2.3,
            pickupTime = System.currentTimeMillis(),
            pickupAddress = "SNN Raj Spiritua, Begur Road, Akshayanagar, Bengaluru - 560068",
            mapLink = "",
            latitude = 12.8896,
            longitude = 77.6318
        ),

        PickupUi(
            pickupId = "PK2026007",
            customerName = "Karthik Iyer",
            customerPhone = "9876543216",
            status = PickupStatus.PENDING,
            wasteType = WasteType.SANITARY,
            estimatedWeightKg = 4.0f,
            distanceKm = 1.9,
            pickupTime = System.currentTimeMillis(),
            pickupAddress = "Brigade Cosmopolis, Whitefield Main Road, Whitefield, Bengaluru - 560066",
            mapLink = "",
            latitude = 12.9856,
            longitude = 77.7484
        ),

        PickupUi(
            pickupId = "PK2026008",
            customerName = "Venkatesh Murthy",
            customerPhone = "9876543217",
            status = PickupStatus.COMPLETED,
            wasteType = WasteType.SPECIAL_CARE,
            estimatedWeightKg = 1.8f,
            distanceKm = 6.4,
            pickupTime = System.currentTimeMillis(),
            pickupAddress = "Prestige Falcon City, Kanakapura Main Road, Konanakunte, Bengaluru - 560062",
            mapLink = "",
            latitude = 12.8858,
            longitude = 77.5639
        )
    )

}