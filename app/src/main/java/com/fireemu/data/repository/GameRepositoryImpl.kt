package com.fireemu.data.repository

import com.fireemu.data.local.room.GameDao
import com.fireemu.data.local.room.GameEntity
import com.fireemu.domain.models.Game
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepositoryImpl(
    private val gameDao: GameDao
) : GameRepository {

    override fun getAllGames(): Flow<List<Game>> =
        gameDao.getAllGames().map { list -> list.map { it.toDomain() } }

    override fun getGamesForConsole(consoleId: String): Flow<List<Game>> =
        gameDao.getGamesByConsole(consoleId).map { list -> list.map { it.toDomain() } }

    override fun getFavoriteGames(): Flow<List<Game>> =
        gameDao.getFavoriteGames().map { list -> list.map { it.toDomain() } }

    override fun getRecentGames(limit: Int): Flow<List<Game>> =
        gameDao.getRecentGames(limit).map { list -> list.map { it.toDomain() } }

    override fun getTopPlayedGames(limit: Int): Flow<List<Game>> =
        gameDao.getTopPlayedGames(limit).map { list -> list.map { it.toDomain() } }

    override fun getRecentlyAddedGames(limit: Int): Flow<List<Game>> =
        gameDao.getRecentlyAddedGames(limit).map { list -> list.map { it.toDomain() } }

    override fun getGamesByGenre(genre: String): Flow<List<Game>> =
        gameDao.getGamesByGenre(genre).map { list -> list.map { it.toDomain() } }

    override suspend fun getDistinctGenres(): List<String> =
        gameDao.getDistinctGenres()

    override suspend fun getGameById(id: Long): Game? =
        gameDao.getGameById(id)?.toDomain()

    override suspend fun getGameByPath(romPath: String): Game? =
        gameDao.getGameByPath(romPath)?.toDomain()

    override suspend fun insertOrUpdateGames(games: List<Game>) {
        val entities = games.map { GameEntity.fromDomain(it) }
        gameDao.insertOrUpdate(entities)
    }

    override suspend fun updateFavorite(gameId: Long, isFavorite: Boolean) {
        gameDao.updateFavorite(gameId, isFavorite)
    }

    override suspend fun updatePlayStats(gameId: Long, sessionSeconds: Long) {
        val now = System.currentTimeMillis()
        gameDao.updatePlayStats(gameId, now, sessionSeconds)
    }

    override suspend fun deleteGame(gameId: Long) {
        gameDao.deleteGame(gameId)
    }

    override suspend fun searchGames(query: String): List<Game> =
        gameDao.searchGames(query).map { it.toDomain() }

    override suspend fun getGameCountPerConsole(): Map<String, Int> {
        val counts = gameDao.getCountsByConsole()
        return counts.associate { it.consoleId to it.count }
    }
}
