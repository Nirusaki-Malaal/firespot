package com.fireemu.data.local.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fireemu.domain.models.Game

@Entity(
    tableName = "games",
    indices = [
        Index(value = ["romPath"], unique = true),
        Index(value = ["consoleId"]),
        Index(value = ["isFavorite"]),
        Index(value = ["lastPlayedTimestamp"]),
        Index(value = ["genre"])
    ]
)
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val cleanTitle: String,
    val romPath: String,
    val consoleId: String,
    val coverArtPath: String?,
    val fileSizeBytes: Long,
    val crc32: String,
    val isFavorite: Boolean,
    val lastPlayedTimestamp: Long,
    val playTimeSeconds: Long,
    val dateAddedTimestamp: Long,
    val genre: String = "Action",
    val releaseYear: Int = 1995,
    val maxPlayers: Int = 2,
    val rating: String = "4.8",
    val description: String = "Experience this classic retro masterpiece on FireSpot with enhanced low-latency emulation."
) {
    fun toDomain(): Game = Game(
        id = id,
        title = title,
        cleanTitle = cleanTitle,
        romPath = romPath,
        consoleId = consoleId,
        coverArtPath = coverArtPath,
        fileSizeBytes = fileSizeBytes,
        crc32 = crc32,
        isFavorite = isFavorite,
        lastPlayedTimestamp = lastPlayedTimestamp,
        playTimeSeconds = playTimeSeconds,
        dateAddedTimestamp = dateAddedTimestamp,
        genre = genre,
        releaseYear = releaseYear,
        maxPlayers = maxPlayers,
        rating = rating,
        description = description
    )

    companion object {
        fun fromDomain(domain: Game): GameEntity = GameEntity(
            id = domain.id,
            title = domain.title,
            cleanTitle = domain.cleanTitle,
            romPath = domain.romPath,
            consoleId = domain.consoleId,
            coverArtPath = domain.coverArtPath,
            fileSizeBytes = domain.fileSizeBytes,
            crc32 = domain.crc32,
            isFavorite = domain.isFavorite,
            lastPlayedTimestamp = domain.lastPlayedTimestamp,
            playTimeSeconds = domain.playTimeSeconds,
            dateAddedTimestamp = domain.dateAddedTimestamp,
            genre = domain.genre,
            releaseYear = domain.releaseYear,
            maxPlayers = domain.maxPlayers,
            rating = domain.rating,
            description = domain.description
        )
    }
}
