#!/usr/bin/env python3
"""Original CC0 diagnostic cartridges. Test fixtures, not a game compatibility suite."""
from pathlib import Path
import argparse
p=argparse.ArgumentParser();p.add_argument('--out',type=Path,required=True);a=p.parse_args();a.out.mkdir(parents=True,exist_ok=True)
for ext in ['sms','gg','sg']:
 code=bytearray([0xF3,0x31,0xF0,0xDF]) # DI; stack in RAM
 def emit(*v):code.extend(v)
 def reg(index,value):emit(0x3E,value,0xD3,0xBF,0x3E,0x80|index,0xD3,0xBF)
 # Clear all VRAM.
 emit(0xAF,0xD3,0xBF,0x3E,0x40,0xD3,0xBF,0x01,0,0x40,0xAF)
 clear=len(code);emit(0xD3,0xBE,0x0B,0x78,0xB1,0x20,0,0xAF,0xC3,clear&255,clear>>8)
 # The JR exits when BC reaches zero; otherwise XOR A and loop.
 exit_clear=len(code);code[clear+6]= (3)&255
 # fix loop branch: NZ goes to XOR A/JMP, Z skips to initialization.
 code[clear+5]=0x28;code[clear+6]=4
 if ext!='sg':
  emit(0xAF,0xD3,0xBF,0x3E,0xC0,0xD3,0xBF)
  for i in range(32):
   if ext=='gg':emit(0x3E,(i*7)&255,0xD3,0xBE,0x3E,(i//3)&15,0xD3,0xBE)
   else:emit(0x3E,(i*3)&63,0xD3,0xBE)
 reg(2,0x0E);reg(3,0);reg(4,0);reg(5,0x7E);reg(6,0);reg(7,4)
 reg(0,0 if ext=='sg' else 4);reg(1,0x40 if ext=='sg' else 0)
 for value in [0x80,0x12,0x90]:emit(0x3E,value,0xD3,0x7F) # audible PSG square wave
 loop=len(code);emit(0xDB,0xDC,0x2F,0xE6,0x0F,0xF6,0x01,0xD3,0xBF,0x3E,0x87,0xD3,0xBF,0xC3,loop&255,loop>>8)
 rom=bytearray(32768);rom[:len(code)]=code
 if ext!='sg':rom[0x7FF0:0x7FF8]=b'TMR SEGA';rom[0x7FFF]=0x7C if ext=='gg' else 0x4C
 (a.out/f'input-colour.{ext}').write_bytes(rom)
print('Generated three original Sega input/audio diagnostics.')
# Minimal iNES mapper-0 diagnostic: right changes the backdrop; pulse channel produces audio.
code=bytearray();labels={};fix=[]
def e(*v):code.extend(v)
def label(n):labels[n]=len(code)
def branch(op,n):e(op,0);fix.append((len(code)-1,n))
def store(addr,value):e(0xA9,value,0x8D,addr&255,addr>>8)
e(0x78,0xD8,0xA2,0xFF,0x9A);store(0x2000,0);store(0x2001,0)
store(0x4015,1);store(0x4000,0xBF);store(0x4001,8);store(0x4002,0xFF);store(0x4003,7)
label('loop');store(0x4016,1);store(0x4016,0);e(0xA2,8)
label('read');e(0xAD,0x16,0x40,0xCA);branch(0xD0,'read');e(0x29,1,0x09,0x20,0x85,0)
label('vblank');e(0x2C,2,0x20);branch(0x10,'vblank')
store(0x2006,0x3F);store(0x2006,0);e(0xA5,0,0x8D,7,0x20)
e(0x4C,labels['loop']&255,0x80|(labels['loop']>>8))
for off,n in fix:code[off]=(labels[n]-(off+1))&255
prg=bytearray(16384);prg[:len(code)]=code;prg[-6:]=bytes([0,0x80]*3)
(a.out/'input-colour.nes').write_bytes(b'NES\x1a'+bytes([1,1])+bytes(10)+prg+bytes(8192))
# Run the original GB game in CGB-only mode with explicitly initialized colour palette RAM.
base=Path('app/src/main/assets/games/orbit-collector.gb')
if not base.exists():
 matches=list(Path('app/src/main/assets').rglob('orbit-collector.gb'));base=matches[0] if matches else base
if base.exists():
 rom=bytearray(base.read_bytes());rom[0x100:0x104]=bytes([0xC3,0,0x70,0]);rom[0x143]=0xC0
 init=bytearray([0x3E,0x80,0xE0,0x68])
 for v in [0xFF,0x7F,0x94,0x52,0x4A,0x29,0,0]:init.extend([0x3E,v,0xE0,0x69])
 init.extend([0xC3,0x50,1]);rom[0x7000:0x7000+len(init)]=init
 checksum=0
 for v in rom[0x134:0x14D]:checksum=(checksum-v-1)&255
 rom[0x14D]=checksum;(a.out/'colour-orbit.gbc').write_bytes(rom)
print('Generated original NES and CGB-only input diagnostics.')
