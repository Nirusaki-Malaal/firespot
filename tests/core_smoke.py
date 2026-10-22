#!/usr/bin/env python3
"""Execute a real Linux Libretro core. This does NOT test Android or Fire TV."""
import ctypes as C
import hashlib
import json
import sys
from pathlib import Path

class GameInfo(C.Structure):
    _fields_=[('path',C.c_char_p),('data',C.c_void_p),('size',C.c_size_t),('meta',C.c_char_p)]

core=C.CDLL(sys.argv[1])
rom=Path(sys.argv[2]).read_bytes(); data=C.create_string_buffer(rom)
inputs=set(); frames=[]; audio=[0,0]; pixel_format=[0]
ENV=C.CFUNCTYPE(C.c_bool,C.c_uint,C.c_void_p)
VIDEO=C.CFUNCTYPE(None,C.c_void_p,C.c_uint,C.c_uint,C.c_size_t)
SAMPLE=C.CFUNCTYPE(None,C.c_int16,C.c_int16)
BATCH=C.CFUNCTYPE(C.c_size_t,C.POINTER(C.c_int16),C.c_size_t)
POLL=C.CFUNCTYPE(None)
INPUT=C.CFUNCTYPE(C.c_int16,C.c_uint,C.c_uint,C.c_uint,C.c_uint)
@ENV
def environment(cmd,ptr):
    if cmd==10:
        pixel_format[0]=C.cast(ptr,C.POINTER(C.c_int))[0];return True
    if cmd==3:
        C.cast(ptr,C.POINTER(C.c_bool))[0]=True;return True
    if cmd==17:
        C.cast(ptr,C.POINTER(C.c_bool))[0]=False;return True
    return False
@VIDEO
def video(ptr,w,h,pitch):
    if ptr: frames.append((w,h,hashlib.sha256(C.string_at(ptr,pitch*h)).hexdigest()))
@SAMPLE
def sample(l,r): audio[0]+=2;audio[1]+=int(l!=0 or r!=0)
@BATCH
def batch(ptr,n):
    audio[0]+=n*2;audio[1]+=sum(ptr[i]!=0 for i in range(n*2));return n
@POLL
def poll(): pass
@INPUT
def input_state(port,device,index,button): return int(port==0 and button in inputs)
for name,callback in [('environment',environment),('video_refresh',video),('audio_sample',sample),('audio_sample_batch',batch),('input_poll',poll),('input_state',input_state)]:
    getattr(core,'retro_set_'+name)(callback)
core.retro_load_game.argtypes=[C.POINTER(GameInfo)];core.retro_load_game.restype=C.c_bool
core.retro_serialize_size.restype=C.c_size_t
core.retro_serialize.argtypes=[C.c_void_p,C.c_size_t];core.retro_serialize.restype=C.c_bool
core.retro_unserialize.argtypes=[C.c_void_p,C.c_size_t];core.retro_unserialize.restype=C.c_bool
core.retro_init()
assert core.retro_load_game(C.byref(GameInfo(b'firespot-signal.gb',C.cast(data,C.c_void_p),len(rom),None))), 'ROM rejected'
try:
    for _ in range(180):core.retro_run()
    if len(sys.argv)>3:
        speed=int(sys.argv[3])
        inputs.add(7)
        for _ in range(96//speed):core.retro_run()
        inputs.clear();inputs.add(5)
        for _ in range(40//speed):core.retro_run()
        inputs.clear()
        core.retro_get_memory_data.argtypes=[C.c_uint]
        core.retro_get_memory_data.restype=C.c_void_p
        ram=core.retro_get_memory_data(2)
        assert ram and C.c_uint8.from_address(ram).value==1, 'Collecting the checkpoint did not increment score'
        print('PASS: player reaches checkpoint and scores a point')
        core.retro_reset()
        for _ in range(180):core.retro_run()
    assert frames and frames[-1][:2]==(160,144), 'Missing Game Boy video'
    before=frames[-1][2];inputs.add(7)
    for _ in range(9):core.retro_run()
    inputs.clear();core.retro_run();after=frames[-1][2]
    assert before!=after,'D-pad did not affect rendered output'
    inputs.add(8)
    for _ in range(6):core.retro_run()
    inputs.clear()
    assert audio[0]>0 and audio[1]>0,'No nonzero audio'
    size=core.retro_serialize_size();assert size>0
    state=C.create_string_buffer(size);assert core.retro_serialize(state,size)
    for _ in range(5):core.retro_run()
    expected_frames=frames[-5:]
    assert core.retro_unserialize(state,size)
    inputs.add(7)
    for _ in range(9):core.retro_run()
    inputs.clear()
    assert frames[-1][2]!=expected_frames[-1][2], 'Input did not change the saved scene'
    assert core.retro_unserialize(state,size),'Save state rejected'
    for _ in range(5):core.retro_run()
    assert frames[-5:]==expected_frames, 'Restored game does not reproduce the saved scene'
    core.retro_run()
    print(json.dumps({'scope':'Linux host core only; Android/Fire TV unverified','video_frames':len(frames),'resolution':[160,144],'dpad_changes_video':True,'audio_samples':audio[0],'nonzero_audio_samples':audio[1],'save_state_roundtrip':True,'state_bytes':size,'pixel_format':pixel_format[0]},indent=2))
finally:
    core.retro_unload_game();core.retro_deinit()
