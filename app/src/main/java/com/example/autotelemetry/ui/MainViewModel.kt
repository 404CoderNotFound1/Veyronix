package com.example.autotelemetry.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autotelemetry.domain.logic.ClimateController
import com.example.autotelemetry.domain.logic.SystemStatusEvaluator
import com.example.autotelemetry.domain.model.ConnectionState
import com.example.autotelemetry.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: VehicleRepository,
    private val climateController: ClimateController = ClimateController(),
) : ViewModel() {
    private val tracks = listOf(
        "Night Drive — Demo Artist",
        "Open Highway — Sample Band",
        "Electric Dawn — Portfolio Mix",
    )
    private var trackIndex = 0

    private val _uiState = MutableStateFlow(InfotainmentUiState())
    val uiState: StateFlow<InfotainmentUiState> = _uiState.asStateFlow()

    init {
        observeRepository()
        repository.start()
    }

    private fun observeRepository() {
        viewModelScope.launch {
            repository.telemetry.collect { telemetry ->
                _uiState.update {
                    it.copy(
                        telemetry = telemetry,
                        notification = SystemStatusEvaluator.message(telemetry),
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.connectionState.collect { state ->
                _uiState.update { current ->
                    when (state) {
                        ConnectionState.Connected -> current.copy(connectionLabel = "Connected", errorMessage = null)
                        ConnectionState.Connecting -> current.copy(connectionLabel = "Connecting…")
                        ConnectionState.Disconnected -> current.copy(connectionLabel = "Disconnected")
                        is ConnectionState.Error -> current.copy(
                            connectionLabel = "Connection error",
                            errorMessage = "Telemetry connection error: ${state.message}",
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            repository.errors.collect { error ->
                _uiState.update { it.copy(errorMessage = error) }
            }
        }
    }

    fun reconnect() {
        _uiState.update { it.copy(errorMessage = null) }
        repository.reconnect()
    }

    fun increaseClimate() {
        _uiState.update { it.copy(climateTemperatureC = climateController.increase(it.climateTemperatureC)) }
    }

    fun decreaseClimate() {
        _uiState.update { it.copy(climateTemperatureC = climateController.decrease(it.climateTemperatureC)) }
    }

    fun toggleFanMode() {
        _uiState.update { it.copy(fanAutomatic = !it.fanAutomatic) }
    }

    fun togglePlayback() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun nextTrack() {
        trackIndex = (trackIndex + 1) % tracks.size
        _uiState.update { it.copy(currentTrack = tracks[trackIndex]) }
    }

    fun previousTrack() {
        trackIndex = (trackIndex - 1 + tracks.size) % tracks.size
        _uiState.update { it.copy(currentTrack = tracks[trackIndex]) }
    }

    override fun onCleared() {
        repository.stop()
        super.onCleared()
    }
}
