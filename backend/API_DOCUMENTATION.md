# Smart Campus Portal - Backend REST API Integration Guide

This document specifies the REST APIs exposed by the Spring Boot backend for consumption by the Next.js frontend.

---

## 1. General Architecture & Standards

- **Base URL**: `http://localhost:8080` (or configured deployment host)
- **CORS Allowed Origins**: `http://localhost:3000` (configurable via `CORS_ALLOWED_ORIGINS`)
- **Data Exchange Format**: `application/json` (except file uploads which use `multipart/form-data` and PDF reports which return `application/pdf`)
- **Authentication**: JWT Bearer token passed in the `Authorization` header:
  ```http
  Authorization: Bearer <your_jwt_access_token>
  ```
- **Standard Success Response Envelope**:
  ```json
  {
    "timestamp": "2026-11-19T10:00:00",
    "success": true,
    "message": "Operation description",
    "data": { ... }
  }
  ```
- **Standard Error Response Envelope**:
  ```json
  {
    "timestamp": "2026-11-19T10:00:00",
    "status": 404,
    "error": "Not Found",
    "message": "Resource not found message",
    "path": "/api/...",
    "validationErrors": {
      "fieldName": "Validation message"
    }
  }
  ```

---

## 2. Authentication APIs (`/api/auth`)

### 2.1 Login
- **Endpoint**: `/api/auth/login`
- **Method**: `POST`
- **Auth Required**: No (Public)
- **Request Body**:
  ```json
  {
    "usernameOrEmail": "prof_smith",
    "password": "password123"
  }
  ```
- **Successful Response (200 OK)**:
  ```json
  {
    "timestamp": "2026-11-19T10:00:00",
    "success": true,
    "message": "Authentication successful",
    "data": {
      "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
      "tokenType": "Bearer",
      "userId": 1,
      "username": "prof_smith",
      "email": "smith@campus.edu",
      "role": "FACULTY",
      "facultyOrStudentId": 5,
      "expiresInMs": 86400000
    }
  }
  ```
- **Error Response (401 Unauthorized)**:
  ```json
  {
    "timestamp": "2026-11-19T10:00:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid username/email or password provided",
    "path": "/api/auth/login"
  }
  ```

### 2.2 Register Faculty
- **Endpoint**: `/api/auth/faculty/register`
- **Method**: `POST`
- **Auth Required**: No (Protected by secret administrative authorization key)
- **Request Body**:
  ```json
  {
    "employeeId": "FAC202601",
    "username": "dr_robert",
    "fullName": "Dr. Robert Vance",
    "email": "robert.v@campus.edu",
    "password": "SecurePassword123",
    "phone": "9876543210",
    "departmentId": 1,
    "designation": "Associate Professor",
    "authorizationKey": "FACULTY_SECRET_KEY_2026"
  }
  ```
- **Successful Response (201 Created)**: Returns AuthResponse object.

### 2.3 Change Password
- **Endpoint**: `/api/auth/change-password`
- **Method**: `POST`
- **Auth Required**: Yes (`FACULTY` or `STUDENT`)
- **Request Body**:
  ```json
  {
    "currentPassword": "OldPassword123",
    "newPassword": "NewPassword456",
    "confirmPassword": "NewPassword456"
  }
  ```
- **Successful Response (200 OK)**: `{"success": true, "message": "Password changed successfully"}`

### 2.4 Forgot Password
- **Endpoint**: `/api/auth/forgot-password`
- **Method**: `POST`
- **Auth Required**: No (Public)
- **Request Body**: `{"email": "student@campus.edu"}`
- **Successful Response (200 OK)**: Returns a temporary reset token.

### 2.5 Reset Password
- **Endpoint**: `/api/auth/reset-password`
- **Method**: `POST`
- **Auth Required**: No (Public)
- **Request Body**:
  ```json
  {
    "token": "d748f2c2-...",
    "newPassword": "NewPassword456",
    "confirmPassword": "NewPassword456"
  }
  ```
- **Successful Response (200 OK)**: `{"success": true, "message": "Password has been reset successfully"}`

### 2.6 Activate Student Account
- **Endpoint**: `/api/auth/activate`
- **Method**: `POST`
- **Auth Required**: No (Public)
- **Request Body**:
  ```json
  {
    "identifier": "2026CS101",
    "newPassword": "InitialPassword123",
    "confirmPassword": "InitialPassword123"
  }
  ```

### 2.7 Logout
- **Endpoint**: `/api/auth/logout`
- **Method**: `POST`
- **Auth Required**: Yes (`Authorization: Bearer <token>`)
- **Successful Response (200 OK)**: Invalidates token in server blacklist.

---

## 3. Student Management APIs (`/api/students`)

| Endpoint | Method | Role | Description |
|---|---|---|---|
| `/api/students` | `POST` | `FACULTY` | Create a new student record |
| `/api/students` | `GET` | `FACULTY` | List students with search, filters, pagination |
| `/api/students/{id}` | `GET` | `FACULTY` or self `STUDENT` | Get student details |
| `/api/students/reg/{regNo}` | `GET` | `FACULTY` | Find student by register number |
| `/api/students/{id}` | `PUT` | `FACULTY` | Update student profile |
| `/api/students/{id}` | `DELETE` | `FACULTY` | Soft delete / deactivate student |
| `/api/students/{id}/status` | `PATCH` | `FACULTY` | Change account status (`ACTIVE`, `INACTIVE`, `SUSPENDED`) |

### Example: Create Student
- **POST** `/api/students`
- **Request Body**:
  ```json
  {
    "registerNumber": "2026CS102",
    "fullName": "Sarah Connor",
    "email": "sarah.c@campus.edu",
    "phone": "9876543211",
    "departmentId": 1,
    "year": 2,
    "semester": 4,
    "section": "A",
    "dateOfJoining": "2024-08-01",
    "createAccount": true,
    "initialPassword": "Password@2026"
  }
  ```

---

## 4. Subject & Enrollment APIs (`/api/subjects`, `/api/enrollments`)

| Endpoint | Method | Role | Description |
|---|---|---|---|
| `/api/subjects` | `POST` | `FACULTY` | Add new subject |
| `/api/subjects` | `GET` | Authenticated | List subjects with pagination and department filter |
| `/api/subjects/{id}` | `GET` | Authenticated | Get subject by ID |
| `/api/enrollments` | `POST` | `FACULTY` | Enroll individual student |
| `/api/enrollments/batch` | `POST` | `FACULTY` | Batch enroll multiple student IDs |
| `/api/enrollments/student/{studentId}`| `GET` | `FACULTY` or self `STUDENT` | Get student course enrollments |
| `/api/enrollments/subject/{subjectId}`| `GET` | `FACULTY` | List enrolled students in subject |
| `/api/enrollments/{id}` | `DELETE` | `FACULTY` | Drop student enrollment |

---

## 5. Attendance Management APIs (`/api/attendance`)

### 5.1 Mark Batch Attendance
- **Endpoint**: `/api/attendance/batch`
- **Method**: `POST`
- **Role**: `FACULTY`
- **Request Body**:
  ```json
  {
    "subjectId": 10,
    "attendanceDate": "2026-11-19",
    "records": [
      {
        "studentId": 100,
        "status": "PRESENT",
        "remarks": "On time"
      },
      {
        "studentId": 101,
        "status": "ABSENT",
        "remarks": "Unexcused"
      }
    ]
  }
  ```

### 5.2 Attendance Summaries
- **Class Daily Summary**: `GET /api/attendance/summary/class?subjectId=10&date=2026-11-19` (`FACULTY`)
- **Student Subject-wise Summary**: `GET /api/attendance/student/{studentId}/summary` (`FACULTY` or self `STUDENT`)
- **Current Student Own Summary**: `GET /api/attendance/my-summary` (`STUDENT`)
  - Calculates overall percentage and flags `belowThreshold: true` if $< 75\%$.

---

## 6. Grade Management APIs (`/api/grades`)

### 6.1 Enter / Update Marks
- **Endpoint**: `/api/grades`
- **Method**: `POST`
- **Role**: `FACULTY`
- **Request Body**:
  ```json
  {
    "studentId": 100,
    "subjectId": 10,
    "semester": 4,
    "academicYear": "2025-2026",
    "internalMarks": 22.0,
    "assignmentMarks": 14.0,
    "practicalMarks": 18.0,
    "examMarks": 36.0
  }
  ```
  *(Total automatically computed to 90.0, Letter Grade: A+, Passed: true)*

### 6.2 Publish Results
- **Endpoint**: `/api/grades/publish?subjectId=10&semester=4&academicYear=2025-2026`
- **Method**: `POST`
- **Role**: `FACULTY`

### 6.3 View Grades
- **Current Student Published Grades**: `GET /api/grades/my-grades` (`STUDENT`)
- **Student Semester Performance Summary**: `GET /api/grades/student/{studentId}/semester/{sem}` (`FACULTY` or self `STUDENT`)

---

## 7. Profile Picture Upload (`/api/files`)

- **Upload**: `POST /api/files/upload/profile-image` (Multipart `file`, max 5MB, format: JPEG, PNG, WEBP)
- **View / Render**: `GET /api/files/profile-images/{fileName}` (Publicly viewable)

---

## 8. Academic PDF Reports (`/api/reports`)

| Endpoint | Method | Role | Response |
|---|---|---|---|
| `/api/reports/attendance/student/{id}` | `GET` | `FACULTY` or self `STUDENT` | `application/pdf` |
| `/api/reports/my-attendance-pdf` | `GET` | `STUDENT` | `application/pdf` |
| `/api/reports/attendance/class` | `GET` | `FACULTY` | `application/pdf` |
| `/api/reports/grades/subject` | `GET` | `FACULTY` | `application/pdf` |
| `/api/reports/grades/student/{id}` | `GET` | `FACULTY` or self `STUDENT` | `application/pdf` |
| `/api/reports/my-performance-pdf` | `GET` | `STUDENT` | `application/pdf` |
| `/api/reports/attendance/below-threshold` | `GET` | `FACULTY` | JSON array of at-risk students |

---

## 9. Dashboard Analytics APIs (`/api/dashboard`)

- **Faculty Dashboard**: `GET /api/dashboard/faculty` (`FACULTY`)
  - Total students, present today, absent today, attendance %, avg marks, department distribution, recently added students.
- **Student Dashboard**: `GET /api/dashboard/student` (`STUDENT`)
  - Personal attendance %, total enrolled subjects, average marks, current semester, recent grades, unread notifications count.

---

## 10. Announcements & Notifications

- **Announcements**: `GET`, `POST`, `DELETE` at `/api/announcements`
- **Notifications**:
  - `GET /api/notifications`: Paginated in-app notifications
  - `PATCH /api/notifications/{id}/read`: Mark as read
  - `PATCH /api/notifications/read-all`: Mark all as read
  - `GET /api/notifications/unread-count`: Unread counter badge

---

## 11. User Preferences (`/api/settings`)

- `GET /api/settings`: Returns theme preference (`LIGHT`, `DARK`, `SYSTEM`) and notification toggles.
- `PUT /api/settings`: Updates user preferences.

---

## 12. Audit Logs (`/api/audit`)

- `GET /api/audit`: Paginated audit log of system actions (`FACULTY`).
- `GET /api/audit/entity/{entityType}`: Filter by `STUDENT`, `GRADE`, `ATTENDANCE`, `ANNOUNCEMENT`.
