# CRUD Management Dashboard - Student Management System

A full-stack student management dashboard application built with **Next.js**, **Spring Boot**, and **MySQL**.

---

## 🏛️ Architecture & Modules

| Module | Responsibility | Tech Stack | Status |
|---|---|---|:---:|
| **Dashboard UI** | Dashboard Layout, Student Listing Table & Search/Filter | Next.js | Upcoming |
| **Forms & CRUD** | Add/Edit Student Forms, Form Validation & Interaction | Next.js | Upcoming |
| **Database & Repository** | Database Setup, Student Entity, Repository & JPA/Hibernate | Spring Boot & MySQL | ✅ **Completed** |
| **REST APIs & Service** | Student Service Layer, 5 REST Endpoints & Duplicate Email Checks | Spring Boot | Upcoming |
| **Exception Handling** | Centralized Exception Handling, Error Response & SLF4J Logging | Spring Boot | Upcoming |

---

## 📁 Repository Structure

```
virtusa_miniproject/
├── backend/                                       # Spring Boot 3 Backend
│   ├── pom.xml                                    # Dependencies (JPA, MySQL, Validation, Web)
│   ├── mvnw / mvnw.cmd                            # Maven Wrapper (Java 17/22 compatible)
│   ├── README.md                                  # Detailed Backend Documentation
│   └── src/
│       ├── main/
│       │   ├── java/com/virtusa/studentmanagement/
│       │   │   ├── StudentManagementApplication.java
│       │   │   ├── config/DatabaseInitializer.java# Verifies DB & seeds initial records
│       │   │   ├── entity/Student.java            # Student JPA Entity
│       │   │   └── repository/StudentRepository.java
│       │   └── resources/
│       │       ├── application.properties         # MySQL & Hibernate configuration
│       │       ├── schema.sql                     # DDL Schema script
│       │       └── data.sql                       # Seed data script
│       └── test/
│           └── java/.../StudentRepositoryTest.java# 6 automated CRUD repository tests
├── frontend/                                      # Next.js Frontend
└── README.md
```

---

## 🚀 Getting Started (Backend)

### 1. Prerequisites
- **Java 17+** (JDK 22 tested & supported)
- **MySQL Server** (running on port `3306`)

### 2. Configure Database
In `backend/src/main/resources/application.properties`, update your MySQL username/password if needed:
```properties
spring.datasource.username=root
spring.datasource.password=root
```
*(The database `student_management_db` and `students` table are created automatically on startup)*

### 3. Run the Backend
```bash
cd backend
.\mvnw spring-boot:run
```
*(Offline development mode with in-memory H2: `.\mvnw spring-boot:run "-Dspring-boot.run.profiles=h2"`)*

### 4. Run Automated Tests
```bash
cd backend
.\mvnw test
```
