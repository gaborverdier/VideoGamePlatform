# Client API Testing Guide

This document provides commands and procedures to test the Platform Service Client API.

## Prerequisites

Before running tests, ensure:
1. Infrastructure is running: `cd docker && docker-compose up -d`
2. Platform service is started: `cd services/platform-service-java && ./gradlew bootRun`
3. Service is accessible on port 8083

## Test Sequence

### 1. Health Check

```bash
# Check if service is running
curl http://localhost:8083/actuator/health
```

Expected Response:
```json
{
  "status": "UP"
}
```

### 2. Browse Games (Public Endpoint)

```bash
# Get all games
curl http://localhost:8083/api/client/games
```

Expected Response: Array of games (initially empty)
```json
[]
```

### 3. Get Game by ID (Public Endpoint)

```bash
# Get specific game
curl http://localhost:8083/api/client/games/1
```

Expected Response: Game details or 404 if not found

### 4. Search Games by Title (Public Endpoint)

```bash
# Search for games containing "zelda"
curl "http://localhost:8083/api/client/games/search?title=zelda"
```

Expected Response: Array of matching games

### 5. Get Games by Editor (Public Endpoint)

```bash
# Get games by editor ID
curl http://localhost:8083/api/client/games/by-editor/1
```

Expected Response: Array of games from that editor

### 6. Purchase a Game (Authenticated Endpoint)

```bash
# Purchase a game
curl -X POST http://localhost:8083/api/client/purchases \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "gameId": 1,
    "price": 59.99,
    "platform": "PC",
    "paymentMethod": "credit_card"
  }'
```

Expected Response:
```json
{
  "purchaseId": 1,
  "status": "SUCCESS",
  "message": "Purchase completed successfully"
}
```

### 7. Get Purchase History (Authenticated Endpoint)

```bash
# Get purchase history for a user
curl "http://localhost:8083/api/client/purchases/history?userId=1"
```

Expected Response: Array of purchases

### 8. Submit a Rating (Authenticated Endpoint)

```bash
# Submit a game rating
curl -X POST http://localhost:8083/api/client/ratings \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "gameId": 1,
    "rating": 5,
    "review": "Amazing game! Best RPG ever."
  }'
```

Expected Response:
```json
{
  "success": true,
  "message": "Rating submitted successfully",
  "data": "generated-uuid-here"
}
```

### 9. Get Ratings for a Game (Public Endpoint)

```bash
# Get ratings for a specific game
curl http://localhost:8083/api/client/ratings/game/1
```

Expected Response: Placeholder response (ratings storage not yet implemented)

### 10. Get User Profile (Authenticated Endpoint)

```bash
# Get user profile with purchased games
curl http://localhost:8083/api/client/profile/1
```

Expected Response:
```json
{
  "userId": 1,
  "username": "john_doe",
  "purchasedGames": [
    {
      "id": 1,
      "name": "The Legend of Zelda",
      "editorId": 1,
      "editorName": "Nintendo"
    }
  ]
}
```

## Verification

### Check H2 Database Console

1. Open: http://localhost:8083/h2-console
2. Use connection details:
   - JDBC URL: `jdbc:h2:file:./data/platform-db`
   - Username: `sa`
   - Password: (empty)
3. Query: `SELECT * FROM PURCHASES;`

### Check Kafka Events (When Kafka is Enabled)

```bash
# Monitor game-purchased topic
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic game-purchased \
  --from-beginning

# Monitor game-rating-submitted topic
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic game-rating-submitted \
  --from-beginning
```

### Check Kafka UI

Open http://localhost:8080 to view:
- Topics: `game-purchased`, `game-rating-submitted`
- Messages in each topic
- Consumer groups

## Error Testing

### Invalid Purchase Request

```bash
# Missing required fields
curl -X POST http://localhost:8083/api/client/purchases \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "gameId": 999
  }'
```

Expected Response: 400 Bad Request with validation errors

### Invalid Rating

```bash
# Rating out of range
curl -X POST http://localhost:8083/api/client/ratings \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "gameId": 1,
    "rating": 10
  }'
```

Expected Response: 400 Bad Request with error message

### Non-existent Resource

```bash
# Get non-existent game
curl http://localhost:8083/api/client/games/99999
```

Expected Response: 404 Not Found

## Postman Collection

Import this JSON into Postman for easy testing:

```json
{
  "info": {
    "name": "Platform Service - Client API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Games",
      "item": [
        {
          "name": "Get All Games",
          "request": {
            "method": "GET",
            "url": "http://localhost:8083/api/client/games"
          }
        },
        {
          "name": "Get Game by ID",
          "request": {
            "method": "GET",
            "url": "http://localhost:8083/api/client/games/1"
          }
        },
        {
          "name": "Search Games",
          "request": {
            "method": "GET",
            "url": {
              "raw": "http://localhost:8083/api/client/games/search?title=zelda",
              "query": [
                {
                  "key": "title",
                  "value": "zelda"
                }
              ]
            }
          }
        },
        {
          "name": "Get Games by Editor",
          "request": {
            "method": "GET",
            "url": "http://localhost:8083/api/client/games/by-editor/1"
          }
        }
      ]
    },
    {
      "name": "Purchases",
      "item": [
        {
          "name": "Purchase Game",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"userId\": 1,\n  \"gameId\": 1,\n  \"price\": 59.99,\n  \"platform\": \"PC\",\n  \"paymentMethod\": \"credit_card\"\n}"
            },
            "url": "http://localhost:8083/api/client/purchases"
          }
        },
        {
          "name": "Get Purchase History",
          "request": {
            "method": "GET",
            "url": {
              "raw": "http://localhost:8083/api/client/purchases/history?userId=1",
              "query": [
                {
                  "key": "userId",
                  "value": "1"
                }
              ]
            }
          }
        }
      ]
    },
    {
      "name": "Ratings",
      "item": [
        {
          "name": "Submit Rating",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"userId\": 1,\n  \"gameId\": 1,\n  \"rating\": 5,\n  \"review\": \"Amazing game!\"\n}"
            },
            "url": "http://localhost:8083/api/client/ratings"
          }
        },
        {
          "name": "Get Game Ratings",
          "request": {
            "method": "GET",
            "url": "http://localhost:8083/api/client/ratings/game/1"
          }
        }
      ]
    },
    {
      "name": "Profile",
      "item": [
        {
          "name": "Get User Profile",
          "request": {
            "method": "GET",
            "url": "http://localhost:8083/api/client/profile/1"
          }
        }
      ]
    }
  ]
}
```

## Performance Testing

### Load Test with Apache Bench

```bash
# Test browsing games endpoint
ab -n 1000 -c 10 http://localhost:8083/api/client/games

# Test purchase endpoint
ab -n 100 -c 5 -p purchase.json -T application/json http://localhost:8083/api/client/purchases
```

Where `purchase.json` contains:
```json
{
  "userId": 1,
  "gameId": 1,
  "price": 59.99,
  "platform": "PC",
  "paymentMethod": "credit_card"
}
```

## Troubleshooting

### Service Won't Start
- Check port 8083 is not in use: `lsof -i :8083`
- Check logs: Look for errors in console output
- Verify Java version: `java -version` (should be 17+)

### Database Issues
- Delete existing database: `rm -rf data/platform-db*`
- Check H2 console for connection issues
- Verify DDL auto mode is set to `update` in application.properties

### Kafka Events Not Appearing (When Kafka is Enabled)
- Verify Kafka is running: `docker ps | grep kafka`
- Check Schema Registry: `curl http://localhost:8081/subjects`
- Check producer logs for errors
- Verify topic configuration in application.properties

## Notes

- **IMPORTANT**: Current implementation uses MOCK Kafka producers due to network restrictions
- When deploying to production:
  1. Uncomment Kafka dependencies in `build.gradle.kts`
  2. Replace mock producers with real KafkaProducer instances
  3. Rebuild avro-schemas project
  4. Update producer classes to use actual Kafka clients
- All endpoints use standard REST conventions
- Validation is applied on all POST requests
- Database transactions are atomic for purchases
