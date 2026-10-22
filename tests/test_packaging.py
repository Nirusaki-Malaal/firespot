"""Negative tests against actual built core artifacts; run after build_gameboy.sh."""
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class ArtifactFailures(unittest.TestCase):
    def run_corrupted(self, mutate, message):
        with tempfile.TemporaryDirectory(prefix="firespot-integrity-") as work:
            cores = Path(work) / "cores"
            shutil.copytree(ROOT / "artifacts/gameboy", cores)
            mutate(cores)
            result = subprocess.run(
                ["python3", str(ROOT / "scripts/verify_artifacts.py"), "--cores", str(cores)],
                capture_output=True, text=True,
            )
            self.assertNotEqual(result.returncode, 0)
            self.assertIn(message, result.stderr)

    def test_wrong_cpu(self):
        self.run_corrupted(
            lambda cores: shutil.copyfile(cores / "x86_64/libretro_gambatte.so", cores / "armeabi-v7a/libretro_gambatte.so"),
            "wrong CPU architecture",
        )

    def test_changed_bytes(self):
        def mutate(cores):
            with (cores / "arm64-v8a/libretro_gambatte.so").open("ab") as stream:
                stream.write(b"tampered")
        self.run_corrupted(mutate, "differ from the recorded manifest")


if __name__ == "__main__":
    unittest.main()
