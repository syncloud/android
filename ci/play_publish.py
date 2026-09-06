import base64
import json
import os
import sys

from google.oauth2 import service_account
from googleapiclient.discovery import build

PACKAGE = "org.syncloud.android"
SCOPE = "https://www.googleapis.com/auth/androidpublisher"

bundle_path = sys.argv[1]
track = sys.argv[2]

encoded = os.environ.get("PLAY_SERVICE_ACCOUNT")
if not encoded:
    raise SystemExit("PLAY_SERVICE_ACCOUNT is not set")

credentials = service_account.Credentials.from_service_account_info(
    json.loads(base64.b64decode(encoded)), scopes=[SCOPE]
)
edits = build("androidpublisher", "v3", credentials=credentials, cache_discovery=False).edits()

edit_id = edits.insert(body={}, packageName=PACKAGE).execute()["id"]
print("edit %s" % edit_id, flush=True)

uploaded = edits.bundles().upload(
    packageName=PACKAGE,
    editId=edit_id,
    media_body=bundle_path,
    media_mime_type="application/octet-stream",
).execute()
version_code = uploaded["versionCode"]
print("uploaded %s as version code %s" % (bundle_path, version_code), flush=True)

edits.tracks().update(
    packageName=PACKAGE,
    editId=edit_id,
    track=track,
    body={"releases": [{"versionCodes": [str(version_code)], "status": "completed"}]},
).execute()

edits.commit(packageName=PACKAGE, editId=edit_id).execute()
print("released version code %s to %s" % (version_code, track), flush=True)
