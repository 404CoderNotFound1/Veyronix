package com.example.autotelemetry.data.repository

import com.example.autotelemetry.data.service.TelemetryStreamService
import com.example.autotelemetry.domain.model.ConnectionState
import com.example.autotelemetry.domain.model.VehicleTelemetry
import com.example.autotelemetry.domain.repository.VehicleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VehicleRepositoryImpl(
    private val streamService: TelemetryStreamService,
) : VehicleRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var streamJob: Job? = null

    private val _telemetry = MutableStateFlow(VehicleTelemetry())
    override val telemetry: StateFlow<VehicleTelemetry> = _telemetry.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 8)
    override val errors: SharedFlow<String> = _errors.asSharedFlow()

    override fun start() {
        if (streamJob?.isActive == true) return
        streamJob = scope.launch {
            streamService.events().collect { event ->
                when (event) {
                    TelemetryStreamService.Event.Connecting -> _connectionState.value = ConnectionState.Connecting
                    TelemetryStreamService.Event.Connected -> _connectionState.value = ConnectionState.Connected
                    TelemetryStreamService.Event.Disconnected -> _connectionState.value = ConnectionState.Disconnected
                    is TelemetryStreamService.Event.ConnectionError -> {
                        _connectionState.value = ConnectionState.Error(event.message)
                    }
                    is TelemetryStreamService.Event.ParseError -> {
                        _errors.emit("Telemetry parse error: ${event.message}")
                    }
                    is TelemetryStreamService.Event.Update -> {
                        _telemetry.value = VehicleTelemetryReducer.reduce(_telemetry.value, event.value)
                    }
                }
            }
        }
    }

    override fun reconnect() {
        streamJob?.cancel()
        streamJob = null
        start()
    }

    override fun stop() {
        scope.cancel()
    }
}
