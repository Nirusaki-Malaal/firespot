#!/usr/bin/env python3
"""Generate the original FireSpot Signal GB hardware test (CC0-1.0).

No third-party game assets or boot ROM. The logo field is zero: run with
Gambatte's built-in boot sequence, not a proprietary hardware boot ROM.
D-pad scrolls a checkerboard; A/B trigger a short square-wave tone.
"""
from pathlib import Path
import argparse

def generate():
    rom = bytearray(32768)
    rom[0x100:0x104] = bytes([0x00, 0xC3, 0x50, 0x01])
    rom[0x134:0x143] = b'FIRESPOT SIGNAL'
    rom[0x147:0x14A] = b'\x00\x00\x00'  # ROM only, 32K, no RAM
    code = bytearray()
    labels, jumps = {}, []
    def emit(*values): code.extend(values)
    def label(name): labels[name] = len(code)
    def jump(op, name):
        emit(op, 0, 0); jumps.append((len(code)-2, name))
    def reg(addr, value): emit(0x3E, value, 0xE0, addr)
    emit(0xF3, 0x31, 0xFE, 0xFF)  # DI; LD SP,$FFFE
    label('wait_initial'); emit(0xF0, 0x44, 0xFE, 144); jump(0xDA, 'wait_initial')
    reg(0x40, 0)  # LCD off while writing VRAM
    emit(0x21, 0, 0x80, 0x06, 16)
    label('tile'); emit(0x3E, 0xAA, 0x22, 0x05); jump(0xC2, 'tile')
    emit(0x21, 0, 0x98, 0x01, 0, 4)  # 1024 map cells, tile zero
    label('map'); emit(0xAF, 0x22, 0x0B, 0x78, 0xB1); jump(0xC2, 'map')
    reg(0x47, 0xE4); reg(0x42, 0); reg(0x43, 0)
    reg(0x26, 0x80); reg(0x24, 0x77); reg(0x25, 0x11)
    reg(0x40, 0x91)
    label('frame'); emit(0xF0, 0x44, 0xFE, 144); jump(0xDA, 'frame')
    reg(0, 0x20); emit(0xF0, 0, 0xF0, 0, 0x4F)
    for bit, addr, opcode, name in [(0,0x43,0x3C,'right'),(1,0x43,0x3D,'left'),(2,0x42,0x3D,'up'),(3,0x42,0x3C,'down')]:
        emit(0xCB, 0x41+8*bit); jump(0xC2, name)
        emit(0xF0, addr, opcode, 0xE0, addr); label(name)
    reg(0, 0x10); emit(0xF0,0,0xF0,0,0xE6,3,0xFE,3); jump(0xCA, 'no_tone')
    reg(0x11,0x80); reg(0x12,0xA1); reg(0x13,0); reg(0x14,0x87)
    label('no_tone'); reg(0,0x30)
    label('wait_end'); emit(0xF0,0x44,0xFE,144); jump(0xD2,'wait_end')
    jump(0xC3,'frame')
    for offset,name in jumps:
        target = 0x150 + labels[name]
        code[offset:offset+2] = target.to_bytes(2,'little')
    rom[0x150:0x150+len(code)] = code
    checksum = 0
    for value in rom[0x134:0x14D]: checksum = (checksum-value-1)&255
    rom[0x14D] = checksum
    rom[0x14E:0x150] = (sum(rom)&65535).to_bytes(2,'big')
    return rom

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('output', type=Path)
    args = parser.parse_args()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_bytes(generate())
    print(f'Generated original test cartridge: {args.output} (32768 bytes)')
