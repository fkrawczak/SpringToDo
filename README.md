# First API

A REST API for managing user tasks, built with Java and Spring Boot. The application allows users to register, log in, create, edit, retrieve, filter, and delete their own tasks.

## Key features

- CQRS approach with separate commands, queries, and dedicated handlers,
- asynchronous communication via Apache Kafka — after registration, an event is published that triggers a welcome email,
- PostgreSQL with database migrations managed by Flyway,
- API documentation using the OpenAPI standard.
- secured endpoints providing access only to the authenticated user's tasks,
- session renewal using a refresh token stored in an `HttpOnly` cookie,
- authentication using JWT tokens,
- user registration and login,

## Architecture

The code is organized according to Clean Architecture principles:

- `api` — HTTP controllers, request/response contracts, and global error handling,
- `application` — commands, queries, and handlers,
- `domain` — domain models, business rules, and repository interfaces,
- `infrastructure` — JPA, PostgreSQL, JWT, Spring Security, Kafka, and email handling.

## Technologies

Java 26, Spring Boot, Spring Security, Spring Data JPA, PostgreSQL, Flyway, Apache Kafka, Gradle, Docker Compose, and OpenAPI.

## Local tools

Once the Docker containers are running, the following services are available:

- PostgreSQL database,
- API documentation: `http://localhost:8080/api/docs` or `http://localhost:8080/scalar`,
- Mailpit: `http://localhost:8025`,
- Kafka UI: `http://localhost:8090`.

