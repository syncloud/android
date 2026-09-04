## Syncloud Android app

Finds Syncloud devices on the local network over mDNS and opens them.

### Build

    ./gradlew clean testDebugUnitTest assemble bundleRelease

Release signing comes from the environment, not from a file. CI supplies
`KEY_STORE` (base64 keystore), `ANDROID_STORE_FILE`, `ANDROID_STORE_PASSWORD`,
`ANDROID_KEY_ALIAS` and `ANDROID_KEY_PASSWORD` as Drone secrets. Without them
the release build is unsigned, which is fine for local work.

### CI

[ci.syncloud.org](http://ci.syncloud.org:8080/syncloud/android) builds every
push and tag, and publishes to `/home/artifact/repo/android/<build>`:

    syncloud-<version>.apk                     sideloading, github releases
    syncloud-<version>.aab                     google play
    screenshots/discovery-with-device.png      discovery screen with a real device
    screenshots/01-auth .. 06-settings.png     every screen, rendered by robolectric
    discovery-logcat.txt                       device log, discovery tags only
    instrument.log                             instrumented test output

Tagging publishes the apk and the aab to a github release.

### Tests

Unit tests and screenshots run on the JVM. The screenshots are rendered with
Robolectric and Roborazzi, so they need no emulator, and are recorded fresh on
every run rather than compared against a baseline.

Discovery is tested against a real device. The pipeline runs the Syncloud
platform image as a service in systemd mode, so avahi advertises exactly as it
does on hardware, and runs Android in a redroid container beside it. The
instrumented tests drive `DiscoveryManager` through the platform's own
`NsdManager` and assert a device is found and listed.

An emulator cannot do this: its guest sits behind QEMU user mode NAT, which
does not carry multicast, so mDNS never reaches it. redroid runs Android on the
host kernel and gets ordinary docker networking.

The build host therefore needs `binder_linux` loaded with binderfs mounted, and
the repository marked trusted in Drone, since both services run privileged.

### Publish to Google Play

1. Take `syncloud-<version>.aab` from the build artifacts or the github release
2. [Play Console](https://play.google.com/console) -> Syncloud -> Production -> Create new release
3. Upload the aab and roll out

Play has required app bundles for updates since November 2021. The apk remains
for sideloading and github releases.

### Discovery

Devices advertise `_ssh._tcp` named `syncloud on <host>` from
`/etc/avahi/services/syncloud.service` in the rootfs image. The app browses that
type with `NsdManager` and matches the name.

`NsdManager` only listens for multicast, so an access point that filters
multicast to wireless clients makes discovery blind while the devices are still
reachable. A unicast fallback runs alongside it, querying from an ephemeral port
so responders reply directly, and reports whatever answers.

### Signing key

Generated with:

    keytool -genkey -v -keystore syncloud.keystore -alias syncloud \
      -keyalg RSA -keysize 2048 -validity 10000

Valid to 2041. The package name and this key's SHA-256 are registered for
Android developer verification.
