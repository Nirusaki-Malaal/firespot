#!/usr/bin/env python3
"""Black-box checks against an explicitly selected developer test device.

Uses only the com.fireemu.demo test application. Does not establish Fire OS or
Bluetooth-controller performance. Run after installing the demo debug APK.
"""
import argparse,json,re,subprocess,time,xml.etree.ElementTree as ET
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--serial',required=True);p.add_argument('--out',type=Path,default=Path('artifacts/evidence/monochrome'));args=p.parse_args();args.out.mkdir(parents=True,exist_ok=True)
ADB=['adb','-s',args.serial];results=[]
def adb(*cmd): return subprocess.check_output(ADB+list(cmd),stderr=subprocess.STDOUT)
def shell(*cmd):return adb('shell',*cmd).decode()
def tree():
 shell('uiautomator','dump','/data/local/tmp/firespot-test.xml')
 return ET.fromstring(shell('cat','/data/local/tmp/firespot-test.xml'))
def texts(t):return [n.get('text','') for n in t.iter('node') if n.get('text')]
def wait(value):
 for _ in range(6):
  t=tree()
  if any(value in x for x in texts(t)):return t
 raise AssertionError(f'Missing {value!r}. Current texts: {texts(t)}')
def click(value):
 t=wait(value)
 nodes=[n for n in t.iter('node') if value in n.get('text','')]
 n=nodes[0];x,y,a,b=map(int,re.findall(r'\d+',n.get('bounds')))
 assert n.get('enabled')!='false',f'{value} is disabled'
 shell('input','tap',str((x+a)//2),str((y+b)//2))
def key(code):shell('input','keyevent',code)
def shot(name):
 path=args.out/(name+'.png');path.write_bytes(adb('exec-out','screencap','-p'));return path

def passed(name):results.append({'check':name,'result':'passed'});print('PASS:',name,flush=True)

def main():
    shell('am','force-stop','com.fireemu.demo')
    shell('am','start','-n','com.fireemu.demo/com.fireemu.MainActivity')
    t=tree()
    if any('Your next little escape' in x for x in texts(t)):
     key('KEYCODE_DPAD_CENTER')
    wait('Less setup.');shot('home');passed('Launch and seeded library')
    key('KEYCODE_DPAD_CENTER');wait('Your library');key('KEYCODE_DPAD_CENTER');wait('Play from start');passed('Remote center activates Library and first game')
    if not any('Remove favorite' in x for x in texts(tree())): click('Add to favorites')
    wait('Remove favorite');passed('Favorite persisted in details')
    click('Play from start')
    # SurfaceView has no text; dump synchronizes with the new activity.
    tree();shot('orbit-game')
    key('KEYCODE_MENU');wait('Resume playing');shot('pause');passed('Native game launch and pause')
    click('Controls guide');wait('Game Boy controls');key('KEYCODE_BACK');wait('Resume playing');passed('Controls guide')
    click('Overwrite Slot' if any('Overwrite Slot' in x for x in texts(tree())) else 'Save State');tree()
    files=shell('run-as','com.fireemu.demo','ls','files/states/gameboy')
    assert 'orbit-collector.gb.state1' in files,files
    key('KEYCODE_MENU');wait('Load State');click('Load State');tree();passed('Manual save and load slot')
    key('KEYCODE_MENU');wait('Scaling:');click('Scaling:');wait('Scaling:');passed('In-game scaling cycles')
    click('Resume playing');key('KEYCODE_MENU');wait('Save & Exit');click('Save & Exit');wait('Resume');passed('Autosave creates a Resume action on return')
    click('Resume');tree();key('KEYCODE_MENU');wait('Resume playing');click('Reset Game');tree();key('KEYCODE_MENU');wait('Save & Exit');click('Save & Exit');wait('Play from start');passed('Resume and reset')
    key('KEYCODE_BACK');wait('Your library');key('KEYCODE_BACK');wait('Less setup.');shot('home-with-history');passed('Return to launcher with recent history')
    click('Search');wait('Search Library');click('O');wait('Orbit Collector');shot('search');passed('On-screen keyboard search')
    click('Back');wait('Less setup.');click('Settings');wait('Make yourself at home.');shot('settings')
    click('Scaling:');wait('Scaling:');click('Frame counter:');wait('Frame counter: On');click('Frame counter:');wait('Frame counter: Off');passed('Settings scaling and frame counter persistence')
    click('Remap controls');wait('Controller & Remote Mapping');click('Reset Defaults');wait('Controller & Remote Mapping');shot('controls');passed('Controller mapping and reset screen')
    log=adb('logcat','-d','-s','FireEmuBridge','FireSpotRuntime','AndroidRuntime').decode(errors='replace')
    assert 'Got internal game name: ORBIT COLLECTOR' in log,'Native game load log missing'
    assert 'FATAL EXCEPTION' not in log,'Runtime crash detected'
    (args.out/'runtime.log').write_text(log)
    (args.out/'results.json').write_text(json.dumps({'device':args.serial,'scope':'Local Android emulator; not official Fire TV','checks':results},indent=2)+'\n')
    print('All scripted checks passed.',flush=True)

if __name__=='__main__': main()
