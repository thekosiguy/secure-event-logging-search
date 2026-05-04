# Secure Event Logging & Search Platform

A secure event logging and search platform built with Java and Spring Boot. Enables organizations to log, store, retrieve, and search events with security, validation, and search capabilities.

## Tech Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17+ (tested on JDK 25)
- **Database**: PostgreSQL 18
- **ORM**: Hibernate (JPA) with hypersistence-utils for jsonb support
- **Build Tool**: Maven 3.9.x
- **IDE**: Kiro

## Project Structure

```
src/
└── main/
    ├── java/com/secureeventloggingandsearch/
    │   ├── SecureEventLoggingApplication.java  # Entry point
    │   ├── controller/                          # REST controllers
    │   ├── service/                             # Business logic + SLF4J logging
    │   ├── repository/                          # Data access layer
    │   ├── model/                               # JPA entities
    │   ├── dto/                                 # Request/response/error objects
    │   └── exception/                           # Global exception handler + custom exceptions
    └── resources/
        └── application.properties              # App configuration
```

## Setup

### Prerequisites
- JDK 17+ — [Eclipse Temurin](https://adoptium.net/temurin/releases/)
- Maven 3.9.x — [Download](https://maven.apache.org/download.cgi)
- PostgreSQL 18 — [Download](https://www.postgresql.org/download/)

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
App starts on `http://localhost:8080`

## API Endpoints

### Active

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| GET | `/api/v1/health` | Health check | ✅ Sprint 1 |
| POST | `/api/v1/events` | Create a new event | ✅ Sprint 2 |
| GET | `/api/v1/events` | Retrieve all events (paginated) | ✅ Sprint 2 |
| GET | `/api/v1/events/{id}` | Retrieve event by ID | ✅ Sprint 2 |

### Planned

| Method | Endpoint | Description | Sprint |
|--------|----------|-------------|--------|
| GET | `/api/v1/events/search` | Search events by filters | 5-6 |

### Examples

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

**GET /api/v1/events?page=0&size=10**
```bash
curl http://localhost:8080/api/v1/events?page=0&size=10
```

**GET /api/v1/events/{id}**
```bash
curl http://localhost:8080/api/v1/events/550e8400-e29b-41d4-a716-446655440000
```

### Error Responses
All errors return a structured JSON body instead of Spring's default error page:
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
| `type` | String | Event type (e.g. LOGIN, LOGOUT) — required |
| `timestamp` | Instant | Event time — defaults to now if not provided |
| `payload` | jsonb | Structured JSON payload — required |

## Validation Rules

| Field | Rule |
|-------|------|
| `type` | `@NotBlank` — must not be null or empty |
| `payload` | `@NotNull` — must be valid JSON object |

## Sprint Progress

| Sprint | Goal | Status |
|--------|------|--------|
| 1 | Project Foundation | ✅ Done |
| 2 | Event CRUD API, validation, error handling, logging | ✅ Done |
| 3 | Advanced search & filtering | 🔄 Upcoming |
| 4 | Security (Auth/Authorization) | 🔄 Upcoming |
| 5 | Audit Logging | 🔄 Upcoming |
| 6 | Advanced Filtering & Pagination | 🔄 Upcoming |
| 7 | Performance Optimization | 🔄 Upcoming |
| 8 | Integration Testing | 🔄 Upcoming |
| 9 | API Documentation (Swagger) | 🔄 Upcoming |
| 10 | Deployment & Production Readiness | 🔄 Upcoming |

---

**Version**: 0.2.0 | **Last Updated**: May 4, 2026
