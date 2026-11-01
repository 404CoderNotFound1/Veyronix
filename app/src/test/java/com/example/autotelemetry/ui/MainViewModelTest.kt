package com.example.autotelemetry.ui

import com.example.autotelemetry.domain.model.ConnectionState
import com.example.autotelemetry.domain.model.VehicleTelemetry
import com.example.autotelemetry.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `telemetry updates transition view model state`() {
        val repository = FakeVehicleRepository()
        val viewModel = MainViewModel(repository)

        repository.telemetryFlow.value = VehicleTelemetry(
            speedKph = 42.5f,
            batteryPercent = 14,
            ambientTemperatureC = 23f,
        )

        assertEquals(42.5f, viewModel.uiState.value.telemetry.speedKph)
        assertEquals("Low battery: consider charging soon", viewModel.uiState.value.notification)
        assertTrue(repository.started)
    }

    @Test
    fun `media and climate actions update local interface state`() {
        val viewModel = MainViewModel(FakeVehicleRepository())
        viewModel.increaseClimate()
        viewModel.togglePlayback()
        viewModel.nextTrack()

        assertEquals(22.5f, viewModel.uiState.value.climateTemperatureC)
        assertTrue(viewModel.uiState.value.isPlaying)
        assertEquals("Open Highway — Sample Band", viewModel.uiState.value.currentTrack)
    }

    @Test
    fun `connection error is shown and reconnect delegates to repository`() {
        val repository = FakeVehicleRepository()
        val viewModel = MainViewModel(repository)

        repository.connectionFlow.value = ConnectionState.Error("Connection refused")

        assertEquals("Connection error", viewModel.uiState.value.connectionLabel)
        assertTrue(viewModel.uiState.value.errorMessage!!.contains("Connection refused"))

        viewModel.reconnect()

        assertEquals(1, repository.reconnectCount)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    private class FakeVehicleRepository : VehicleRepository {
        val telemetryFlow = MutableStateFlow(VehicleTelemetry())
        val connectionFlow = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
        val errorFlow = MutableSharedFlow<String>()
        var started = false
        var reconnectCount = 0

        override val telemetry: StateFlow<VehicleTelemetry> = telemetryFlow
        override val connectionState: StateFlow<ConnectionState> = connectionFlow
        override val errors: SharedFlow<String> = errorFlow

        override fun start() { started = true }
        override fun reconnect() {
            started = true
            reconnectCount++
        }
        override fun stop() = Unit
    }
}
