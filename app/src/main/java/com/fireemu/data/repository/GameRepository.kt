package com.fireemu.data.repository

import com.fireemu.domain.models.Game
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun getAllGames(): Flow<List<Game>>
    fun getGamesForConsole(consoleId: String): Flow<List<Game>>
    fun getFavoriteGames(): Flow<List<Game>>
    fun getRecentGames(limit: Int = 10): Flow<List<Game>>
    fun getTopPlayedGames(limit: Int = 10): Flow<List<Game>>
    fun getRecentlyAddedGames(limit: Int = 10): Flow<List<Game>>
    fun getGamesByGenre(genre: String): Flow<List<Game>>
    suspend fun getDistinctGenres(): List<String>
    suspend fun getGameById(id: Long): Game?
    suspend fun getGameByPath(romPath: String): Game?
    suspend fun insertOrUpdateGames(games: List<Game>)
    suspend fun updateFavorite(gameId: Long, isFavorite: Boolean)
    suspend fun updatePlayStats(gameId: Long, sessionSeconds: Long)
    suspend fun deleteGame(gameId: Long)
    suspend fun searchGames(query: String): List<Game>
    suspend fun getGameCountPerConsole(): Map<String, Int>
}
