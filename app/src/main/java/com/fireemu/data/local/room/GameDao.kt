package com.fireemu.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY cleanTitle ASC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE consoleId = :consoleId ORDER BY cleanTitle ASC")
    fun getGamesByConsole(consoleId: String): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE isFavorite = 1 ORDER BY cleanTitle ASC")
    fun getFavoriteGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE lastPlayedTimestamp > 0 ORDER BY lastPlayedTimestamp DESC LIMIT :limit")
    fun getRecentGames(limit: Int): Flow<List<GameEntity>>

    @Query("SELECT * FROM games ORDER BY playTimeSeconds DESC LIMIT :limit")
    fun getTopPlayedGames(limit: Int): Flow<List<GameEntity>>

    @Query("SELECT * FROM games ORDER BY dateAddedTimestamp DESC LIMIT :limit")
    fun getRecentlyAddedGames(limit: Int): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE genre = :genre ORDER BY cleanTitle ASC")
    fun getGamesByGenre(genre: String): Flow<List<GameEntity>>

    @Query("SELECT DISTINCT genre FROM games ORDER BY genre ASC")
    suspend fun getDistinctGenres(): List<String>

    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    suspend fun getGameById(id: Long): GameEntity?

    @Query("SELECT * FROM games WHERE romPath = :romPath LIMIT 1")
    suspend fun getGameByPath(romPath: String): GameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(games: List<GameEntity>)

    @Query("UPDATE games SET isFavorite = :isFavorite WHERE id = :gameId")
    suspend fun updateFavorite(gameId: Long, isFavorite: Boolean)

    @Query("UPDATE games SET lastPlayedTimestamp = :timestamp, playTimeSeconds = playTimeSeconds + :sessionSeconds WHERE id = :gameId")
    suspend fun updatePlayStats(gameId: Long, timestamp: Long, sessionSeconds: Long)

    @Query("DELETE FROM games WHERE id = :gameId")
    suspend fun deleteGame(gameId: Long)

    @Query("SELECT * FROM games WHERE cleanTitle LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' ORDER BY cleanTitle ASC")
    suspend fun searchGames(query: String): List<GameEntity>

    @Query("SELECT consoleId, COUNT(*) as count FROM games GROUP BY consoleId")
    suspend fun getCountsByConsole(): List<ConsoleCount>

    data class ConsoleCount(
        val consoleId: String,
        val count: Int
    )
}
