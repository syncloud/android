local platform = "26.08.01";
local distro = "bookworm";
local redroid = "redroid/redroid:14.0.0-latest";
local sdk = "runmymind/docker-android-sdk:ubuntu-standalone-20240812";
local instrumentation = "org.syncloud.android.test/androidx.test.runner.AndroidJUnitRunner";
local screenshot = "artifact/screenshots/discovery-with-device.png";

local build() = {
    kind: "pipeline",
    type: "docker",
    name: "android",

    platform: {
        os: "linux",
        arch: "amd64"
    },
    steps: [
        {
            name: "build",
            image: sdk,
            environment: {
                KEY_STORE: {
                  from_secret: "KEY_STORE"
                },
                ANDROID_STORE_FILE: {
                  from_secret: "ANDROID_STORE_FILE"
                },
                ANDROID_STORE_PASSWORD: {
                  from_secret: "ANDROID_STORE_PASSWORD"
                },
                ANDROID_KEY_ALIAS: {
                  from_secret: "ANDROID_KEY_ALIAS"
                },
                ANDROID_KEY_PASSWORD: {
                  from_secret: "ANDROID_KEY_PASSWORD"
                },
            },
            commands: [
                "./gradlew clean testDebugUnitTest assemble assembleDebugAndroidTest bundleRelease"
            ]
        },
        {
            name: "discovery",
            image: sdk,
            commands: [
                "for i in $(seq 1 60); do adb connect redroid:5555 >/dev/null 2>&1; [ \"$(adb -s redroid:5555 shell getprop sys.boot_completed 2>/dev/null | tr -d '\\r')\" = \"1\" ] && break; sleep 5; done",
                "adb devices",
                "adb -s redroid:5555 shell getprop ro.build.version.sdk",
                "adb -s redroid:5555 install -r -t syncloud/build/outputs/apk/debug/*.apk",
                "adb -s redroid:5555 install -r -t syncloud/build/outputs/apk/androidTest/debug/*.apk",
                "adb -s redroid:5555 shell am instrument -w " + instrumentation + " 2>&1 | tee instrument.log",
                "grep -q 'OK (' instrument.log"
            ]
        },
        {
            name: "collect",
            image: sdk,
            commands: [
                "mkdir -p artifact/screenshots",
                "VERSION=$(grep versionName syncloud/build.gradle | head -1 | cut -d'\"' -f2)",
                "for apk in syncloud/build/outputs/apk/release/*.apk; do [ -f \"$apk\" ] && cp \"$apk\" artifact/syncloud-$VERSION.apk; done || true",
                "for aab in syncloud/build/outputs/bundle/release/*.aab; do [ -f \"$aab\" ] && cp \"$aab\" artifact/syncloud-$VERSION.aab; done || true",
                "cp syncloud/build/outputs/roborazzi/*.png artifact/screenshots/ || true",
                "timeout 30 adb connect redroid:5555 >/dev/null 2>&1 || true",
                "timeout 60 adb -s redroid:5555 exec-out run-as org.syncloud.android cat files/screenshots/discovery-with-device.png > " + screenshot + " || true",
                "head -c 8 " + screenshot + " 2>/dev/null | grep -q PNG || rm -f " + screenshot,
                "timeout 60 adb -s redroid:5555 logcat -d -s NsdDiscovery Resolver EventToDeviceConverter DiscoveryManager MulticastLock UnicastDiscovery NsdService serviceDiscovery > artifact/discovery-logcat.txt || true",
                "cp instrument.log artifact/instrument.log || true",
                "ls -la artifact artifact/screenshots"
            ],
            when: {
                status: [ "failure", "success" ]
            }
        },
        {
            name: "publish to github",
            image: "plugins/github-release:1.0.0",
            settings: {
                api_key: {
                    from_secret: "github_token"
                },
                files: [ "artifact/*.apk", "artifact/*.aab" ],
                overwrite: true,
                file_exists: "overwrite"
            },
            when: {
                event: [ "tag" ]
            }
        },
        {
            name: "publish to play",
            image: "python:3.12-slim-bookworm",
            environment: {
                PLAY_SERVICE_ACCOUNT: {
                    from_secret: "PLAY_SERVICE_ACCOUNT"
                }
            },
            commands: [
                "pip install --quiet google-auth google-api-python-client",
                "VERSION=$(grep versionName syncloud/build.gradle | head -1 | cut -d'\"' -f2)",
                "if [ \"$DRONE_BRANCH\" = stable ]; then TRACK=production; else TRACK=internal; fi",
                "python3 ci/play_publish.py artifact/syncloud-$VERSION.aab $TRACK"
            ],
            when: {
                branch: [ "master", "stable" ],
                event: [ "push" ]
            }
        },
        {
            name: "artifact",
            image: "appleboy/drone-scp",
            settings: {
                host: {
                    from_secret: "artifact_host"
                },
                username: "artifact",
                key: {
                    from_secret: "artifact_key"
                },
                timeout: "2m",
                command_timeout: "2m",
                target: "/home/artifact/repo/android/${DRONE_BUILD_NUMBER}",
                source: "artifact/*",
                strip_components: 1
            },
            when: {
                status: [ "failure", "success" ]
            }
        }
    ],
    services: [
        {
            name: "device." + distro + ".com",
            image: "syncloud/platform-" + distro + ":" + platform,
            privileged: true,
            volumes: [
                { name: "dbus", path: "/var/run/dbus" },
                { name: "dev", path: "/dev" }
            ]
        },
        {
            name: "redroid",
            image: redroid,
            privileged: true,
            command: [ "androidboot.redroid_gpu_mode=guest" ],
            volumes: [
                { name: "redroid-data", path: "/data" }
            ]
        }
    ],
    volumes: [
        { name: "dbus", host: { path: "/var/run/dbus" } },
        { name: "dev", host: { path: "/dev" } },
        { name: "redroid-data", temp: {} }
    ],
    trigger: {
        event: [ "push", "tag" ]
    }
};

[
    build()
]
