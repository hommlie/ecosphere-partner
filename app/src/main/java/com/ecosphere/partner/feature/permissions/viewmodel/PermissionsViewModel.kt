package com.ecosphere.partner.feature.permissions.viewmodel

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosphere.partner.R
import com.ecosphere.partner.feature.permissions.manager.PermissionManager
import com.ecosphere.partner.feature.permissions.model.PermissionAction
import com.ecosphere.partner.feature.permissions.model.PermissionCard
import com.ecosphere.partner.feature.permissions.model.PermissionType
import com.ecosphere.partner.feature.permissions.model.PermissionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val permissionManager: PermissionManager
) : ViewModel() {

    private data class PermissionConfig(

        @DrawableRes
        val icon: Int,

        @StringRes
        val title: Int,

        @StringRes
        val description: Int,

        @StringRes
        val note: Int?

    )

    private val permissionConfigs = mapOf(

        PermissionType.LOCATION to
                PermissionConfig(
                    icon = R.drawable.ic_location,
                    title = R.string.permission_location_title,
                    description = R.string.permission_location_description,
                    note = R.string.permission_location_note
                ),

        PermissionType.BACKGROUND_LOCATION to
                PermissionConfig(
                    icon = R.drawable.ic_location_cog,
                    title = R.string.permission_background_title,
                    description = R.string.permission_background_description,
                    note = R.string.permission_background_note
                ),

        PermissionType.NOTIFICATION to
                PermissionConfig(
                    icon = R.drawable.ic_notifications,
                    title = R.string.permission_notification_title,
                    description = R.string.permission_notification_description,
                    note = R.string.permission_notification_note
                ),

        PermissionType.BATTERY_OPTIMIZATION to
                PermissionConfig(
                    icon = R.drawable.ic_battery,
                    title = R.string.permission_battery_title,
                    description = R.string.permission_battery_description,
                    note = R.string.permission_battery_note
                ),

        PermissionType.GPS to PermissionConfig(
            icon = R.drawable.ic_location,
            title = R.string.permission_gps_title,
            description = R.string.permission_gps_description,
            note = R.string.permission_gps_note
        )

    )

    private val _from = MutableStateFlow("")
    val from = _from.asStateFlow()
    fun setFrom(from: String) {
        _from.value = from
    }


    private val _uiState = MutableStateFlow(
        PermissionUiState()
    )
    val uiState = _uiState.asStateFlow()

    private val _actions = MutableSharedFlow<PermissionAction>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val actions = _actions.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {

        val cards = buildPermissionCards()

        val missingPermission = permissionManager.getCurrentMissingPermission()

        _uiState.update {

            it.copy(

                permissions = cards,
                currentPermission = cards.firstOrNull {
                    it.type == missingPermission
                                                      },
                isCompleted = missingPermission == null
            )
        }
        navigateIfEverythingGranted()
    }

    fun onPrimaryButtonClick() {

        viewModelScope.launch {

            if (_uiState.value.isCompleted) {

                _actions.emit(
                    PermissionAction.Finish
                )

                return@launch
            }

            val currentPermission =
                _uiState.value.currentPermission ?: return@launch

            when (currentPermission.type) {

                PermissionType.LOCATION -> {

                    _actions.emit(
                        PermissionAction.RequestRuntimePermission(
                            PermissionType.LOCATION
                        )
                    )

                }

                PermissionType.BACKGROUND_LOCATION -> {

                    _actions.emit(
                        PermissionAction.RequestRuntimePermission(
                            PermissionType.BACKGROUND_LOCATION
                        )
                    )

                }

                PermissionType.NOTIFICATION -> {

                    _actions.emit(
                        PermissionAction.RequestRuntimePermission(
                            PermissionType.NOTIFICATION
                        )
                    )

                }

                PermissionType.BATTERY_OPTIMIZATION -> {

                    _actions.emit(
                        PermissionAction.OpenBatteryOptimizationSettings
                    )
                }
                PermissionType.GPS -> {

                    _actions.emit(
                        PermissionAction.OpenLocationSettings
                    )
                }

            }

        }

    }

    fun onPermissionResult(
        permanentlyDenied: Boolean
    ) {
        if (permanentlyDenied) {
            openAppSettings()
            return

        }
        refresh()
    }

    fun onReturnedFromSettings() {
        viewModelScope.launch {
            delay(500)
            refresh()
        }
    }

    private fun buildPermissionCards(): List<PermissionCard> {

        return permissionManager.getPermissionOrder().map { type ->

            val config = permissionConfigs.getValue(type)

            PermissionCard(
                type = type,
                iconRes = config.icon,
                titleRes = config.title,
                descriptionRes = config.description,
                noteRes = config.note,
                status = permissionManager.getPermissionStatus(type)
            )

        }

    }

    private fun navigateIfEverythingGranted() {

        if (!permissionManager.hasAllRequiredPermissions()) {
            return
        }

        if (!permissionManager.isGpsEnabled()) {

            viewModelScope.launch {
                _actions.emit(
                    PermissionAction.OpenLocationSettings
                )
            }
            return
        }
        viewModelScope.launch {
            _actions.emit(PermissionAction.ResumeTracking)
        }
    }

    private fun openAppSettings() {

        viewModelScope.launch {
            _actions.emit(
                PermissionAction.OpenAppSettings
            )
        }
    }
}