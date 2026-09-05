#!/bin/sh -e
pip install --quiet google-auth google-api-python-client

VERSION=$(grep versionName syncloud/build.gradle | head -1 | cut -d'"' -f2)

if [ "$DRONE_BRANCH" = stable ]; then
    TRACK=production
else
    TRACK=internal
fi

python3 ci/play_publish.py "artifact/syncloud-$VERSION.aab" "$TRACK"
