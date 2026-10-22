#!/usr/bin/env python3
"""UI/runtime acceptance for an already installed, signed standard APK; no run-as/debug privileges."""
import json,time
import android_demo_smoke as u
PKG='com.fireemu'
def launch():
 u.shell('am','start','-n',f'{PKG}/com.fireemu.MainActivity');t=u.tree()
 if any('Your next little escape' in x for x in u.texts(t)):u.key('KEYCODE_DPAD_CENTER')
 u.wait('Less setup.')
def game():
 for _ in range(45):
  if any('EmulatorActivity' in line for line in u.shell('dumpsys','activity','activities').splitlines() if 'mResumedActivity:' in line):return
  time.sleep(1)
 raise AssertionError(str(u.texts(u.tree())))
u.shell('am','force-stop',PKG);launch();u.shot('release-home');u.click('Open library');u.wait('Your library');u.click('Orbit Collector');u.wait('Play from start');u.click('Play from start');game();u.tree();u.shot('release-game');u.passed('Signed/minified core-free APK downloads console support and launches native gameplay')
before=u.shot('release-keyboard-before').read_bytes();u.shell('input','keyevent','--longpress','KEYCODE_D');u.tree();after=u.shot('release-keyboard-after').read_bytes();assert before!=after
u.key('KEYCODE_ESCAPE');u.wait('Resume playing');u.shot('release-pause');u.click('Controls guide');u.wait('Keyboard:');u.key('KEYCODE_BACK');u.wait('Resume playing');u.click('Save & Exit');u.wait('Resume');u.click('Resume');game();u.tree();u.key('KEYCODE_ESCAPE');u.wait('Save & Exit');u.click('Save & Exit');u.wait('Resume');u.passed('Release keyboard movement, pause, controls, save/exit and resume')
u.key('KEYCODE_BACK');u.wait('Your library');u.key('KEYCODE_BACK');u.wait('Less setup.');u.click('Discover games');u.wait('Discover & play');u.shot('release-discover');u.shell('input','swipe','1400','930','1400','400','350');u.tree();u.click('Get Signal Runner' if any('Get Signal Runner' in x for x in u.texts(u.tree())) else 'Open Signal Runner');u.wait('Play from start');u.click('Play from start');game();u.tree();u.key('KEYCODE_ESCAPE');u.wait('Save & Exit');u.click('Save & Exit');u.wait('Resume');u.passed('Release catalog installs and plays a new downloadable game')
u.shell('am','force-stop',PKG);launch();u.click('Search');u.wait('Search Library');u.shell('input','text','Signal');u.wait('Signal Runner');u.key('KEYCODE_ESCAPE');u.wait('Less setup.');u.click('Open library');u.wait('Your library');u.click('Signal Runner');u.wait('Resume');u.click('Resume');game();u.tree();u.key('KEYCODE_ESCAPE');u.wait('Save & Exit');u.click('Save & Exit');u.wait('Resume');u.passed('Release search, installed library and resume survive process restart')
log=u.adb('logcat','-d','-s','FireEmuBridge','FireSpotRuntime','AndroidRuntime').decode(errors='replace');assert 'FATAL EXCEPTION' not in log;assert 'SIGNAL RUNNER' in log
(u.args.out/'release-runtime.log').write_text(log)
(u.args.out/'release-results.json').write_text(json.dumps({'scope':'Signed/minified release APK on local AOSP Android 11, not official Fire TV','checks':u.results},indent=2)+'\n')
print('Signed release checks passed.',flush=True)
