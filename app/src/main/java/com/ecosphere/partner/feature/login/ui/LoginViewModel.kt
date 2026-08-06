package com.ecosphere.partner.feature.login.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosphere.partner.core.common.UIState
import com.ecosphere.partner.core.network.ApiResult
import com.ecosphere.partner.core.notification.FcmTokenProvider
import com.ecosphere.partner.feature.login.model.LoginData
import com.ecosphere.partner.feature.login.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository : AuthRepository,
    private val fcmTokenProvider: FcmTokenProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState<LoginData>>(UIState.Idle)
    val uiState: StateFlow<UIState<LoginData>> = _uiState

    private val _enteredEmail = MutableStateFlow("")
    val enteredEmail: StateFlow<String> = _enteredEmail

    private val _enteredPassword = MutableStateFlow("")
    val enteredPassword: StateFlow<String> = _enteredPassword

    private val _firebasetoken = MutableStateFlow("")
    val firebasetoken: StateFlow<String> = _firebasetoken

    init {
        fetchTokenAndSendToServer()
    }

    fun onEmailChanged(number: String) {
        _enteredEmail.value = number
    }
    fun onPasswordChanged(number: String) {
        _enteredPassword.value = number
    }

    fun loginUser() {
        val hashMap = HashMap<String,String>()
        hashMap["vehicleNumber"] = enteredEmail.value
        hashMap["password"] = enteredPassword.value
        hashMap["firebase_token"] = firebasetoken.value

        viewModelScope.launch {
            _uiState.value = UIState.Loading

            when (val result = repository.login(hashMap)) {
                is ApiResult.Success -> {

                    result.data?.let {
                        _uiState.value = UIState.Success(it)
                    } ?: run {
                        _uiState.value =
                            UIState.Error("Login data not found.")
                    }
                }
                else -> {
                    _uiState.value = UIState.Error(result.errorMessage?:"Something went wrong.")
                }

            }
        }
    }


    fun fetchTokenAndSendToServer() {
        viewModelScope.launch {
            try {
                _firebasetoken.value = fcmTokenProvider.getToken()
                Log.d("FCM_TOKEN", firebasetoken.value)
            } catch (e: Exception) {
                Log.e("FCM_TOKEN", "Failed: ${e.message}")
            }
        }
    }

    fun resetUIState() {
        _uiState.value = UIState.Idle
    }


}