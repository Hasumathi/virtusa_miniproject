# Student Management Backend (Spring Boot)

**Team 8 – Mini Project**  
**Role:** Member 3 – Backend (Database, Entity and Repository)

---

## 📋 Member 3 Responsibilities & Deliverables

| Deliverable | File / Location | Description |
|---|---|---|
| **Spring Boot Project Setup** | `backend/pom.xml`, `StudentManagementApplication.java` | Spring Boot 3.3.4 project configured with JPA, MySQL, Validation, and Web dependencies |
| **MySQL Database Connectivity** | `src/main/resources/application.properties` | Configured HikariCP connection pool, MySQL 8 dialect, and auto-creation flags |
| **Student Entity** | `src/main/java/.../entity/Student.java` | JPA entity with all required fields, table mapping, constraints, and validation |
| **Student Repository** | `src/main/java/.../repository/StudentRepository.java` | `JpaRepository` with custom query methods (duplicate email checks, search, filtering) |
| **Database Operations & Connectivity Test** | `src/main/java/.../config/DatabaseInitializer.java` | Verifies DB connection on startup and demonstrates Create/Read operations |
| **Automated Repository Tests** | `src/test/java/.../repository/StudentRepositoryTest.java` | JUnit 5 tests covering all CRUD operations and custom query methods |
| **Database DDL & Seed Scripts** | `src/main/resources/schema.sql`, `data.sql` | Manual SQL scripts for database creation and sample data |

---

## 🗄️ Student Entity Schema

The `Student` entity is mapped to the `students` table in MySQL:

| Field | Java Type | Column Name | Constraints / Validation |
|---|---|---|---|
| Student ID | `Long` | `id` | Primary Key, Auto-increment (`IDENTITY`) |
| Name | `String` | `name` | NOT NULL, 2–100 chars, `@NotBlank` |
| Email | `String` | `email` | NOT NULL, UNIQUE, `@NotBlank`, `@Email` |
| Department | `String` | `department` | NOT NULL, `@NotBlank` |
| Year of Study | `Integer` | `year_of_study` | NOT NULL, `@Min(1)`, `@Max(4)` |
| Phone Number | `String` | `phone_number` | NOT NULL, 10-digit regex, `@NotBlank` |

---

## 🔍 StudentRepository Methods

Extends `JpaRepository<Student, Long>`:

- **Standard CRUD:**
  - `save(student)` — Create or update record
  - `findById(id)` — Retrieve by primary key
  - `findAll()` — Retrieve all records
  - `deleteById(id)` — Delete record
  - `count()` — Total record count
- **Custom Methods (Ready for Member 4 & 5):**
  - `Optional<Student> findByEmail(String email)` — Lookup by email
  - `boolean existsByEmail(String email)` — Duplicate check for Create Student API
  - `boolean existsByEmailAndIdNot(String email, Long id)` — Duplicate check for Update Student API
  - `List<Student> findByDepartmentIgnoreCase(String department)` — Filter by department
  - `List<Student> findByNameContainingIgnoreCase(String name)` — Search by name
  - `List<Student> searchByKeyword(String keyword)` — Search by keyword across name and department

---

## 🚀 How to Run the Project

### 1. Configure MySQL
Ensure MySQL is running on port `3306`.
If your MySQL password differs from `root`, update it in `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```
*(The database `student_management_db` will be created automatically on startup due to `createDatabaseIfNotExist=true`)*

### 2. Run the Application
Open a terminal in the `backend` folder and run:
```bash
mvn spring-boot:run
```
Or open the `backend` folder in IntelliJ IDEA, Eclipse, or VS Code and run `StudentManagementApplication.java`.

### 3. Run the Unit & Integration Tests
```bash
mvn test
```
The test suite executes using an in-memory database, verifying all database operations without requiring an active MySQL instance.

---

## 🤝 Handover Notes for Team Members

- **For Member 4 (Service & Controller):**
  - Inject `StudentRepository` into your `StudentService.java`.
  - Use `studentRepository.existsByEmail(...)` in `createStudent()` to prevent duplicates.
  - Use `studentRepository.existsByEmailAndIdNot(...)` in `updateStudent()` to prevent duplicate emails during edits.
- **For Member 5 (Exception Handling & Errors):**
  - Throw custom exceptions like `DuplicateEmailException` when repository checks fail, and `StudentNotFoundException` when `findById(...)` returns empty.
