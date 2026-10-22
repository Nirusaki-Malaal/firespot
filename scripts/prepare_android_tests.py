#!/usr/bin/env python3
"""Stage developer-only Android test assets; never adds them to production APKs."""
import argparse,hashlib,json,shutil,subprocess,urllib.request,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--out',type=Path,default=Path('artifacts/android-test-assets'));a=p.parse_args();a.out.mkdir(parents=True,exist_ok=True)
root=Path(__file__).resolve().parent.parent
fixtures=root/'artifacts/console-fixtures';fixtures.mkdir(parents=True,exist_ok=True)
subprocess.run(['python3',str(root/'tests/generate_console_diagnostics.py'),'--out',str(fixtures)],check=True,cwd=root)
ndk=Path(__import__('os').environ.get('ANDROID_NDK_HOME','/missing-ndk'))
clang=ndk/'toolchains/llvm/prebuilt/linux-x86_64/bin/clang'
if not clang.exists():raise SystemExit('Set ANDROID_NDK_HOME to the installed NDK 28.2 directory.')
subprocess.run([str(clang),'--target=armv4t-none-eabi','-nostdlib','-Wl,-Ttext=0x08000000','-Wl,--oformat=binary',str(root/'tests/gba_input_diagnostic.s'),'-o',str(fixtures/'input-colour.gba')],check=True)
def fetch(url):
 with urllib.request.urlopen(urllib.request.Request(url,headers={'User-Agent':'FireSpot-developer-tests'}),timeout=60) as r:return r.read()
catalog=json.loads((root/'app/src/main/assets/catalog/catalog.json').read_text())
for core in catalog['cores']:
 asset=next(x for x in core['artifacts'] if x['abi']=='x86_64');target=a.out/(core['id']+'.so')
 if not target.exists() or hashlib.sha256(target.read_bytes()).hexdigest()!=asset['sha256']:
  content=fetch(asset['url']);assert len(content)==asset['bytes'] and hashlib.sha256(content).hexdigest()==asset['sha256'];target.write_bytes(content)
for name in ['input-colour.nes','input-colour.gba','input-colour.sms','input-colour.gg','input-colour.sg','colour-orbit.gbc']:shutil.copy2(fixtures/name,a.out/name)
shutil.copy2(root/'app/src/main/assets/starter/orbit-collector.gb',a.out/'orbit-collector.gb')
for url,suffix,target in [
 ('https://github.com/gilyon/snes-tests/releases/download/v1.4/snes-tests.zip','cputest/cputest-basic.sfc','cputest-basic.sfc'),
 ('https://github.com/JaCzekanski/ps1-tests/releases/download/build-158/tests.zip','input/pad/pad.exe','pad.exe')]:
 path=fixtures/Path(url).name
 if not path.exists():path.write_bytes(fetch(url))
 with zipfile.ZipFile(path) as z:
  entry=next(n for n in z.namelist() if n.endswith(suffix));(a.out/target).write_bytes(z.read(entry))
print('Staged test-only fixtures and six hash-verified x86_64 cores. No production source-set uses this directory.')
