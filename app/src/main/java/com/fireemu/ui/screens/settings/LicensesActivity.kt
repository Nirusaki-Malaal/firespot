package com.fireemu.ui.screens.settings

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.fireemu.R

class LicensesActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_licenses)

        val btnBack = findViewById<Button>(R.id.btn_licenses_back)
        btnBack?.setOnClickListener { finish() }

        val tvLicenses = findViewById<TextView>(R.id.tv_licenses_body)
        tvLicenses?.text = """
            FireEmu Suite Open Source Licenses & Acknowledgements:
            
            1. FCEUmm (NES)
               License: GNU General Public License v2.0
               Upstream: https://github.com/libretro/libretro-fceumm
               
            2. Gambatte (Game Boy / Game Boy Color)
               License: GNU General Public License v2.0
               Upstream: https://github.com/libretro/gambatte-libretro
               
            3. mGBA (Game Boy Advance)
               License: Mozilla Public License 2.0 (MPL 2.0)
               Upstream: https://github.com/libretro/mgba
               
            4. PCSX ReARMed (Sony PlayStation 1)
               License: GNU General Public License v2.0
               Upstream: https://github.com/libretro/pcsx_rearmed
               
            5. PPSSPP (PlayStation Portable)
               License: GNU General Public License v2.0+
               Upstream: https://github.com/hrydgard/ppsspp
               
            Engineered exclusively for Amazon Fire TV (Fire OS 7 / 8).
        """.trimIndent()
    }
}
