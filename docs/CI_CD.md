# CI, signing and releases

## Continuous integration

The first standalone repository run passed: https://github.com/Nirusaki-Malaal/firespot/actions/runs/39773037599.

Android CI runs on main/codex branch pushes, pull requests and manual dispatch. It installs pinned Android SDK/NDK tools, builds genuine Gambatte assets from the pinned source, builds both app flavors, runs lint/JVM tests, checks APK CPU/packaging rules, runs negative packaging tests, and executes the real host core against original cartridges. Reports and debug APKs are uploaded even when a prior check fails.

The build uses JDK 17, Gradle 8.6 with wrapper checksum, SDK/build-tools 34 and NDK 28.2.13676358. Unit tests cover catalog signature tampering, another publisher's signature, altered bytes/length, wrong ELF architecture and redirect host/protocol restrictions. Host tests cover real frames, audio samples, controls, scoring and deterministic state restoration.

CI's Linux host checks are not Android device tests or Fire TV certification. Use `tests/android_online_smoke.py` with an explicitly selected ADB test device as described in VALIDATION.md.

## Signed APK delivery

The release workflow runs on version tags (`v*`) and manual dispatch. Tag runs publish a GitHub release; manual runs only upload a signed build artifact. The workflow builds/minifies both release flavors, runs release lint/JVM tests and verifies native packaging before publishing APKs, source archives, notices and SHA256SUMS.

Required repository Actions secrets:

| Secret | Value |
|---|---|
| FIRESPOT_KEYSTORE_BASE64 | Base64 PKCS12 distribution keystore |
| FIRESPOT_STORE_PASSWORD | Keystore password |
| FIRESPOT_KEY_PASSWORD | Password for the `firespot` key alias |

These have been configured for this repository. Keep independent private backups of the keystore and passwords. Losing the key prevents normal updates of existing installations. Keystore files and passwords must never be committed, pasted into issue reports or printed in workflow logs.

The job restores the key into runner temporary storage, sets signing inputs only on the build step, and removes the temporary keystore in an always-run cleanup step. Pull-request CI does not use release signing secrets. A build failure stops release publication.

Local signing uses environment variables rather than hard-coded paths or credentials:

```sh
export FIRESPOT_KEYSTORE=/private/path/firespot-release.p12
# Set FIRESPOT_STORE_PASSWORD and FIRESPOT_KEY_PASSWORD privately.
./gradlew :app:assembleStandardRelease :app:assembleDemoRelease -PincludeX86=true
```

Before a version release, update versionName/versionCode, RELEASE_NOTES.md, validation results and screenshots, then tag the reviewed commit:

```sh
git tag -a v0.2.0 -m 'FireSpot 0.2.0'
git push origin v0.2.0
```

Do not retag an already distributed release to silently replace its app or core binaries. Publish a new version. The separate core release has its own pinned release ID and catalog revision.

## Core catalog publication

The public core repository hosts native binaries, exact corresponding source, licenses and original game downloads as release assets. The application embeds the catalog signing public key. The RSA catalog private key is separate from the Android APK key and is not a GitHub Actions secret in this app repository.

1. Rebuild pinned cores with the documented scripts.
2. Test each new core and assign an honest experimental/verified status.
3. Stage release files and sign the exact catalog bytes using prepare_catalog_release.py and the private catalog key.
4. Verify signatures, checksums, ELF ABIs, sources and notices.
5. Publish all referenced release assets before shipping an APK that points to them.
6. Verify unauthenticated downloads with scripts/verify_online_release.py and the real Android flow.

The current app uses the explicit phase1-v2 catalog endpoint. Adding a new catalog release also needs the app endpoint/revision policy updated intentionally. Offline cached metadata does not grant arbitrary remote code execution privileges: catalog URLs and artifact bounds are still checked, and the code runs only through the supported native frontend.

## History and contribution policy

The Git history preserves the upstream commits and adds real implementation commits. It does not backdate work or fabricate changes. Record abandoned designs only when they explain a remaining tradeoff. Never commit build outputs, machine SDK paths, private signing material, AWS credentials, user ROM collections or personal saves.
