package com.example.autotelemetry.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.autotelemetry.data.repository.VehicleRepositoryImpl
import com.example.autotelemetry.data.service.TelemetryStreamService
import com.example.autotelemetry.R
import com.example.autotelemetry.databinding.ActivityMainBinding
import com.example.autotelemetry.databinding.CardClimateBinding
import com.example.autotelemetry.databinding.CardMediaBinding
import com.example.autotelemetry.databinding.CardNotificationsBinding
import com.example.autotelemetry.databinding.CardVehicleStatusBinding
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_TELEMETRY_HOST = "telemetry_host"
        const val EXTRA_TELEMETRY_PORT = "telemetry_port"
        private const val DEFAULT_HOST = "127.0.0.1"
        private const val DEFAULT_PORT = 5555
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var vehicleStatusBinding: CardVehicleStatusBinding
    private lateinit var mediaBinding: CardMediaBinding
    private lateinit var climateBinding: CardClimateBinding
    private lateinit var notificationsBinding: CardNotificationsBinding

    private val viewModel: MainViewModel by viewModels {
        val host = intent.getStringExtra(EXTRA_TELEMETRY_HOST) ?: DEFAULT_HOST
        val port = intent.getIntExtra(EXTRA_TELEMETRY_PORT, DEFAULT_PORT)
        MainViewModelFactory(
            VehicleRepositoryImpl(
                TelemetryStreamService(host = host, port = port),
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vehicleStatusBinding = CardVehicleStatusBinding.bind(findViewById(R.id.vehicleStatusCard))
        mediaBinding = CardMediaBinding.bind(findViewById(R.id.mediaCard))
        climateBinding = CardClimateBinding.bind(findViewById(R.id.climateCard))
        notificationsBinding = CardNotificationsBinding.bind(findViewById(R.id.notificationsCard))

        binding.reconnectButton.setOnClickListener { viewModel.reconnect() }
        climateBinding.climateUpButton.setOnClickListener { viewModel.increaseClimate() }
        climateBinding.climateDownButton.setOnClickListener { viewModel.decreaseClimate() }
        climateBinding.fanButton.setOnClickListener { viewModel.toggleFanMode() }
        mediaBinding.playPauseButton.setOnClickListener { viewModel.togglePlayback() }
        mediaBinding.nextButton.setOnClickListener { viewModel.nextTrack() }
        mediaBinding.previousButton.setOnClickListener { viewModel.previousTrack() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: InfotainmentUiState) = with(binding) {
        val telemetry = state.telemetry
        connectionText.text = state.connectionLabel
        vehicleStatusBinding.speedText.text = String.format(Locale.US, "%.1f km/h", telemetry.speedKph)
        vehicleStatusBinding.batteryText.text = "Battery: ${telemetry.batteryPercent}%"
        vehicleStatusBinding.ambientTempText.text = String.format(Locale.US, "Ambient: %.1f°C", telemetry.ambientTemperatureC)
        vehicleStatusBinding.vehicleFlagsText.text = buildString {
            append(if (telemetry.doorOpen) "Door open" else "Doors closed")
            append(" • ")
            append(if (telemetry.charging) "Charging" else "Not charging")
        }
        notificationsBinding.notificationText.text = state.notification
        climateBinding.climateTempText.text = String.format(Locale.US, "%.1f°C", state.climateTemperatureC)
        climateBinding.fanButton.text = if (state.fanAutomatic) "Fan: Auto" else "Fan: Manual"
        mediaBinding.trackText.text = state.currentTrack
        mediaBinding.playPauseButton.text = if (state.isPlaying) "Pause" else "Play"
        errorText.text = state.errorMessage.orEmpty()
        errorText.visibility = if (state.errorMessage == null) View.GONE else View.VISIBLE
    }
}
