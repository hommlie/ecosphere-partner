package com.ecosphere.partner.core.network

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {

    fun isConnected(): Boolean

    val isConnectedFlow: Flow<Boolean>
}