# EduTrack — Smart Campus Management Portal

EduTrack is an enterprise-grade academic management system engineered for modern educational institutions. It streamlines daily academic operations for faculty and students, featuring attendance tracking, continuous internal and semester grading, role-based dashboards, and user personalization.

---

## 🏛️ System Architecture

- **Backend:** Spring Boot 3.4.0 (Java 17), Spring Security, Stateless JWT Authentication, Flyway Database Migrations, Swagger OpenAPI 3 documentation.
- **Frontend:** Next.js 16 (Turbopack, App Router, React 19), Tailwind CSS v4, dynamic theme switching (Forest Green Light, Deep Charcoal Dark, System default).
- **Database:** PostgreSQL (Neon Serverless PostgreSQL).

---

## 📂 Repository Structure

```
.
├── backend/                  # Spring Boot 3 REST API Server
│   ├── src/main/java/        # Controllers, Services, Entities, Repositories, DTOs
│   ├── src/main/resources/   # Application YAML configs & Flyway migrations
│   ├── pom.xml               # Maven dependencies and build configuration
│   └── API_DOCUMENTATION.md  # Comprehensive REST API specifications
├── frontend/                 # Next.js 16 Client Portal
│   ├── app/                  # Next.js App Router (Pages & API route proxies)
│   ├── components/           # UI Components (Dashboards, Actions, Theme Initializer)
│   ├── lib/                  # Authentication & database helpers
│   ├── package.json          # Node dependencies & build scripts
│   └── tsconfig.json         # TypeScript configuration
└── README.md
```

---

## 🚀 Getting Started

### 1. Prerequisites
- **Java 17+** & **Maven 3.8+**
- **Node.js 18+** & **npm**
- **PostgreSQL Database** (e.g. Neon PostgreSQL)

### 2. Backend Setup
```bash
cd backend

# Copy sample environment configuration
cp .env.example .env

# Configure your database credentials in .env or application.yml
# Run the Spring Boot server
./mvnw spring-boot:run
```
The backend API server will start on `http://localhost:8080`.
- Swagger UI Documentation: `http://localhost:8080/swagger-ui.html`
- OpenAPI Specification: `http://localhost:8080/v3/api-docs`

### 3. Frontend Setup
```bash
cd frontend

# Install dependencies
npm install

# Copy sample environment configuration
cp .env.example .env

# Run development server
npm run dev
```
The client portal will be accessible at `http://localhost:3000`.

---

## 🔐 Default Credentials (Sample Data)

| Role | Email | Password |
|---|---|---|
| **Faculty Member** | `smith@campus.edu` | `Password123` |
| **Student** | `student@campus.edu` | `Password123` |
