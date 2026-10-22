#!/usr/bin/env python3
"""Verify published catalog signatures and every release core/game without GitHub credentials."""
import argparse,hashlib,json,subprocess,tempfile,urllib.request
from pathlib import Path
from verify_artifacts import check_elf
BASE='https://github.com/Nirusaki-Malaal/firespot-cores/releases/download/phase1-v2'
def fetch(url):
 req=urllib.request.Request(url,headers={'User-Agent':'FireSpot-release-check'})
 with urllib.request.urlopen(req,timeout=30) as r:return r.read()
def main():
 with tempfile.TemporaryDirectory(prefix='firespot-release-') as temp:
  d=Path(temp);payload=fetch(BASE+'/catalog.json');signature=fetch(BASE+'/catalog.sig')
  (d/'catalog.json').write_bytes(payload);(d/'catalog.sig').write_bytes(signature)
  subprocess.run(['openssl','dgst','-sha256','-verify','app/src/main/assets/catalog/public-key.der','-keyform','DER','-signature',str(d/'catalog.sig'),str(d/'catalog.json')],check=True)
  data=json.loads(payload);count=0
  for core in data['cores']:
   for asset in core['artifacts']:
    binary=fetch(asset['url']);assert len(binary)==asset['bytes'] and hashlib.sha256(binary).hexdigest()==asset['sha256'];check_elf(binary,asset['abi']);count+=1
   for url in [core['sourceArchiveUrl'],core['licenseUrl']]:assert len(fetch(url))>0
  for game in data['games']:
   a=game['artifact'];rom=fetch(a['url']);assert len(rom)==a['bytes'] and hashlib.sha256(rom).hexdigest()==a['sha256']
  print(f'PASS: Public signed catalog; {count} CPU-correct core downloads; matching source/licenses; {len(data["games"])} verified game downloads.')
if __name__=='__main__':main()
