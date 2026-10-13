package com.fireemu.data.repository

import com.fireemu.domain.models.Game
import com.fireemu.domain.models.SaveStateInfo

interface SaveStateRepository {
    fun getSaveStatesForGame(game: Game): List<SaveStateInfo>
    fun hasAutoSave(game: Game): Boolean
    fun getAutoSavePath(game: Game): String
    fun getSaveStatePath(game: Game, slot: Int): String
    fun deleteSaveState(game: Game, slot: Int): Boolean
}
