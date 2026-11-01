package com.example.autotelemetry.data.repository

import com.example.autotelemetry.domain.model.TelemetryUpdate
import com.example.autotelemetry.domain.model.VehicleTelemetry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleTelemetryReducerTest {
    @Test
    fun `individual updates preserve unrelated telemetry fields`() {
        val initial = VehicleTelemetry(
            speedKph = 10f,
            batteryPercent = 80,
            ambientTemperatureC = 19f,
            doorOpen = false,
            charging = true,
            warningActive = false,
        )

        val updated = VehicleTelemetryReducer.reduce(initial, TelemetryUpdate.Speed(55.5f))

        assertEquals(55.5f, updated.speedKph)
        assertEquals(80, updated.batteryPercent)
        assertEquals(19f, updated.ambientTemperatureC)
        assertTrue(updated.charging)
    }

    @Test
    fun `status update maps all status flags`() {
        val updated = VehicleTelemetryReducer.reduce(
            VehicleTelemetry(),
            TelemetryUpdate.Status(doorOpen = true, charging = true, warningActive = true),
        )

        assertTrue(updated.doorOpen)
        assertTrue(updated.charging)
        assertTrue(updated.warningActive)
    }
}
