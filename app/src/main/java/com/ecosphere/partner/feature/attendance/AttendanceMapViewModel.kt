package com.ecosphere.partner.feature.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosphere.partner.core.common.UIState
import com.ecosphere.partner.core.network.ApiResult
import com.ecosphere.partner.feature.attendance.repository.AttendanceRepository
import com.ecosphere.partner.feature.tracking.model.TrackingPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttendanceMapViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState<List<TrackingPoint>>>(UIState.Idle)
    val uiState = _uiState.asStateFlow()

    fun getTrackingPoints(
        sessionId: String
    ) {
        viewModelScope.launch {
            _uiState.value = UIState.Loading

            when(
                val result = repository.getTrackingPoints(sessionId)
            ){

                is ApiResult.Success -> {
                    val points = result.data

                    when{
                        points == null -> _uiState.value = UIState.Error("Unable to load route.")

                        points.isEmpty() -> _uiState.value = UIState.Empty

                        else -> _uiState.value = UIState.Success(points)
                    }
                }
                else -> {
                    _uiState.value = UIState.Error(result.errorMessage ?: "Something went wrong.")
                }
            }
        }
    }

}