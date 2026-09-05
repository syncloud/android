#!/bin/sh
OUT=artifact/diagnostics
mkdir -p "$OUT"

for image in "$@"; do
    name=$(docker ps -a --filter "ancestor=$image" --format '{{.Names}}' | head -1)
    safe=$(echo "$image" | tr '/:.' '___')
    if [ -z "$name" ]; then
        echo "no container found for $image" > "$OUT/$safe.state"
        continue
    fi
    docker logs --tail 3000 "$name" > "$OUT/$safe.log" 2>&1 || true
    docker inspect "$name" --format \
        'image={{.Config.Image}}
status={{.State.Status}}
exitCode={{.State.ExitCode}}
oomKilled={{.State.OOMKilled}}
error={{.State.Error}}
startedAt={{.State.StartedAt}}
finishedAt={{.State.FinishedAt}}
restartCount={{.RestartCount}}' > "$OUT/$safe.state" 2>&1 || true
done

docker stats --no-stream --format 'table {{.Name}}\t{{.MemUsage}}\t{{.CPUPerc}}' > "$OUT/docker-stats.txt" 2>&1 || true
ls -la "$OUT"
