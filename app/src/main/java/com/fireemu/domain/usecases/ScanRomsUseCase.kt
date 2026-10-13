package com.fireemu.domain.usecases

import com.fireemu.data.repository.GameRepository
import com.fireemu.data.repository.SettingsRepository
import com.fireemu.data.scanner.RomScanner
import com.fireemu.domain.models.Game
import kotlinx.coroutines.flow.first

class ScanRomsUseCase(
    private val gameRepository: GameRepository,
    private val settingsRepository: SettingsRepository,
    private val romScanner: RomScanner
) {
    suspend operator fun invoke(): List<Game> {
        val settings = settingsRepository.getSettings().first()
        val scannedGames = romScanner.scanFolders(settings.romDirectories)
        if (scannedGames.isNotEmpty()) {
            gameRepository.insertOrUpdateGames(scannedGames)
        }
        return scannedGames
    }
}
