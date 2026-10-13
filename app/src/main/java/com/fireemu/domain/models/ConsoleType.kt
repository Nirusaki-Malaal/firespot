package com.fireemu.domain.models

enum class ConsoleType(
    val id: String,
    val displayName: String,
    val shortName: String,
    val extensions: List<String>,
    val coreLibName: String,
    val themeColorHex: Long
) {
    NES(
        id = "nes",
        displayName = "Nintendo Entertainment System",
        shortName = "NES",
        extensions = listOf(".nes", ".fds"),
        coreLibName = "libretro_fceumm.so",
        themeColorHex = 0xFFD32F2FL
    ),
    GAMEBOY(
        id = "gameboy",
        displayName = "Game Boy / Game Boy Color",
        shortName = "GB / GBC",
        extensions = listOf(".gb", ".gbc"),
        coreLibName = "libretro_gambatte.so",
        themeColorHex = 0xFF7B1FA2L
    ),
    GBA(
        id = "gba",
        displayName = "Game Boy Advance",
        shortName = "GBA",
        extensions = listOf(".gba"),
        coreLibName = "libretro_mgba.so",
        themeColorHex = 0xFF303F9FL
    ),
    PS1(
        id = "ps1",
        displayName = "Sony PlayStation 1",
        shortName = "PS1",
        extensions = listOf(".cue", ".bin", ".chd", ".pbp", ".iso"),
        coreLibName = "libretro_pcsx_rearmed.so",
        themeColorHex = 0xFF0288D1L
    ),
    PSP(
        id = "psp",
        displayName = "PlayStation Portable",
        shortName = "PSP",
        extensions = listOf(".iso", ".cso", ".pbp"),
        coreLibName = "libretro_ppsspp.so",
        themeColorHex = 0xFF00796BL
    );

    companion object {
        fun fromId(id: String): ConsoleType =
            values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: NES

        fun findByExtension(ext: String): ConsoleType? {
            val lower = ext.lowercase()
            return values().firstOrNull { console ->
                console.extensions.any { it.equals(lower, ignoreCase = true) }
            }
        }
    }
}
