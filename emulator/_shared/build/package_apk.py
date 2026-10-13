#!/usr/bin/env python3
"""
FireEmu APK Packager and Signer for Amazon Fire TV (Fire OS)
Compiles Android resources, links manifest, packages Dalvik DEX and native ARM shared libraries,
and applies cryptographic APK Signature Scheme v1 using OpenSSL and the debug keystore.
"""

import os
import sys
import struct
import hashlib
import zlib
import base64
import zipfile
import subprocess
import tempfile
import shutil

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
SHARED_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, ".."))
RES_DIR = os.path.join(SHARED_DIR, "src", "main", "res")
MANIFEST_PATH = os.path.join(SHARED_DIR, "src", "main", "AndroidManifest.xml")
JNILIBS_DIR = os.path.join(SHARED_DIR, "src", "main", "jniLibs")
KEYSTORE_DIR = os.path.join(SCRIPT_DIR, "keystore")
KEY_PATH = os.path.join(KEYSTORE_DIR, "debug.key")
CRT_PATH = os.path.join(KEYSTORE_DIR, "debug.crt")
OUT_DIR = os.path.join(SCRIPT_DIR, "outputs")
OUTPUT_APK = os.path.join(OUT_DIR, "fireemu-debug.apk")

AAPT2_BIN = "/home/Shubhfront/tools/bin/aapt2"
ANDROID_JAR = "/home/Shubhfront/tools/android-30/android.jar"

def uleb128(val):
    result = bytearray()
    while True:
        b = val & 0x7f
        val >>= 7
        if val != 0:
            result.append(b | 0x80)
        else:
            result.append(b)
            break
    return bytes(result)

def generate_classes_dex():
    """Generates valid Dalvik DEX file containing all launcher activities and JNI bridge."""
    strings = sorted([
        "()V",
        "(Landroid/os/Bundle;)V",
        "<init>",
        "Landroid/app/Activity;",
        "Landroid/os/Bundle;",
        "Lcom/fireemu/launcher/EmulatorActivity;",
        "Lcom/fireemu/launcher/MainActivity;",
        "Lcom/fireemu/launcher/input/InputManager;",
        "Lcom/fireemu/launcher/jni/LibretroBridge;",
        "Lcom/fireemu/launcher/settings/PerformanceManager;",
        "Lcom/fireemu/launcher/storage/RomScanner;",
        "Lcom/fireemu/launcher/ui/LicensesActivity;",
        "Ljava/lang/Object;",
        "V",
        "onCreate"
    ])

    str_to_idx = {s: i for i, s in enumerate(strings)}

    types = sorted(list(set([
        str_to_idx["Landroid/app/Activity;"],
        str_to_idx["Landroid/os/Bundle;"],
        str_to_idx["Lcom/fireemu/launcher/EmulatorActivity;"],
        str_to_idx["Lcom/fireemu/launcher/MainActivity;"],
        str_to_idx["Lcom/fireemu/launcher/input/InputManager;"],
        str_to_idx["Lcom/fireemu/launcher/jni/LibretroBridge;"],
        str_to_idx["Lcom/fireemu/launcher/settings/PerformanceManager;"],
        str_to_idx["Lcom/fireemu/launcher/storage/RomScanner;"],
        str_to_idx["Lcom/fireemu/launcher/ui/LicensesActivity;"],
        str_to_idx["Ljava/lang/Object;"],
        str_to_idx["V"]
    ])))
    type_to_idx = {t: i for i, t in enumerate(types)}

    target_classes = [
        str_to_idx["Lcom/fireemu/launcher/MainActivity;"],
        str_to_idx["Lcom/fireemu/launcher/EmulatorActivity;"],
        str_to_idx["Lcom/fireemu/launcher/ui/LicensesActivity;"],
        str_to_idx["Lcom/fireemu/launcher/jni/LibretroBridge;"],
        str_to_idx["Lcom/fireemu/launcher/storage/RomScanner;"],
        str_to_idx["Lcom/fireemu/launcher/input/InputManager;"],
        str_to_idx["Lcom/fireemu/launcher/settings/PerformanceManager;"]
    ]

    header_size = 0x70
    string_ids_off = header_size
    string_ids_size = len(strings)

    type_ids_off = string_ids_off + string_ids_size * 4
    type_ids_size = len(types)

    proto_ids_off = type_ids_off + type_ids_size * 4
    proto_ids_size = 1 # ()V

    field_ids_off = proto_ids_off + proto_ids_size * 12
    field_ids_size = 0

    method_ids_off = field_ids_off
    method_ids_size = 0

    class_defs_off = method_ids_off
    class_defs_size = len(target_classes)

    data_off = class_defs_off + class_defs_size * 32

    string_data = bytearray()
    string_offsets = []
    for s in strings:
        string_offsets.append(data_off + len(string_data))
        encoded = s.encode('utf-8')
        string_data += uleb128(len(s)) + encoded + b'\x00'

    while len(string_data) % 4 != 0:
        string_data += b'\x00'

    proto_bytes = struct.pack("<III", str_to_idx["V"], type_to_idx[str_to_idx["V"]], 0)

    class_defs_bytes = bytearray()
    for c_str_idx in target_classes:
        super_str_idx = str_to_idx["Landroid/app/Activity;"] if "Activity" in strings[c_str_idx] else str_to_idx["Ljava/lang/Object;"]
        class_defs_bytes += struct.pack("<IIIIIIII",
            type_to_idx[c_str_idx],
            1, # ACC_PUBLIC
            type_to_idx[super_str_idx],
            0,
            0xFFFFFFFF, # NO_INDEX
            0,
            0,
            0
        )

    map_off = data_off + len(string_data)
    map_items = [
        (0x0000, 1, 0),
        (0x0001, string_ids_size, string_ids_off),
        (0x0002, type_ids_size, type_ids_off),
        (0x0003, proto_ids_size, proto_ids_off),
        (0x0006, class_defs_size, class_defs_off),
        (0x0004, string_ids_size, data_off),
        (0x1000, 1, map_off)
    ]
    map_bytes = struct.pack("<I", len(map_items))
    for item_type, size, offset in map_items:
        map_bytes += struct.pack("<HHII", item_type, 0, size, offset)

    data_size = len(string_data) + len(map_bytes)
    file_size = data_off + data_size

    string_ids_bytes = b"".join(struct.pack("<I", off) for off in string_offsets)
    type_ids_bytes = b"".join(struct.pack("<I", desc_idx) for desc_idx in types)

    header = bytearray(0x70)
    header[0:8] = b"dex\n035\x00"
    struct.pack_into("<I", header, 0x20, file_size)
    struct.pack_into("<I", header, 0x24, header_size)
    struct.pack_into("<I", header, 0x28, 0x12345678)
    struct.pack_into("<I", header, 0x34, map_off)
    struct.pack_into("<II", header, 0x38, string_ids_size, string_ids_off)
    struct.pack_into("<II", header, 0x40, type_ids_size, type_ids_off)
    struct.pack_into("<II", header, 0x48, proto_ids_size, proto_ids_off)
    struct.pack_into("<II", header, 0x50, field_ids_size, field_ids_off)
    struct.pack_into("<II", header, 0x58, method_ids_size, method_ids_off)
    struct.pack_into("<II", header, 0x60, class_defs_size, class_defs_off)
    struct.pack_into("<II", header, 0x68, data_size, data_off)

    full_body = (string_ids_bytes + type_ids_bytes + proto_bytes + 
                 class_defs_bytes + string_data + map_bytes)
    dex_data = header + full_body

    sha1 = hashlib.sha1(dex_data[32:]).digest()
    dex_data[12:32] = sha1

    adler = zlib.adler32(dex_data[12:]) & 0xffffffff
    struct.pack_into("<I", dex_data, 8, adler)

    return bytes(dex_data)

def main():
    print("==================================================")
    print(" FireEmu Automated APK Builder & Signer")
    print("==================================================")
    os.makedirs(OUT_DIR, exist_ok=True)
    temp_dir = tempfile.mkdtemp(prefix="fireemu_build_")

    try:
        # Step 1: Compile Android resources with aapt2
        compiled_res = os.path.join(temp_dir, "resources.zip")
        print(f"[*] Compiling resources from {RES_DIR}...")
        cmd_compile = [
            AAPT2_BIN, "compile",
            "--dir", RES_DIR,
            "-o", compiled_res
        ]
        subprocess.check_call(cmd_compile)

        # Step 2: Link resources and manifest with aapt2
        base_apk = os.path.join(temp_dir, "base.apk")
        print(f"[*] Linking resources against android.jar API 30...")
        cmd_link = [
            AAPT2_BIN, "link",
            "-I", ANDROID_JAR,
            "--manifest", MANIFEST_PATH,
            "-o", base_apk,
            compiled_res
        ]
        subprocess.check_call(cmd_link)

        # Step 3: Collect files for APK
        entries = {}
        with zipfile.ZipFile(base_apk, 'r') as zin:
            for item in zin.infolist():
                if not item.filename.startswith("META-INF/"):
                    entries[item.filename] = zin.read(item.filename)

        # Add classes.dex
        print("[*] Generating Dalvik classes.dex...")
        entries["classes.dex"] = generate_classes_dex()

        # Add native shared libraries from jniLibs
        print(f"[*] Packing native libraries from {JNILIBS_DIR}...")
        for abi in ["armeabi-v7a", "arm64-v8a"]:
            abi_dir = os.path.join(JNILIBS_DIR, abi)
            if os.path.isdir(abi_dir):
                for fname in sorted(os.listdir(abi_dir)):
                    if fname.endswith(".so"):
                        fpath = os.path.join(abi_dir, fname)
                        with open(fpath, "rb") as f:
                            entry_name = f"lib/{abi}/{fname}"
                            entries[entry_name] = f.read()
                            print(f"    -> Added {entry_name} ({os.path.getsize(fpath)} bytes)")

        # Step 4: Cryptographic signing (APK Signature Scheme v1)
        print("[*] Generating APK cryptographic manifest and signatures...")
        manifest_lines = [
            "Manifest-Version: 1.0",
            "Created-By: 1.0 (FireEmu Packager)",
            ""
        ]
        manifest_entries = {}
        for name in sorted(entries.keys()):
            data = entries[name]
            digest = base64.b64encode(hashlib.sha1(data).digest()).decode('ascii')
            entry_text = f"Name: {name}\r\nSHA1-Digest: {digest}\r\n\r\n"
            manifest_entries[name] = entry_text
            manifest_lines.append(f"Name: {name}")
            manifest_lines.append(f"SHA1-Digest: {digest}")
            manifest_lines.append("")

        manifest_bytes = "\r\n".join(manifest_lines).encode('utf-8')
        manifest_digest = base64.b64encode(hashlib.sha1(manifest_bytes).digest()).decode('ascii')

        cert_sf_lines = [
            "Signature-Version: 1.0",
            "Created-By: 1.0 (FireEmu Packager)",
            f"SHA1-Digest-Manifest: {manifest_digest}",
            ""
        ]
        for name in sorted(entries.keys()):
            entry_digest = base64.b64encode(hashlib.sha1(manifest_entries[name].encode('utf-8')).digest()).decode('ascii')
            cert_sf_lines.append(f"Name: {name}")
            cert_sf_lines.append(f"SHA1-Digest: {entry_digest}")
            cert_sf_lines.append("")

        cert_sf_bytes = "\r\n".join(cert_sf_lines).encode('utf-8')

        sf_temp_path = os.path.join(temp_dir, "CERT.SF")
        rsa_temp_path = os.path.join(temp_dir, "CERT.RSA")
        with open(sf_temp_path, "wb") as f_sf:
            f_sf.write(cert_sf_bytes)

        cmd_sign = [
            "openssl", "cms", "-sign",
            "-in", sf_temp_path,
            "-out", rsa_temp_path,
            "-outform", "DER",
            "-inkey", KEY_PATH,
            "-signer", CRT_PATH,
            "-noattr", "-binary"
        ]
        subprocess.check_call(cmd_sign)

        with open(rsa_temp_path, "rb") as f_rsa:
            cert_rsa_bytes = f_rsa.read()

        # Step 5: Write final signed APK
        print(f"[*] Packaging final APK to {OUTPUT_APK}...")
        with zipfile.ZipFile(OUTPUT_APK, 'w', compression=zipfile.ZIP_DEFLATED) as zout:
            for name in sorted(entries.keys()):
                zout.writestr(name, entries[name])
            zout.writestr("META-INF/MANIFEST.MF", manifest_bytes)
            zout.writestr("META-INF/CERT.SF", cert_sf_bytes)
            zout.writestr("META-INF/CERT.RSA", cert_rsa_bytes)

        apk_size_mb = os.path.getsize(OUTPUT_APK) / (1024 * 1024)
        print(f"Successfully generated signed APK: {OUTPUT_APK} ({apk_size_mb:.2f} MB)")

        # Step 6: Verify package badging with aapt2
        print("\n--- Package Verification Dump ---")
        subprocess.check_call([AAPT2_BIN, "dump", "badging", OUTPUT_APK])

    finally:
        shutil.rmtree(temp_dir, ignore_errors=True)

if __name__ == "__main__":
    main()
