package com.example.autotelemetry.data.parser

import com.example.autotelemetry.domain.model.TelemetryUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CanFrameParserTest {
    private val parser = CanFrameParser()

    @Test
    fun `speed frame maps tenths of kph`() {
        assertEquals(TelemetryUpdate.Speed(123.4f), parser.parse("100#04D2"))
    }

    @Test
    fun `battery frame maps percentage`() {
        assertEquals(TelemetryUpdate.Battery(87), parser.parse("101#57"))
    }

    @Test
    fun `temperature frame handles signed values`() {
        assertEquals(TelemetryUpdate.AmbientTemperature(-5.5f), parser.parse("102#FFC9"))
    }

    @Test
    fun `status bitfield maps door charging and warning flags`() {
        val update = parser.parse("103#07") as TelemetryUpdate.Status
        assertTrue(update.doorOpen)
        assertTrue(update.charging)
        assertTrue(update.warningActive)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid battery value is rejected`() {
        parser.parse("101#FF")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `malformed line is rejected`() {
        parser.parse("not-a-frame")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `odd length payload is rejected`() {
        parser.parse("100#123")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `unsupported identifier is rejected`() {
        parser.parse("200#00")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `speed above protocol limit is rejected`() {
        parser.parse("100#0BB9")
    }
}
