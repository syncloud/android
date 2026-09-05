#!/bin/sh -e
pip install --quiet google-auth google-api-python-client

python3 ci/play_publish.py "artifact/syncloud-$DRONE_TAG.aab" internal
