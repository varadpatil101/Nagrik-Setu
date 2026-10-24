# Nagrik Setu - Civic Issue Reporting System

Local Spring Boot application for citizens to report civic issues and municipal teams to track them.

## Prerequisites
Java 21+, Maven 3.9+, and MySQL 8. Create a MySQL user or adjust the environment values below.

```powershell
mvn spring-boot:run
```

Open `http://localhost:8080`. MySQL database `nagrik_setu` is created automatically.

## Groq AI (optional)
When ready, provide a Groq API key and start with `$env:GROQ_API_KEY='...'`. Each submitted report is then sent to Groq for a priority score from `1` (highest/critical) to `5` (lowest); all citizen and staff queues are sorted with priority `1` first. Without the key, reports safely default to priority `3`. Do not place the key in source control.

## Departments
Staff registration and citizen report submission both use the full Maharashtra government department list. A report is visible only to staff who registered for its selected department.

## REST API
- `GET /api/issues` - signed-in citizen sees their reports; staff sees their department's reports
- `POST /api/issues` - citizen-only report submission
- `PATCH /api/issues/{id}` - staff-only update, restricted to the staff department
- `POST /api/ai/categorize` - Groq-backed suggested categorisation

## Role-based access
Register as a **Citizen** to submit reports, view city updates, and track your own reports. Register as **Municipal staff** to select a department during sign-up and manage only issues routed to that department. Passwords are stored as BCrypt hashes; Lombok generates boilerplate accessors for the JPA entities.
