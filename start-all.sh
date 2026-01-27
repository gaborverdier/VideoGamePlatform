#!/usr/bin/env bash
# Cross-platform-ish start script for Unix-like systems
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVICES=(
  "services/platform-service-java"
  "services/publisher-service-java"
  "services/player-simulator-java"
  "services/analytics-service-kotlin"
)

mkdir -p "$ROOT_DIR/logs"

for s in "${SERVICES[@]}"; do
  echo "Starting $s"
  if [ -f "$ROOT_DIR/$s/gradlew" ]; then
    (cd "$ROOT_DIR/$s" && nohup ./gradlew :app:bootRun > "$ROOT_DIR/logs/$(basename $s).log" 2>&1 &)
  else
    echo "  gradlew not found in $s, skipping"
  fi
done

echo "Started all services. Logs: $ROOT_DIR/logs"