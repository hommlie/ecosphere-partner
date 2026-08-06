package com.ecosphere.partner.feature.main.repository

import com.ecosphere.partner.core.network.ApiResult
import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeAttendanceRepository @Inject constructor() : AttendanceRepository {

    override suspend fun punchIn(): ApiResult<Unit?> {

        delay(1000)

        return ApiResult.Success(Unit)

    }

    override suspend fun punchOut(): ApiResult<Unit?> {

        delay(1000)

        return ApiResult.Success(Unit)

    }
}