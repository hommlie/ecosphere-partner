package com.ecosphere.partner.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosphere.partner.core.network.ApiResult
import com.ecosphere.partner.core.common.UIState
import com.ecosphere.partner.feature.main.repository.AttendanceRepository
import com.ecosphere.partner.feature.tracking.controller.TrackingController
import com.ecosphere.partner.feature.tracking.model.TrackingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val trackingController: TrackingController
) : ViewModel() {

    fun onDutyToggleChanged(enableDuty: Boolean) {
        if (enableDuty) {
            punchIn()
        } else {
            punchOut()
        }
    }

    val trackingUiState =
        trackingController.runtime
            .map { runtime ->
                TrackingUiState(
                    distanceText = "Total Distance : ${String.format(
                        Locale.getDefault(),
                        "%.1f km",
                        (runtime?.totalDistance ?: 0f ) / 1000
                    )}",
                    speedText = "${String.format(
                        Locale.getDefault(),
                        "%.0f",
                        (runtime?.currentSpeed ?: 0f) * 3.6f
                    ) }"
                )
            }

    private val _punchInState = MutableStateFlow<UIState<Unit>?>(null)
    val punchInState: StateFlow<UIState<Unit>?> = _punchInState

    private val _punchOutState = MutableStateFlow<UIState<Unit>?>(null)
    val punchOutState: StateFlow<UIState<Unit>?> = _punchOutState

    fun punchIn() {

        viewModelScope.launch {

            _punchInState.value = UIState.Loading

            when (val result = attendanceRepository.punchIn()) {

                is ApiResult.Success<*> -> {
                    _punchInState.value = UIState.Success(Unit)
                }

                is ApiResult.ApiError -> {
                    _punchInState.value = UIState.Error(result.message)
                }

                is ApiResult.NetworkError -> {
                    _punchInState.value = UIState.Error(result.message)
                }

                is ApiResult.TimeoutError -> {
                    _punchInState.value = UIState.Error(result.message)
                }

                is ApiResult.SerializationError -> {
                    _punchInState.value = UIState.Error(result.message)
                }

                is ApiResult.UnknownError -> {
                    _punchInState.value = UIState.Error(result.message)
                }
            }
        }
    }

    fun punchOut() {

        viewModelScope.launch {

            _punchOutState.value = UIState.Loading

            when (val result = attendanceRepository.punchOut()) {

                is ApiResult.Success<*> -> {
                    _punchOutState.value = UIState.Success(Unit)
                }

                is ApiResult.ApiError -> {
                    _punchOutState.value = UIState.Error(result.message)
                }

                is ApiResult.NetworkError -> {
                    _punchOutState.value = UIState.Error(result.message)
                }

                is ApiResult.TimeoutError -> {
                    _punchOutState.value = UIState.Error(result.message)
                }

                is ApiResult.SerializationError -> {
                    _punchOutState.value = UIState.Error(result.message)
                }

                is ApiResult.UnknownError -> {
                    _punchOutState.value = UIState.Error(result.message)
                }
            }
        }
    }

    fun clearPunchInState() {
        _punchInState.value = null
    }

    fun clearPunchOutState() {
        _punchOutState.value = null
    }
}