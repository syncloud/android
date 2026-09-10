#!/bin/sh -e
CACHE=.adb-device

connect() {
    adb connect "$1" >/dev/null 2>&1 || true
    [ -n "$(adb -s "$1" shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r')" ]
}

if [ -s "$CACHE" ] && connect "$(cat "$CACHE")"; then
    cat "$CACHE"
    exit 0
fi
rm -f "$CACHE"

candidates() {
    getent hosts redroid 2>/dev/null | awk '{ print $1 }'
    for own in $(hostname -I 2>/dev/null); do
        prefix=$(echo "$own" | cut -d. -f1-3)
        for host in 2 3 4 5 6 7 8 9; do
            echo "$prefix.$host"
        done
    done
}

for ip in $(candidates | sort -u); do
    timeout 2 bash -c "echo > /dev/tcp/$ip/5555" 2>/dev/null || continue
    if connect "$ip:5555"; then
        printf '%s:5555' "$ip" > "$CACHE"
        cat "$CACHE"
        exit 0
    fi
    adb disconnect "$ip:5555" >/dev/null 2>&1 || true
done

exit 1
