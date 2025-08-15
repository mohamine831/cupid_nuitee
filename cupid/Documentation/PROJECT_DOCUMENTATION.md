# Project Documentation

This document provides a comprehensive overview of the Cupid Spring Backend application, including instructions for running the project, the database schema, API documentation, and testing results.

## Instructions for Running the Project

Follow these steps to run the Cupid Spring Backend application locally.

### Prerequisites

*   Java 17 or higher
*   Maven
*   PostgreSQL

### 1. Configure the Application

Before running the application, you need to configure your database credentials and Cupid API key in `cupid/src/main/resources/application.yml`.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/cupid
    username: your_db_username
    password: your_db_password
    driver-class-name: org.postgresql.Driver
  # ...
cupid:
  api:
    base-url: https://content-api.cupid.travel/v3.0
    api-key: your_cupid_api_key
    # ...
```

Replace `your_db_username`, `your_db_password`, and `your_cupid_api_key` with your actual credentials. The sandbox key `i2O4p6A8s0D3f5G7h9J1k3L5m7N9b` is pre-filled.

### 2. Build the Project

Open a terminal in the `cupid` directory and run the following command to build the project:

```bash
mvn clean package
```

This command will compile the code, run the tests, and package the application into a JAR file in the `target` directory.

### 3. Run the Application

Once the build is complete, you can run the application with the following command:

```bash
java -jar target/cupid-0.0.1-SNAPSHOT.jar
```

The application will start on port 8080 by default.

## Database Schema

The database schema is managed by Flyway and the migration files are located in `cupid/src/main/resources/db/migration`. The initial schema is defined in `V1__init.sql`.

Below are the tables and their columns:

### `property`

| Column | Type | Constraints |
| --- | --- | --- |
| hotel_id | BIGINT | PRIMARY KEY |
| cupid_id | BIGINT | |
| name | TEXT | |
| hotel_type | TEXT | |
| hotel_type_id | INTEGER | |
| chain | TEXT | |
| chain_id | INTEGER | |
| latitude | DOUBLE PRECISION | |
| longitude | DOUBLE PRECISION | |
| phone | TEXT | |
| email | TEXT | |
| fax | TEXT | |
| pets_allowed | BOOLEAN | |
| child_allowed | BOOLEAN | |
| airport_code | TEXT | |
| group_room_min | INTEGER | |
| main_image_th | TEXT | |
| checkin_json | JSONB | |
| parking | TEXT | |
| address_json | JSONB | |
| stars | INTEGER | |
| rating | NUMERIC | |
| review_count | INTEGER | |
| description_html | TEXT | |
| markdown_description | TEXT | |
| important_info | TEXT | |
| created_at | TIMESTAMP WITH TIME ZONE | DEFAULT now() |
| updated_at | TIMESTAMP WITH TIME ZONE | DEFAULT now() |

### `property_translation`

| Column | Type | Constraints |
| --- | --- | --- |
| id | BIGSERIAL | PRIMARY KEY |
| hotel_id | BIGINT | REFERENCES property(hotel_id) ON DELETE CASCADE |
| lang | VARCHAR(10) | NOT NULL, UNIQUE(hotel_id, lang) |
| description_html | TEXT | |
| markdown_description | TEXT | |
| fetched_at | TIMESTAMP WITH TIME ZONE | DEFAULT now() |

### `property_photo`

| Column | Type | Constraints |
| --- | --- | --- |
| id | BIGSERIAL | PRIMARY KEY |
| hotel_id | BIGINT | REFERENCES property(hotel_id) ON DELETE CASCADE |
| url | TEXT | |
| hd_url | TEXT | |
| image_description | TEXT | |
| image_class1 | TEXT | |
| main_photo | BOOLEAN | |
| score | NUMERIC | |
| class_id | INTEGER | |
| class_order | INTEGER | |

### `room`

| Column | Type | Constraints |
| --- | --- | --- |
| id | BIGINT | PRIMARY KEY |
| hotel_id | BIGINT | REFERENCES property(hotel_id) ON DELETE CASCADE |
| room_name | TEXT | |
| description | TEXT | |
| room_size_square | NUMERIC | |
| room_size_unit | TEXT | |
| max_adults | INTEGER | |
| max_children | INTEGER | |
| max_occupancy | INTEGER | |
| bed_relation | TEXT | |
| bed_types_json | JSONB | |
| views_json | JSONB | |

### `room_photo`

| Column | Type | Constraints |
| --- | --- | --- |
| id | BIGSERIAL | PRIMARY KEY |
| room_id | BIGINT | REFERENCES room(id) ON DELETE CASCADE |
| url | TEXT | |
| hd_url | TEXT | |
| image_description | TEXT | |
| class_order | INTEGER | |
| image_class1 | VARCHAR(255) | |
| class_id | INTEGER | |
| main_photo | BOOLEAN | |
| score | NUMERIC | |

### `review`

| Column | Type | Constraints |
| --- | --- | --- |
| id | BIGSERIAL | PRIMARY KEY |
| hotel_id | BIGINT | REFERENCES property(hotel_id) ON DELETE CASCADE |
| average_score | NUMERIC | |
| country | VARCHAR(50) | |
| type | TEXT | |
| name | TEXT | |
| review_date | TIMESTAMP | |
| headline | TEXT | |
| language | VARCHAR(10) | |
| pros | TEXT | |
| cons | TEXT | |
| source | TEXT | |

### `property_facility`

| Column | Type | Constraints |
| --- | --- | --- |
| id | BIGSERIAL | PRIMARY KEY |
| hotel_id | BIGINT | REFERENCES property(hotel_id) ON DELETE CASCADE |
| facility_id | INTEGER | |
| facility_name | TEXT | |

### `policy`

| Column | Type | Constraints |
| --- | --- | --- |
| id | BIGSERIAL | PRIMARY KEY |
| hotel_id | BIGINT | REFERENCES property(hotel_id) ON DELETE CASCADE |
| policy_type | VARCHAR(255) | |
| name | VARCHAR(255) | |
| description | TEXT | |

### `room_amenity`

| Column | Type | Constraints |
| --- | --- | --- |
| id | BIGSERIAL | PRIMARY KEY |
| room_id | BIGINT | REFERENCES room(id) ON DELETE CASCADE |
| amenities_id | INTEGER | |
| name | VARCHAR(255) | |
| sort | INTEGER | |

## API Documentation

The project uses `springdoc-openapi` to generate API documentation automatically. Once the application is running, you can access the Swagger UI to view and interact with the API endpoints.

*   **Swagger UI URL**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
*   **OpenAPI Spec (JSON)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

The following endpoints are available:

*   `POST /api/properties/import`: Import properties by providing a list of hotel IDs in the request body.
*   `GET /api/properties/{hotelId}`: Get a saved property with its translations and reviews.
*   `POST /api/properties/{hotelId}/refresh`: Re-fetch and update a property's details.
