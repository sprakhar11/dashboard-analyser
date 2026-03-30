# Dashboard Analyser — Backend

Interactive product analytics dashboard backend built with Kotlin, Spring Boot, MyBatis, and PostgreSQL.

**Live Demo:** [https://sprakhar11.github.io/dashboard-analyser-web/](https://sprakhar11.github.io/dashboard-analyser-web/)

**Backend API:** [https://dashboard-analyser.onrender.com](https://dashboard-analyser.onrender.com)

---

## What It Does

- User registration and authentication with token-based sessions
- Feature interaction tracking (records clicks, selections, changes on UI components)
- Analytics APIs serving bar chart totals and line chart trends with optional date/demographic filters
- Combined dashboard endpoint for single-request page loads

## Tech Stack

- Kotlin 1.9 + Spring Boot 3.4
- MyBatis (SQL mapper)
- PostgreSQL (with Flyway migrations)
- HMAC-SHA256 password hashing with salt
- Maven multi-module build

## Project Structure

```
├── commons/          Flyway migrations (V1–V14), shared resources
├── auth/             Authentication, tracking, analytics (controllers, services, mappers)
├── pingService/      Spring Boot application entry point
├── pom.xml           Parent POM
└── Dockerfile        Multi-stage Docker build
```

---

## Run Locally

### Prerequisites

- Java 17+
- Maven 3.8+ (or use the included `./mvnw` wrapper)
- PostgreSQL 14+ (running and accessible)

### Step 1: Clone the repo

```bash
git clone https://github.com/sprakhar11/dashboard-analyser.git
cd dashboard-analyser
```

### Step 2: Create the `.env` file

Create a `.env` file in the project root. Use `.env.example` as a template:

```bash
cp .env.example .env
```

Then edit `.env` and fill in your values:

```env
# Database connection
DB_HOST=localhost
DB_PORT=5432
DB_NAME=your_database_name
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# Server port
PORT=8080

# Auth token expiry (hours)
AUTH_TOKEN_EXPIRY_HOURS=24

# Password hashing secret key — use a strong random string
PASSWORD_SECRET_KEY=your-secret-key-here

# CORS allowed origins (comma-separated)
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

> For production database credentials and the secret key, please contact the project author.

### Step 3: Create the PostgreSQL database

```bash
psql -U postgres -c "CREATE DATABASE your_database_name;"
```

Flyway will automatically create all tables and seed data on first startup.

### Step 4: Build the project

```bash
./mvnw package -DskipTests -B
```

### Step 5: Run the application

```bash
java -jar pingService/target/pingService-1.0.0.jar
```

The server starts on `http://localhost:8080`.

### Step 6: Verify it's running

```bash
curl http://localhost:8080/api/ping
```

You should see:

```json
{
  "appName": "dashboard-analyser",
  "databaseStatus": "connected",
  ...
}
```

---

## Quick API Test

### Register a user

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "secret123",
    "genderId": 1,
    "age": 25
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "secret123",
    "browserId": "my-browser"
  }'
```

Copy the `token` from the response and use it in subsequent requests.

### Track an event

```bash
curl -X POST http://localhost:8080/api/track \
  -H "Content-Type: application/json" \
  -H "auth-token: YOUR_TOKEN" \
  -d '{
    "featureId": 1,
    "eventTypeId": 1,
    "eventTime": "2026-03-29T10:30:00Z"
  }'
```

### View dashboard

```bash
curl -H "auth-token: YOUR_TOKEN" \
  "http://localhost:8080/api/analytics/dashboard?selectedFeatureId=1"
```

---

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/ping` | No | Health check |
| POST | `/api/auth/register` | No | Register a new user |
| POST | `/api/auth/login` | No | Login and get token |
| POST | `/api/auth/logout` | Yes | Logout (invalidate token) |
| GET | `/api/auth/me` | Yes | Get current user profile |
| POST | `/api/track` | Yes | Record a feature interaction event |
| GET | `/api/analytics/config` | Yes | Get filter dropdown data |
| GET | `/api/analytics/features` | Yes | Feature totals (bar chart) |
| GET | `/api/analytics/features/{id}/trend` | Yes | Feature trend (line chart) |
| GET | `/api/analytics/dashboard` | Yes | Combined bar + line chart |

All analytics endpoints accept optional `fromDate`, `toDate` (format: `YYYY-MM-DDTHH:mm:ss`), `ageBucketId`, and `genderId` query parameters.

---

## Docker

```bash
docker build -t dashboard-analyser .
docker run -p 8080:8080 --env-file .env dashboard-analyser
```

---

## Seed Data

To populate the database with sample users and events:

```bash
psql "postgresql://$DB_USERNAME:$DB_PASSWORD@$DB_HOST:$DB_PORT/$DB_NAME" -f docs/seed-data.sql
```

This creates 5 users and ~80 feature events spread across the last 7 days.

---

## License

See [LICENSE](LICENSE) file.
