package com.fireemu.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fireemu.domain.models.ConsoleType
import com.fireemu.domain.models.Game
import com.fireemu.domain.usecases.GetGamesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LibraryUiState(
    val console: ConsoleType = ConsoleType.NES,
    val games: List<Game> = emptyList(),
    val isLoading: Boolean = true
)

class LibraryViewModel(
    private val consoleId: String,
    private val getGamesUseCase: GetGamesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState(console = ConsoleType.fromId(consoleId)))
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadGames()
    }

    private fun loadGames() {
        viewModelScope.launch {
            getGamesUseCase.getGamesForConsole(consoleId).collect { gamesList ->
                _uiState.value = _uiState.value.copy(
                    games = gamesList,
                    isLoading = false
                )
            }
        }
    }
}
