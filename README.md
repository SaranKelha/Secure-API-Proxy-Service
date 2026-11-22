# Secure API Proxy Service

A secure, role-based API proxy application built using Java and Spring Boot. The project consists of two independent microservices: an Authentication Service and a Proxy Service.

## Project Overview

The Secure API Proxy Service provides secure access to weather information through the OpenWeatherMap API. It uses JWT authentication, role-based authorization, request rate limiting, and usage tracking.

### Architecture

The application contains two microservices:

1. **Auth Service** – Handles user registration, login, password encryption, JWT generation, and role management.
2. **Proxy Service** – Validates JWT tokens independently, enforces role-based access, communicates with the OpenWeatherMap API, and tracks API usage.

## Technologies Used

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- Spring Web
- JWT Authentication
- BCrypt Password Encryption
- PostgreSQL
- Maven
- OpenWeatherMap API

## User Roles and Permissions

| Role | Permissions |
|---|---|
| USER | Current weather, city search, and personal usage history |
| PREMIUM | All USER permissions and extended weather forecast |
| ADMIN | All weather endpoints, personal usage history, user role management, and overall usage statistics |

## API Endpoints

### Authentication Service

Base URL: `http://localhost:8081`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and generate JWT |
| PUT | `/api/auth/admin/users/{id}/role` | Update a user's role (ADMIN only) |

### Proxy Service

Base URL: `http://localhost:8082`

| Method | Endpoint | Description | Access |
|---|---|---|---|
| GET | `/api/weather/current?city=London` | Get current weather by city | USER, PREMIUM, ADMIN |
| GET | `/api/weather/current?lat=51.5074&lon=-0.1278` | Get current weather by coordinates | USER, PREMIUM, ADMIN |
| GET | `/api/weather/search?city=London` | Search city using geocoding | USER, PREMIUM, ADMIN |
| GET | `/api/weather/forecast?city=London` | Get extended weather forecast | PREMIUM, ADMIN |
| GET | `/api/usage/me` | Get personal API usage history | Authenticated users |
| GET | `/api/usage/admin` | Get overall API usage statistics | ADMIN |

All protected endpoints require a valid JWT token in the Authorization header.

Example:

`Authorization: Bearer <your-jwt-token>`

## Usage Tracking

The Proxy Service tracks API requests made by authenticated users and stores usage records in PostgreSQL.

### Personal Usage

- `GET /api/usage/me` returns the authenticated user's API usage history.
- Users can only view their own usage records.
- Users cannot access another user's usage records through this endpoint.

### ADMIN Usage Statistics

The ADMIN endpoint provides overall API usage information.

`GET /api/usage/admin`

The response includes:

- Total number of API requests.
- Request counts grouped by user.
- Request counts grouped by endpoint.
- Latest 10 API requests, including user email, endpoint, and request time.

Only users with the ADMIN role can access this endpoint.

## Rate Limiting

The application implements role-based rate limiting for weather endpoints.

| Role | Requests per minute |
|---|---:|
| USER | 10 |
| PREMIUM | 30 |
| ADMIN | 60 |

Rate limits are maintained using an in-memory data structure. Expired user counters are periodically removed from memory to prevent unnecessary memory usage.

The counters reset when the application restarts and are not shared across multiple service instances.

## Error Handling

The Proxy Service handles errors from the external weather API.

| Error | Response |
|---|---|
| Invalid upstream authentication | 502 Bad Gateway |
| Upstream rate limit exceeded | 429 Too Many Requests |
| Weather data not found | 404 Not Found |
| Other upstream HTTP errors | 502 Bad Gateway |
| Upstream timeout | 504 Gateway Timeout |

The application also validates request parameters and handles empty or invalid API responses.

## Manual Testing

The application endpoints were tested manually using Postman.

The following functionalities were checked:

- User registration and login.
- JWT token authentication.
- Role-based access restrictions.
- Current weather using city names.
- Current weather using geographic coordinates.
- City search using the Geocoding API.
- Premium weather forecast access.
- Unauthorized access restrictions for different roles.
- Personal API usage history.
- ADMIN usage statistics.
- ADMIN endpoint authorization (200 for ADMIN, 403 for PREMIUM, and 401 without a token).
- Verification of usage count updates after API requests.
- Role-based rate limiting for USER, PREMIUM, and ADMIN.

## Project Structure

```text
Secure-API-Proxy-Service/
│
├── auth-service/
│   └── src/
│       └── main/
│           ├── java/
│           └── resources/
│
├── proxy-service/
│   └── src/
│       └── main/
│           ├── java/
│           └── resources/
│
└── .gitignore
```

## Configuration

Configure the following environment variables before running the application.

| Variable | Description |
|---|---|
| `JWT_SECRET` | Secret key used for signing JWT tokens |
| `DB_PASSWORD` | PostgreSQL database password |
| `OPENWEATHER_API_KEY` | OpenWeatherMap API key |
| `ADMIN_EMAIL` | Initial administrator email |
| `ADMIN_PASSWORD` | Initial administrator password |

### Database Configuration

Create the following PostgreSQL databases before starting the services:

- `auth_db` – Used by the Auth Service.
- `proxy_db` – Used by the Proxy Service.

The Auth Service uses `AdminInitializer` to create an initial ADMIN account if one does not already exist.

**Important:** `ADMIN_EMAIL` and `ADMIN_PASSWORD` must be configured before starting the Auth Service. The application will fail to start if either variable is missing.

Never commit actual passwords, API keys, or JWT secrets to GitHub.

## Running the Application

### Prerequisites

- Java installed
- Maven installed
- PostgreSQL installed and running
- OpenWeatherMap API key

### 1. Configure the environment variables

Set the required environment variables before starting the services.

### 2. Start Auth Service

Navigate to the Auth Service directory:

```bash
cd auth-service
```

Run:

```bash
mvn spring-boot:run
```

Auth Service runs on port `8081`.

### 3. Start Proxy Service

Open another terminal and navigate to:

```bash
cd proxy-service
```

Run:

```bash
mvn spring-boot:run
```

Proxy Service runs on port `8082`.

## Security Features

- JWT-based authentication
- BCrypt password hashing
- Minimum password length of 8 characters
- Role-based access control
- Independent JWT validation in Proxy Service
- Stateless authentication using `SessionCreationPolicy.STATELESS`
- Protected external API key
- Role-based request rate limiting
- Automatic cleanup of expired rate-limit counters
- User-specific usage tracking
- ADMIN-only overall usage statistics
- SQL query logging disabled

## Limitations

- Rate limiting uses in-memory storage.
- Rate-limit counters reset after application restart.
- Rate limiting is not shared between multiple application instances.
- PostgreSQL is required for persistent user and usage data.
- External weather API availability depends on OpenWeatherMap.

## Future Improvements

- Redis-based distributed rate limiting
- Docker and Docker Compose deployment
- Integration testing
- Centralized logging and monitoring
- Improved API documentation using Swagger/OpenAPI

## Author

Developed as part of a Software Engineer Internship Assignment.
