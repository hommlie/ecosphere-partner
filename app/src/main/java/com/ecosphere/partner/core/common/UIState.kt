package com.ecosphere.partner.core.common

sealed interface UIState<out T> {

    data object Idle : UIState<Nothing>

    data object Loading : UIState<Nothing>

    data class Success<T>(val data: T) : UIState<T>

    data object Empty : UIState<Nothing>

    data class Error(val message: String) : UIState<Nothing>

}