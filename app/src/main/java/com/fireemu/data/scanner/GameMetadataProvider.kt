package com.fireemu.data.scanner

import java.util.Locale

object GameMetadataProvider {

    data class GameMeta(
        val genre: String,
        val year: Int,
        val players: Int,
        val rating: String,
        val description: String
    )

    private val TITLE_META_MAP = mapOf(
        "super mario bros" to GameMeta(
            genre = "Platformer",
            year = 1985,
            players = 2,
            rating = "4.9",
            description = "Rescue Princess Peach from Bowser in the defining NES side-scrolling platformer."
        ),
        "super mario land" to GameMeta(
            genre = "Platformer",
            year = 1989,
            players = 1,
            rating = "4.6",
            description = "Journey through Sarasaland as Mario to rescue Princess Daisy from Tatanga."
        ),
        "the legend of zelda" to GameMeta(
            genre = "Action RPG",
            year = 1986,
            players = 1,
            rating = "4.9",
            description = "Explore the vast kingdom of Hyrule, conquer dungeons, and assemble the Triforce."
        ),
        "zelda" to GameMeta(
            genre = "Action RPG",
            year = 1993,
            players = 1,
            rating = "4.8",
            description = "Awaken the Wind Fish on mysterious Koholint Island in this legendary top-down quest."
        ),
        "metroid" to GameMeta(
            genre = "Shooter",
            year = 1986,
            players = 1,
            rating = "4.7",
            description = "Infiltrate planet Zebes as bounty hunter Samus Aran to neutralize the Space Pirates."
        ),
        "metroid fusion" to GameMeta(
            genre = "Platformer",
            year = 2002,
            players = 1,
            rating = "4.9",
            description = "Battle the deadly X Parasite aboard the B.S.L. station in this thrilling GBA saga."
        ),
        "mega man 2" to GameMeta(
            genre = "Action",
            year = 1988,
            players = 1,
            rating = "4.9",
            description = "Defeat eight Robot Masters and stop Dr. Wily with iconic weapon swapping."
        ),
        "pokemon - red version" to GameMeta(
            genre = "RPG",
            year = 1996,
            players = 2,
            rating = "4.9",
            description = "Catch, train, and battle 151 Pokémon across the Kanto region to become Champion."
        ),
        "pokemon - emerald version" to GameMeta(
            genre = "RPG",
            year = 2004,
            players = 2,
            rating = "5.0",
            description = "Stop Team Aqua and Team Magma and summon Rayquaza across the vibrant Hoenn region."
        ),
        "tetris" to GameMeta(
            genre = "Puzzle",
            year = 1989,
            players = 2,
            rating = "4.8",
            description = "Rotate and stack falling tetrominoes in the timeless puzzle phenomenon."
        ),
        "advance wars" to GameMeta(
            genre = "Strategy",
            year = 2001,
            players = 4,
            rating = "4.8",
            description = "Master turn-based tactical warfare across varied terrain with specialized Commanding Officers."
        ),
        "castlevania" to GameMeta(
            genre = "Action",
            year = 1997,
            players = 1,
            rating = "5.0",
            description = "Explore Dracula's inverted castle as Alucard in the pinnacle of action RPG exploration."
        ),
        "crash bandicoot" to GameMeta(
            genre = "Platformer",
            year = 1996,
            players = 1,
            rating = "4.7",
            description = "Spin and leap through Wumpa Islands to stop Dr. Neo Cortex and save Tawna."
        ),
        "final fantasy vii" to GameMeta(
            genre = "RPG",
            year = 1997,
            players = 1,
            rating = "5.0",
            description = "Join Cloud Strife and AVALANCHE to prevent Sephiroth from destroying Gaia with Meteor."
        ),
        "god of war" to GameMeta(
            genre = "Action",
            year = 2010,
            players = 1,
            rating = "4.9",
            description = "Unleash Spartan fury as Kratos confronts his tragic past in ancient Atlantis and Sparta."
        ),
        "crisis core" to GameMeta(
            genre = "Action RPG",
            year = 2007,
            players = 1,
            rating = "4.8",
            description = "Follow SOLDIER operative Zack Fair on the heroic journey preceding Final Fantasy VII."
        ),
        "monster hunter" to GameMeta(
            genre = "Action RPG",
            year = 2008,
            players = 4,
            rating = "4.7",
            description = "Track, trap, and hunt colossal wyverns and beasts with deep crafting and weapons."
        )
    )

    fun resolve(cleanTitle: String, consoleId: String): GameMeta {
        val lower = cleanTitle.lowercase(Locale.ROOT)
        for ((key, meta) in TITLE_META_MAP) {
            if (lower.contains(key)) return meta
        }

        // Genre inference by keywords
        val inferredGenre = when {
            lower.contains("kart") || lower.contains("racing") || lower.contains("speed") || lower.contains("grand prix") -> "Racing"
            lower.contains("fighter") || lower.contains("tekken") || lower.contains("kombat") || lower.contains("street") -> "Fighting"
            lower.contains("soccer") || lower.contains("fifa") || lower.contains("nba") || lower.contains("madden") || lower.contains("tennis") -> "Sports"
            lower.contains("puzzle") || lower.contains("dr.") || lower.contains("dr ") || lower.contains("blocks") -> "Puzzle"
            lower.contains("wars") || lower.contains("tactics") || lower.contains("strategy") -> "Strategy"
            lower.contains("fantasy") || lower.contains("quest") || lower.contains("dragon") || lower.contains("tales") -> "RPG"
            lower.contains("mario") || lower.contains("sonic") || lower.contains("donkey") || lower.contains("jump") || lower.contains("land") -> "Platformer"
            lower.contains("shooter") || lower.contains("gun") || lower.contains("doom") || lower.contains("invaders") -> "Shooter"
            else -> "Action"
        }

        val inferredYear = when (consoleId) {
            "nes" -> 1987
            "gameboy" -> 1992
            "gba" -> 2002
            "ps1" -> 1998
            "psp" -> 2006
            else -> 1995
        }

        return GameMeta(
            genre = inferredGenre,
            year = inferredYear,
            players = 2,
            rating = "4.8",
            description = "Enjoy $cleanTitle on FireSpot with full controller mapping, instant save states, and HD display scaling."
        )
    }
}
