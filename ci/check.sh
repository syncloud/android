#!/bin/sh -e
pip install --quiet google-auth requests

python3 ci/play_check.py
