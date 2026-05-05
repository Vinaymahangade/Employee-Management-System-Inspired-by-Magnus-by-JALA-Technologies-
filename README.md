# EmpFlow – Employee Management System

This is a full-stack Employee Management System built using Spring Boot and JavaScript. The project is inspired by the Magnus application from JALA Technologies, but implemented with my own backend structure and logic.

Tech Stack
Backend: Spring Boot (Java)
Frontend: HTML, CSS, JavaScript
Database: PostgreSQL
Security: JWT Authentication
Build Tool: Maven

## 📁 Project Structure

```
emp-mgmt/
├── backend/                          ← Spring Boot Maven project
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/jala/empmanagement/
│       │   ├── EmpManagementApplication.java    ← Main entry point
│       │   ├── config/
│       │   │   ├── DataSeeder.java              ← Seeds default admin on startup
│       │   │   ├── SecurityConfig.java          ← Spring Security + JWT config
│       │   │   ├── SwaggerConfig.java           ← OpenAPI/Swagger setup
│       │   │   └── WebMvcConfig.java            ← Static files & uploads
│       │   ├── controller/
│       │   │   ├── AuthController.java          ← POST /api/auth/login
│       │   │   ├── EmployeeController.java      ← CRUD, search, upload
│       │   │   └── SettingsController.java      ← Profile & password
│       │   ├── dto/
│       │   │   ├── request/
│       │   │   │   ├── LoginRequest.java
│       │   │   │   ├── CreateEmployeeRequest.java
│       │   │   │   ├── UpdateEmployeeRequest.java
│       │   │   │   └── ChangePasswordRequest.java
│       │   │   └── response/
│       │   │       ├── ApiResponse.java         ← Unified {success, message, data}
│       │   │       ├── AuthResponse.java
│       │   │       ├── EmployeeResponse.java    ← Never exposes password
│       │   │       └── DashboardStats.java
│       │   ├── entity/
│       │   │   └── Employee.java               ← JPA entity with enums
│       │   ├── exception/
│       │   │   ├── BadRequestException.java
│       │   │   ├── ResourceNotFoundException.java
│       │   │   └── GlobalExceptionHandler.java  ← @RestControllerAdvice
│       │   ├── repository/
│       │   │   └── EmployeeRepository.java      ← JPA + custom JPQL queries
│       │   ├── security/
│       │   │   ├── CustomUserDetailsService.java
│       │   │   ├── JwtAuthFilter.java           ← OncePerRequestFilter
│       │   │   └── JwtUtil.java                 ← Token gen/validation
│       │   └── service/
│       │       ├── AuthService.java
│       │       ├── EmployeeService.java
│       │       └── impl/
│       │           ├── AuthServiceImpl.java
│       │           └── EmployeeServiceImpl.java  ← Full business logic
│       └── resources/
│           └── application.properties
├── frontend/
│   ├── index.html                    ← Single-page app (all pages inside)
│   ├── css/style.css                 ← Complete stylesheet
│   └── js/
│       ├── api.js                    ← All backend fetch calls
│       ├── utils.js                  ← Toast, Modal, validation, debounce
│       └── app.js                    ← Routing, CRUD logic, event bindings
└── schema.sql                        ← PostgreSQL schema + indexes + trigger
```

---

## ⚡ Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ |
| Maven | 3.8+ |
| PostgreSQL | 13+ |
| Node / npm | Not required (vanilla JS) |
| Any browser | Chrome / Firefox / Edge |

---

## 🚀 Step-by-Step Setup

### 1. PostgreSQL — Create Database

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE emp_management_db;

# Exit
\q
```

Run the schema (optional — JPA auto-creates tables):
```bash
psql -U postgres -d emp_management_db -f schema.sql
```

---

### 2. Configure Database Credentials

Edit `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/emp_management_db
spring.datasource.username=postgres      
spring.datasource.password=postgres       
```

---

### 3. Build & Run the Backend

```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
```

Backend starts at: **http://localhost:8080**

On first run, `DataSeeder` auto-creates these accounts:

| Email | Password | Role |
|-------|----------|------|
| admin@jala.com | admin123 | ADMIN |
| john@jala.com | user123 | USER |
| jane@jala.com | user123 | USER (Inactive) |

---

### 4. Open the Frontend

Simply open the file in your browser:

```bash
# Option A: Open directly
open frontend/index.html

# Option B: Use VS Code Live Server extension
# Right-click index.html → Open with Live Server

# Option C: Simple Python server
cd frontend
python3 -m http.server 3000
# then visit http://localhost:3000
```

> ⚠️ If running frontend from `file://`, CORS is already configured to allow all origins.

---

Default Login

Admin:
email: admin@jala.com
password: admin123

User:
email: john@jala.com
password: user123

---

## 📡 API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/login` | Public | Login → JWT token |
| GET | `/api/employees/dashboard` | Any | Stats |
| POST | `/api/employees` | Admin | Create employee |
| GET | `/api/employees/{id}` | Any | Get by ID |
| PUT | `/api/employees/{id}` | Admin | Update employee |
| DELETE | `/api/employees/{id}` | Admin | Soft delete |
| GET | `/api/employees/search` | Any | Search + filter + paginate |
| GET | `/api/employees/autocomplete?query=` | Any | Name/email suggestions |
| POST | `/api/employees/{id}/upload-image` | Any | Upload profile pic |
| GET | `/api/settings/profile` | Any | Own profile |
| PUT | `/api/settings/profile` | Any | Update own profile |
| PUT | `/api/settings/change-password` | Any | Change password |


## 🔐 Security Architecture

```
Client Request
    │
    ▼
JwtAuthFilter (OncePerRequestFilter)
    │  Extract Bearer token from Authorization header
    │  Validate signature + expiration
    │  Set SecurityContextHolder authentication
    ▼
Spring Security Filter Chain
    │  Check URL pattern permissions
    │  @PreAuthorize role checks
    ▼
Controller → Service → Repository → PostgreSQL
```

Passwords are hashed with **BCrypt** (strength 10). Never stored or returned in plain text.

---

## 🏗️ Architecture Layers

```
Controller    → Handles HTTP request/response, input validation via @Valid
    ↓
Service (IF)  → Defines business contract
    ↓
ServiceImpl   → Business logic, transactions, exception handling
    ↓
Repository    → JPA CRUD + custom JPQL queries
    ↓
Entity        → JPA-mapped PostgreSQL table with audit fields
```


