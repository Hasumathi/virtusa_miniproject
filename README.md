# Student Management System - Backend API

> **Team 8 - Mini Project**  
> A robust, production-ready RESTful backend built with **Spring Boot 3**, **Spring Data JPA**, and **Neon Serverless PostgreSQL**, featuring centralized exception handling, field-level validation, OpenAPI 3 (Swagger UI), Spring Boot Actuator monitoring, and automated test coverage.

---

## 🛠 Tech Stack

| Technology | Purpose |
| :--- | :--- |
| **Java 17 / 22** | Programming Language & Runtime |
| **Spring Boot 3.3.4** | Enterprise Application Framework |
| **Spring Data JPA & Hibernate 6** | Object-Relational Mapping (ORM) & Persistence |
| **Neon PostgreSQL** | Primary Cloud Database (Serverless) |
| **H2 Database** | In-Memory Database for Offline Testing & Dev Profile |
| **Jakarta Bean Validation** | Declarative Data Validation (`@Valid`, `@NotBlank`, etc.) |
| **SpringDoc OpenAPI 3** | Interactive Swagger Documentation & UI |
| **Spring Boot Actuator** | Production Health Checks & Telemetry |
| **SLF4J & Logback** | Structured Logging Across All Application Layers |
| **Bruno / Postman** | API Client Collections & Automated Testing |
| **JUnit 5 & MockMvc** | Unit, Repository & Integration Testing Suite |

---

## 🏛 Architecture & Package Structure

The backend follows a clean, layered architectural design pattern:

```text
com.virtusa.studentmanagement
├── config/
│   ├── DatabaseInitializer.java      # Startup connectivity test & initial data seeder
│   └── OpenApiConfig.java            # Swagger / OpenAPI 3 metadata & documentation
├── controller/
│   └── StudentController.java        # REST presentation layer, route handlers & OpenAPI tags
├── entity/
│   └── Student.java                  # JPA Entity mapped to 'students' table with validation rules
├── exception/
│   ├── DuplicateEmailException.java  # Domain exception for email uniqueness violations (409)
│   ├── ErrorResponse.java            # Unified JSON error response DTO contract
│   ├── GlobalExceptionHandler.java   # Centralized @RestControllerAdvice interceptor
│   ├── InvalidStudentDataException.java # Business validation failure exception (400)
│   └── StudentNotFoundException.java # Domain exception for non-existent records (404)
├── repository/
│   └── StudentRepository.java        # Spring Data JPA data access layer & custom JPQL queries
└── service/
    └── StudentService.java           # Business logic layer with transactional boundary & SLF4J logging
```

---

## 🚀 Live API Endpoints

Base URL: `http://localhost:8080`

### 1. Student Management REST APIs

| Method | Endpoint | Description | Sample Query / Body |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/students` | Retrieve all student records | — |
| `GET` | `/api/students/{id}` | Retrieve a student by ID | `/api/students/1` |
| `GET` | `/api/students/page` | Paginated & sorted student retrieval | `?page=0&size=10&sortBy=name&direction=asc` |
| `POST` | `/api/students` | Register a new student | JSON payload (see below) |
| `PUT` | `/api/students/{id}` | Update an existing student | `/api/students/1` + JSON payload |
| `DELETE`| `/api/students/{id}` | Delete a student by ID | `/api/students/1` |
| `GET` | `/api/students/search` | Search students by name or department | `?keyword=Computer` |
| `GET` | `/api/students/department/{dept}` | Filter students by department | `/api/students/department/Computer%20Science` |
| `GET` | `/api/students/year/{year}` | Filter students by academic year (1–4) | `/api/students/year/3` |

### 2. Documentation & Monitoring Endpoints

| Method | Endpoint | Description |
| :---: | :--- | :--- |
| `GET` | `/swagger-ui/index.html` | Interactive Swagger UI web console |
| `GET` | `/v3/api-docs` | OpenAPI 3 JSON specification |
| `GET` | `/actuator/health` | Live application & PostgreSQL database health check |
| `GET` | `/actuator/info` | Application metadata |

---

## 📋 Data Model & Validation Rules

### Student Entity Schema

```json
{
  "name": "Kavitha Raman",
  "email": "kavitha.raman@example.com",
  "department": "Computer Science",
  "yearOfStudy": 2,
  "phoneNumber": "9840123456"
}
```

| Field | Type | Validation Rules | Constraints |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | Primary Key, Auto-generated (Identity) | Unique |
| `name` | `String` | `@NotBlank`, `@Size(min = 2, max = 100)` | Length 2–100 |
| `email` | `String` | `@NotBlank`, `@Email` | Unique across database |
| `department` | `String` | `@NotBlank`, `@Size(max = 100)` | Max length 100 |
| `yearOfStudy` | `Integer` | `@NotNull`, `@Min(1)`, `@Max(4)` | Allowed values: 1, 2, 3, 4 |
| `phoneNumber` | `String` | `@NotBlank`, `@Pattern(regexp = "^[0-9]{10}$")` | Exactly 10 digits |

---

## 🛡️ Exception Handling & Error Contracts

All errors return a consistent, structured JSON schema intercepted by `GlobalExceptionHandler` (`@RestControllerAdvice`).

### A. 404 Not Found (`StudentNotFoundException`)
```json
{
  "timestamp": "2026-11-13T09:06:52",
  "status": 404,
  "error": "Not Found",
  "message": "Student not found with ID: 99999",
  "path": "/api/students/99999"
}
```

### B. 409 Conflict (`DuplicateEmailException`)
```json
{
  "timestamp": "2026-11-13T09:07:12",
  "status": 409,
  "error": "Conflict",
  "message": "Failed to create student: A student with email 'aarav.sharma@example.com' already exists",
  "path": "/api/students"
}
```

### C. 400 Bad Request (Field-by-Field Validation Failure)
```json
{
  "timestamp": "2026-11-13T09:07:12",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed for 5 field(s)",
  "path": "/api/students",
  "validationErrors": {
    "name": "Student name must be between 2 and 100 characters",
    "email": "Please provide a valid email address",
    "department": "Department is required",
    "yearOfStudy": "Year of study cannot exceed 4",
    "phoneNumber": "Phone number must be a valid 10-digit number"
  }
}
```

---

## ⚡ Pagination & Dynamic Sorting

The `/api/students/page` endpoint supports server-side pagination and dynamic sorting:

```http
GET /api/students/page?page=0&size=2&sortBy=name&direction=asc
```

**Response Format**:
```json
{
  "content": [ ... ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 2,
    "sort": { "sorted": true }
  },
  "totalElements": 5,
  "totalPages": 3,
  "size": 2,
  "number": 0,
  "first": true,
  "last": false
}
```

---

## 🧪 Testing with Bruno

The `testing/` directory contains a pre-built Bruno API collection with **13 requests**:

1. `1-get-all-students` (`GET /api/students`)
2. `2-get-student-by-id` (`GET /api/students/1`)
3. `3-create-student` (`POST /api/students`)
4. `4-update-student` (`PUT /api/students/1`)
5. `5-search-students` (`GET /api/students/search?keyword=Computer`)
6. `6-filter-by-department` (`GET /api/students/department/Computer%20Science`)
7. `7-filter-by-year` (`GET /api/students/year/3`)
8. `8-delete-student` (`DELETE /api/students/1`)
9. `9-error-not-found-404` (Tests `404 Not Found` response)
10. `10-error-validation-400` (Tests `400 Validation Failure` response)
11. `11-error-duplicate-email-409` (Tests `409 Duplicate Email Conflict` response)
12. `12-get-students-paginated` (`GET /api/students/page?page=0&size=2&sortBy=name&direction=asc`)
13. `13-actuator-health` (`GET /actuator/health`)

---

## 💻 How to Run Locally

### Prerequisites
* **Java 17** or **Java 22** installed
* **Git**

### 1. Build and Run the Application
```bash
# Clone the repository
git clone https://github.com/KoushikCodesWebpages/team-8.git
cd team-8/backend

# Run automated tests (9/9 tests pass)
./mvnw test

# Package executable JAR
./mvnw package -DskipTests

# Run Spring Boot
java -jar target/student-management-backend-0.0.1-SNAPSHOT.jar
```

### 2. Database Profiles
* **PostgreSQL (Default)**: Connects to Neon Serverless PostgreSQL defined in `application.properties`.
* **Offline H2 Profile**: To run without an external database connection:
  ```bash
  java -jar target/student-management-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=h2
  ```

---

## 📊 Automated Test Coverage

Run tests using the bundled Maven wrapper:
```bash
./mvnw test
```

* **`StudentRepositoryTest`** (6 tests): CREATE, READ by ID/email, UPDATE, DELETE, duplicate email check, filter by department & search by keyword.
* **`GlobalExceptionHandlerTest`** (3 tests): 404 structured error response, 400 validation error map, 409 conflict detection.
* **Total Results**: **9 tests run, 0 failures, 0 errors, 0 skipped** (`BUILD SUCCESS`).
