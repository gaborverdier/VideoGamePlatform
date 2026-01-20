# Platform Service

Player interaction and game platform service for the Video Game Platform.

## Features

- User registration and management
- Game catalog browsing and searching
- Game purchase functionality
- User library management
- REST API endpoints
- Kafka event publishing (user-registered, game-purchased)
- Kafka event consumption (game updates from publisher)
- PostgreSQL/H2 database support
- Avro schema integration

## Tech Stack

- Spring Boot 3.3.5
- Java 21
- PostgreSQL / H2
- Apache Kafka
- Apache Avro
- Lombok
- Spring Data JPA

## Getting Started

### Prerequisites
- Java 21
- Kafka running on localhost:9092
- Schema Registry on localhost:8081
- PostgreSQL (optional, H2 used by default)

### Build and Run

```bash
cd services/platform-service-java
./gradlew. bat clean build
./gradlew.bat : app: run