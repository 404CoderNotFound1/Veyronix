package com.example.autotelemetry.domain.model

sealed interface TelemetryUpdate {
    data class Speed(val kph: Float) : TelemetryUpdate
    data class Battery(val percent: Int) : TelemetryUpdate
    data class AmbientTemperature(val celsius: Float) : TelemetryUpdate
    data class Status(
        val doorOpen: Boolean,
        val charging: Boolean,
        val warningActive: Boolean,
    ) : TelemetryUpdate
}
