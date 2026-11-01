package com.example.autotelemetry.domain.repository

import com.example.autotelemetry.domain.model.ConnectionState
import com.example.autotelemetry.domain.model.VehicleTelemetry
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface VehicleRepository {
    val telemetry: StateFlow<VehicleTelemetry>
    val connectionState: StateFlow<ConnectionState>
    val errors: SharedFlow<String>

    fun start()
    fun reconnect()
    fun stop()
}
