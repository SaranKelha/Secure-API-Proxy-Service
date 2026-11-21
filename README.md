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
- JUnit and Mockito
- OpenWeatherMap API

## User Roles and Permissions

| Role | Permissions |
|---|---|
| USER | Current weather, city search, and personal usage history |
| PREMIUM | All USER permissions and extended weather forecast |
| ADMIN | User role management and overall usage statistics |

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
| GET | `/api/weather/current?city=London` | Get current weather by city | USER, PREMIUM |
| GET | `/api/weather/current?lat=51.5074&lon=-0.1278` | Get current weather by coordinates | USER, PREMIUM |
| GET | `/api/weather/search?city=London` | Search city using geocoding | USER, PREMIUM |
| GET | `/api/weather/forecast?city=London` | Get extended weather forecast | PREMIUM |
| GET | `/api/usage/me` | Get personal API usage history | Authenticated users |
| GET | `/api/usage/admin` | Get total API usage statistics | ADMIN |

All protected endpoints require a valid JWT token in the Authorization header.

Example:

`Authorization: Bearer <your-jwt-token>`

## Rate Limiting

The application implements role-based rate limiting.

| Role | Requests per minute |
|---|---:|
| USER | 10 |
| PREMIUM | 30 |
| ADMIN | 60 |

Rate limits are maintained using an in-memory data structure. The counters reset when the application restarts and are not shared across multiple service instances.

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

## Project Structure

```text
Secure-API-Proxy-Service/
│
├── auth-service/
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   └── resources/
│       └── test/
│
├── proxy-service/
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   └── resources/
│       └── test/
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

## Testing

The project includes automated unit tests using JUnit and Mockito.

Tests cover:

- User registration and login
- Duplicate email validation
- Password verification
- Role-based authorization
- Rate limiting
- Usage tracking
- Weather API responses
- Upstream API errors and timeouts

Run the tests from each service directory:

```bash
mvn test
```

## Security Features

- JWT-based authentication
- BCrypt password hashing
- Role-based access control
- Independent JWT validation in Proxy Service
- Stateless authentication
- Protected external API key
- Request rate limiting
- User-specific usage tracking

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

Developed as part of a Software Engineer Internship  Assignment.
