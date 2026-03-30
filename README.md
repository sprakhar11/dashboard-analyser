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

---

## Scaling POST /api/track to 1 Million Requests Per Minute

1M per minute = ~16,700 requests per second. This is very achievable with Phase 1 and Phase 2 below — no distributed databases or complex infrastructure needed.

### What We Have Today

A single `POST /api/track` endpoint. When a user clicks a filter, selects a date range, or interacts with a chart, the frontend calls this API. The backend validates the request, looks up user info, and inserts one row into `app.feature_event` in PostgreSQL.

Current write path (5 database calls per request):
1. Check auth token (SELECT)
2. Check featureId is valid (SELECT)
3. Check eventTypeId is valid (SELECT)
4. Get user's gender and age bucket (SELECT)
5. Insert the event row (INSERT)

This works fine for small traffic. But each request waits for all 5 database calls to finish before responding. As traffic grows, the database becomes the bottleneck.

### What Slows Us Down

- 5 database round trips per request — most are lookups for data that rarely changes
- Single PostgreSQL instance — one server handling all writes
- 7 indexes on the event table — every INSERT updates all 7
- Foreign key checks on 6 tables — PostgreSQL locks parent rows briefly on every INSERT
- BIGSERIAL primary key — a global counter that all inserts compete for
- Generated columns (event_date, event_hour) — extra computation on every INSERT

### Phase 1: Stop Hitting the Database for Things That Don't Change (~5K RPS / ~300K per minute)

The biggest win with zero infrastructure changes.

Cache dimension data (features, event types, genders, age buckets) in memory — these are small tables that almost never change. Cache user dimensions and auth tokens with short TTLs. This drops database calls from 5 to 1 (just the INSERT).

Then stop waiting for the INSERT to finish — return 202 Accepted immediately and write in the background. Response time drops from ~50ms to ~2ms.

Result: ~5K RPS (~300K per minute) on a single server. No new infrastructure. Just code changes.

### Phase 2: Write in Batches (~50K RPS / ~3M per minute) ← Target achieved here

One INSERT of 1000 rows is ~100x faster than 1000 individual INSERTs. Buffer events in memory, flush every 100ms as a single multi-row INSERT.

Add a message queue (like Redis) as a durable buffer so events aren't lost if the app crashes. Drop foreign key constraints (already validated in app layer) and unused indexes to reduce write overhead.

Result: ~50K RPS (~3M per minute). This already exceeds the 1M/minute target by 3x on a single server.

### Phase 3: Run Multiple Copies of the App (if needed for redundancy)

At 1M per minute you likely don't need this for throughput, but you may want it for high availability. A load balancer distributes requests across 2-3 identical app instances. If one goes down, the others keep serving.

Add PgBouncer (a connection pooler) between workers and PostgreSQL. Tune PostgreSQL with `synchronous_commit = off` for async disk writes.

### Scaling Summary

| Phase | Per Minute | Per Second | What Changes |
|-------|-----------|-----------|-------------|
| 0 (Current) | ~6K | ~100 | Synchronous, 5 DB calls per request |
| 1 | ~300K | ~5K | Cache lookups in memory, async response |
| 2 | ~3M | ~50K | Batch writes, message queue, drop FKs/indexes |
| 3 | ~3M+ | ~50K+ | Multiple instances for high availability |

Phase 2 alone gives 3x headroom over the 1M/minute target.

### Key Principles

1. Don't over-build early. Phase 1 gets 50x improvement with just code changes.
2. Batch everything. 1000-row INSERT is ~100x faster than 1000 individual INSERTs.
3. Don't make the client wait for the database. Accept, respond, write later.
4. Cache things that don't change often.
5. Tune PostgreSQL before adding servers.
6. Measure before optimizing.
7. Analytics events are not bank transactions — losing a few in a crash is acceptable.
