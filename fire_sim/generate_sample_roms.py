#!/usr/bin/env python3
"""
Generates valid sample ROM files with correct headers for all 5 consoles:
NES (.nes with iNES header), GB (.gb with Game Boy header), GBA (.gba), PS1 (.cue/.bin), PSP (.iso)
"""

import os
import struct

ROMS_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "roms")

def make_nes_rom(path, title):
    # iNES Header: "NES\x1a", 1x 16KB PRG, 1x 8KB CHR, mapper 0
    header = bytearray(b"NES\x1a\x01\x01\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00")
    prg = bytearray(16384)
    # Minimal 6502 reset vector at 0xFFFC pointing to 0xC000
    prg[16380] = 0x00
    prg[16381] = 0xC0
    # At 0xC000 (offset 0): SEI, CLD, JMP 0xC000
    prg[0] = 0x78 # SEI
    prg[1] = 0xD8 # CLD
    prg[2] = 0x4C # JMP
    prg[3] = 0x00
    prg[4] = 0xC0
    chr_rom = bytearray(8192)
    with open(path, "wb") as f:
        f.write(header + prg + chr_rom)

def make_gb_rom(path, title):
    # Game Boy ROM: 32KB
    rom = bytearray(32768)
    # Title at 0x0134 (up to 15 bytes)
    title_bytes = title[:15].encode('ascii')
    rom[0x0134:0x0134+len(title_bytes)] = title_bytes
    # Cartridge type: ROM ONLY (0x00)
    rom[0x0147] = 0x00
    # ROM size: 32KB (0x00)
    rom[0x0148] = 0x00
    # Entry point at 0x0100: NOP, JP 0x0150
    rom[0x0100] = 0x00 # NOP
    rom[0x0101] = 0xC3 # JP
    rom[0x0102] = 0x50
    rom[0x0103] = 0x01
    with open(path, "wb") as f:
        f.write(rom)

def make_gba_rom(path, title):
    # GBA ROM: 64KB
    rom = bytearray(65536)
    # Title at 0xA0 (12 bytes)
    title_bytes = title[:12].encode('ascii')
    rom[0x00A0:0x00A0+len(title_bytes)] = title_bytes
    # Entry point at 0x00: B 0x0080
    rom[0:4] = struct.pack("<I", 0xEA00001E)
    with open(path, "wb") as f:
        f.write(rom)

def make_ps1_disc(bin_path, cue_path, title):
    # PS1 CD-ROM 2048-byte user data sector ISO
    with open(bin_path, "wb") as f:
        f.write(bytearray(2048 * 16)) # 16 sectors
    cue_content = f'FILE "{os.path.basename(bin_path)}" BINARY\n  TRACK 01 MODE2/2352\n    INDEX 01 00:00:00\n'
    with open(cue_path, "w") as f:
        f.write(cue_content)

def make_psp_iso(path, title):
    # PSP ISO header
    iso = bytearray(2048 * 32)
    # PVD at sector 16
    pvd_offset = 16 * 2048
    iso[pvd_offset] = 0x01 # Type 1
    iso[pvd_offset+1:pvd_offset+6] = b"CD001"
    iso[pvd_offset+40:pvd_offset+40+len(title)] = title.encode('ascii')
    with open(path, "wb") as f:
        f.write(iso)

def main():
    consoles = ["nes", "gameboy", "gba", "ps1", "psp", "bios"]
    for c in consoles:
        os.makedirs(os.path.join(ROMS_DIR, c), exist_ok=True)

    # 1. NES Games
    make_nes_rom(os.path.join(ROMS_DIR, "nes", "Super_Mario_Bros_(USA)_[!].nes"), "SUPER MARIO BROS")
    make_nes_rom(os.path.join(ROMS_DIR, "nes", "The_Legend_of_Zelda_(USA).nes"), "THE LEGEND OF ZELDA")
    make_nes_rom(os.path.join(ROMS_DIR, "nes", "Metroid_(USA)_[!].nes"), "METROID")
    make_nes_rom(os.path.join(ROMS_DIR, "nes", "Mega_Man_2_(USA).nes"), "MEGA MAN 2")

    # 2. Game Boy / Color Games
    make_gb_rom(os.path.join(ROMS_DIR, "gameboy", "Pokemon - Red Version (USA).gb"), "POKEMON RED")
    make_gb_rom(os.path.join(ROMS_DIR, "gameboy", "Tetris (World) (Rev A).gb"), "TETRIS")
    make_gb_rom(os.path.join(ROMS_DIR, "gameboy", "The Legend of Zelda - Link's Awakening (USA).gbc"), "ZELDA LINK AWAK")
    make_gb_rom(os.path.join(ROMS_DIR, "gameboy", "Super Mario Land (World).gb"), "SUPER MARIO LAND")

    # 3. Game Boy Advance Games
    make_gba_rom(os.path.join(ROMS_DIR, "gba", "Pokemon - Emerald Version (USA).gba"), "POKEMON EMER")
    make_gba_rom(os.path.join(ROMS_DIR, "gba", "Advance Wars (USA).gba"), "ADVANCE WARS")
    make_gba_rom(os.path.join(ROMS_DIR, "gba", "Metroid Fusion (USA).gba"), "METROID FUSION")
    make_gba_rom(os.path.join(ROMS_DIR, "gba", "Castlevania - Aria of Sorrow (USA).gba"), "CASTLEVANIA")

    # 4. PlayStation 1 Games
    make_ps1_disc(
        os.path.join(ROMS_DIR, "ps1", "Castlevania - Symphony of the Night (USA).bin"),
        os.path.join(ROMS_DIR, "ps1", "Castlevania - Symphony of the Night (USA).cue"),
        "CASTLEVANIA SOTN"
    )
    make_ps1_disc(
        os.path.join(ROMS_DIR, "ps1", "Crash Bandicoot (USA).bin"),
        os.path.join(ROMS_DIR, "ps1", "Crash Bandicoot (USA).cue"),
        "CRASH BANDICOOT"
    )
    make_ps1_disc(
        os.path.join(ROMS_DIR, "ps1", "Final Fantasy VII (USA) (Disc 1).bin"),
        os.path.join(ROMS_DIR, "ps1", "Final Fantasy VII (USA) (Disc 1).cue"),
        "FINAL FANTASY VII"
    )

    # 5. PSP Games
    make_psp_iso(os.path.join(ROMS_DIR, "psp", "God of War - Ghost of Sparta (USA).cso"), "GOW GHOST OF SPARTA")
    make_psp_iso(os.path.join(ROMS_DIR, "psp", "Crisis Core - Final Fantasy VII (USA).iso"), "CRISIS CORE FFVII")
    make_psp_iso(os.path.join(ROMS_DIR, "psp", "Monster Hunter Freedom Unite (USA).iso"), "MONSTER HUNTER FU")

    # BIOS Files
    with open(os.path.join(ROMS_DIR, "bios", "scph1001.bin"), "wb") as f:
        f.write(bytearray(524288)) # 512KB PS1 BIOS
    with open(os.path.join(ROMS_DIR, "bios", "gba_bios.bin"), "wb") as f:
        f.write(bytearray(16384)) # 16KB GBA BIOS

    print("Successfully generated test ROM library in:", ROMS_DIR)

if __name__ == "__main__":
    main()
