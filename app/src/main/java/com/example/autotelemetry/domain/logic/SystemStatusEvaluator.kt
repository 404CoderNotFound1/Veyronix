package com.example.autotelemetry.domain.logic

import com.example.autotelemetry.domain.model.VehicleTelemetry

object SystemStatusEvaluator {
    fun message(telemetry: VehicleTelemetry): String = when {
        telemetry.doorOpen && telemetry.speedKph > 0.5f -> "Warning: door open while vehicle is moving"
        telemetry.warningActive -> "Vehicle warning flag active"
        telemetry.batteryPercent <= 15 -> "Low battery: consider charging soon"
        telemetry.charging -> "Vehicle is charging"
        else -> "All systems normal"
    }
}
