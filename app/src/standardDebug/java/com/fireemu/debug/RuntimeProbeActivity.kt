package com.fireemu.debug

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.fireemu.FireEmuApp
import com.fireemu.domain.models.ConsoleType
import com.fireemu.game.EmulatorActivity
import java.io.File

/** Debug-only, original-cartridge probe. Never accepts arbitrary paths or core URLs. */
class RuntimeProbeActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val cores = FireEmuApp.instance.coreRepository
        val path = cores.getCorePath(ConsoleType.GAMEBOY)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 32)
            setBackgroundColor(0xFF0B0F1A.toInt())
        }
        root.addView(TextView(this).apply {
            text = "FireSpot · Phase 0 runtime check\n\n${Build.MANUFACTURER} ${Build.MODEL} · API ${Build.VERSION.SDK_INT}\n" +
                "Process: ${if (Process.is64Bit()) "64" else "32"}-bit · ABIs: ${Build.SUPPORTED_ABIS.joinToString()}\n\n" +
                "Core: $path\nInstalled: ${File(path).isFile}\n\n" +
                "FireSpot Signal is an original hardware test, not a commercial game.\n" +
                "D-pad scrolls the pattern; Select / Play-Pause trigger audio; Menu pauses.\n" +
                "This checks runtime behavior. It does not certify Fire TV compatibility on other devices."
            setTextColor(-1)
            textSize = 18f
        })
        root.addView(Button(this).apply {
            text = "Import Phase 0 Game Boy core"
            setOnClickListener {
                try {
                    startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT)
                        .addCategory(Intent.CATEGORY_OPENABLE).setType("*/*"), 100)
                } catch (_: android.content.ActivityNotFoundException) {
                    android.widget.Toast.makeText(this@RuntimeProbeActivity, "No file picker available. Use the ADB setup instructions.", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        })
        root.addView(Button(this).apply {
            text = "Run Game Boy test"
            setOnClickListener {
                if (!File(path).isFile) {
                    android.app.AlertDialog.Builder(this@RuntimeProbeActivity)
                        .setTitle("Game Boy core required")
                        .setMessage("Install the separate Phase 0 core using the documented ADB setup, then retry.")
                        .setPositiveButton("OK", null).show()
                } else {
                    val rom = File(filesDir, "diagnostics/firespot-signal.gb")
                    rom.parentFile?.mkdirs()
                    assets.open("firespot-signal.gb").use { input -> rom.outputStream().use { input.copyTo(it) } }
                    startActivity(Intent(this@RuntimeProbeActivity, EmulatorActivity::class.java)
                        .putExtra(EmulatorActivity.EXTRA_CONSOLE_ID, "gameboy")
                        .putExtra(EmulatorActivity.EXTRA_ROM_PATH, rom.absolutePath)
                        .putExtra(EmulatorActivity.EXTRA_GAME_TITLE, "FireSpot Signal"))
                }
            }
            requestFocus()
        })
        setContentView(root)
    }
    @Deprecated("Debug-only legacy Activity callback")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 100 || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        val target = File(FireEmuApp.instance.coreRepository.getCorePath(ConsoleType.GAMEBOY))
        val abi = target.parentFile!!.name
        val temp = File(cacheDir, "core-import.tmp")
        Thread {
            val result = runCatching {
                val manifest = org.json.JSONObject(assets.open("phase0-core-manifest.json").bufferedReader().use { it.readText() })
                val entries = manifest.getJSONArray("artifacts")
                val entry = (0 until entries.length()).map { entries.getJSONObject(it) }.first { it.getString("abi") == abi }
                val limit = entry.getLong("bytes")
                val digest = java.security.MessageDigest.getInstance("SHA-256")
                val stream = checkNotNull(contentResolver.openInputStream(uri))
                stream.use { input ->
                    temp.outputStream().use { output ->
                        val buffer = ByteArray(65536)
                        var total = 0L
                        while (true) {
                            val n = input.read(buffer)
                            if (n < 0) break
                            total += n
                            check(total <= limit) { "Core file is larger than the pinned release." }
                            digest.update(buffer, 0, n); output.write(buffer, 0, n)
                        }
                        check(total == limit) { "Incomplete core file." }
                    }
                }
                val hash = digest.digest().joinToString("") { "%02x".format(it) }
                check(hash == entry.getString("sha256")) { "Core does not match the pinned Phase 0 build for $abi." }
                target.parentFile!!.mkdirs()
                // Staging and target live on the same private app data filesystem.
                check(temp.renameTo(target)) { "Could not install core." }
                "Core imported. Run the Game Boy test."
            }
            temp.delete()
            runOnUiThread {
                android.app.AlertDialog.Builder(this).setTitle("Phase 0 core import")
                    .setMessage(result.getOrElse { it.message ?: "Import failed." })
                    .setPositiveButton("OK", null).show()
            }
        }.start()
    }

}
