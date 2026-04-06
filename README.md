# Finance Dashboard — Full-Stack Application

A full-stack finance dashboard built with **Java Spring Boot 3**, **React 18**, **Spring Data JPA / Hibernate**, and **MySQL**.

---

## Live URLs (Local)

| Service | URL |
|---|---|
| **React Frontend** | http://localhost:5173 |
| **Spring Boot Backend (API)** | http://localhost:8080 |

**Default Login:** `admin@finance.com` / `admin123`

---

## Tech Stack

### Backend
| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| ORM / Persistence | Spring Data JPA + Hibernate 6 |
| Database | MySQL 8 (XAMPP) |
| Security | Spring Security 6 + JWT (JJWT 0.11) |
| Validation | Jakarta Bean Validation (`@Valid`, `@NotNull`, `@Positive`, etc.) |
| Boilerplate | Lombok |
| Build | Maven 3.9 |
| Language | Java 17 |

### Frontend
| Layer | Technology |
|---|---|
| Framework | React 18 |
| Routing | React Router v6 |
| HTTP Client | Axios (with request interceptors) |
| Charts | Recharts |
| Build Tool | Vite 5 |
| Auth Storage | localStorage (JWT) |

---

## How to Run

### Prerequisites
- Java 17+
- XAMPP (MySQL running on port 3306)
- Node.js 18+
- Maven (or use `C:/tools/apache-maven-3.9.6/bin/mvn`)

### 1 — Start the Backend

```bash
cd finance-backend
mvn spring-boot:run
```

On first run it automatically:
- Creates the `finance_db` MySQL database
- Creates all tables via Hibernate DDL (`ddl-auto=update`)
- Seeds a default **admin** account: `admin@finance.com` / `admin123`

**API is available at:** `http://localhost:8080`

### 2 — Start the Frontend

```bash
cd finance-frontend
npm install       # only needed once
npm run dev
```

**App is available at:** `http://localhost:5173`

### Configuration

Edit `finance-backend/src/main/resources/application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=          # set if your MySQL has a root password
server.port=8080
```

---

## Assignment Coverage

Every requirement from the assignment is mapped below to the exact implementation.

---

### ✅ 1. User and Role Management

> *"Creating and managing users, assigning roles, managing status, restricting actions based on roles"*

**Implementation:**

| Requirement | Where |
|---|---|
| Create users | `POST /api/users` → `UserController` → `UserService.create()` |
| List users | `GET /api/users` → `UserController.findAll()` |
| Update role / status | `PUT /api/users/{id}` → `UserService.update()` |
| Delete users | `DELETE /api/users/{id}` → `UserService.delete()` |
| Manage active/inactive status | `User.UserStatus` enum, enforced at login in `AuthService` |
| Role-based restrictions | `@PreAuthorize` on all controllers, `JwtAuthFilter` on every request |

**Roles defined (`User.java`):**

| Role | Description |
|---|---|
| `VIEWER` | Read-only access to transactions and dashboard summary |
| `ANALYST` | Everything Viewer can + full dashboard analytics |
| `ADMIN` | Full access — create/edit/delete transactions + manage users |

---

### ✅ 2. Financial Records Management

> *"Amount, type (income/expense), category, date, notes — CRUD + filtering by date, category, type"*

**Entity: `Transaction.java`**

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | Auto-generated primary key |
| `amount` | `BigDecimal(15,2)` | Must be positive — enforced at DB and validation layer |
| `type` | `ENUM(INCOME, EXPENSE)` | |
| `category` | `VARCHAR(100)` | e.g. Salary, Rent, Groceries |
| `date` | `DATE` | ISO format `YYYY-MM-DD` |
| `notes` | `TEXT` | Optional description |
| `created_by` | FK → `users.id` | Which admin created the record |
| `created_at` | `DATETIME` | Set by `@PrePersist` |
| `updated_at` | `DATETIME` | Updated by `@PreUpdate` |
| `deleted_at` | `DATETIME` | `null` = active; set = soft-deleted |

**API:**

| Operation | Endpoint | Access |
|---|---|---|
| List + filter + paginate | `GET /api/transactions` | All roles |
| Create | `POST /api/transactions` | Admin |
| Update (partial) | `PUT /api/transactions/{id}` | Admin |
| Soft-delete | `DELETE /api/transactions/{id}` | Admin |

**Filtering** (`TransactionSpec.java` — JPA Specification pattern):

```
GET /api/transactions?type=INCOME&category=Salary&from=2024-01-01&to=2024-12-31&page=1&size=20
```

---

### ✅ 3. Dashboard Summary APIs

> *"Total income, total expenses, net balance, category-wise totals, recent activity, monthly/weekly trends"*

| Endpoint | Access | Returns |
|---|---|---|
| `GET /api/dashboard/summary` | Viewer+ | `totalIncome`, `totalExpenses`, `netBalance`, `transactionCount` |
| `GET /api/dashboard/category-totals` | Analyst+ | Grouped by `category + type` with total and count |
| `GET /api/dashboard/trends?period=monthly` | Analyst+ | Aggregated income/expense per month or week |
| `GET /api/dashboard/recent` | Analyst+ | Last 10 transactions with creator username |

All implemented in `DashboardService.java` using `@Query` native SQL aggregations in `TransactionRepository.java`.

---

### ✅ 4. Access Control Logic

> *"Backend-level access control — middleware, guards, role-based enforcement"*

**Implementation layers:**

1. **`JwtAuthFilter.java`** — `OncePerRequestFilter` that reads the `Authorization: Bearer <token>` header on every request, validates the JWT, and populates `SecurityContextHolder`.

2. **`SecurityConfig.java`** — Stateless session policy, public routes whitelisted (`/api/auth/**`, `/health`), all other routes require authentication.

3. **`@PreAuthorize` on controllers:**
   - `@PreAuthorize("hasRole('ADMIN')")` — `UserController` (all methods), transaction POST/PUT/DELETE
   - `@PreAuthorize("hasAnyRole('ANALYST','ADMIN')")` — dashboard category-totals, trends, recent
   - `@PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")` — dashboard summary

4. **`UserDetailsServiceImpl.java`** — Inactive (`status=INACTIVE`) users are blocked at authentication time (`enabled=false`).

**Enforcement matrix:**

| Action | VIEWER | ANALYST | ADMIN |
|---|:---:|:---:|:---:|
| Login / Register | ✅ | ✅ | ✅ |
| View all transactions | ✅ | ✅ | ✅ |
| Filter transactions | ✅ | ✅ | ✅ |
| Dashboard summary | ✅ | ✅ | ✅ |
| Category totals, trends, recent | ❌ | ✅ | ✅ |
| Create transaction | ❌ | ❌ | ✅ |
| Update transaction | ❌ | ❌ | ✅ |
| Delete transaction | ❌ | ❌ | ✅ |
| Manage users | ❌ | ❌ | ✅ |

---

### ✅ 5. Validation and Error Handling

> *"Input validation, useful error responses, correct status codes, protection against invalid operations"*

**Validation (`@Valid` + Jakarta Bean Validation):**

| Field | Constraint |
|---|---|
| `amount` | `@NotNull`, `@Positive` |
| `type` | `@NotNull`, must be `INCOME` or `EXPENSE` |
| `category` | `@NotBlank` |
| `date` | `@NotNull` |
| `email` | `@Email` |
| `password` | `@Size(min=6)` |

**`GlobalExceptionHandler.java` (`@RestControllerAdvice`):**

| Exception | HTTP Status | Response |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | `{ "errors": { "field": "message" } }` |
| `IllegalArgumentException` | 400 | `{ "error": "message" }` |
| `AccessDeniedException` | 403 | `{ "error": "Insufficient permissions" }` |
| Any other `Exception` | 500 | `{ "error": "Internal server error" }` |

**Protection against invalid operations:**
- Deleting your own admin account → blocked with `400`
- Accessing soft-deleted transaction → `404`
- Inactive user trying to login → `400`
- Duplicate email/username → `409`

---

### ✅ 6. Data Persistence

> *"Use a persistence approach suitable for your project"*

- **MySQL** (relational database) via XAMPP
- **Spring Data JPA + Hibernate** handles all ORM mapping
- **`ddl-auto=update`** — Hibernate auto-creates/updates the schema on startup, no migration scripts needed
- **`createDatabaseIfNotExist=true`** in the JDBC URL — database is created automatically on first run
- **Soft delete** — transactions are never physically removed; `deleted_at` timestamp marks deletion, preserving full audit history

---

### ✅ Optional: Authentication Using Tokens

JWT-based stateless authentication:
- Login returns a signed JWT (HS256, 24-hour expiry)
- Every protected request requires `Authorization: Bearer <token>`
- Token carries `email` and `role` claims — no database lookup per request
- `JwtUtil.java` handles generation and validation

---

### ✅ Optional: Pagination

`GET /api/transactions` supports full pagination:

```
?page=1&size=20
```

Response (Spring Data `Page<T>` format):
```json
{
  "content": [...],
  "totalElements": 150,
  "totalPages": 8,
  "number": 0,
  "size": 20
}
```

Frontend renders page buttons and navigates between pages.

---

### ✅ Optional: Search / Filtering

Transactions can be filtered by any combination of:
- `type` — `INCOME` or `EXPENSE`
- `category` — exact match
- `from` / `to` — date range

Implemented using **JPA Specification** (`TransactionSpec.java`) — each filter is a `Predicate` composed dynamically, avoiding hardcoded query branching.

---

### ✅ Optional: Soft Delete

Transactions are never permanently deleted. `DELETE /api/transactions/{id}` sets `deleted_at = NOW()`. All queries filter with `WHERE deleted_at IS NULL`. Deleted records remain in the database for audit purposes.

---

### ✅ Optional: API Documentation

Fully documented in this README — all endpoints, request/response shapes, status codes, and examples.

---

### ❌ Optional: Rate Limiting

Not implemented. For production, this would be added using Spring's `Bucket4j` integration or a gateway-level solution (e.g., Nginx, API Gateway).

---

### ❌ Optional: Unit / Integration Tests

Not implemented within scope of this assessment. The natural next step would be:
- `@SpringBootTest` integration tests for controllers
- `@DataJpaTest` for repository queries
- Mockito-based unit tests for service layer

---

## Data Model

```
┌─────────────────────────────────────┐     ┌──────────────────────────────────────────┐
│              users                  │     │             transactions                  │
├─────────────────────────────────────┤     ├──────────────────────────────────────────┤
│ id           INT PK AUTO_INCREMENT  │     │ id           INT PK AUTO_INCREMENT        │
│ username     VARCHAR(50) UNIQUE     │◄────│ created_by   INT FK → users.id            │
│ email        VARCHAR(100) UNIQUE    │     │ amount       DECIMAL(15,2)                │
│ password     VARCHAR(255)           │     │ type         ENUM(INCOME, EXPENSE)        │
│ role         ENUM(VIEWER,ANALYST,   │     │ category     VARCHAR(100)                 │
│              ADMIN)                 │     │ date         DATE                         │
│ status       ENUM(ACTIVE,INACTIVE)  │     │ notes        TEXT                         │
│ created_at   DATETIME               │     │ created_at   DATETIME                     │
└─────────────────────────────────────┘     │ updated_at   DATETIME                     │
                                            │ deleted_at   DATETIME (null = active)     │
                                            └──────────────────────────────────────────┘
```

---

## Project Structure

```
finance-backend/
└── src/main/java/com/finance/
    ├── FinanceApplication.java            Entry point
    ├── config/
    │   ├── SecurityConfig.java            Spring Security chain, CORS, JWT filter wiring
    │   └── DataInitializer.java           Seeds default admin on startup
    ├── controller/
    │   ├── AuthController.java            POST /api/auth/login, /register
    │   ├── UserController.java            CRUD /api/users  (Admin only)
    │   ├── TransactionController.java     CRUD /api/transactions (role-gated)
    │   ├── DashboardController.java       GET  /api/dashboard/*
    │   └── HealthController.java          GET  /health
    ├── dto/
    │   ├── request/                       LoginRequest, RegisterRequest,
    │   │                                  TransactionRequest, TransactionUpdateRequest,
    │   │                                  UserUpdateRequest
    │   └── response/                      AuthResponse, UserResponse,
    │                                      TransactionResponse, DashboardSummaryResponse
    ├── entity/
    │   ├── User.java                      JPA entity — role + status enums, @PrePersist
    │   └── Transaction.java               JPA entity — soft-delete, @PrePersist/@PreUpdate
    ├── exception/
    │   └── GlobalExceptionHandler.java    @RestControllerAdvice — structured error responses
    ├── repository/
    │   ├── UserRepository.java            JpaRepository + findByEmail
    │   ├── TransactionRepository.java     JpaSpecificationExecutor + @Query analytics
    │   ├── TransactionSpec.java           JPA Specification — dynamic multi-filter
    │   ├── CategoryTotalProjection.java   Native query projection interface
    │   └── TrendProjection.java           Native query projection interface
    ├── security/
    │   ├── JwtUtil.java                   Token generation + validation (JJWT)
    │   ├── JwtAuthFilter.java             OncePerRequestFilter — reads + verifies JWT
    │   └── UserDetailsServiceImpl.java    Loads user by email, checks ACTIVE status
    └── service/
        ├── AuthService.java               Register + login business logic
        ├── UserService.java               User CRUD
        ├── TransactionService.java        Transaction CRUD + JPA Specification filtering
        └── DashboardService.java          Aggregation queries — summary, trends, categories

finance-frontend/
└── src/
    ├── api/
    │   └── api.js                         Axios instance — JWT interceptor, 401 redirect
    ├── context/
    │   └── AuthContext.jsx                Global auth state — login/logout, localStorage
    ├── components/
    │   ├── Layout.jsx                     Sidebar nav + <Outlet /> shell
    │   └── ProtectedRoute.jsx             Redirects unauthenticated users to /login
    ├── pages/
    │   ├── Login.jsx                      Email/password form → JWT login
    │   ├── Dashboard.jsx                  Summary cards, bar chart, category table, recent
    │   ├── Transactions.jsx               Paginated table, filters, admin CRUD modal
    │   └── Users.jsx                      User list, create/edit/delete (Admin only)
    ├── App.jsx                            Router setup — protected + public routes
    ├── main.jsx                           React entry point
    └── index.css                          Full custom CSS — no external UI library
```

---

## API Reference

All protected endpoints require: `Authorization: Bearer <token>`

### Auth

| Method | Endpoint | Auth | Body |
|---|---|---|---|
| POST | `/api/auth/register` | Public | `{ username, email, password, role? }` |
| POST | `/api/auth/login` | Public | `{ email, password }` |

**Login Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": { "id": 1, "username": "admin", "email": "admin@finance.com", "role": "ADMIN" }
}
```

---

### Users — `Admin only`

| Method | Endpoint | Body |
|---|---|---|
| GET | `/api/users` | — |
| POST | `/api/users` | `{ username, email, password, role }` |
| PUT | `/api/users/{id}` | `{ role?, status? }` |
| DELETE | `/api/users/{id}` | — |

---

### Transactions

| Method | Endpoint | Access | Body / Params |
|---|---|---|---|
| GET | `/api/transactions` | All | `?type=INCOME&category=Salary&from=2024-01-01&to=2024-12-31&page=1&size=20` |
| POST | `/api/transactions` | Admin | `{ amount, type, category, date, notes? }` |
| PUT | `/api/transactions/{id}` | Admin | Any subset of above fields |
| DELETE | `/api/transactions/{id}` | Admin | — (soft delete) |

**Create example:**
```json
{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2024-03-15",
  "notes": "March salary"
}
```

---

### Dashboard

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/dashboard/summary` | Viewer+ | Total income, expenses, net balance, count |
| GET | `/api/dashboard/category-totals` | Analyst+ | Grouped by category and type |
| GET | `/api/dashboard/trends?period=monthly` | Analyst+ | Monthly or weekly aggregates (last 24 periods) |
| GET | `/api/dashboard/recent` | Analyst+ | Last 10 transactions with creator name |

**Summary response:**
```json
{
  "totalIncome": 150000.00,
  "totalExpenses": 87500.00,
  "netBalance": 62500.00,
  "transactionCount": 34
}
```

---

## Error Responses

```json
{ "error": "Human-readable message" }
```
```json
{ "errors": { "amount": "Amount must be a positive number", "date": "must not be null" } }
```

| Status | When |
|---|---|
| 400 | Validation failure, bad input, self-delete attempt |
| 401 | Missing or invalid JWT |
| 403 | Valid JWT but role is insufficient |
| 404 | Resource not found or already deleted |
| 409 | Duplicate username or email |
| 500 | Unexpected server error |

---

## Assumptions & Design Decisions

| # | Decision | Reason |
|---|---|---|
| 1 | **Linear role hierarchy** — `VIEWER < ANALYST < ADMIN` | Avoids complex permission matrices; hierarchy is clear from the role descriptions in the assignment |
| 2 | **Admin creates financial records** | Assignment states "Admin: Can create, update, and manage records"; Analyst is described as "view records and access insights" — read-only |
| 3 | **Transactions are globally shared** | Assignment does not specify per-user record isolation; all authenticated users see all records |
| 4 | **Soft delete for transactions** | Preserves audit history; deleted records remain queryable if needed |
| 5 | **Hibernate `ddl-auto=update`** | Zero-setup for local development; tables are created automatically without migration scripts |
| 6 | **JWT, no refresh tokens** | Stateless, 24-hour expiry is sufficient for assessment scope |
| 7 | **Separate request/response DTOs** | Entities are never exposed directly; prevents leaking internal fields (e.g., password hash), decouples API contract from DB schema |
| 8 | **JPA Specification for filtering** | Composable, type-safe, avoids building SQL strings manually |
| 9 | **Native SQL for analytics** | `DATE_FORMAT` (trends) is MySQL-specific; JPQL cannot express it, so native queries are used only where necessary |
| 10 | **React frontend with no UI component library** | Keeps dependencies minimal; demonstrates CSS and layout skills directly |
