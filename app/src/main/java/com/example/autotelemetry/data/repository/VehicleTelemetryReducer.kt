package com.example.autotelemetry.data.repository

import com.example.autotelemetry.domain.model.TelemetryUpdate
import com.example.autotelemetry.domain.model.VehicleTelemetry

object VehicleTelemetryReducer {
    fun reduce(current: VehicleTelemetry, update: TelemetryUpdate): VehicleTelemetry = when (update) {
        is TelemetryUpdate.Speed -> current.copy(speedKph = update.kph)
        is TelemetryUpdate.Battery -> current.copy(batteryPercent = update.percent)
        is TelemetryUpdate.AmbientTemperature -> current.copy(ambientTemperatureC = update.celsius)
        is TelemetryUpdate.Status -> current.copy(
            doorOpen = update.doorOpen,
            charging = update.charging,
            warningActive = update.warningActive,
        )
    }
}
