# Secure Event Logging & Search Platform

A comprehensive, secure event logging and search platform built with modern Java technologies. This platform enables organizations to log, store, retrieve, and search events with enhanced security, validation, and search capabilities.

## Project Overview

This project is being developed in **12 sprints**, with each sprint building upon the previous foundation to deliver a production-ready event logging system.

### Current Status: Sprint 1 - Project Foundation ✅

**Goal**: Clean and professional project setup with foundational architecture.

## Tech Stack

- **Backend Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Database**: PostgreSQL
- **ORM**: Hibernate (JPA)
- **Build Tool**: Maven
- **Version Control**: Git

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/secureeventloggingandsearch/
│   │       ├── SecureEventLoggingApplication.java  # Main Spring Boot entry point
│   │       ├── controller/                          # REST API controllers
│   │       │   └── HealthController.java
│   │       ├── service/                             # Business logic (placeholder)
│   │       ├── repository/                          # Data access layer (placeholder)
│   │       ├── model/                               # Entity models (placeholder)
│   │       └── dto/                                 # Data Transfer Objects (placeholder)
│   └── resources/
│       └── application.properties                   # Configuration file
└── pom.xml                                          # Maven dependencies
```

## Features - Sprint 1

### Implemented
- ✅ Clean Spring Boot project setup with Maven
- ✅ PostgreSQL database configuration (via `application.properties`)
- ✅ Basic REST API endpoint: `GET /health`
- ✅ Professional package structure (controller, service, repository, model, dto)
- ✅ JPA/Hibernate integration
- ✅ Proper HTTP responses with Spring's `ResponseEntity`

### Placeholders for Future Sprints
- 🔄 Event entity model (id, type, timestamp, payload)
- 🔄 Event repository and CRUD operations
- 🔄 Event service layer
- 🔄 REST API endpoints: `POST /events`, `GET /events`, `GET /events/{id}`
- 🔄 Input validation and error handling
- 🔄 Search functionality
- 🔄 Security features

## Setup Instructions

### Prerequisites
- **JDK 17+** (tested with Eclipse Temurin JDK 25)
  - Download: https://adoptium.net/temurin/releases/
  - Set `JAVA_HOME` environment variable
- **Maven** (optional if using IntelliJ IDEA Maven plugin)
  - Download: https://maven.apache.org/download.cgi
- **PostgreSQL** (tested with PostgreSQL 15+)
  - Download: https://www.postgresql.org/download/
  - Ensure service is running

### Step 1: Clone the Repository
```bash
git clone https://github.com/yourusername/secure-event-logging-search.git
cd secure-event-logging-search
```

### Step 2: Configure Database
1. Ensure PostgreSQL is running
2. Create a database:
   ```sql
   CREATE DATABASE eventdb;
   ```
3. Update credentials in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/eventdb
   spring.datasource.username=postgres
   spring.datasource.password=your_password
   ```

### Step 3: Build the Project
Using Maven:
```bash
mvn clean install
```

Or using IntelliJ IDEA Maven plugin:
1. Open the Maven tool window
2. Right-click Lifecycle > `clean`
3. Right-click Lifecycle > `install`

### Step 4: Run the Application
Using Maven:
```bash
mvn spring-boot:run
```

Or from IntelliJ IDEA:
1. Open `SecureEventLoggingApplication.java`
2. Right-click > `Run 'SecureEventLoggingApplication'`

The application will start on `http://localhost:8080`

## API Endpoints

### Sprint 1 - Current

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| GET | `/health` | Health check endpoint | ✅ Active |

**Example Request**:
```bash
curl http://localhost:8080/health
```

**Example Response**:
```
OK
```

### Future Endpoints (Planned)

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | `/events` | Create a new event | 🔄 Sprint 2-3 |
| GET | `/events` | Retrieve all events (with pagination/filtering) | 🔄 Sprint 3-4 |
| GET | `/events/{id}` | Retrieve a specific event by ID | 🔄 Sprint 2-3 |
| PUT | `/events/{id}` | Update an event | 🔄 Sprint 4 |
| DELETE | `/events/{id}` | Delete an event | 🔄 Sprint 4 |
| GET | `/events/search` | Search events with advanced filters | 🔄 Sprint 5-6 |

## Development Workflow

### Creating a New Feature
1. Create a branch for your feature: `git checkout -b feature/feature-name`
2. Make your changes
3. Commit with a meaningful message: `git commit -m "Feature: description"`
4. Push to GitHub: `git push origin feature/feature-name`
5. Create a Pull Request

### Building and Testing
- Clean build: `mvn clean install`
- Run tests: `mvn test` (tests will be added in future sprints)
- Check code quality: `mvn checkstyle:check` (to be configured)

## Configuration

### application.properties
Key configurations:
- `spring.datasource.url`: PostgreSQL connection URL
- `spring.datasource.username`: PostgreSQL username
- `spring.datasource.password`: PostgreSQL password
- `spring.jpa.hibernate.ddl-auto`: Auto-generate/update database schema (`update` for development)
- `server.port`: Application port (default: 8080)

## Future Sprints (Overview)

1. **Sprint 1**: Project Foundation ✅
2. **Sprint 2**: Event Entity & Basic CRUD
3. **Sprint 3**: REST API Endpoints (POST, GET)
4. **Sprint 4**: Input Validation & Error Handling
5. **Sprint 5**: Event Search Functionality
6. **Sprint 6**: Advanced Filtering & Pagination
7. **Sprint 7**: Security Features (Authentication/Authorization)
8. **Sprint 8**: Audit Logging
9. **Sprint 9**: Performance Optimization
10. **Sprint 10**: Integration Testing
11. **Sprint 11**: Documentation & API Documentation (Swagger)
12. **Sprint 12**: Deployment & Production Readiness

## Troubleshooting

### Issue: "PostgreSQL connection refused"
- **Solution**: Ensure PostgreSQL service is running and credentials are correct in `application.properties`

### Issue: "JDK version not supported"
- **Solution**: Ensure you have JDK 17+ installed and `JAVA_HOME` is set correctly

### Issue: "Maven not recognized"
- **Solution**: Add Maven bin directory to your system PATH or use IntelliJ IDEA's Maven plugin

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes and commit
4. Push to the branch
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact & Support

For questions or issues, please open an issue on GitHub.

---

**Last Updated**: April 22, 2026  
**Sprint**: 1 (Project Foundation)  
**Version**: 0.1.0

