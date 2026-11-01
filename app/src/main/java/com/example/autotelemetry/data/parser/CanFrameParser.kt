package com.example.autotelemetry.data.parser

import com.example.autotelemetry.data.protocol.TelemetryProtocol
import com.example.autotelemetry.domain.model.TelemetryUpdate

class CanFrameParser {
    fun parse(line: String): TelemetryUpdate {
        val trimmed = line.trim()
        val parts = trimmed.split('#')
        require(parts.size == 2) { "Expected CAN frame in ID#DATA format" }
        require(parts[0].isNotBlank()) { "CAN identifier is missing" }

        val id = parts[0].toIntOrNull(16)
            ?: throw IllegalArgumentException("CAN identifier must be hexadecimal")
        val payloadHex = parts[1]
        require(payloadHex.length % 2 == 0) { "CAN payload must contain complete bytes" }
        require(payloadHex.matches(Regex("[0-9A-Fa-f]*"))) { "CAN payload must be hexadecimal" }

        val bytes = payloadHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        return when (id) {
            TelemetryProtocol.SPEED_ID -> parseSpeed(bytes)
            TelemetryProtocol.BATTERY_ID -> parseBattery(bytes)
            TelemetryProtocol.AMBIENT_TEMP_ID -> parseAmbientTemperature(bytes)
            TelemetryProtocol.STATUS_ID -> parseStatus(bytes)
            else -> throw IllegalArgumentException("Unsupported CAN identifier: 0x${id.toString(16).uppercase()}")
        }
    }

    private fun parseSpeed(bytes: ByteArray): TelemetryUpdate.Speed {
        require(bytes.size == 2) { "Speed frame must contain 2 bytes" }
        val raw = TelemetryProtocol.unsigned16BigEndian(bytes[0], bytes[1])
        require(raw <= 3000) { "Speed exceeds 300.0 km/h protocol limit" }
        return TelemetryUpdate.Speed(raw / 10f)
    }

    private fun parseBattery(bytes: ByteArray): TelemetryUpdate.Battery {
        require(bytes.size == 1) { "Battery frame must contain 1 byte" }
        val percent = TelemetryProtocol.unsigned(bytes[0])
        require(percent in 0..100) { "Battery percentage must be between 0 and 100" }
        return TelemetryUpdate.Battery(percent)
    }

    private fun parseAmbientTemperature(bytes: ByteArray): TelemetryUpdate.AmbientTemperature {
        require(bytes.size == 2) { "Temperature frame must contain 2 bytes" }
        val raw = TelemetryProtocol.signed16BigEndian(bytes[0], bytes[1])
        val celsius = raw / 10f
        require(celsius in -50f..80f) { "Ambient temperature is outside the supported range" }
        return TelemetryUpdate.AmbientTemperature(celsius)
    }

    private fun parseStatus(bytes: ByteArray): TelemetryUpdate.Status {
        require(bytes.size == 1) { "Status frame must contain 1 byte" }
        val flags = TelemetryProtocol.unsigned(bytes[0])
        return TelemetryUpdate.Status(
            doorOpen = flags and 0b0000_0001 != 0,
            charging = flags and 0b0000_0010 != 0,
            warningActive = flags and 0b0000_0100 != 0,
        )
    }
}
