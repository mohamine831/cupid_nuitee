# Cupid Spring Backend

Minimal Spring Boot project that fetches property details, translations (fr/es), and reviews from the Cupid sandbox API and persists them into PostgreSQL using JPA + Flyway.

Quick start (local PostgreSQL expected):

1. Configure `src/main/resources/application.yml` with your DB credentials and Cupid API key (the provided sandbox key is pre-filled).
2. `mvn package`
3. `java -jar target/cupid-spring-backend-0.0.1-SNAPSHOT.jar`

Endpoints:
- `POST /api/properties/import` -> body: [1238361, 1238362]
- `GET /api/properties/{hotelId}` -> get saved property with translations & reviews
- `POST /api/properties/{hotelId}/refresh` -> re-fetch and update

