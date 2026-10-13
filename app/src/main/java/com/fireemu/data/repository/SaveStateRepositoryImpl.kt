package com.fireemu.data.repository

import android.content.Context
import com.fireemu.domain.models.Game
import com.fireemu.domain.models.SaveStateInfo
import java.io.File

class SaveStateRepositoryImpl(
    private val context: Context
) : SaveStateRepository {

    private fun getStateDir(game: Game): File {
        val dir = File(context.filesDir, "states/${game.consoleId}")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    override fun getSaveStatesForGame(game: Game): List<SaveStateInfo> {
        val dir = getStateDir(game)
        val romBase = File(game.romPath).name
        val states = mutableListOf<SaveStateInfo>()

        for (slot in 1..4) {
            val stateFile = File(dir, "$romBase.state$slot")
            val thumbFile = File(dir, "$romBase.state$slot.png")
            states.add(
                SaveStateInfo(
                    slot = slot,
                    filePath = stateFile.absolutePath,
                    exists = stateFile.exists() && stateFile.length() > 0,
                    timestamp = if (stateFile.exists()) stateFile.lastModified() else 0L,
                    thumbnailPath = if (thumbFile.exists()) thumbFile.absolutePath else null
                )
            )
        }
        return states
    }

    override fun hasAutoSave(game: Game): Boolean {
        val autoFile = File(getAutoSavePath(game))
        return autoFile.exists() && autoFile.length() > 0
    }

    override fun getAutoSavePath(game: Game): String {
        val dir = getStateDir(game)
        val romBase = File(game.romPath).name
        return File(dir, "$romBase.state99").absolutePath
    }

    override fun getSaveStatePath(game: Game, slot: Int): String {
        val dir = getStateDir(game)
        val romBase = File(game.romPath).name
        return File(dir, "$romBase.state$slot").absolutePath
    }

    override fun deleteSaveState(game: Game, slot: Int): Boolean {
        val file = File(getSaveStatePath(game, slot))
        return if (file.exists()) file.delete() else false
    }
}
