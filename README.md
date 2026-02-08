[![CircleCI](https://dl.circleci.com/status-badge/img/gh/viktor-mazepa/ecommerce-user-service/tree/master.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/viktor-mazepa/ecommerce-user-service/tree/master)

# ecommerce-user-service

User management service for the ecommerce platform. It exposes admin endpoints to create, update, enable/disable, and delete users, and integrates with Keycloak for identity management.

## Tech Stack
- Java 17
- Spring Boot 3.3.x
- PostgreSQL
- Flyway
- Keycloak (OIDC)

## Prerequisites
- Java 17+
- PostgreSQL
- Keycloak

## Configuration
The service reads configuration from `src/main/resources/application.yaml` and environment variables.

Required environment variables:
- `SPRING_DATASOURCE_URL` (default: `jdbc:postgresql://postgres:5432/users`)
- `SPRING_DATASOURCE_USERNAME` (default: `app`)
- `SPRING_DATASOURCE_PASSWORD` (default: `app`)
- `KEYCLOAK_BASE_URL` (default: `http://keycloak:8080`)
- `KEYCLOAK_REALM` (default: `ecommerce`)
- `APP_KEYCLOAK_LOGIN_CLIENT_SECRET` (no default)
- `APP_KEYCLOAK_ADMIN_CLIENT_SECRET` (no default)

## Database Migrations
Flyway runs automatically at startup using scripts in `src/main/resources/db/migration`.

## Build and Run
```bash
./mvnw clean package
./mvnw spring-boot:run
```

## Tests
```bash
./mvnw test
```

## HTTP Endpoints
Base URL: `http://localhost:8081`

Admin endpoints (require `ADMIN` or `WORKER` role):
- `POST /admin/users`
- `PUT /admin/users/{id}`
- `POST /admin/users/{id}/enable`
- `POST /admin/users/{id}/disable`
- `DELETE /admin/users/{id}`

Actuator:
- `GET /actuator/health`
- `GET /actuator/info`

