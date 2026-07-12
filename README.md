# First API

REST API do zarządzania zadaniami użytkownika, zbudowane w Javie i Spring Boot. Aplikacja pozwala rejestrować użytkowników, logować się oraz tworzyć, pobierać, filtrować i usuwać własne zadania.

## Najważniejsze elementy

- rejestracja i logowanie użytkowników,
- uwierzytelnianie za pomocą tokenów JWT,
- odnawianie sesji przy użyciu refresh tokena przechowywanego w ciasteczku `HttpOnly`,
- zabezpieczone endpointy z dostępem wyłącznie do zadań zalogowanego użytkownika,
- podejście CQRS — osobne komendy i zapytania wraz z dedykowanymi handlerami,
- asynchroniczna komunikacja przez Apache Kafka — po rejestracji publikowane jest zdarzenie, które uruchamia wysyłkę powitalnej wiadomości email,
- PostgreSQL oraz migracje bazy danych obsługiwane przez Flyway,
- dokumentacja API w standardzie OpenAPI.

## Architektura

Kod jest podzielony zgodnie z założeniami Clean Architecture:

- `api` — kontrolery HTTP, kontrakty request/response i globalna obsługa błędów,
- `application` — komendy, zapytania, handlery,
- `domain` — modele domenowe, reguły biznesowe i interfejsy repozytoriów,
- `infrastructure` — JPA, PostgreSQL, JWT, Spring Security, Kafka i obsługa poczty.

## Technologie

Java 26, Spring Boot, Spring Security, Spring Data JPA, PostgreSQL, Flyway, Apache Kafka, Gradle, Docker Compose oraz OpenAPI.

## Lokalne narzędzia

Po uruchomieniu kontenerów dockerowych dostępne są:

- baza danych PostgreSQL,
- dokumentacja: `http://localhost:8080/api/docs` lub `http://localhost:8080/scalar`,
- Mailpit: `http://localhost:8025`,
- Kafka UI: `http://localhost:8090`.

