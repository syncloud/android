local platform = "26.08.01";
local distro = "bookworm";
local redroid = "14.0.0-latest";
local sdk = "ubuntu-standalone-20240812";
local python = "3.12-slim-bookworm";
local github_release = "1.0.0";

local platform_image = "syncloud/platform-" + distro + ":" + platform;
local redroid_image = "redroid/redroid:" + redroid;
local sdk_image = "runmymind/docker-android-sdk:" + sdk;

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
            image: sdk_image,
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
            image: sdk_image,
            commands: [
                "sh ci/discovery.sh"
            ]
        },
        {
            name: "collect",
            image: sdk_image,
            commands: [
                "sh ci/collect.sh"
            ],
            when: {
                status: [ "failure", "success" ]
            }
        },
        {
            name: "publish to github",
            image: "plugins/github-release:" + github_release,
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
            image: "python:" + python,
            environment: {
                PLAY_SERVICE_ACCOUNT: {
                    from_secret: "PLAY_SERVICE_ACCOUNT"
                }
            },
            commands: [
                "sh ci/publish.sh"
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
            image: platform_image,
            privileged: true,
            volumes: [
                { name: "dbus", path: "/var/run/dbus" },
                { name: "dev", path: "/dev" }
            ]
        },
        {
            name: "redroid",
            image: redroid_image,
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
