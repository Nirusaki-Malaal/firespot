package com.fireemu.domain.usecases

import com.fireemu.data.repository.GameRepository
import com.fireemu.domain.models.Game
import kotlinx.coroutines.flow.Flow

class GetGamesUseCase(
    private val gameRepository: GameRepository
) {
    fun getAllGames(): Flow<List<Game>> = gameRepository.getAllGames()
    fun getGamesForConsole(consoleId: String): Flow<List<Game>> = gameRepository.getGamesForConsole(consoleId)
    fun getFavoriteGames(): Flow<List<Game>> = gameRepository.getFavoriteGames()
    fun getRecentGames(limit: Int = 10): Flow<List<Game>> = gameRepository.getRecentGames(limit)
    fun getTopPlayedGames(limit: Int = 10): Flow<List<Game>> = gameRepository.getTopPlayedGames(limit)
    fun getRecentlyAddedGames(limit: Int = 10): Flow<List<Game>> = gameRepository.getRecentlyAddedGames(limit)
    fun getGamesByGenre(genre: String): Flow<List<Game>> = gameRepository.getGamesByGenre(genre)
    suspend fun getDistinctGenres(): List<String> = gameRepository.getDistinctGenres()
    suspend fun getGameById(id: Long): Game? = gameRepository.getGameById(id)
    suspend fun getGameCounts(): Map<String, Int> = gameRepository.getGameCountPerConsole()
    suspend fun search(query: String): List<Game> = gameRepository.searchGames(query)
}
