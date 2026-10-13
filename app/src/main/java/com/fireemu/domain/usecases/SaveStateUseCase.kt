package com.fireemu.domain.usecases

import com.fireemu.data.repository.SaveStateRepository
import com.fireemu.domain.models.Game
import com.fireemu.domain.models.SaveStateInfo

class SaveStateUseCase(
    private val saveStateRepository: SaveStateRepository
) {
    fun getStates(game: Game): List<SaveStateInfo> =
        saveStateRepository.getSaveStatesForGame(game)

    fun hasAutoSave(game: Game): Boolean =
        saveStateRepository.hasAutoSave(game)

    fun getAutoSavePath(game: Game): String =
        saveStateRepository.getAutoSavePath(game)

    fun getStatePath(game: Game, slot: Int): String =
        saveStateRepository.getSaveStatePath(game, slot)

    fun deleteState(game: Game, slot: Int): Boolean =
        saveStateRepository.deleteSaveState(game, slot)
}
