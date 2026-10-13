# License & Attribution Notice: PCSX ReARMed

## Upstream Project
- **Project:** PCSX ReARMed
- **Original Developers:** Notaz, Exophase, Ari64, PCSX Team, PCSX-df team, and libretro team.
- **Upstream URL:** https://github.com/libretro/pcsx_rearmed

## License Terms
PCSX ReARMed is licensed under the **GNU General Public License, Version 2 (GPL-2.0)**:

```
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
```

## Fire OS Integration Notice
All Fire OS performance profiles, Ari64 ARMv7 dynarec integration adjustments, OpenSL ES audio timing hooks, and safe state serialization routines are provided under the GPL-2.0 in the `patches/` and `build/` directories. No Sony proprietary BIOS files (such as `scph1001.bin`, `scph5501.bin`, etc.) or proprietary game discs are included; users provide their own legally acquired BIOS in `/sdcard/ROMs/bios/`.
