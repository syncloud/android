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
            image: "runmymind/docker-android-sdk:ubuntu-standalone-20240812",
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
                "sdkmanager 'build-tools;35.0.0'",
                "./gradlew clean test assemble"
            ]
        },
        {
                name: "publish to github",
                image: "plugins/github-release:1.0.0",
                settings: {
                    api_key: {
                        from_secret: "github_token"
                    },
                    files: "syncloud/build/outputs/apk/release/*",
                    overwrite: true,
                    file_exists: "overwrite"
                },
                when: {
                    event: [ "tag" ]
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
                source: "syncloud/build/outputs/apk/release/*.apk",
                    strip_components: 5
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
