# VideoGamePlatform

An event-driven gaming platform leveraging Kafka, Avro, Schema Registry, and Postgres.

## Table of Contents
1. [Tech Stack](#tech-stack)
2. [Folder Structure](#folder-structure)
3. [Prerequisites](#prerequisites)
4. [First-Time Setup](#first-time-setup)
5. [Infrastructure Setup](#infrastructure-setup)
6. [Avro Schemas Module](#avro-schemas-module)
7. [Services](#services)
8. [Common Kafka/Schema Registry Configuration](#common-kafka-schema-registry-configuration)

---

## 1. Tech Stack

### Backend
- **Java**: Toolchain 23 configured in Gradle.
- **Kotlin**: For Kafka Streams services.
- **Gradle**: Kotlin DSL.

### Streaming
- **Kafka**: Confluent cp-kafka:7.8.3 (KRaft mode).
- **Schema Registry**: cp-schema-registry:7.8.3.
- **Avro**: Gradle Avro plugin and Confluent Avro serializer.
- **Kafka Streams**: For analytics/quality services.

### Database
- **Postgres**: Version 16.
- **PgAdmin**: Version 4.

### Tooling
- **Kafka UI**: provectuslabs/kafka-ui.
- **Docker Desktop**.

---

## 2. Folder Structure

```
VideoGamePlatform/
├─ README.md
├─ docker/
│  └─ docker-compose.yml
├─ docs/
│  └─ notes.md
├─ common/
│  └─ avro-schemas/
│     ├─ build.gradle.kts
│     ├─ settings.gradle.kts
│     └─ src/main/avro/   (Avro .avsc schemas)
├─ services/
│  ├─ platform-service-java/
│  │  └─ app/ (Gradle Java app)
│  ├─ publisher-service-java/
│  │  └─ app/
│  ├─ player-simulator-java/
│  │  └─ app/
│  ├─ analytics-service-kotlin/
│  │  └─ app/
│  └─ quality-service-kotlin/
│     └─ app/
└─ scripts/
   └─ (SQL, helper scripts – to be filled)
```

---

## 3. Prerequisites

Ensure you have the following installed:
- **Git**.
- **Docker Desktop** (2–3 GB disk space for images).
- **Java 23** (or JDK compatible with Gradle toolchain, 17+ recommended).
- **IDE**: VS Code (with Java + Kotlin plugins) or IntelliJ.

Gradle wrappers are included; no need to install Gradle globally.

---

## 4. First-Time Setup

1. Clone the repository:
   ```bash
   git clone <repo-url> VideoGamePlatform
   cd VideoGamePlatform
   ```

2. Open the root folder in your IDE:
   - **VS Code**: `code .`
   - **IntelliJ**: Open the folder as a project and import Gradle projects.

---

## 5. Infrastructure Setup

1. Navigate to the `docker` folder:
   ```bash
   cd docker
   docker compose up -d
   ```

2. Verify containers:
   ```bash
   docker ps
   ```

   Expected containers:
   - **kafka**: cp-kafka:7.8.3 (port 9092).
   - **schema-registry**: (port 8081).
   - **kafka-ui**: (port 8080).
   - **postgres**: (port 5432).
   - **pgadmin**: (port 5050).

3. Quick checks:
   - Kafka UI: [http://localhost:8080](http://localhost:8080).
   - Schema Registry: [http://localhost:8081/subjects](http://localhost:8081/subjects) (should return `[]` initially).
   - PgAdmin: [http://localhost:5050](http://localhost:5050).
     - **Login**: `admin@local.com` / `admin`.

4. Stop everything:
   ```bash
   docker compose down
   ```
   Use `docker compose down -v` to remove volumes (if you are fine with losing DB data).

---

## 6. Avro Schemas Module

### Location
`common/avro-schemas`

### Features
- Uses the Gradle Avro plugin.
- Expects `.avsc` schemas in `src/main/avro`.
- Generates Java classes under `build/generated-main-avro-java`.

### Build
```bash
cd common/avro-schemas
./gradlew.bat build
```

### Example Schema
`src/main/avro/user-registered.avsc`:
```json
{
  "type": "record",
  "namespace": "com.gaming.events",
  "name": "UserRegistered",
  "fields": [
    { "name": "userId", "type": "string" },
    { "name": "username", "type": "string" },
    { "name": "email", "type": "string" },
    { "name": "registrationTimestamp", "type": "long" }
  ]
}
```

---

## 7. Services

### 7.1 Platform Service (Java)
- **Path**: `services/platform-service-java`.
- **Run**:
  ```bash
  cd services/platform-service-java
  ./gradlew.bat :app:run
  ```
- **Main Class**: `org.example.App` (currently prints a message).
- **Future Plans**:
  - Consume events (e.g., `user-registered`, `game-purchased`).
  - Maintain Postgres tables (e.g., `users`, `games`, `purchases`).

### 7.2 Kotlin Services (Kafka Streams)
- **Paths**:
  - `services/analytics-service-kotlin`.
  - `services/quality-service-kotlin`.
- **Run**:
  ```bash
  cd services/analytics-service-kotlin
  ./gradlew.bat :app:run
  ```
- **Dependencies**:
  - `kafka-streams` 7.8.3-ce.
  - `kafka-avro-serializer` 7.8.3.
  - `avro` 1.11.3.
  - `kotlin-stdlib`.
- **Purpose**:
  - Aggregation, analytics, and quality metrics (e.g., crash stats, top games).

---

## 8. Common Kafka/Schema Registry Configuration

### Host Configuration
For services running on the host:
```properties
bootstrap.servers=localhost:9092
schema.registry.url=http://localhost:8081
```

### Dockerized Configuration
For services running inside Docker containers:
```properties
bootstrap.servers=kafka:29092
schema.registry.url=http://schema-registry:8081
```

Currently, only the host configuration is relevant.