# Custom API Gateway

A Spring Boot-based API Gateway that provides JWT authentication, role-based authorization, dynamic route configuration, backend request forwarding, Redis-based rate limiting, and target URL validation.

## Features

- **JWT Authentication** — Authenticates requests using JSON Web Tokens.
- **Role-Based Authorization** — Restricts route creation to administrators.
- **Dynamic Routing** — Stores route configurations in PostgreSQL.
- **HTTP Request Forwarding** — Supports GET, POST, PUT, DELETE, and PATCH requests.
- **Redis Rate Limiting** — Limits each authenticated user to 5 requests per minute.
- **Correlation IDs** — Adds identifiers to requests for tracing and debugging.
- **Centralized Exception Handling** — Handles validation errors, missing routes, and backend failures.
- **Target URL Validation** — Uses an allowlist and DNS/IP validation to mitigate Server-Side Request Forgery (SSRF).
- **HTTP Client Timeouts** — Configures connection-request and response timeouts.
- **Actuator Monitoring** — Exposes health and metrics endpoints.
- **Automated Testing** — Uses JUnit and Mockito to test application components.

## Technology Stack

| Category | Technologies |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot, Spring Security |
| Persistence | Spring Data JPA, PostgreSQL |
| Rate Limiting | Redis |
| HTTP Client | Apache HttpClient 5 |
| Authentication | JWT (JJWT) |
| Build Tool | Maven |
| Containerization | Docker, Docker Compose |
| Testing | JUnit, Mockito |

## Architecture

The gateway listens on port `8080` and forwards requests to configured backend services, such as the backend application running on port `8081`.

```text
                        Client
                          |
                          v
                 Custom API Gateway
                       Port 8080
                          |
             +------------+------------+
             |            |            |
             v            v            v
        JWT Security   Route Lookup   Rate Limiting
             |        (PostgreSQL)      (Redis)
             |            |
             +------------+
                          |
                  Target URL Validation
                          |
                  HTTP Request Forwarding
                          |
                          v
                  Backend Service
                     Port 8081
```

Supporting services:

- **PostgreSQL:** Persists application users and route configurations.
- **Redis:** Maintains rate-limiting state.
- **Backend Service:** Processes requests forwarded by the gateway.

## Prerequisites

- Java 17
- Docker Desktop and Docker Compose
- Maven Wrapper, included in the gateway project

For local execution without Docker, PostgreSQL and Redis must also be installed and running.

## Configuration

Configure the required environment variables before starting the applications.

| Variable | Purpose |
|---|---|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC connection URL |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL username |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password |
| `SPRING_DATA_REDIS_HOST` | Redis hostname |
| `SPRING_DATA_REDIS_PORT` | Redis port |
| `APP_ADMIN_PASSWORD` | Administrator password configured during initialization |
| `GATEWAY_ALLOWED_TARGETS` | List of permitted backend target URLs |

Check `src/main/resources/application.properties` and the parent Compose configuration for the exact settings and required environment variables.

For Docker Compose, configure the required values in the parent project's `.env` file. The Compose setup maps `POSTGRES_PASSWORD` and `ADMIN_PASSWORD` to the relevant application configuration.

The target allowlist must use the expected Spring configuration format. Allowed targets are matched by scheme, hostname, and effective port. Explicitly allowlisted targets bypass the validator's additional DNS/IP address checks, so only trusted destinations should be configured.

**Security:** Never commit `.env` files, passwords, JWTs, or other secrets to version control. The application also initializes a development `testuser` account with a hardcoded password. Replace or remove these development credentials before production deployment.

## Getting Started

### Option 1: Run with Docker Compose

The Compose configuration is located in the parent project directory alongside the gateway and backend service directories.

1. Configure the required variables in the parent `.env` file.
2. Open a terminal in the parent project directory.
3. Build and start the services:

```powershell
docker compose up --build
```

To stop the services:

```powershell
docker compose down
```

### Option 2: Run Locally

1. Start PostgreSQL and Redis.
2. Configure the gateway's required database and application environment variables.
3. Start the backend service on port `8081`.
4. From the gateway project directory, run:

```powershell
.\mvnw.cmd spring-boot:run
```

The gateway starts on port `8080`, provided the required configuration and dependent services are available.

## API Documentation

### Authentication

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/auth/register` | Registers a regular user | Public |
| POST | `/auth/login` | Authenticates a user and returns a JWT | Public |

Successful registration assigns the `USER` role. The login endpoint returns the JWT as a response string.

### Route Management

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/routes` | Creates a route configuration | ADMIN |
| GET | `/routes` | Lists configured routes | USER or ADMIN |
| GET | `/routes/{id}` | Retrieves a route by ID | USER or ADMIN |

Route creation requires administrator authorization. The current route controller supports creation, listing, and retrieval; it does not expose route update or deletion endpoints.

### Backend Request Forwarding

The gateway forwarding controller supports `GET`, `POST`, `PUT`, `DELETE`, and `PATCH` requests for configured routes.

Requests to protected endpoints must include a valid JWT in the `Authorization` header:

```http
Authorization: Bearer <JWT>
```

The gateway validates the destination, applies rate limiting, forwards the request, and returns the backend response with relevant gateway headers.

### Example Request Flow

1. Register a user through `POST /auth/register`, or use an existing account.
2. Log in through `POST /auth/login` to obtain a JWT.
3. Include the JWT when calling protected endpoints.
4. An administrator creates a route through `POST /routes`.
5. Send a request to the configured gateway path.
6. The gateway validates the destination, applies rate limiting, forwards the request to the backend, and returns the response.

## Rate Limiting

The gateway uses Redis to enforce a limit of **5 requests per authenticated user per minute**.

When the limit is exceeded, the gateway returns HTTP `429 Too Many Requests` and includes rate-limit information such as:

- `X-RateLimit-Limit`
- `X-RateLimit-Remaining`
- `Retry-After`

## Testing

Run the gateway test suite from the gateway project directory:

```powershell
.\mvnw.cmd test
```

Run a specific test class:

```powershell
.\mvnw.cmd -Dtest=GlobalExceptionHandlerTest test
```

The test suite covers gateway forwarding, route services, URL validation, DNS resolution, exception handling, rate limiting, and related application components.

## Monitoring

The application exposes these Actuator endpoints:

- `/actuator/health` — Gateway health status.
- `/actuator/metrics` — Available application metrics.

Health details are not exposed by default. Metrics access is subject to the application's security configuration.

## Security Considerations

- Use strong, unique credentials and protect environment files.
- Replace development credentials before deployment.
- Restrict administrative operations to authorized users.
- Configure the target URL allowlist with only trusted destinations.
- Preserve URL and DNS/IP validation to mitigate SSRF risks.
- Keep dependencies and runtime versions updated.
- Review network restrictions, logging, secret management, and deployment settings before production use.

## Project Scope

This project demonstrates API gateway fundamentals, JWT authentication, role-based authorization, dynamic route management, backend request forwarding, Redis-based rate limiting, observability, and security validation in a microservice architecture. It provides a foundation for learning and testing API gateway concepts in a local development environment.
