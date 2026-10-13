package com.fireemu.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fireemu.domain.models.Game
import com.fireemu.domain.usecases.GetGamesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<Game> = emptyList(),
    val isSearching: Boolean = false
)

class SearchViewModel(
    private val getGamesUseCase: GetGamesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun appendChar(char: Char) {
        val newQuery = _uiState.value.query + char
        updateQuery(newQuery)
    }

    fun backspace() {
        val current = _uiState.value.query
        if (current.isNotEmpty()) {
            val newQuery = current.substring(0, current.length - 1)
            updateQuery(newQuery)
        }
    }

    fun appendSpace() {
        val newQuery = _uiState.value.query + " "
        updateQuery(newQuery)
    }

    fun clearQuery() {
        updateQuery("")
    }

    fun setQuery(newQuery: String) {
        updateQuery(newQuery)
    }

    private fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query, isSearching = query.isNotBlank())
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(results = emptyList(), isSearching = false)
            return
        }

        searchJob = viewModelScope.launch {
            delay(150) // debounce
            try {
                val found = getGamesUseCase.search(query.trim())
                _uiState.value = _uiState.value.copy(results = found, isSearching = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSearching = false)
            }
        }
    }
}
