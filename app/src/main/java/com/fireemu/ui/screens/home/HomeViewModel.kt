package com.fireemu.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fireemu.domain.models.Game
import com.fireemu.domain.usecases.GetGamesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val consoleCounts: Map<String, Int> = emptyMap(),
    val allGames: List<Game> = emptyList(),
    val heroGames: List<Game> = emptyList(),
    val recentGames: List<Game> = emptyList(),
    val top10Games: List<Game> = emptyList(),
    val recentlyAdded: List<Game> = emptyList(),
    val favoriteGames: List<Game> = emptyList(),
    val actionGames: List<Game> = emptyList(),
    val platformerGames: List<Game> = emptyList(),
    val rpgGames: List<Game> = emptyList()
)

class HomeViewModel(
    private val getGamesUseCase: GetGamesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private var loadJob: kotlinx.coroutines.Job? = null

    fun loadData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {

                getGamesUseCase.getAllGames().collect { games ->
                    val featured = games.take(5)
                    val recents = games.filter { it.lastPlayedTimestamp > 0 }
                        .sortedByDescending { it.lastPlayedTimestamp }
                    val top10 = games.sortedByDescending { it.playTimeSeconds }.take(10)
                    val recAdded = games.sortedByDescending { it.dateAddedTimestamp }.take(10)
                    val favs = games.filter { it.isFavorite }

                    val action = games.filter { it.genre.equals("Action", ignoreCase = true) || it.genre.equals("Shooter", ignoreCase = true) }
                    val platformers = games.filter { it.genre.equals("Platformer", ignoreCase = true) }
                    val rpgs = games.filter { it.genre.contains("RPG", ignoreCase = true) || it.genre.equals("Strategy", ignoreCase = true) }

                    _uiState.value = HomeUiState(
                        isLoading = false,
                        consoleCounts = games.groupingBy { it.consoleId }.eachCount(),
                        allGames = games,
                        heroGames = if (featured.isNotEmpty()) featured else games,
                        recentGames = recents,
                        top10Games = if (top10.isNotEmpty()) top10 else games.take(10),
                        recentlyAdded = if (recAdded.isNotEmpty()) recAdded else games,
                        favoriteGames = favs,
                        actionGames = action,
                        platformerGames = platformers,
                        rpgGames = rpgs
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Failed to load FireSpot library"
                )
            }
        }
    }
}
