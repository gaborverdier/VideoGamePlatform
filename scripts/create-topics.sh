#!/bin/sh
set -eu

BOOTSTRAP_SERVER="${BOOTSTRAP_SERVER:-kafka:9092}"

echo "[kafka-init] Waiting for Kafka at ${BOOTSTRAP_SERVER} ..."

until kafka-broker-api-versions --bootstrap-server "${BOOTSTRAP_SERVER}" >/dev/null 2>&1; do
  sleep 1
done

echo "[kafka-init] Kafka is up. Creating topics..."

create_topic() {
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

# ============================================
# USER / PLAYER EVENTS
# ============================================
create_topic "user-registered" 3 1

# ============================================
# GAME CATALOG EVENTS
# ============================================
create_topic "game-released" 3 1
create_topic "game-updated" 5 1
create_topic "game-deleted" 1 1
create_topic "game-patch-released" 5 1
create_topic "game-availability-changed" 3 1

# ============================================
# PURCHASE / TRANSACTION EVENTS
# ============================================
create_topic "game-purchased" 5 1
create_topic "new-notification" 3 1

# ============================================
# GAMEPLAY / SESSION EVENTS
# ============================================
create_topic "game-session-started" 5 1
create_topic "game-session-ended" 5 1

# ============================================
# QUALITY / CRASH EVENTS
# ============================================
create_topic "game-crash-reported" 5 1

# ============================================
# ANALYTICS / AGGREGATION
# ============================================
create_topic "crash-aggregated" 3 1

echo "[kafka-init] All topics created successfully!"
echo "[kafka-init] Total topics: 14"
