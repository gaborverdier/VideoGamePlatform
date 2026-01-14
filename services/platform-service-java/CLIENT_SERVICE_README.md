# Platform Service - Client Side Implementation

## Overview

The Platform Service Client implementation provides player-facing operations for the Video Game Platform. It handles game browsing, purchases, ratings, and user profiles with full event-driven architecture using Kafka.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Platform Service (Port 8083)              │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              Client Controllers Layer                  │  │
│  │  - GameStoreController  (Browse/Search Games)         │  │
│  │  - PurchaseController   (Buy Games)                   │  │
│  │  - RatingController     (Rate Games)                  │  │
│  │  - ProfileController    (User Profiles)               │  │
│  └───────────────────────────────────────────────────────┘  │
│                          ▼                                    │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              Service Layer (Business Logic)            │  │
│  │  - GameStoreService     (Game queries)                │  │
│  │  - PurchaseService      (Transaction + Kafka)         │  │
│  │  - RatingService        (Rating + Kafka)              │  │
│  │  - ProfileService       (User data)                   │  │
│  └───────────────────────────────────────────────────────┘  │
│                          ▼                                    │
│  ┌─────────────────────┐      ┌─────────────────────────┐  │
│  │   JPA Repositories  │      │   Kafka Producers       │  │
│  │  - GameRepository   │      │  - GamePurchasedProducer│  │
│  │  - PurchaseRepo     │      │  - RatingProducer       │  │
│  │  - UserRepository   │      │                         │  │
│  └─────────────────────┘      └─────────────────────────┘  │
│           ▼                              ▼                    │
│  ┌─────────────────────┐      ┌─────────────────────────┐  │
│  │   H2 Database       │      │   Kafka Topics          │  │
│  │  - games            │      │  - game-purchased       │  │
│  │  - purchases        │      │  - game-rating-submitted│  │
│  │  - users            │      │                         │  │
│  │  - editors          │      │   Schema Registry       │  │
│  └─────────────────────┘      └─────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Project Structure

```
services/platform-service-java/app/src/main/java/com/vgp/
├── PlatformServiceApplication.java      # Main Spring Boot application
│
├── config/
│   └── KafkaConfig.java                 # Centralized Kafka configuration
│
├── shared/                               # Shared domain layer
│   ├── entity/
│   │   ├── Editor.java                  # Game publisher/studio
│   │   ├── Game.java                    # Game entity
│   │   ├── Patch.java                   # Game updates
│   │   ├── User.java                    # Platform user
│   │   └── Purchase.java                # Purchase transaction
│   └── repository/
│       ├── EditorRepository.java
│       ├── GameRepository.java          # Custom queries for search
│       ├── PatchRepository.java
│       ├── UserRepository.java
│       └── PurchaseRepository.java      # Query by user
│
└── client/                               # Client-facing package
    ├── controller/                       # REST API endpoints
    │   ├── GameStoreController.java     # Public: Browse/search
    │   ├── PurchaseController.java      # Auth: Buy games
    │   ├── RatingController.java        # Auth: Submit ratings
    │   └── ProfileController.java       # Auth: User profile
    │
    ├── service/                          # Business logic
    │   ├── GameStoreService.java        # Game queries + DTO mapping
    │   ├── PurchaseService.java         # @Transactional purchase flow
    │   ├── RatingService.java           # Rating validation + event
    │   └── ProfileService.java          # Profile aggregation
    │
    ├── dto/                              # Data Transfer Objects
    │   ├── request/
    │   │   ├── PurchaseRequest.java     # Purchase input (validated)
    │   │   └── RatingRequest.java       # Rating input (validated)
    │   └── response/
    │       ├── PurchaseResponse.java    # Purchase result
    │       ├── GameDto.java             # Game details for API
    │       ├── UserProfileDto.java      # User + purchased games
    │       └── ApiResponse.java         # Generic API response
    │
    └── kafka/
        └── producer/
            ├── GamePurchasedProducer.java    # Publishes to Kafka
            └── RatingSubmittedProducer.java  # Publishes to Kafka
```

## API Endpoints

### Public Endpoints (No Authentication)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/client/games` | List all games |
| GET | `/api/client/games/{id}` | Get game details |
| GET | `/api/client/games/search?title={query}` | Search games by title |
| GET | `/api/client/games/by-editor/{editorId}` | Get games by publisher |
| GET | `/api/client/ratings/game/{gameId}` | Get ratings for a game |

### Authenticated Endpoints (Require User Session)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/client/purchases` | Purchase a game |
| GET | `/api/client/purchases/history?userId={id}` | Get purchase history |
| POST | `/api/client/ratings` | Submit a game rating |
| GET | `/api/client/profile/{userId}` | Get user profile |

## Kafka Topics

### Published Events

#### 1. `game-purchased`
**Schema**: `common/avro-schemas/src/main/avro/game-purchased.avsc`

Emitted when a user purchases a game.

```json
{
  "purchaseId": "123",
  "userId": "1",
  "gameId": "42",
  "gameTitle": "The Legend of Zelda",
  "price": 59.99,
  "currency": "USD",
  "platform": "PC",
  "purchaseTimestamp": 1704067200000,
  "paymentMethod": "credit_card"
}
```

**Consumers**: 
- Analytics Service (track revenue)
- Notification Service (send confirmation email)
- Inventory Service (manage licenses)

#### 2. `game-rating-submitted`
**Schema**: `common/avro-schemas/src/main/avro/game-rating-submitted.avsc`

Emitted when a player rates a game.

```json
{
  "ratingId": "uuid-here",
  "userId": "1",
  "gameId": "42",
  "rating": 5,
  "review": "Amazing game!",
  "submittedTimestamp": 1704067200000
}
```

**Consumers**:
- Publisher Service (aggregate ratings)
- Recommendation Service (personalize suggestions)
- Moderation Service (review inappropriate content)

## Database Schema

### Tables

#### `purchases`
```sql
CREATE TABLE purchases (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    game_id INT NOT NULL,
    price DOUBLE,
    currency VARCHAR(3),
    platform VARCHAR(50),
    payment_method VARCHAR(50),
    purchase_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES app_user(id),
    FOREIGN KEY (game_id) REFERENCES game(id)
);
```

#### Other Tables
- `game` - Game metadata
- `app_user` - Platform users
- `editor` - Game publishers
- `patch` - Game updates

## Configuration

### application.properties

```properties
# Application
spring.application.name=platform-service
server.port=8083

# Database (H2 for development)
spring.datasource.url=jdbc:h2:file:./data/platform-db
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true

# Kafka
kafka.bootstrap.servers=localhost:9092
kafka.schema.registry.url=http://localhost:8081
kafka.topic.game-purchased=game-purchased
kafka.topic.game-rating-submitted=game-rating-submitted

# Logging
logging.level.com.vgp=DEBUG
logging.level.org.apache.kafka=INFO
```

## Running Locally

### Prerequisites
- Java 17 or higher
- Docker (for Kafka infrastructure)
- Gradle 9.x

### Steps

1. **Start Infrastructure**
   ```bash
   cd docker
   docker-compose up -d
   ```
   This starts:
   - Kafka (port 9092)
   - Zookeeper (port 2181)
   - Schema Registry (port 8081)
   - Kafka UI (port 8080)

2. **Build Avro Schemas** (When Kafka is enabled)
   ```bash
   cd common/avro-schemas
   ./gradlew clean build
   ```

3. **Build Platform Service**
   ```bash
   cd services/platform-service-java
   ./gradlew clean build
   ```

4. **Run the Service**
   ```bash
   ./gradlew bootRun
   ```

5. **Access the Service**
   - API: http://localhost:8083/api/client/games
   - H2 Console: http://localhost:8083/h2-console
   - Actuator: http://localhost:8083/actuator/health
   - Kafka UI: http://localhost:8080

## Development

### Adding a New Endpoint

1. Create DTO in `client/dto/request` or `client/dto/response`
2. Add business logic in appropriate service
3. Create controller method with proper validation
4. Add tests
5. Document in `TEST_CLIENT_API.md`

### Adding a New Kafka Event

1. Create Avro schema in `common/avro-schemas/src/main/avro/`
2. Build avro-schemas: `./gradlew build`
3. Create producer in `client/kafka/producer/`
4. Inject producer into service
5. Call producer in service method
6. Add topic config to `application.properties`

## Testing

See `TEST_CLIENT_API.md` for:
- cURL commands for all endpoints
- Postman collection
- Expected responses
- Kafka verification
- Error testing scenarios

### Quick Tests

```bash
# Browse games
curl http://localhost:8083/api/client/games

# Purchase a game
curl -X POST http://localhost:8083/api/client/purchases \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"gameId":1,"price":59.99,"platform":"PC","paymentMethod":"credit_card"}'

# Submit rating
curl -X POST http://localhost:8083/api/client/ratings \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"gameId":1,"rating":5,"review":"Great!"}'
```

## Troubleshooting

### Build Fails: "Could not resolve io.confluent"
**Cause**: Confluent repository is blocked in environment.  
**Solution**: Current implementation uses mock Kafka producers. When deploying:
1. Uncomment Kafka dependencies in `build.gradle.kts`
2. Replace mock producers with real `KafkaProducer`
3. Rebuild

### Database Already Exists Error
**Cause**: H2 file database persists between runs.  
**Solution**: 
```bash
rm -rf data/platform-db*
```

### Port 8083 Already in Use
**Solution**:
```bash
# Find process
lsof -i :8083
# Kill it
kill -9 <PID>
```

### Kafka Events Not Visible
**Cause**: Current implementation uses MOCK mode.  
**Check**: Look for log messages containing "MOCK:" in console.  
**Solution**: Enable actual Kafka as described above.

## Production Deployment

### Checklist
- [ ] Uncomment Kafka dependencies in `build.gradle.kts`
- [ ] Enable real Kafka producers (remove mock implementations)
- [ ] Configure production database (PostgreSQL, MySQL)
- [ ] Set up proper authentication (OAuth2, JWT)
- [ ] Configure HTTPS/TLS
- [ ] Set up monitoring (Prometheus, Grafana)
- [ ] Configure log aggregation (ELK, Splunk)
- [ ] Implement rate limiting
- [ ] Add caching (Redis)
- [ ] Set up CI/CD pipeline

### Environment Variables
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/platform
KAFKA_BOOTSTRAP_SERVERS=kafka-prod:9092
KAFKA_SCHEMA_REGISTRY_URL=http://schema-registry-prod:8081
SERVER_PORT=8083
```

## Security Considerations

- All purchase and rating endpoints should require authentication
- Implement CSRF protection for state-changing operations
- Validate all input with Spring Validation
- Use parameterized queries to prevent SQL injection
- Implement rate limiting on API endpoints
- Encrypt sensitive data in database
- Use HTTPS in production
- Implement proper CORS configuration

## Performance Optimization

- Database indexes on `user_id`, `game_id` in purchases table
- Connection pooling for database
- Kafka producer batching and compression
- API response caching for game lists
- Pagination for large result sets
- Asynchronous Kafka publishing

## Monitoring & Observability

### Metrics to Track
- Purchase success/failure rate
- API response times
- Kafka producer lag
- Database connection pool utilization
- Error rates by endpoint

### Health Checks
- `/actuator/health` - Overall service health
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics

## Contributing

1. Create feature branch from `main`
2. Follow existing code structure
3. Add tests for new features
4. Update documentation
5. Submit pull request

## License

[Your License Here]

## Support

For issues and questions:
- GitHub Issues: [repository-url]/issues
- Documentation: This README and `TEST_CLIENT_API.md`
- Contact: [team-email]
