# Fire TV virtual-device connection friction

**Task attempted:** Set up an Amazon Fire TV virtual device and install FireSpot for testing.

**Steps taken:** I installed the AWS CLI, Session Manager plugin, and Android Platform Tools, then configured AWS credentials and opened the virtual-device connection flow. I expected the flow to provide a downloadable ADB connection script, as referenced by the setup instructions.

**Expected result:** The session page provides the connection script or an equivalent command, and running it makes the virtual device visible in `adb devices`.

**Actual result:** The connection flow did not provide a script to download, so I could not run the documented `chmod` and script commands to connect ADB. This blocked testing on the target Fire TV virtual device.

**Severity:** High. The missing script blocked target-device testing.

**Workaround:** I installed the APK in Waydroid and continued local Android checks. Waydroid allowed basic installation and interaction, but it did not replace testing on a Fire TV virtual device.

**Actionable suggestion:** Show the generated connection script as a clearly labeled download in the virtual-device session page. Include a copyable command as a fallback, display the expected ADB serial, and report whether the AWS session, Session Manager plugin, and ADB connection are each ready. This would make setup failures easier to identify and recover from.
