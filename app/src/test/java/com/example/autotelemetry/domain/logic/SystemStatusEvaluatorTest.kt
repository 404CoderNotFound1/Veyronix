package com.example.autotelemetry.domain.logic

import com.example.autotelemetry.domain.model.VehicleTelemetry
import org.junit.Assert.assertEquals
import org.junit.Test

class SystemStatusEvaluatorTest {
    @Test
    fun `moving with door open has highest priority warning`() {
        val message = SystemStatusEvaluator.message(
            VehicleTelemetry(speedKph = 20f, batteryPercent = 5, doorOpen = true, warningActive = true),
        )
        assertEquals("Warning: door open while vehicle is moving", message)
    }

    @Test
    fun `low battery produces charging recommendation`() {
        val message = SystemStatusEvaluator.message(VehicleTelemetry(batteryPercent = 15))
        assertEquals("Low battery: consider charging soon", message)
    }
}
