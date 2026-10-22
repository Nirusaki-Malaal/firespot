#!/usr/bin/env python3
"""Additional checks for the explicitly selected developer demo device."""
import json,re
import android_demo_smoke as u
u.shell('am','force-stop','com.fireemu.demo')
u.shell('am','start','-n','com.fireemu.demo/com.fireemu.MainActivity')
u.wait('Less setup.')
u.click('Open library');u.wait('Your library');u.click('Pocket Courier');u.wait('Play from start');u.click('Play from start');u.tree()
before=u.shot('courier-before').read_bytes()
u.shell('input','keyevent','--longpress','KEYCODE_DPAD_RIGHT');u.tree()
after=u.shot('courier-game').read_bytes()
assert before!=after,'D-pad input did not change the rendered game'
u.key('KEYCODE_MENU');u.wait('Resume playing');u.click('Overwrite Slot' if any('Overwrite Slot' in x for x in u.texts(u.tree())) else 'Save State');u.tree()
files=u.shell('run-as','com.fireemu.demo','ls','files/states/gameboy')
assert 'pocket-courier.gb.state1' in files
u.key('KEYCODE_MENU');u.wait('Save & Exit');u.click('Save & Exit');u.wait('Resume')
u.passed('Pocket Courier loads, responds to input, and saves')
u.key('KEYCODE_BACK');u.wait('Your library');u.key('KEYCODE_BACK');u.wait('Less setup.');u.click('Settings');u.wait('Make yourself at home.');u.click('Remap controls');u.wait('Controller & Remote Mapping')
u.click('Button B');u.wait('PRESS BUTTON NOW');u.key('KEYCODE_MEDIA_FAST_FORWARD');u.wait('KEYCODE_MEDIA_FAST_FORWARD')
prefs=u.shell('run-as','com.fireemu.demo','cat','shared_prefs/fireemu_key_bindings.xml')
assert 'btn_0' in prefs and 'value="90"' in prefs,prefs
u.click('Reset Defaults');u.wait('KEYCODE_BUTTON_B');u.passed('Remapping captures a real key, persists, and resets')
u.key('KEYCODE_BACK');u.wait('Make yourself at home.')
u.adb('push','app/src/main/assets/starter/orbit-collector.gb','/sdcard/Download/OrbitImport.gb')
u.click('Import Game Boy ROM');t=u.tree()
print('IMPORT PICKER:',u.texts(t),flush=True)
u.shot('import-picker')

# Android DocumentsUI on the local AOSP test image; Fire TV providers may differ.
roots=[n for n in t.iter('node') if n.get('content-desc')=='Show roots']
assert roots,'Expected a file provider with a roots menu'
x,y,a,b=map(int,re.findall(r'\d+',roots[0].get('bounds')))
u.shell('input','tap',str((x+a)//2),str((y+b)//2))
u.click('Downloads');u.wait('OrbitImport.gb')
# This TV-profile provider focuses its preview separately from the file.
for _ in range(24):
    t=u.tree()
    if any(n.get('focused')=='true' and n.get('content-desc')=='Preview the file OrbitImport.gb' for n in t.iter('node')):
        u.key('KEYCODE_DPAD_LEFT');u.key('KEYCODE_ENTER');break
    u.key('KEYCODE_TAB')
else: raise AssertionError('Could not focus the provider file')
u.wait('Added OrbitImport to your library.');u.shot('import-success')
u.key('KEYCODE_BACK');u.wait('Less setup.');u.click('Open library');u.wait('OrbitImport');u.click('OrbitImport');u.wait('Play from start');u.click('Play from start');u.tree()
u.key('KEYCODE_MENU');u.wait('Resume playing');u.shot('imported-game');u.click('Save & Exit');u.wait('Resume')
u.passed('File-provider import adds a game, plays it, and creates autosave')
(u.args.out/'extra-results.json').write_text(json.dumps({'device':u.args.serial,'scope':'Local Android emulator; not official Fire TV','checks':u.results},indent=2)+'\n')
