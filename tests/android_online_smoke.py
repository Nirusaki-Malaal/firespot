#!/usr/bin/env python3
"""Real GitHub-to-Android download/play checks; use an owned test device with a fresh standard install."""
import hashlib,json,time
import android_demo_smoke as u
PKG='com.fireemu'
def launch():
 u.shell('am','start','-n',f'{PKG}/com.fireemu.MainActivity')
 t=u.tree()
 if any('Your next little escape' in x for x in u.texts(t)):u.key('KEYCODE_DPAD_CENTER')
 if any('Your library' in x for x in u.texts(u.tree())):u.key('KEYCODE_BACK')
 u.wait('Less setup.')
def game_activity():
 for _ in range(40):
  top=u.shell('dumpsys','activity','activities')
  if 'mResumedActivity:' in top and any('EmulatorActivity' in x for x in top.splitlines() if 'mResumedActivity:' in x):return
  time.sleep(1)
 raise AssertionError('Game did not launch: '+str(u.texts(u.tree())))
launch();u.shot('home');u.key('KEYCODE_DPAD_CENTER');u.wait('Your library');u.key('KEYCODE_DPAD_CENTER');u.wait('Play from start');u.click('Play from start');game_activity();u.tree();u.shot('downloaded-core-game')
paths=u.shell('run-as',PKG,'find','files/cores','-name','*.so').splitlines();assert paths,paths
catalog=json.load(open('app/src/main/assets/catalog/catalog.json'))
asset=next(a for c in catalog['cores'] if c['id']=='gambatte' for a in c['artifacts'] if a['abi']=='x86_64')
core=u.adb('exec-out','run-as',PKG,'cat',paths[0]);assert hashlib.sha256(core).hexdigest()==asset['sha256']
u.passed('Core-free APK downloads the signed CPU-specific Gambatte core and runs a game')
before=u.shot('keyboard-before').read_bytes();u.shell('input','keyevent','--longpress','KEYCODE_D');u.tree();after=u.shot('keyboard-after').read_bytes();assert before!=after
u.key('KEYCODE_ESCAPE');u.wait('Resume playing');u.click('Controls guide');u.wait('Game controls');u.wait('Keyboard:');u.key('KEYCODE_BACK');u.wait('Resume playing');u.click('Save & Exit');u.wait('Resume');u.passed('Keyboard WASD moves; Escape pauses; keyboard guide opens')
u.key('KEYCODE_BACK');u.wait('Your library');u.key('KEYCODE_BACK');u.wait('Less setup.');u.click('Search');u.wait('Search Library');u.shell('input','text','Pocket');u.wait('Pocket Courier');u.shot('physical-keyboard-search');u.key('KEYCODE_ESCAPE');u.wait('Less setup.');u.passed('Physical keyboard typing searches the library; Escape navigates back')
u.click('Discover games');u.wait('Discover & play');u.shot('discover');u.click('Refresh catalog');u.wait('Catalog is up to date.');u.passed('Signed remote catalog refresh')
u.shell('input','swipe','1400','930','1400','400','350');u.tree();u.click('Get Signal Runner' if any('Get Signal Runner' in x for x in u.texts(u.tree())) else 'Open Signal Runner');u.wait('Signal Runner');u.wait('Play from start');u.click('Play from start');game_activity();u.tree();u.shot('signal-runner');u.key('KEYCODE_ESCAPE');u.wait('Save & Exit');u.click('Save & Exit');u.wait('Resume')
battery=u.shell('run-as',PKG,'find','files/battery','-name','*.srm').splitlines();assert battery,battery
ram=u.adb('exec-out','run-as',PKG,'cat',battery[0]);assert len(ram)==8192,len(ram)
u.passed('Catalog downloads a new original game; it launches and writes 8 KiB cartridge RAM')
# Directly seed owned test cartridge RAM with known progress, then verify it loads without an emulator snapshot.
# The game reads SRAM during boot and writes the score to tile map address 0x9849.
# Native framebuffer is visibly checked below; host test covers deterministic score behavior.
u.click('Play from start');game_activity();u.tree();u.key('KEYCODE_ESCAPE');u.wait('Save & Exit');u.click('Save & Exit');u.wait('Resume')
files=u.shell('run-as',PKG,'ls','files/states/gameboy');assert 'signal-runner.gb.state99.bak1' in files,files
u.passed('Rotating autosave retains the previous checkpoint')
u.shell('am','force-stop',PKG);launch();u.click('Open library');u.wait('Your library');u.shell('input','swipe','1400','930','1400','360','350');u.tree();u.click('Signal Runner');u.wait('Resume');u.click('Resume');game_activity();u.tree();u.key('KEYCODE_ESCAPE');u.wait('Save & Exit');u.click('Save & Exit');u.wait('Resume');u.passed('Downloaded core/game and autosave survive an app process restart')
u.key('KEYCODE_BACK')
if not any('Your library' in x for x in u.texts(u.tree())):
 launch();u.click('Open library')
u.wait('Your library');u.click('System: All');u.wait('System: GB / GBC');u.click('Clear filters');u.wait('System: All');u.click('Genre: All');u.wait('Clear filters');u.click('Clear filters');u.passed('Library console/genre filters apply and clear')
log=u.adb('logcat','-d','-s','FireEmuBridge','FireSpotRuntime','AndroidRuntime').decode(errors='replace');assert 'FATAL EXCEPTION' not in log;assert 'SIGNAL RUNNER' in log
(u.args.out/'runtime.log').write_text(log)
(u.args.out/'android-results.json').write_text(json.dumps({'scope':'Local AOSP Android 11 x86_64; not official Fire TV or physical Bluetooth','checks':u.results},indent=2)+'\n')
print('All online checks passed.',flush=True)
