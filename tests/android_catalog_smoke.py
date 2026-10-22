#!/usr/bin/env python3
"""Catalog navigation and real native package checks on an explicitly selected owned debug device."""
import json,time,hashlib
import android_demo_smoke as u
PKG='com.fireemu'
def wait_for(value):
 for _ in range(25):
  t=u.tree()
  if any(value in x for x in u.texts(t)):return t
 raise AssertionError('Missing '+value+': '+str(u.texts(t)))
def scroll_to(value):
 for _ in range(5):
  t=u.tree()
  if any(value in x for x in u.texts(t)):return
  u.shell('input','swipe','1450','940','1450','470','300')
 raise AssertionError('Could not reveal '+value)
u.shell('am','force-stop',PKG);u.shell('am','start','-n',f'{PKG}/com.fireemu.MainActivity');t=u.tree()
if any('Your next little escape' in x for x in u.texts(t)):u.key('KEYCODE_DPAD_CENTER')
u.wait('Less setup.');u.click('Discover games');u.wait('Discover & play');u.shot('catalog-games')
u.click('Search catalog');u.shell('input','text','Courier');u.wait('1 matching titles');u.shot('catalog-search');u.click('Show results');u.wait('Pocket Courier');assert 'Orbit Collector' not in u.texts(u.tree());u.passed('Catalog keyboard search filters real titles')
u.click('Clear filters');u.wait('3 TITLES');u.click('Installed');u.wait('2 TITLES');assert not any('Get Signal Runner' in x for x in u.texts(u.tree()));u.passed('Installed-only catalog shows seeded titles without a duplicate download')
u.click('Games');u.click('Sort: A–Z');u.wait('Sort: Size');u.click('Sort: Size');u.wait('Sort: Newest');u.click('Sort: Newest');u.wait('Sort: A–Z');u.passed('Catalog sort controls cycle')
u.click('Consoles');scroll_to('SNES');u.shot('catalog-consoles');u.click('SNES');u.wait('0 TITLES');u.wait('Import a cartridge');u.click('Import a cartridge');u.wait('Make yourself at home.');u.key('KEYCODE_ESCAPE');u.wait('Discover & play');
if any('Clear filters' in x for x in u.texts(u.tree())):u.click('Clear filters')
u.passed('Console browsing uses real zero counts and offers import for an empty system')
u.click('Console support');scroll_to('Install Gearsystem');u.click('Install Gearsystem');wait_for('Gearsystem is installed.');u.passed('New Sega core downloads and passes native CPU/hash checks')
u.click('Consoles');scroll_to('SNES');u.click('SNES');u.click('Console support');scroll_to('Install bsnes 2014 Performance');u.click('Install bsnes 2014 Performance');wait_for('bsnes 2014 Performance is installed.');u.shot('catalog-support');u.passed('New SNES core downloads and passes native CPU/hash checks')
paths=u.shell('run-as',PKG,'find','files/cores','-name','*.so').splitlines()
catalog=json.load(open('app/src/main/assets/catalog/catalog.json'))
for core in catalog['cores']:
 if core['id'] not in ['gearsystem','bsnes2014']:continue
 a=next(a for a in core['artifacts'] if a['abi']=='x86_64');path=next(p for p in paths if '/'+core['id']+'/' in p)
 data=u.adb('exec-out','run-as',PKG,'cat',path);assert len(data)==a['bytes'] and hashlib.sha256(data).hexdigest()==a['sha256']
(u.args.out/'catalog-results.json').write_text(json.dumps({'scope':'Local AOSP Android, not official Fire TV; new cores package-tested, not game-tested','checks':u.results},indent=2)+'\n');print('Catalog checks passed.',flush=True)
