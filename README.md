# 🏢 JALA Employee Management System

> **Production-level full-stack application**  
> Spring Boot · PostgreSQL · JWT · Vanilla JS

---

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
spring.datasource.username=postgres       ← change if different
spring.datasource.password=postgres       ← change to your password
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

## 🔑 Default Login

```
URL:      http://localhost:8080/swagger-ui.html  (API docs)
Frontend: open frontend/index.html

Admin:  admin@jala.com / admin123
User:   john@jala.com  / user123
```

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

### Search API — Query Parameters

```
GET /api/employees/search
  ?keyword=john        # search name or email
  &status=ACTIVE       # ACTIVE | INACTIVE
  &role=USER           # ADMIN | USER
  &page=0              # 0-based page number
  &size=10             # items per page
  &sortBy=createdAt    # field to sort
  &sortDir=desc        # asc | desc
```

### Response Format

```json
{
  "success": true,
  "message": "Employees fetched",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## 🎨 Frontend Features

| Feature | Location in UI |
|---------|----------------|
| Login with JWT | Login page |
| Dashboard stats | Dashboard page |
| Employee CRUD | Employees page (Admin) |
| Search + Filter | Employees page |
| Pagination + Sort | Employees page |
| Tab switching | More → Tabs Demo |
| Nested sidebar menu | More Features submenu |
| Autocomplete | More → Autocomplete |
| Accordion (FAQ) | More → Accordion |
| Image upload + preview | More → Image Upload |
| Range sliders | More → Slider |
| CSS-only tooltips | More → Slider (hover buttons) |
| Popup / confirm modal | Any delete action |
| Internal links | More → Links |
| CSS properties demo | More → CSS Demo |
| iFrame embed | More → iFrame |
| Profile update | Settings |
| Change password | Settings |
| Logout with confirm | Sidebar bottom |

---

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

---

## 🧪 Testing with Swagger UI

1. Run backend → visit http://localhost:8080/swagger-ui.html
2. Click `POST /api/auth/login` → Try it out
3. Enter: `{ "email": "admin@jala.com", "password": "admin123" }`
4. Copy the `token` from response
5. Click **Authorize** button (top right) → paste `Bearer <token>`
6. Now all secured endpoints are accessible

---

## 🗄️ Database Schema

```sql
employees (
  id            BIGSERIAL PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  email         VARCHAR(150) NOT NULL UNIQUE,
  password      VARCHAR(255) NOT NULL,       -- BCrypt hash
  role          VARCHAR(20) NOT NULL,        -- ADMIN | USER
  status        VARCHAR(20) DEFAULT 'ACTIVE',-- ACTIVE | INACTIVE
  phone         VARCHAR(20),
  department    VARCHAR(100),
  designation   VARCHAR(100),
  profile_image VARCHAR(255),
  created_at    TIMESTAMP DEFAULT NOW(),
  updated_at    TIMESTAMP DEFAULT NOW()
)
```

Indexes on: `email` (unique), `status`, `role`, `department`

---

## 🏷️ Key Libraries & Versions

| Library | Version | Purpose |
|---------|---------|---------|
| Spring Boot | 3.2.0 | Framework |
| Spring Security | 6.x | Auth & RBAC |
| Spring Data JPA | 3.x | ORM layer |
| JJWT | 0.11.5 | JWT tokens |
| PostgreSQL Driver | Latest | DB connection |
| Lombok | Latest | Boilerplate reduction |
| SpringDoc OpenAPI | 2.3.0 | Swagger UI |
| BCrypt | Built-in | Password hashing |

---

## 💡 Interview Notes

- **Soft Delete**: `status = INACTIVE` instead of `DELETE FROM` — preserves history
- **DTO Pattern**: Entities never sent directly; mapped to response DTOs (no password leakage)
- **Global Exception Handler**: Single `@RestControllerAdvice` handles all errors uniformly
- **@Transactional**: Read-only transactions for queries (better performance), write transactions for mutations
- **Lombok**: `@Data`, `@Builder`, `@RequiredArgsConstructor` reduce boilerplate significantly
- **Layered Architecture**: Controller → Service Interface → ServiceImpl → Repository → Entity
- **JPQL Queries**: Named parameter queries with `@Query` for complex search/filter
- **Spring Security Stateless**: No HTTP session — JWT in every request header
- **BCrypt**: Auto-salted password hashing — never compare plaintext to hash directly
- **Pageable**: Spring Data handles `LIMIT`/`OFFSET`/`ORDER BY` automatically

---

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| CORS error in browser | Backend allows all origins; check `SecurityConfig.corsConfigurationSource()` |
| 401 Unauthorized | Token expired or missing; re-login |
| 403 Forbidden | USER trying to access ADMIN endpoint |
| DB connection refused | Check PostgreSQL is running + credentials in `application.properties` |
| Port 8080 in use | Change `server.port` in `application.properties` |
| Image upload fails | Check `uploads/` directory exists; app auto-creates it |
