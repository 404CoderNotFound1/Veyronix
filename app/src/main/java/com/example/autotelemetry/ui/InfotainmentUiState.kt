package com.example.autotelemetry.ui

import com.example.autotelemetry.domain.model.VehicleTelemetry

data class InfotainmentUiState(
    val telemetry: VehicleTelemetry = VehicleTelemetry(),
    val connectionLabel: String = "Disconnected",
    val errorMessage: String? = null,
    val notification: String = "All systems normal",
    val climateTemperatureC: Float = 22f,
    val fanAutomatic: Boolean = true,
    val currentTrack: String = "Night Drive — Demo Artist",
    val isPlaying: Boolean = false,
)
