# Secure Event Logging & Search Platform

A secure event logging and search platform built with Java and Spring Boot. Enables organizations to log, store, retrieve, and search events with security, validation, and search capabilities.

## Tech Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17+ (tested on JDK 25)
- **Database**: PostgreSQL 18
- **ORM**: Hibernate (JPA)
- **Build Tool**: Maven 3.9.x
- **IDE**: Kiro

## Project Structure

```
src/
└── main/
    ├── java/com/secureeventloggingandsearch/
    │   ├── SecureEventLoggingApplication.java  # Entry point
    │   ├── controller/                          # REST controllers
    │   ├── service/                             # Business logic
    │   ├── repository/                          # Data access layer
    │   ├── model/                               # JPA entities
    │   └── dto/                                 # Request/response objects
    └── resources/
        └── application.properties              # App configuration
```

## Setup

### Prerequisites
- JDK 17+ — [Eclipse Temurin](https://adoptium.net/temurin/releases/)
- Maven 3.9.x — [Download](https://maven.apache.org/download.cgi)
- PostgreSQL 18 — [Download](https://www.postgresql.org/download/)

### Environment Variables
Set these in your system environment variables (`sysdm.cpl`):

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_USERNAME` | PostgreSQL username | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | `postgres` |

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
| GET | `/api/v1/events` | Retrieve all events | ✅ Sprint 2 |

### Planned

| Method | Endpoint | Description | Sprint |
|--------|----------|-------------|--------|
| GET | `/api/v1/events/{id}` | Get event by ID | 3 |
| GET | `/api/v1/events/search` | Search events | 5-6 |

### Example

**POST /api/v1/events**
```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d "{\"type\":\"LOGIN\",\"payload\":\"{\\\"user\\\":\\\"john\\\"}\"}"
```

Response `201 Created`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "type": "LOGIN",
  "timestamp": "2026-05-04T10:28:00Z",
  "payload": "{\"user\":\"john\"}"
}
```

**GET /api/v1/events**
```bash
curl http://localhost:8080/api/v1/events
```

## Event Entity

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Auto-generated unique identifier |
| `type` | String | Event type (e.g. LOGIN, LOGOUT) |
| `timestamp` | Instant | Event time (defaults to now if not provided) |
| `payload` | String (TEXT) | JSON payload |

## Sprint Progress

| Sprint | Goal | Status |
|--------|------|--------|
| 1 | Project Foundation | ✅ Done |
| 2 | Event Entity & Basic CRUD API | ✅ Done |
| 3 | GET /events/{id} & Error Handling | 🔄 Upcoming |
| 4 | Input Validation | 🔄 Upcoming |
| 5 | Event Search | 🔄 Upcoming |
| 6 | Advanced Filtering & Pagination | 🔄 Upcoming |
| 7 | Security (Auth/Authorization) | 🔄 Upcoming |
| 8 | Audit Logging | 🔄 Upcoming |
| 9 | Performance Optimization | 🔄 Upcoming |
| 10 | Integration Testing | 🔄 Upcoming |
| 11 | API Documentation (Swagger) | 🔄 Upcoming |
| 12 | Deployment & Production Readiness | 🔄 Upcoming |

---

**Version**: 0.2.0 | **Last Updated**: May 4, 2026
