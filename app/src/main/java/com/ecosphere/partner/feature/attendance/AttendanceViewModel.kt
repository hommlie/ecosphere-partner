package com.ecosphere.partner.feature.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosphere.partner.core.common.UIState
import com.ecosphere.partner.core.datastore.SessionManager
import com.ecosphere.partner.core.network.ApiResult
import com.ecosphere.partner.feature.attendance.model.TrackingSessionUi
import com.ecosphere.partner.feature.attendance.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.toString

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val repository: AttendanceRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState<List<TrackingSessionUi>>>(UIState.Idle)
    val uiState: StateFlow<UIState<List<TrackingSessionUi>>> = _uiState

    fun getTrackingSessions(
        date: Long
    ) {
        viewModelScope.launch {
            _uiState.value = UIState.Loading

            when (
                val result = repository.getTrackingSessions(sessionManager.getDriverId().toString(), date)) {

                is ApiResult.Success -> {
                    val sessions = result.data

                    when {
                        sessions == null -> {
                            _uiState.value = UIState.Error("Unable to load tracking sessions.")
                        }
                        sessions.isEmpty() -> {
                            _uiState.value = UIState.Empty
                        }
                        else -> {
                            _uiState.value = UIState.Success(sessions)
                        }
                    }
                }
                else -> {
                    _uiState.value = UIState.Error(result.errorMessage ?: "Something went wrong.")
                }
            }
        }
    }
}