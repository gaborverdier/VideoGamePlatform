#!/bin/sh
set -eu

BOOTSTRAP_SERVER="${BOOTSTRAP_SERVER:-kafka:9092}"

echo "[kafka-init] Waiting for Kafka at ${BOOTSTRAP_SERVER} ..."

until kafka-broker-api-versions --bootstrap-server "${BOOTSTRAP_SERVER}" >/dev/null 2>&1; do
  sleep 1
done

echo "[kafka-init] Kafka is up. Creating topics..."

create_topic () {
  topic="$1"
  partitions="$2"
  rf="$3"

  kafka-topics \
    --bootstrap-server "${BOOTSTRAP_SERVER}" \
    --create --if-not-exists \
    --topic "${topic}" \
    --partitions "${partitions}" \
    --replication-factor "${rf}"

  echo "[kafka-init] ensured topic: ${topic} (partitions=${partitions}, rf=${rf})"
}

# Topics pour Publisher Service
create_topic "game-patched"              3 1
create_topic "game-metadata-updated"     3 1
create_topic "game-crash-reported"       3 1
create_topic "game-rating-aggregated"    3 1

# Topics pour Platform Service
create_topic "user-registered"           3 1

echo "[kafka-init] Done. All topics created."
