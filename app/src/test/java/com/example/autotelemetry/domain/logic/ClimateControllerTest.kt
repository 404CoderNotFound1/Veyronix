package com.example.autotelemetry.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class ClimateControllerTest {
    private val controller = ClimateController()

    @Test
    fun `temperature changes in half degree steps`() {
        assertEquals(22.5f, controller.increase(22f))
        assertEquals(21.5f, controller.decrease(22f))
    }

    @Test
    fun `temperature is clamped to safe demo range`() {
        assertEquals(30f, controller.increase(30f))
        assertEquals(16f, controller.decrease(16f))
    }
}
