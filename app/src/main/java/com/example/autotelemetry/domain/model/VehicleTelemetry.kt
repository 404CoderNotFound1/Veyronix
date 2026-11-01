package com.example.autotelemetry.domain.model

data class VehicleTelemetry(
    val speedKph: Float = 0f,
    val batteryPercent: Int = 100,
    val ambientTemperatureC: Float = 20f,
    val doorOpen: Boolean = false,
    val charging: Boolean = false,
    val warningActive: Boolean = false,
)
