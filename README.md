# Nagrik Setu

### A citizen-first civic issue reporting and resolution platform

**Nagrik Setu** connects residents with the public departments responsible for their city. Citizens can report problems with a description, location, and photo; municipal staff receive only the reports routed to their department, manage the work, and publish visual proof once an issue is resolved.

Built as a local full-stack project with **Spring Boot, Thymeleaf, MySQL, JavaScript, REST APIs, Lombok, and Groq AI**.

---

## Why Nagrik Setu?

Everyday issues such as overflowing waste, damaged roads, faulty streetlights, or public-safety concerns can be difficult to report and even harder to track. Nagrik Setu brings the journey into one place:

`Report an issue` → `Route it to the right department` → `Track action` → `View verified resolution`

The platform is designed around transparency, departmental accountability, and a clear experience for both citizens and staff.

## Key capabilities

| For citizens | For municipal staff |
| --- | --- |
| Register and sign in securely | Register under a specific department |
| Submit an issue with a photo and location | See only department-assigned reports |
| Let AI suggest the best department | Review the priority assigned to each issue |
| Receive AI-backed priority scoring (1–5) | Add resolution evidence from the field |
| Track personal reports and city activity | Mark a report resolved only with evidence |
| View resolution photos and status updates | Remove or replace submitted evidence when needed |

### AI-assisted reporting

Nagrik Setu uses **Groq** to make reports more useful from the moment they are submitted:

1. **Priority automation** — analyzes the report title, description, category, department, and optional photo to set a priority from **1 (critical)** to **5 (low)**.
2. **Department routing** — suggests a supported department from the entered issue title. Citizens can switch to manual selection at any time.
3. **Image validation** — checks whether an uploaded report image is relevant to the stated issue title before it can be submitted.

> AI suggestions are constrained to the departments supported by the application. If the AI service is unavailable, users can still choose a department manually and the application uses safe fallback behavior.

## Technology stack

| Layer | Technologies |
| --- | --- |
| Backend | Java 21, Spring Boot 3, Spring MVC, Spring Data JPA |
| Frontend | Thymeleaf, HTML5, CSS3, vanilla JavaScript |
| Database | MySQL 8 |
| AI | Groq REST API (text and vision analysis) |
| Security | Session-based role checks, BCrypt password hashing |
| Developer tooling | Maven, Lombok, JUnit / Spring Boot Test |

## Application architecture

```text
Browser (Thymeleaf + HTML/CSS/JavaScript)
                 │
                 ▼
Spring MVC Controllers ──► Services ──► Repositories ──► MySQL
                 │              │
                 │              ├── File storage (report & resolution images)
                 │              └── Groq API (priority, routing, image validation)
                 ▼
          REST endpoints
```

The backend follows a layered package structure:

```text
com.nagriksetu.application
├── controller/   Web pages and REST endpoints
├── dto/          Request and response objects
├── entity/       JPA database entities
├── mapper/       Entity-to-response mapping
├── model/        Departments, roles, priorities, and statuses
├── repository/   Spring Data database access
├── service/      Business rules, Groq integration, and file storage
└── config/       Application configuration
```

## Roles and access control

Nagrik Setu uses role-based access control (RBAC) with two roles:

- **Citizen** — can create reports, follow personal submissions, and view city updates.
- **Municipal staff** — select a department during sign-up and can manage only reports assigned to that department.

Sensitive operations are checked on the server, not just hidden in the interface. Staff cannot submit citizen reports; citizens cannot access staff dashboards or management routes.

## Local setup

### Prerequisites

- Java **21** or later
- Maven **3.9** or later
- MySQL **8** running locally (default port: `3306`)

### 1. Clone the repository

```bash
git clone https://github.com/varadpatil101/Nagrik-Setu.git
cd Nagrik-Setu
```

### 2. Configure Groq securely

Create a local `.env` file in the project root. It is ignored by Git and must never be committed.

```properties
GROQ_API_KEY=your_groq_api_key_here
```

The application loads this file through Spring configuration. Without a Groq key, the platform remains usable, but AI-backed suggestions and validation are unavailable or use their documented fallback behavior.

### 3. Configure MySQL (if needed)

The default configuration creates and uses the local `nagrik_setu` database. For a different local setup, set environment variables before starting the app:

```powershell
$env:DB_URL='jdbc:mysql://localhost:3306/nagrik_setu?createDatabaseIfNotExist=true'
$env:DB_USER='your_mysql_user'
```

### 4. Run the app

```powershell
mvn spring-boot:run
```

Then visit **[http://localhost:8080](http://localhost:8080)**.

## Main routes

| Route | Purpose |
| --- | --- |
| `/` | Government-style welcome page |
| `/login` | Sign in / registration |
| `/citizen/home` | Citizen homepage and city issue feed |
| `/report` | New civic report form |
| `/citizen/dashboard` | Citizen’s submitted reports |
| `/staff/dashboard` | Department-specific staff workspace |
| `/profile` | Profile settings |

## REST API overview

All endpoints use the signed-in session and enforce the caller’s role.

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/issues` | Returns reports available to the current user |
| `POST` | `/api/issues` | Creates a citizen report from JSON |
| `POST` | `/api/issues/upload` | Creates a report with optional image upload |
| `PATCH` | `/api/issues/{id}` | Staff update for an authorized department report |
| `POST` | `/api/issues/{id}/manage` | Staff uploads resolution evidence |
| `DELETE` | `/api/issues/{id}/evidence` | Staff removes resolution evidence |
| `POST` | `/api/ai/department-suggestion` | AI-backed department suggestion from a title |
| `POST` | `/api/ai/image-verification` | AI-backed title-to-image relevance check |

## Privacy and security notes

- The Groq API key is read from `GROQ_API_KEY` and is excluded through `.gitignore`.
- Report and resolution images are stored locally in the ignored `uploads/` directory.
- Passwords are stored using BCrypt hashes.
- A production deployment should move all database credentials to environment variables or a secrets manager, serve media through protected storage, and add CSRF protection, HTTPS, audit logs, and stronger session controls.

## Project status

This project is intended for **local hosting and demonstration**. It is a functional civic-reporting prototype with a clear path to future integrations such as municipal work-order systems, notifications, geospatial dashboards, and operational analytics.

---

<p align="center"><b>Better cities begin with informed citizens and accountable action.</b></p>
