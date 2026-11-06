# CRUD Management Dashboard - Team 8 Mini Project

A full-stack student management dashboard application built with **Next.js**, **Spring Boot**, and **MySQL**.

---

## 👥 Team Responsibilities

| Role | Responsibility | Tech Stack | Status |
|---|---|---|:---:|
| **Member 1** | Dashboard UI, Student Listing Table & Search/Filter | Next.js | Upcoming |
| **Member 2** | Add/Edit Student Forms, CRUD Interaction & Validation | Next.js | Upcoming |
| **Member 3** | Database Setup, Student Entity, Repository & JPA/Hibernate | Spring Boot & MySQL | ✅ **Completed** |
| **Member 4** | Student Service Layer, REST APIs & Duplicate Email Checks | Spring Boot | Upcoming |
| **Member 5** | Centralized Exception Handling, Error Response & SLF4J Logging | Spring Boot | Upcoming |

---

## 📁 Repository Structure

```
virtusa_miniproject/
├── backend/                                       # Spring Boot 3 Backend
│   ├── pom.xml                                    # Dependencies (JPA, MySQL, Validation, Web)
│   ├── mvnw / mvnw.cmd                            # Maven Wrapper (Java 17/22 compatible)
│   ├── README.md                                  # Detailed Backend & Handover Documentation
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
├── frontend/                                      # Next.js Frontend (Members 1 & 2)
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
