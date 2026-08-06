package com.ecosphere.partner.feature.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosphere.partner.core.common.UIState
import com.ecosphere.partner.core.network.ApiResult
import com.ecosphere.partner.feature.setting.model.CmsPageResponse
import com.ecosphere.partner.feature.setting.repository.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacyPolicyViewModel @Inject constructor(private val repository: SettingRepository) : ViewModel() {

    private val _cmsDataState = MutableStateFlow<UIState<CmsPageResponse>>(UIState.Idle)
    val cmsDataState: StateFlow<UIState<CmsPageResponse>> = _cmsDataState

    fun fetchCmsData() {
        viewModelScope.launch {
            _cmsDataState.value = UIState.Loading
            when (val result = repository.getCms()) {
                is ApiResult.Success -> {
                    result.data?.let {
                        _cmsDataState.value = UIState.Success(it)
                    } ?: run {
                        _cmsDataState.value = UIState.Error("CMS data not found.")
                    }
                }
                else -> {
                    _cmsDataState.value = UIState.Error(result.errorMessage?:"Something went wrong.")
                }
            }
        }
    }

    fun resetUICMSDataState(){
        _cmsDataState.value= UIState.Idle
    }

}