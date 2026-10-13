package com.fireemu.launcher.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.fireemu.launcher.R;

public class LicensesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);

        TextView tvContent = findViewById(R.id.tv_licenses_content);
        Button btnBack = findViewById(R.id.btn_licenses_back);

        StringBuilder sb = new StringBuilder();
        sb.append("FIRE EMU SUITE FOR AMAZON FIRE TV\n");
        sb.append("===================================\n\n");

        sb.append("1. FCEUmm (Nintendo Entertainment System / Famicom)\n");
        sb.append("   License: GNU General Public License v2.0 (GPL-2.0)\n");
        sb.append("   Authors: BERO, Xodnizel, CaH4e3, Zeromus, libretro team.\n");
        sb.append("   URL: https://github.com/libretro/libretro-fceumm\n\n");

        sb.append("2. Gambatte (Game Boy & Game Boy Color)\n");
        sb.append("   License: GNU General Public License v2.0 (GPL-2.0)\n");
        sb.append("   Authors: sinamas, blargg, libretro team.\n");
        sb.append("   URL: https://github.com/libretro/gambatte-libretro\n\n");

        sb.append("3. mGBA (Game Boy Advance)\n");
        sb.append("   License: Mozilla Public License 2.0 (MPL-2.0)\n");
        sb.append("   Author: Jeffrey Pfau (endrift) and contributors.\n");
        sb.append("   URL: https://github.com/mgba-emu/mgba / https://github.com/libretro/mgba\n\n");

        sb.append("4. PCSX ReARMed (PlayStation 1)\n");
        sb.append("   License: GNU General Public License v2.0 (GPL-2.0)\n");
        sb.append("   Authors: Notaz, Exophase, Ari64, PCSX Team, libretro team.\n");
        sb.append("   URL: https://github.com/libretro/pcsx_rearmed\n\n");

        sb.append("5. PPSSPP (PlayStation Portable)\n");
        sb.append("   License: GNU General Public License v2.0 or later (GPL-2.0+)\n");
        sb.append("   Author: Henrik Rydgård (hrydgard) and contributors.\n");
        sb.append("   URL: https://github.com/hrydgard/ppsspp\n\n");

        sb.append("6. PlayStation 2 Feasibility Study\n");
        sb.append("   Reference: PCSX2 Team (GPL-3.0/LGPL-3.0) & Play! (BSD-2-Clause).\n");
        sb.append("   Status: Documented and stopped due to Fire TV 32-bit hardware constraints.\n\n");

        sb.append("All Fire OS patches, build scripts, and launcher source code are distributed\n");
        sb.append("under GPL-2.0. No copyrighted ROMs, BIOS files, or commercial assets are included.\n");

        tvContent.setText(sb.toString());

        btnBack.setOnClickListener(v -> finish());
        btnBack.requestFocus();
    }
}
