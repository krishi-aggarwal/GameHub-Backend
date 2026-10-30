# GameHub-Backend
Backend service for GameHub, a multiplayer gaming platform

## Tech Stack
- Java
- Springboot
- Spring Data JPA
- PostgreSQL (Database)
- Maven (Build Tool)

## Local configuration

Spring Boot imports the ignored `.env` file from the backend directory. Copy
`.env.example` to `.env`, then set the database connection and a unique
`JWT_SECRET` containing at least 32 bytes. Keep `.env` out of version control.

`ADMIN_USERNAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`, and `ADMIN_DISPLAYNAME` are
optional. All four must be set to create an initial admin account; without them
the API starts without seeding an admin.

Set `CORS_ALLOWED_ORIGINS` to a comma-separated list of frontend origins when
using Expo Web from a different host or port.