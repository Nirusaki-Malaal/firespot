import sys,json
sys.path.insert(0,str(__import__('pathlib').Path(__file__).resolve().parent))
import android_demo_smoke as u
u.shell('am','force-stop','com.fireemu');u.shell('am','start','-n','com.fireemu/com.fireemu.MainActivity');u.wait('Less setup.')
u.click('Settings');u.wait('Make yourself at home.')
for _ in range(4):
 t=u.tree()
 if any('Controller & keyboard tester' in x for x in u.texts(t)):break
 u.shell('input','swipe','1500','900','1500','450','250')
u.click('Controller & keyboard tester');u.wait('CONNECTED INPUT DEVICES');u.shot('input-doctor');u.click('Start live test');u.key('KEYCODE_Z');u.wait('KEYCODE_Z');u.wait('Held: None');u.key('KEYCODE_BACK');u.wait('Navigation mode');u.key('KEYCODE_BACK');u.wait('YOUR SETUP');u.passed('Input doctor captures real keys, releases held state and stops on Back')
u.click('Storage & playtime');u.wait('Game cartridges');u.wait('available on app storage');u.shot('storage');u.click('Clear image cache');u.wait('Image cache cleared.');u.passed('Storage dashboard reports real usage and clears image cache')
u.key('KEYCODE_BACK');u.wait('YOUR SETUP')
u.shell('input','swipe','1500','350','1500','950','300');u.tree();u.click('Browse ROMsGames');u.wait('My library');
for _ in range(10):
 t=u.tree()
 if any('List of Console ROMs' in x for x in u.texts(t)):break
else:raise AssertionError('Provider did not load: '+str(u.texts(t)))
u.shot('romsgames-browser');u.passed('ROMsGames loads inside the app with native return controls')
u.click('My library');u.wait('Browse ROMsGames')
(u.args.out/'setup-tools-results.json').write_text(json.dumps({'scope':'Local AOSP Android 11; keyboard events injected, physical wireless unverified','checks':u.results},indent=2)+'\n')
