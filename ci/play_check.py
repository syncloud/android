import base64
import json
import os
import sys

import google.auth.transport.requests as transport
from google.oauth2 import service_account

PACKAGE = "org.syncloud.android"
SCOPE = "https://www.googleapis.com/auth/playdeveloperreporting"
BASE = "https://playdeveloperreporting.googleapis.com/v1beta1/apps/" + PACKAGE

info = json.loads(base64.b64decode(os.environ["PLAY_SERVICE_ACCOUNT"]))
credentials = service_account.Credentials.from_service_account_info(info, scopes=[SCOPE])
session = transport.AuthorizedSession(credentials)


def get(path):
    response = session.get(BASE + path, timeout=30)
    response.raise_for_status()
    return response.json()


issues = get("/errorIssues:search?pageSize=10").get("errorIssues", [])
for issue in issues:
    print("error issue: %s %s %s" % (
        issue.get("type", ""), issue.get("cause", ""), issue.get("location", "")), flush=True)

anomalies = get("/anomalies").get("anomalies", [])
if not anomalies:
    print("no anomalies reported by play", flush=True)
    sys.exit(0)

for anomaly in anomalies:
    print("anomaly: %s" % json.dumps(anomaly), flush=True)

print("play is reporting anomalies, not publishing", flush=True)
sys.exit(1)
