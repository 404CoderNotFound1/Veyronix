package com.example.autotelemetry.data.service

import android.util.Log
import com.example.autotelemetry.data.network.TelemetryTcpClient
import com.example.autotelemetry.data.parser.CanFrameParser
import com.example.autotelemetry.domain.model.TelemetryUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

class TelemetryStreamService(
    private val host: String,
    private val port: Int,
    private val parser: CanFrameParser = CanFrameParser(),
) {
    sealed interface Event {
        data object Connecting : Event
        data object Connected : Event
        data object Disconnected : Event
        data class Update(val value: TelemetryUpdate) : Event
        data class ParseError(val message: String) : Event
        data class ConnectionError(val message: String) : Event
    }

    fun events(): Flow<Event> = channelFlow {
        send(Event.Connecting)
        try {
            val client = TelemetryTcpClient(host, port)
            client.stream(
                onConnected = { send(Event.Connected) },
                onLine = { line ->
                    try {
                        send(Event.Update(parser.parse(line)))
                    } catch (e: IllegalArgumentException) {
                        val message = e.message ?: "Invalid telemetry frame"
                        Log.w(TAG, "Rejected telemetry frame '$line': $message")
                        send(Event.ParseError(message))
                    }
                },
            )
            send(Event.Disconnected)
        } catch (e: Exception) {
            val message = e.message ?: e::class.java.simpleName
            Log.e(TAG, "Telemetry connection failed for $host:$port: $message", e)
            send(Event.ConnectionError(message))
        }
    }

    private companion object {
        const val TAG = "TelemetryStreamService"
    }
}
