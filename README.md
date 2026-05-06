# Secure Event Logging & Search Platform

A secure event logging and search platform built with Java and Spring Boot. Enables organizations to log, store, retrieve, and search events with security, validation, and search capabilities.

## Tech Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17+ (tested on JDK 25)
- **Database**: PostgreSQL 17
- **ORM**: Hibernate (JPA) with hypersistence-utils for jsonb support
- **Build Tool**: Maven 3.9.x
- **Containerisation**: Docker + Docker Compose
- **IDE**: Kiro

## Project Structure

```
├── src/
│   └── main/
│       ├── java/com/secureeventloggingandsearch/
│       │   ├── SecureEventLoggingApplication.java
│       │   ├── controller/
│       │   │   ├── EventController.java        # Event CRUD + filtering endpoints
│       │   │   └── HealthController.java       # Health check endpoint
│       │   ├── service/
│       │   │   └── EventService.java           # Business logic + SLF4J logging
│       │   ├── repository/
│       │   │   └── EventRepository.java        # JPA queries with filter support
│       │   ├── model/
│       │   │   └── Event.java                  # JPA entity with DB indexes
│       │   ├── dto/
│       │   │   ├── EventRequest.java           # POST request body
│       │   │   ├── EventResponse.java          # Single event response
│       │   │   ├── PagedResponse.java          # Paginated response wrapper
│       │   │   └── ErrorResponse.java          # Structured error response
│       │   └── exception/
│       │       ├── GlobalExceptionHandler.java # Handles all exceptions globally
│       │       └── EventNotFoundException.java # 404 for missing events
│       └── resources/
│           └── application.properties
├── Dockerfile                                  # Multi-stage build
├── docker-compose.yml                          # App + DB orchestration
├── .env.example                                # Environment variable template
├── mvnw / mvnw.cmd                             # Maven wrapper
└── pom.xml
```

## Running with Docker (Recommended)

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)

### Steps
```bash
# 1. Copy the environment template
cp .env.example .env

# 2. Build and start all containers
docker-compose up --build
```

App starts on `http://localhost:8080`. No local Java, Maven, or PostgreSQL installation required.

```bash
# Stop containers
docker-compose down

# Stop and remove volumes (fresh DB)
docker-compose down -v
```

## Running Locally (Without Docker)

### Prerequisites
- JDK 17+ — [Eclipse Temurin](https://adoptium.net/temurin/releases/)
- Maven 3.9.x — [Download](https://maven.apache.org/download.cgi)
- PostgreSQL 17+ — [Download](https://www.postgresql.org/download/)

### Environment Variables
Set these via `sysdm.cpl` → Advanced → Environment Variables:

| Variable | Description |
|----------|-------------|
| `DB_USERNAME` | PostgreSQL username (e.g. `postgres`) |
| `DB_PASSWORD` | PostgreSQL password |

### Database Setup
```sql
CREATE DATABASE eventdb;
```

### Run
```bash
mvn spring-boot:run
```

## API Endpoints

### Active

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/health` | Health check |
| POST | `/api/v1/events` | Create a new event |
| GET | `/api/v1/events` | Retrieve events (paginated, filtered, sorted) |
| GET | `/api/v1/events/{id}` | Retrieve event by ID |

### Query Parameters — GET /api/v1/events

| Parameter | Type | Description |
|-----------|------|-------------|
| `type` | String | Filter by event type (case-insensitive) |
| `from` | ISO-8601 | Filter events from this timestamp |
| `to` | ISO-8601 | Filter events up to this timestamp |
| `page` | int | Page number (default: 0) |
| `size` | int | Page size (default: 20) |
| `sort` | String | `timestamp` or `type` only (e.g. `sort=timestamp,desc`) |

**Examples:**
```bash
GET /api/v1/events
GET /api/v1/events?type=LOGIN
GET /api/v1/events?from=2026-05-01T00:00:00Z&to=2026-05-04T23:59:59Z
GET /api/v1/events?type=LOGIN&page=0&size=10&sort=timestamp,desc
```

### Request & Response Examples

**POST /api/v1/events**
```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d "{\"type\":\"LOGIN\",\"payload\":{\"user\":\"john\",\"ip\":\"192.168.1.1\"}}"
```

Response `201 Created`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "type": "LOGIN",
  "timestamp": "2026-05-04T10:28:00Z",
  "payload": { "user": "john", "ip": "192.168.1.1" }
}
```

**GET /api/v1/events**

Response `200 OK`:
```json
{
  "content": [{ "id": "...", "type": "LOGIN", "timestamp": "...", "payload": {} }],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3,
  "last": false,
  "sort": ["timestamp,desc"]
}
```

**Error Response:**
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more fields are invalid",
  "timestamp": "2026-05-04T10:28:00Z",
  "details": ["type: must not be blank"]
}
```

## Event Entity

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Auto-generated unique identifier |
| `type` | String | Event type (e.g. LOGIN, LOGOUT) — stored uppercase |
| `timestamp` | Instant | Event time — defaults to now if not provided |
| `payload` | jsonb | Structured JSON payload — required |

## DB Indexes

| Index | Columns | Purpose |
|-------|---------|---------|
| `idx_events_type` | `type` | Fast filter by type |
| `idx_events_timestamp` | `timestamp` | Fast filter and sort by timestamp |
| `idx_events_type_timestamp` | `type, timestamp` | Optimised combined filter queries |

## Sprint Progress

| Sprint | Goal | Status |
|--------|------|--------|
| 1 | Project Foundation | ✅ Done |
| 2 | Event CRUD API, validation, error handling, logging | ✅ Done |
| 3 | Pagination, filtering, sorting, query efficiency | ✅ Done |
| 4 | Dockerisation | ✅ Done |
| 5 | Security (Auth/Authorization) | 🔄 Upcoming |
| 6 | Audit Logging | 🔄 Upcoming |
| 7 | Performance Optimization | 🔄 Upcoming |
| 8 | Integration Testing | 🔄 Upcoming |
| 9 | API Documentation (Swagger) | 🔄 Upcoming |
| 10 | Deployment & Production Readiness | 🔄 Upcoming |

---

**Version**: 0.4.0 | **Last Updated**: May 5, 2026
