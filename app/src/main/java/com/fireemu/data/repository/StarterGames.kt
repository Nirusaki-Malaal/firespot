package com.fireemu.data.repository

import android.content.Context
import com.fireemu.domain.models.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File

/** Idempotent seeding preserves the user's favorites, history and saves. */
suspend fun installStarterGames(context: Context, repository: GameRepository) = withContext(Dispatchers.IO) {
    val entries=JSONArray(context.assets.open("starter/games.json").bufferedReader().use {it.readText()})
    val dir=File(context.filesDir,"games/starter").apply {mkdirs()}
    for(i in 0 until entries.length()) {
        val item=entries.getJSONObject(i)
        val file=File(dir,item.getString("file"))
        if(!file.isFile) context.assets.open("starter/${file.name}").use {input->file.outputStream().use {input.copyTo(it)}}
        if(repository.getGameByPath(file.absolutePath)==null) repository.insertOrUpdateGames(listOf(Game(
            title=item.getString("title"),cleanTitle=item.getString("title"),romPath=file.absolutePath,
            consoleId="gameboy",fileSizeBytes=file.length(),genre="Original homebrew",releaseYear=2026,maxPlayers=1,rating="",
            description=item.getString("description")
        )))
    }
}
