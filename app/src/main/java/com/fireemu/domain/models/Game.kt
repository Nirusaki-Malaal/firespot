package com.fireemu.domain.models

data class Game(
    val id: Long = 0,
    val title: String,
    val cleanTitle: String,
    val romPath: String,
    val consoleId: String,
    val coverArtPath: String? = null,
    val fileSizeBytes: Long = 0L,
    val crc32: String = "",
    val isFavorite: Boolean = false,
    val lastPlayedTimestamp: Long = 0L,
    val playTimeSeconds: Long = 0L,
    val dateAddedTimestamp: Long = System.currentTimeMillis(),
    val genre: String = "Action",
    val releaseYear: Int = 1995,
    val maxPlayers: Int = 2,
    val rating: String = "4.8",
    val description: String = "Experience this classic retro masterpiece on FireSpot with enhanced low-latency emulation."
) {
    val console: ConsoleType
        get() = ConsoleType.fromId(consoleId)

    val formattedPlayTime: String
        get() {
            if (playTimeSeconds == 0L) return "Never played"
            val hours = playTimeSeconds / 3600
            val minutes = (playTimeSeconds % 3600) / 60
            return when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m"
                else -> "< 1m"
            }
        }

    val formattedLastPlayed: String
        get() {
            if (lastPlayedTimestamp == 0L) return "Never"
            val diffMs = System.currentTimeMillis() - lastPlayedTimestamp
            val diffDays = diffMs / (1000 * 60 * 60 * 24)
            return when {
                diffDays == 0L -> "Today"
                diffDays == 1L -> "Yesterday"
                diffDays < 7L -> "$diffDays days ago"
                else -> "${diffDays / 7} weeks ago"
            }
        }
}
