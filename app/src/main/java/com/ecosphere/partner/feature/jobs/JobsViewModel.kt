package com.ecosphere.partner.feature.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosphere.partner.core.common.UIState
import com.ecosphere.partner.core.network.ApiResult
import com.ecosphere.partner.feature.jobs.model.PickupUi
import com.ecosphere.partner.feature.jobs.repository.JobsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobsViewModel @Inject constructor(
    private val repository: JobsRepository
) : ViewModel(){

    private val _jobs = MutableStateFlow<UIState<List<PickupUi>>>(UIState.Idle)
    val jobs: StateFlow<UIState<List<PickupUi>>> = _jobs

    private val _allJobs = MutableStateFlow<List<PickupUi>>(emptyList())
    private val _filteredJobs = MutableStateFlow<List<PickupUi>>(emptyList())
    val filteredJobs: StateFlow<List<PickupUi>> = _filteredJobs

    fun getPickUps(){
        viewModelScope.launch {
            _jobs.value = UIState.Loading
            when(val result = repository.getPickups()){
                is ApiResult.Success -> {

                    result.data?.let {
                        _jobs.value = UIState.Success(it)
                        _allJobs.value = it
                        _filteredJobs.value = it
                    } ?: run {
                        _jobs.value =
                            UIState.Error("No Pickups assigned\nPlease contact to your manager.")
                    }
                }
                else -> {
                    _jobs.value = UIState.Error(result.errorMessage?:"Something went wrong.")
                }
            }

        }
    }

    fun filter(query: String) {
        val cleanQuery = query.trim()
        _filteredJobs.value = if (cleanQuery.isEmpty()) {
            _allJobs.value
        } else {
            _allJobs.value.filter {
                it.customerName.contains(cleanQuery, ignoreCase = true) ||
                        it.pickupAddress.contains(cleanQuery,ignoreCase = true)
            }
        }
    }
}