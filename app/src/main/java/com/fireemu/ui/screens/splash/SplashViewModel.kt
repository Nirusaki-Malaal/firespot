package com.fireemu.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fireemu.data.repository.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _navigateToRoute = MutableStateFlow<String?>(null)
    val navigateToRoute: StateFlow<String?> = _navigateToRoute

    init {
        viewModelScope.launch {
            // Guarantee fast splash transition under 1.2s for Fire TV
            com.fireemu.FireEmuApp.instance.prepareStarterGames()
            val settings = settingsRepository.getSettings().first()
            if (settings.isOnboardingCompleted) {
                _navigateToRoute.value = "home"
            } else {
                _navigateToRoute.value = "onboarding"
            }
        }
    }
}
