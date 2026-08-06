package com.ecosphere.partner.feature.main.repository

import com.ecosphere.partner.core.network.ApiResult

interface AttendanceRepository {

    suspend fun punchIn(): ApiResult<Unit?>

    suspend fun punchOut(): ApiResult<Unit?>
}