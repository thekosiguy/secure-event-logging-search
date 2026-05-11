# Secure Event Logging & Search Platform

A secure event logging and search platform built with Java and Spring Boot. Enables organizations to log, store, retrieve, and search events with security, validation, and search capabilities.

## Tech Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17+ (tested on JDK 25)
- **Database**: PostgreSQL 17
- **ORM**: Hibernate (JPA) with hypersistence-utils for jsonb support
- **Security**: Spring Security + JWT (HMAC-SHA, stateless)
- **Build Tool**: Maven 3.9.x
- **Testing**: JUnit 5 + Mockito + jqwik (property-based testing)
- **Containerisation**: Docker + Docker Compose
- **Cloud**: AWS (ECS Fargate, ECR, RDS PostgreSQL, CloudWatch)
- **IDE**: Kiro

## Project Structure

```
├── src/
│   └── main/
│       ├── java/com/secureeventloggingandsearch/
│       │   ├── SecureEventLoggingApplication.java
│       │   ├── controller/
│       │   │   ├── AuthController.java         # Register + login endpoints
│       │   │   ├── EventController.java        # Event CRUD + filtering endpoints
│       │   │   └── HealthController.java       # Health check endpoint
│       │   ├── service/
│       │   │   ├── AuthService.java            # Registration, login, token issuance
│       │   │   └── EventService.java           # Business logic + SLF4J logging
│       │   ├── repository/
│       │   │   ├── EventRepository.java        # JPA queries with filter support
│       │   │   └── UserRepository.java         # User lookup by username
│       │   ├── model/
│       │   │   ├── Event.java                  # JPA entity with DB indexes
│       │   │   ├── Role.java                   # Enum: ROLE_ADMIN, ROLE_USER
│       │   │   └── User.java                   # JPA entity for users table
│       │   ├── dto/
│       │   │   ├── AuthResponse.java           # Login response (token)
│       │   │   ├── EventRequest.java           # POST request body
│       │   │   ├── EventResponse.java          # Single event response
│       │   │   ├── ErrorResponse.java          # Structured error response
│       │   │   ├── LoginRequest.java           # Login request body
│       │   │   ├── PagedResponse.java          # Paginated response wrapper
│       │   │   ├── RegisterRequest.java        # Registration request body
│       │   │   └── RegisterResponse.java       # Registration response
│       │   ├── exception/
│       │   │   ├── GlobalExceptionHandler.java # Handles all exceptions globally
│       │   │   └── EventNotFoundException.java # 404 for missing events
│       │   └── security/
│       │       ├── AccessDeniedHandlerImpl.java # 403 JSON response handler
│       │       ├── AuthEntryPoint.java          # 401 JSON response handler
│       │       ├── JwtAuthenticationFilter.java # Extracts + validates JWT per request
│       │       ├── JwtProvider.java             # Token generation + validation
│       │       └── SecurityConfig.java          # Filter chain, RBAC rules, BCrypt
│       └── resources/
│           └── application.properties
├── Dockerfile                                  # Multi-stage build
├── docker-compose.yml                          # App + DB orchestration
├── ecs-task-definition.json                    # AWS ECS Fargate task definition
├── ecs-trust-policy.json                       # IAM trust policy for ECS
├── ecs-execution-policy.json                   # Scoped IAM execution policy
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
| `JWT_SECRET` | JWT signing key (min 32 bytes). Defaults to dev key if unset |
| `JWT_EXPIRATION_MS` | Token expiration in ms (default: 86400000 = 24h) |

### Database Setup
```sql
CREATE DATABASE eventdb;
```

### Run
```bash
mvn spring-boot:run
```

## AWS Fargate Deployment

The app is deployed on AWS ECS Fargate with RDS PostgreSQL.

### Architecture
- **ECR**: Docker image registry
- **ECS Fargate**: Serverless container orchestration
- **RDS PostgreSQL**: Managed database (db.t3.micro)
- **CloudWatch**: Centralised logging
- **IAM**: Least-privilege execution and task roles

### Security
| Component | Configuration |
|-----------|--------------|
| ECS Security Group | Inbound TCP 8080 from `0.0.0.0/0` |
| RDS Security Group | Inbound TCP 5432 from VPC CIDR only |
| Task Execution Role | Scoped to ECR pull + CloudWatch `/ecs/secure-event-logging` only |
| Task Role | Same as execution role (expandable for S3, etc.) |
| Network | `awsvpc` mode — each task gets its own ENI within the VPC |

### Deploy a New Version
```bash
docker build -t secure-event-logging .
docker tag secure-event-logging:latest <account-id>.dkr.ecr.eu-west-2.amazonaws.com/secure-event-logging:latest
aws ecr get-login-password --region eu-west-2 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.eu-west-2.amazonaws.com
docker push <account-id>.dkr.ecr.eu-west-2.amazonaws.com/secure-event-logging:latest
aws ecs update-service --cluster secure-event-logging-cluster --service secure-event-logging-service --force-new-deployment --region eu-west-2
```

## API Endpoints

### Public (No Authentication Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/health` | Health check |
| POST | `/api/v1/auth/register` | Register a new user |
| POST | `/api/v1/auth/login` | Authenticate and receive JWT |

### Protected (JWT Required)

| Method | Endpoint | Roles | Description |
|--------|----------|-------|-------------|
| POST | `/api/v1/events` | ADMIN | Create a new event |
| GET | `/api/v1/events` | ADMIN, USER | Retrieve events (paginated, filtered, sorted) |
| GET | `/api/v1/events/{id}` | ADMIN, USER | Retrieve event by ID |

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

**POST /api/v1/auth/register**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"securepass123\",\"role\":\"ROLE_ADMIN\"}"
```

Response `201 Created`:
```json
{
  "username": "admin",
  "role": "ROLE_ADMIN"
}
```

**POST /api/v1/auth/login**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"securepass123\"}"
```

Response `200 OK`:
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "type": "Bearer"
}
```

**POST /api/v1/events** (authenticated)
```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
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

## Security

### Authentication & Authorization

The platform follows **Zero Trust** principles — every request to a protected endpoint must carry a valid JWT token.

| Aspect | Implementation |
|--------|---------------|
| Authentication | JWT Bearer tokens via `Authorization` header |
| Password storage | BCrypt hashing |
| Token signing | HMAC-SHA with configurable secret |
| Token expiration | Configurable (default 24 hours) |
| Session management | Stateless (no server-side sessions) |
| CSRF | Disabled (stateless REST API) |
| Role-based access | ADMIN can write events; ADMIN + USER can read |
| Error responses | Structured JSON for 401/403 with timestamp |

### Roles

| Role | Permissions |
|------|-------------|
| `ROLE_ADMIN` | Read + write events |
| `ROLE_USER` | Read events only |

## Testing

58 tests across unit tests and property-based tests:

```bash
# Run all tests
mvnw.cmd test

# Run only property tests
mvnw.cmd test -Dtest="*PropertyTest"
```

| Test Class | Type | Count | Coverage |
|------------|------|-------|----------|
| AuthControllerTest | Unit | 9 | Registration, login, validation |
| EventControllerTest | Unit | 8 | Event CRUD with @WithMockUser |
| SecurityConfigTest | Integration | 9 | RBAC, public endpoints, 401/403 |
| JwtProviderTest | Unit | 10 | Token lifecycle, expiry, tampering |
| JwtProviderPropertyTest | Property | 4 | Token round-trip, invalid rejection |
| JwtAuthenticationFilterPropertyTest | Property | 2 | SecurityContext population |
| PasswordHashingPropertyTest | Property | 3 | BCrypt round-trip |
| AuthServicePropertyTest | Property | 4 | Registration/login correctness |
| EventServiceTest | Unit | 9 | Service layer logic |

## Sprint Progress

| Sprint | Goal | Status |
|--------|------|--------|
| 1 | Project Foundation | ✅ Done |
| 2 | Event CRUD API, validation, error handling, logging | ✅ Done |
| 3 | Pagination, filtering, sorting, query efficiency | ✅ Done |
| 4 | Dockerisation | ✅ Done |
| 5 | AWS Fargate Deployment | ✅ Done |
| 6 | Security (Auth/Authorization) | ✅ Done |
| 7 | Audit Logging | 🔄 Upcoming |
| 8 | Performance Optimization | 🔄 Upcoming |
| 9 | Integration Testing | 🔄 Upcoming |
| 10 | API Documentation (Swagger) | 🔄 Upcoming |

---

**Version**: 0.6.0 | **Last Updated**: May 11, 2026