# User Aggregator

Spring Boot application that reads users from multiple relational databases, maps heterogeneous schemas into a unified model, and exposes them through one REST endpoint.

## Features

- `GET /users` aggregates users from all configured sources
- Dynamic root-level `data-sources` YAML configuration
- Extensible strategy pattern for `postgres`, `mysql`
- JDBC-based query execution with composable specification filters
- Parallel fetching with graceful degradation on source failures
- Deduplication by user `id`
- OpenAPI/Swagger UI
- Unit tests and Testcontainers integration tests

## API

### Get users

```http
GET /users?username=jdoe&name=John&surname=Doe
```

All filters are optional and combined with `AND` logic. Matching is case-insensitive exact match and is pushed down to each database query.

### Response

```json
[
  {
    "id": "100",
    "username": "jdoe",
    "name": "John",
    "surname": "Doe"
  }
]
```

### Swagger UI

- `http://localhost:8080/swagger-ui.html`

## Configuration

The application reads source definitions from `application.yml` using the required root-level structure:

```yaml
data-sources:
  - name: postgres-users
    strategy: postgres
    url: jdbc:postgresql://localhost:5432/users_db
    table: public.app_users
    user: postgres
    password: postgres
    mapping:
      id: external_id
      username: login
      name: first_name
      surname: last_name
```

The checked-in [`application.yml`](/Users/macos/Desktop/java/JavaTestTask/src/main/resources/application.yml) is a runnable example for the provided Docker Compose setup.

## Run locally

### Prerequisites

- Java 17+
- Maven 3.9+ or Docker

### With Maven

```bash
./mvnw spring-boot:run
```

### With Docker Compose

```bash
docker compose up --build
```

This starts:

- PostgreSQL with table `public.app_users`
- MySQL with table `legacy_users`
- The Spring Boot application on port `8080`

## Test

```bash
./mvnw test
```

Integration tests require a working Docker daemon because they use Testcontainers.

## Notes

- Failed sources are logged and skipped, so one unavailable database does not fail the endpoint.
- Duplicate user ids keep the first occurrence according to source order in configuration (with logging duplicated entities).
