package com.example.avaride_1.presentation.screens.inride

import androidx.lifecycle.ViewModel
import com.example.avaride_1.domain.model.LightingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class InRideUiState(
    val destination: String = "Home",
    val remainingMinutes: Int = 12,
    val progress: Float = 0.35f, // 0.0 to 1.0
    val temperature: Int = 22,
    val lightingMode: LightingMode = LightingMode.WARM,
    val isPlaying: Boolean = false
)

class InRideViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(InRideUiState())
    val uiState: StateFlow<InRideUiState> = _uiState.asStateFlow()

    fun updateTemperature(temp: Int) {
        _uiState.update { it.copy(temperature = temp.coerceIn(16, 28)) }
        // In production: Send command to vehicle API
    }

    fun updateLighting(mode: LightingMode) {
        _uiState.update { it.copy(lightingMode = mode) }
        // In production: Send command to vehicle API
    }

    fun toggleMusic() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
        // In production: Integrate with Apple Music API
    }

    // Simulate journey progress (in production, would receive real-time updates)
    fun updateProgress(progress: Float) {
        _uiState.update {
            it.copy(
                progress = progress,
                remainingMinutes = ((1f - progress) * 20).toInt()
            )
        }
    }
}

