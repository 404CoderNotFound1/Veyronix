package com.example.autotelemetry.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.coroutines.coroutineContext

class TelemetryTcpClient(
    private val host: String,
    private val port: Int,
    private val connectTimeoutMs: Int = 2_000,
) {
    suspend fun stream(
        onConnected: suspend () -> Unit,
        onLine: suspend (String) -> Unit,
    ) = withContext(Dispatchers.IO) {
        Log.i(TAG, "Connecting to $host:$port")
        Socket().use { socket ->
            socket.connect(InetSocketAddress(host, port), connectTimeoutMs)
            Log.i(TAG, "Connected to $host:$port")
            onConnected()
            BufferedReader(InputStreamReader(socket.getInputStream())).use { reader ->
                while (coroutineContext.isActive) {
                    val line = reader.readLine() ?: break
                    Log.d(TAG, "RX $line")
                    onLine(line)
                }
            }
        }
        Log.i(TAG, "Telemetry socket closed")
    }

    private companion object {
        const val TAG = "TelemetryTcpClient"
    }
}
