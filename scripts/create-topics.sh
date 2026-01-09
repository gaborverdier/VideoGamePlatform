#!/usr/bin/env bash
set -euo pipefail

BOOTSTRAP_SERVER="${BOOTSTRAP_SERVER:-kafka:9092}"

echo "[kafka-init] Waiting for Kafka at ${BOOTSTRAP_SERVER} ..."

until kafka-broker-api-versions --bootstrap-server "${BOOTSTRAP_SERVER}" >/dev/null 2>&1; do
  sleep 1
done

echo "[kafka-init] Kafka is up. Creating topics..."

create_topic () {
  local topic="$1"
  local partitions="$2"
  local rf="$3"

  kafka-topics \
    --bootstrap-server "${BOOTSTRAP_SERVER}" \
    --create --if-not-exists \
    --topic "${topic}" \
    --partitions "${partitions}" \
    --replication-factor "${rf}"

  echo "[kafka-init] ensured topic: ${topic} (partitions=${partitions}, rf=${rf})"
}

create_topic "game.events"        3 1
create_topic "users.registered"   1 1
create_topic "games.published"    1 1
create_topic "games.purchased"    3 1
create_topic "games.reviews"      3 1

echo "[kafka-init] Done."
