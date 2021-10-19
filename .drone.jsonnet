local build() = {
    kind: "pipeline",
    name: "android",

    platform: {
        os: "linux",
        arch: "amd64"
    },
    steps: [
        {
            name: "build",
            image: "syncloud/android-sdk",
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
                "yes | /opt/android/tools/bin/sdkmanager build-tools;31.0.0",
                "./gradlew clean test assemble"
            ]
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
                source: "syncloud/build/outputs/apk/prod/release/*",
                    strip_components: 6
            },
            when: {
                status: [ "failure", "success" ]
            }
        }
    ]
};

[
    build()
]
