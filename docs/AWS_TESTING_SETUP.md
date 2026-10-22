# Fire TV remote testing prerequisites

Installed and verified on October 22, 2026:

- AWS CLI 2.38.4: `/home/nirusaki/.local/bin/aws`
- Session Manager plugin 1.2.896.0: `/home/nirusaki/.local/bin/session-manager-plugin`
- ADB 37.0.0p1 (protocol 1.0.41): `/usr/bin/adb`

The AWS tools are stored under `/mnt/files/FireSpotTools/` and linked into the existing user PATH. That filesystem must be mounted to use them. The system partition was nearly full, so the installation uses the larger data partition.

Official AWS Linux x86_64 packages were downloaded and their detached signatures verified against the public-key fingerprints published in AWS documentation. On Arch Linux, the Session Manager Debian package payload was extracted to the user tools directory rather than installed with Debian's package manager. The plugin executable and minimum version check both pass.

Verify in a terminal:

```sh
aws --version
session-manager-plugin --version
adb version
```

Next, start an official Fire TV virtual-device session and follow its supplied AWS/ADB connection instructions. No AWS credentials, account configuration, or remote session were created by this setup.

References:

- https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html
- https://docs.aws.amazon.com/systems-manager/latest/userguide/session-manager-working-with-install-plugin.html
