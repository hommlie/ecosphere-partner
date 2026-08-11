package com.ecosphere.partner.feature.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosphere.partner.core.common.UIState
import com.ecosphere.partner.core.network.ApiResult
import com.ecosphere.partner.feature.jobs.model.PickUpDataByQR
import com.ecosphere.partner.feature.jobs.model.SubmitWasteCollectionRequest
import com.ecosphere.partner.feature.jobs.repository.JobsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobDetailsViewModel @Inject constructor(
    private val jobsRepository: JobsRepository
) : ViewModel() {

    private val _pickUpDataByQr = MutableStateFlow<UIState<PickUpDataByQR>>(UIState.Idle)
    val pickUpDataByQr : StateFlow<UIState<PickUpDataByQR>> = _pickUpDataByQr

    fun getPickUpDataByQr(hashMap: HashMap<String, String>){
        viewModelScope.launch {
            _pickUpDataByQr.value = UIState.Loading

            when(val result = jobsRepository.getPickUpDataBy(hashMap)){
                is ApiResult.Success -> {
                    result.data?.let {
                        _pickUpDataByQr.value = UIState.Success(it)
                    } ?: run {
                        _pickUpDataByQr.value = UIState.Error("Something went wrong.")
                    }
                }
                else -> {
                    _pickUpDataByQr.value = UIState.Error(result.errorMessage?:"Something went wrong.")
                }
            }
        }
    }
    fun resetPickUpDataByQr(){
        _pickUpDataByQr.value = UIState.Idle
    }

    private val _submitPickUpCollection = MutableStateFlow<UIState<Any>>(UIState.Idle)
    val submitPickUpCollection : StateFlow<UIState<Any>> = _submitPickUpCollection

    fun submitPickUpCollection(hashMap: SubmitWasteCollectionRequest) {
        viewModelScope.launch {
            _submitPickUpCollection.value = UIState.Loading
            when(val result = jobsRepository.submitPickUpCollection(hashMap)){
                is ApiResult.Success -> {
                    _submitPickUpCollection.value = UIState.Success(true)
                }
                else -> {
                    _submitPickUpCollection.value = UIState.Error(result.errorMessage?:"Something went wrong.")
                }
            }
        }
    }
    fun resetSubmitPickUpCollection(){
        _submitPickUpCollection.value = UIState.Idle
    }
}