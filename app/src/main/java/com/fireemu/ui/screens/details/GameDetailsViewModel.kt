package com.fireemu.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fireemu.data.repository.GameRepository
import com.fireemu.domain.models.Game
import com.fireemu.domain.models.SaveStateInfo
import com.fireemu.domain.usecases.SaveStateUseCase
import com.fireemu.domain.usecases.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class GameDetailsUiState(
    val isLoading: Boolean = false,
    val game: Game? = null,
    val isFavorite: Boolean = false,
    val hasSaveState: Boolean = false,
    val saveStates: List<SaveStateInfo> = emptyList(),
    val relatedGames: List<Game> = emptyList()
)

class GameDetailsViewModel(
    private val gameId: Long,
    private val gameRepository: GameRepository,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val saveStateUseCase: SaveStateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameDetailsUiState(isLoading = true))
    val uiState: StateFlow<GameDetailsUiState> = _uiState.asStateFlow()

    init {
        loadGame()
    }

    private fun loadGame() {
        viewModelScope.launch {
            val game = gameRepository.getGameById(gameId)
            if (game != null) {
                val states = saveStateUseCase.getSaveStatesForGame(game.id)
                val hasAutoOrSlot = states.any { it.exists }
                val related = gameRepository.getGamesForConsole(game.consoleId).firstOrNull()
                    ?.filter { it.id != game.id }
                    ?.take(8) ?: emptyList()

                _uiState.value = GameDetailsUiState(
                    isLoading = false,
                    game = game,
                    isFavorite = game.isFavorite,
                    hasSaveState = hasAutoOrSlot,
                    saveStates = states,
                    relatedGames = related
                )
            } else {
                _uiState.value = GameDetailsUiState(isLoading = false, game = null)
            }
        }
    }

    fun toggleFavorite() {
        val currentGame = _uiState.value.game ?: return
        viewModelScope.launch {
            val updated = !currentGame.isFavorite
            toggleFavoriteUseCase(currentGame.id, updated)
            _uiState.value = _uiState.value.copy(
                isFavorite = updated,
                game = currentGame.copy(isFavorite = updated)
            )
        }
    }
}
