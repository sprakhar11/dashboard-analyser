# feature.md

# Dashboard Analyser — Auth, Track, and Analytics Design

## 1. Objective

Build a single Spring Boot application with:
- database-backed authentication
- browser-aware token sessions
- feature interaction tracking
- analytics APIs for dashboard charts

The application is a **single deployable service**:
- `pingService` is the only runnable module
- `auth` is a library module containing authentication, tracking, and analytics logic
- `commons` contains Flyway migrations and shared infrastructure

This design avoids splitting into microservices and keeps all business logic inside one codebase while still separating concerns at module/package level.

---

## 2. Existing Project Structure Fit

### Current runnable flow
- `pingService` remains the only Spring Boot application
- `auth` remains a library module
- new tracking and analytics code will be added inside the `auth` module
- `pingService` will scan and expose all controllers from `auth`

### Recommended package additions inside `auth`

```text
auth/
└── src/main/kotlin/com/project/analytics/auth/
    ├── controller/
    │   ├── AuthController.kt
    │   ├── TrackController.kt
    │   ├── AnalyticsController.kt
    │   └── GlobalExceptionHandler.kt
    ├── service/
    │   ├── AuthService.kt
    │   ├── TrackService.kt
    │   └── AnalyticsService.kt
    ├── mapper/
    │   ├── UserMapper.kt
    │   ├── AuthTokenMapper.kt
    │   ├── FeatureMapper.kt
    │   ├── EventTypeMapper.kt
    │   ├── FeatureEventMapper.kt
    │   ├── FeatureEventDailyAggMapper.kt
    │   └── FeatureEventHourlyAggMapper.kt
    ├── model/
    │   ├── User.kt
    │   ├── AuthToken.kt
    │   ├── Feature.kt
    │   ├── EventType.kt
    │   ├── FeatureEvent.kt
    │   ├── FeatureEventDailyAgg.kt
    │   └── FeatureEventHourlyAgg.kt
    ├── dto/
    │   ├── auth/
    │   ├── track/
    │   └── analytics/
    ├── security/
    │   ├── SecurityConfig.kt
    │   └── AuthTokenAuthenticationFilter.kt
    └── util/
        └── PasswordUtil.kt
```

---

## 3. Key Design Decisions

### 3.1 Single application
Only `pingService` is runnable.  
All auth, tracking, and analytics APIs are exposed from the same application.

### 3.2 Soft delete
Every table includes:
- `delete_info JSONB NULL`

Rule:
- active row => `delete_info IS NULL`
- deleted row => `delete_info IS NOT NULL`

All hot-path queries and indexes must be built around:
```sql
WHERE delete_info IS NULL
```

### 3.3 Database-backed tokens
JWT will **not** be used.

Instead:
- login generates or reuses a token
- token is stored in `app.auth_token`
- every secured request sends header:
```http
auth-token: <token>
```
- backend validates the token from PostgreSQL on every protected request

### 3.4 Browser-aware session handling
Frontend sends `browserId` during login.

Rules:
- if same user logs in again from same browser and token is active, same token may be reused
- if different browser logs in, behavior depends on `multiple_session_allowed`
- all logic is controlled from DB-backed session records

### 3.5 Analytics must be query-driven
Aggregation will be done at **SQL level**, not in Kotlin loops.

Kotlin code should:
- validate request
- choose query source
- combine response DTOs

PostgreSQL queries should do:
- filtering
- grouping
- sorting
- counting
- aggregation

### 3.6 Scalable storage design
Do not rely on:
- feature name strings in large fact tables
- analytics filters on `meta_info`
- repeated joins to `user` for age/gender during chart queries

Instead:
- use normalized dimension tables
- use integer ids in fact tables
- store analytics-ready snapshot fields in event rows
- read charts from aggregate tables

---

## 4. Scope

## Part 1 — Login Authentication
- register
- login
- logout
- current user (`/me`)
- token validation by DB
- browser-aware login/session reuse

## Part 2 — Track API
- store feature interaction event
- attach authenticated user/session context
- write raw event row
- update aggregate tables

## Part 3 — Analytics API
- load config for filters and feature metadata
- bar chart totals
- line chart trend for selected feature
- optional combined dashboard response

---

## 5. Database Design

## 5.1 Schema
All tables live in:
```sql
app
```

## 5.2 Master / dimension tables

### `app.user`
```sql
id bigint primary key
name varchar(...)
email varchar(...)
password varchar(...)
age integer
gender_id smallint
age_bucket_id smallint
multiple_session_allowed boolean not null default false
delete_info jsonb null
```

### `app.gender`
```sql
id smallint primary key
name varchar(20) not null
delete_info jsonb null
```

Suggested data:
- 1 => Male
- 2 => Female
- 3 => Other

### `app.age_bucket`
```sql
id smallint primary key
name varchar(20) not null
min_age integer null
max_age integer null
delete_info jsonb null
```

Suggested data:
- 1 => `<18`
- 2 => `18-40`
- 3 => `>40`

### `app.feature`
```sql
id integer primary key
name varchar(100) not null
display_name varchar(150) not null
sort_order integer not null default 0
delete_info jsonb null
```

Suggested data:
- 1 => `date_picker`
- 2 => `filter_age`
- 3 => `filter_gender`
- 4 => `chart_bar`

### `app.event_type`
```sql
id integer primary key
name varchar(50) not null
delete_info jsonb null
```

Suggested data:
- 1 => `clicked`
- 2 => `changed`
- 3 => `selected`
- 4 => `applied`
- 5 => `opened`

---

## 5.3 Authentication session table

### `app.auth_token`
```sql
id bigint primary key
user_id bigint not null
token varchar(255) not null
browser_id varchar(255) not null
add_date timestamptz not null
log_out_date timestamptz null
expiry_date timestamptz not null
delete_info jsonb null
```

Active token definition:
- `delete_info IS NULL`
- `log_out_date IS NULL`
- `expiry_date > now()`

---

## 5.4 Raw event fact table

### `app.feature_event`
```sql
id bigint primary key
user_id bigint not null
auth_token_id bigint not null
feature_id integer not null
event_type_id integer not null
browser_id varchar(255) not null
age_bucket_id smallint not null
gender_id smallint not null
event_time timestamptz not null
event_date date not null
event_hour smallint not null
meta_info jsonb null
delete_info jsonb null
```

### Why these columns exist directly in fact table
To avoid expensive runtime joins for analytics:
- `feature_id` instead of feature name string
- `event_type_id` instead of event name string
- `age_bucket_id` and `gender_id` are stored directly for fast filtering
- `event_date` and `event_hour` are stored directly for fast aggregation

### `meta_info`
Allowed only for optional context such as:
- selected filter value
- chart click context
- debug information

`meta_info` must **not** be used for:
- primary filters
- chart aggregation keys
- hot path grouping

---

## 5.5 Aggregate tables

### `app.feature_event_daily_agg`
```sql
event_date date not null
feature_id integer not null
age_bucket_id smallint not null
gender_id smallint not null
total_count bigint not null
delete_info jsonb null
```

### `app.feature_event_hourly_agg`
```sql
event_date date not null
event_hour smallint not null
feature_id integer not null
age_bucket_id smallint not null
gender_id smallint not null
total_count bigint not null
delete_info jsonb null
```

---

## 6. Index Strategy

Important rule:
All performance-critical indexes must use:
```sql
WHERE delete_info IS NULL
```

## 6.1 `app.user`
```sql
create unique index ux_user_email_active
    on app."user"(email)
    where delete_info is null;

create index ix_user_gender_active
    on app."user"(gender_id)
    where delete_info is null;

create index ix_user_age_bucket_active
    on app."user"(age_bucket_id)
    where delete_info is null;
```

## 6.2 `app.auth_token`
```sql
create unique index ux_auth_token_token_active
    on app.auth_token(token)
    where delete_info is null;

create index ix_auth_token_user_active
    on app.auth_token(user_id)
    where delete_info is null;

create index ix_auth_token_user_browser_active
    on app.auth_token(user_id, browser_id)
    where delete_info is null;

create index ix_auth_token_expiry_active
    on app.auth_token(expiry_date)
    where delete_info is null;

create index ix_auth_token_user_browser_expiry_active
    on app.auth_token(user_id, browser_id, expiry_date)
    where delete_info is null;
```

## 6.3 `app.feature`
```sql
create unique index ux_feature_name_active
    on app.feature(name)
    where delete_info is null;
```

## 6.4 `app.event_type`
```sql
create unique index ux_event_type_name_active
    on app.event_type(name)
    where delete_info is null;
```

## 6.5 `app.feature_event`
```sql
create index ix_feature_event_event_date_active
    on app.feature_event(event_date)
    where delete_info is null;

create index ix_feature_event_feature_date_active
    on app.feature_event(feature_id, event_date)
    where delete_info is null;

create index ix_feature_event_feature_date_age_gender_active
    on app.feature_event(feature_id, event_date, age_bucket_id, gender_id)
    where delete_info is null;

create index ix_feature_event_date_age_gender_active
    on app.feature_event(event_date, age_bucket_id, gender_id)
    where delete_info is null;

create index ix_feature_event_user_date_active
    on app.feature_event(user_id, event_date)
    where delete_info is null;

create index ix_feature_event_auth_token_active
    on app.feature_event(auth_token_id)
    where delete_info is null;

create index ix_feature_event_browser_date_active
    on app.feature_event(browser_id, event_date)
    where delete_info is null;
```

Optional for very large event tables:
```sql
create index ix_feature_event_event_time_brin_active
    on app.feature_event using brin(event_time)
    where delete_info is null;
```

## 6.6 `app.feature_event_daily_agg`
```sql
create unique index ux_feature_event_daily_agg_pk_active
    on app.feature_event_daily_agg(event_date, feature_id, age_bucket_id, gender_id)
    where delete_info is null;

create index ix_feature_event_daily_agg_feature_date_active
    on app.feature_event_daily_agg(feature_id, event_date)
    where delete_info is null;

create index ix_feature_event_daily_agg_date_age_gender_active
    on app.feature_event_daily_agg(event_date, age_bucket_id, gender_id)
    where delete_info is null;

create index ix_feature_event_daily_agg_age_gender_date_active
    on app.feature_event_daily_agg(age_bucket_id, gender_id, event_date)
    where delete_info is null;
```

## 6.7 `app.feature_event_hourly_agg`
```sql
create unique index ux_feature_event_hourly_agg_pk_active
    on app.feature_event_hourly_agg(event_date, event_hour, feature_id, age_bucket_id, gender_id)
    where delete_info is null;

create index ix_feature_event_hourly_agg_feature_date_hour_active
    on app.feature_event_hourly_agg(feature_id, event_date, event_hour)
    where delete_info is null;

create index ix_feature_event_hourly_agg_date_age_gender_active
    on app.feature_event_hourly_agg(event_date, age_bucket_id, gender_id, event_hour)
    where delete_info is null;
```

---

## 7. Authentication Flow

## 7.1 Register
- validate request
- check active user by email
- hash password
- insert user

## 7.2 Login
Input:
- email
- password
- browserId

Flow:
1. fetch active user by email
2. verify password
3. find active token for same `user_id + browser_id`
4. if same-browser active token exists, reuse token
5. else check `multiple_session_allowed`
6. if multiple session not allowed and active token exists on another browser, reject login or expire old token based on business rule
7. create new token if needed
8. return token and expiry date

## 7.3 Logout
- read `auth-token` header
- mark `log_out_date`

## 7.4 Protected request authentication
- filter reads `auth-token`
- query `app.auth_token`
- validate active token
- load current user
- build authenticated principal in Spring Security context

---

## 8. Track API Design

## Endpoint
```http
POST /api/track
```

Protected endpoint.

## Request DTO
```json
{
  "featureId": 1,
  "eventTypeId": 2,
  "eventTime": "2026-03-29T08:10:15Z",
  "metaInfo": {
    "filterType": "date_range",
    "fromDate": "2026-03-01",
    "toDate": "2026-03-29"
  }
}
```

## Backend behavior
1. authenticate request from `auth-token`
2. resolve session/user context
3. validate feature and event type
4. derive:
   - `user_id`
   - `auth_token_id`
   - `browser_id`
   - `age_bucket_id`
   - `gender_id`
   - `event_date`
   - `event_hour`
5. insert raw row into `app.feature_event`
6. upsert daily aggregate
7. upsert hourly aggregate
8. return success response

## Response
```json
{
  "success": true,
  "message": "Event tracked successfully",
  "data": {
    "eventId": 120045,
    "featureId": 1,
    "eventTypeId": 2,
    "eventTime": "2026-03-29T08:10:15Z"
  }
}
```

## Validation failure response
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    {
      "field": "featureId",
      "message": "featureId is required"
    }
  ]
}
```

---

## 9. Analytics API Design

Analytics should be returned in a frontend-friendly format.  
The frontend should not aggregate raw event rows.

## 9.1 Config API
Used to load:
- features
- event types
- age buckets
- genders

### Endpoint
```http
GET /api/analytics/config
```

### Response
```json
{
  "success": true,
  "data": {
    "features": [
      { "id": 1, "name": "date_picker", "displayName": "Date Picker" },
      { "id": 2, "name": "filter_age", "displayName": "Age" },
      { "id": 3, "name": "filter_gender", "displayName": "Gender" },
      { "id": 4, "name": "chart_bar", "displayName": "Bar Chart" }
    ],
    "eventTypes": [
      { "id": 1, "name": "clicked" },
      { "id": 2, "name": "changed" },
      { "id": 3, "name": "selected" }
    ],
    "ageBuckets": [
      { "id": 1, "name": "<18" },
      { "id": 2, "name": "18-40" },
      { "id": 3, "name": ">40" }
    ],
    "genders": [
      { "id": 1, "name": "Male" },
      { "id": 2, "name": "Female" },
      { "id": 3, "name": "Other" }
    ]
  }
}
```

## 9.2 Feature totals API
Used for bar chart.

### Endpoint
```http
GET /api/analytics/features?fromDate=2026-03-01&toDate=2026-03-29&ageBucketId=2&genderId=1
```

### Response
```json
{
  "success": true,
  "data": {
    "summary": {
      "fromDate": "2026-03-01",
      "toDate": "2026-03-29",
      "ageBucketId": 2,
      "genderId": 1
    },
    "items": [
      {
        "featureId": 1,
        "featureName": "date_picker",
        "displayName": "Date Picker",
        "totalCount": 210
      },
      {
        "featureId": 2,
        "featureName": "filter_age",
        "displayName": "Age",
        "totalCount": 167
      },
      {
        "featureId": 4,
        "featureName": "chart_bar",
        "displayName": "Bar Chart",
        "totalCount": 142
      },
      {
        "featureId": 3,
        "featureName": "filter_gender",
        "displayName": "Gender",
        "totalCount": 121
      }
    ]
  }
}
```

## 9.3 Trend API
Used for line chart after a feature is selected.

### Endpoint
```http
GET /api/analytics/features/1/trend?fromDate=2026-03-01&toDate=2026-03-29&ageBucketId=2&genderId=1&bucket=day
```

### Response
```json
{
  "success": true,
  "data": {
    "featureId": 1,
    "featureName": "date_picker",
    "displayName": "Date Picker",
    "bucket": "day",
    "points": [
      { "time": "2026-03-01", "count": 8 },
      { "time": "2026-03-02", "count": 11 },
      { "time": "2026-03-03", "count": 6 },
      { "time": "2026-03-04", "count": 10 }
    ]
  }
}
```

## 9.4 Hourly trend API
Used for short date range.

### Endpoint
```http
GET /api/analytics/features/1/trend?fromDate=2026-03-29&toDate=2026-03-29&ageBucketId=2&genderId=1&bucket=hour
```

### Response
```json
{
  "success": true,
  "data": {
    "featureId": 1,
    "featureName": "date_picker",
    "displayName": "Date Picker",
    "bucket": "hour",
    "points": [
      { "time": "2026-03-29T00:00:00Z", "count": 2 },
      { "time": "2026-03-29T01:00:00Z", "count": 1 },
      { "time": "2026-03-29T02:00:00Z", "count": 4 }
    ]
  }
}
```

## 9.5 Optional combined dashboard API
Useful for first page load.

### Endpoint
```http
GET /api/analytics/dashboard?fromDate=2026-03-01&toDate=2026-03-29&ageBucketId=2&genderId=1&selectedFeatureId=1
```

### Response
```json
{
  "success": true,
  "data": {
    "summary": {
      "fromDate": "2026-03-01",
      "toDate": "2026-03-29",
      "ageBucketId": 2,
      "genderId": 1,
      "selectedFeatureId": 1
    },
    "barChart": [
      { "featureId": 1, "featureName": "date_picker", "totalCount": 210 },
      { "featureId": 2, "featureName": "filter_age", "totalCount": 167 },
      { "featureId": 4, "featureName": "chart_bar", "totalCount": 142 },
      { "featureId": 3, "featureName": "filter_gender", "totalCount": 121 }
    ],
    "lineChart": {
      "featureId": 1,
      "featureName": "date_picker",
      "bucket": "day",
      "points": [
        { "time": "2026-03-01", "count": 8 },
        { "time": "2026-03-02", "count": 11 }
      ]
    }
  }
}
```

---

## 10. Query vs Code Responsibility

## SQL / query level should do
- filtering by date, age, gender
- grouping and aggregation
- sorting
- raw event insert
- aggregate upsert

## Kotlin code should do
- request validation
- auth context resolution
- choosing daily vs hourly table
- combining mapper outputs into DTO
- error handling
- default selected feature logic

Rule:
**Never load raw events into Kotlin and aggregate in memory for chart APIs.**

---

## 11. Recommended SQL Query Shapes

## 11.1 Active token lookup
```sql
select id, user_id, token, browser_id, add_date, log_out_date, expiry_date, delete_info
from app.auth_token
where token = #{token}
  and delete_info is null
  and log_out_date is null
  and expiry_date > now()
limit 1
```

## 11.2 Same-browser active session
```sql
select id, user_id, token, browser_id, add_date, log_out_date, expiry_date, delete_info
from app.auth_token
where user_id = #{userId}
  and browser_id = #{browserId}
  and delete_info is null
  and log_out_date is null
  and expiry_date > now()
order by add_date desc
limit 1
```

## 11.3 Feature totals query
Read from daily aggregate table.

```sql
select
    f.id as feature_id,
    f.name as feature_name,
    f.display_name as display_name,
    sum(a.total_count) as total_count
from app.feature_event_daily_agg a
join app.feature f
  on f.id = a.feature_id
 and f.delete_info is null
where a.event_date between #{fromDate} and #{toDate}
  and a.delete_info is null
  and (#{ageBucketId} is null or a.age_bucket_id = #{ageBucketId})
  and (#{genderId} is null or a.gender_id = #{genderId})
group by f.id, f.name, f.display_name
order by total_count desc, f.id asc
```

## 11.4 Daily trend query
```sql
select
    a.event_date as bucket_time,
    sum(a.total_count) as total_count
from app.feature_event_daily_agg a
where a.feature_id = #{featureId}
  and a.event_date between #{fromDate} and #{toDate}
  and a.delete_info is null
  and (#{ageBucketId} is null or a.age_bucket_id = #{ageBucketId})
  and (#{genderId} is null or a.gender_id = #{genderId})
group by a.event_date
order by a.event_date asc
```

## 11.5 Hourly trend query
```sql
select
    a.event_date,
    a.event_hour,
    sum(a.total_count) as total_count
from app.feature_event_hourly_agg a
where a.feature_id = #{featureId}
  and a.event_date between #{fromDate} and #{toDate}
  and a.delete_info is null
  and (#{ageBucketId} is null or a.age_bucket_id = #{ageBucketId})
  and (#{genderId} is null or a.gender_id = #{genderId})
group by a.event_date, a.event_hour
order by a.event_date asc, a.event_hour asc
```

---

## 12. Aggregate Update Strategy

## Recommended initial approach
Use synchronous SQL upsert in same request flow.

### On track request:
1. insert into `app.feature_event`
2. upsert into `app.feature_event_daily_agg`
3. upsert into `app.feature_event_hourly_agg`

### Daily aggregate upsert pattern
```sql
insert into app.feature_event_daily_agg (
    event_date,
    feature_id,
    age_bucket_id,
    gender_id,
    total_count,
    delete_info
) values (
    #{eventDate},
    #{featureId},
    #{ageBucketId},
    #{genderId},
    1,
    null
)
on conflict (event_date, feature_id, age_bucket_id, gender_id)
do update set total_count = app.feature_event_daily_agg.total_count + 1
```

### Hourly aggregate upsert pattern
```sql
insert into app.feature_event_hourly_agg (
    event_date,
    event_hour,
    feature_id,
    age_bucket_id,
    gender_id,
    total_count,
    delete_info
) values (
    #{eventDate},
    #{eventHour},
    #{featureId},
    #{ageBucketId},
    #{genderId},
    1,
    null
)
on conflict (event_date, event_hour, feature_id, age_bucket_id, gender_id)
do update set total_count = app.feature_event_hourly_agg.total_count + 1
```

Note:
If partial unique indexes are used for aggregate tables, design conflict handling carefully.  
If needed, prefer a real table unique constraint or active-only table semantics for aggregate rows.

---

## 13. DTO Suggestions

## Auth DTOs
- `RegisterRequest`
- `RegisterResponse`
- `LoginRequest`
- `LoginResponse`
- `LogoutResponse`
- `MeResponse`
- `ErrorResponse`

## Track DTOs
- `TrackRequest`
- `TrackResponse`

## Analytics DTOs
- `AnalyticsConfigResponse`
- `FeatureTotalsResponse`
- `FeatureTrendResponse`
- `DashboardAnalyticsResponse`

---

## 14. Controller Endpoints

## AuthController
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/me`

## TrackController
- `POST /api/track`

## AnalyticsController
- `GET /api/analytics/config`
- `GET /api/analytics/features`
- `GET /api/analytics/features/{featureId}/trend`
- optional: `GET /api/analytics/dashboard`

## PingController
- `GET /api/ping`

---

## 15. Error Handling

Use `GlobalExceptionHandler` to map:
- invalid credentials => `401`
- invalid token => `401`
- already logged in / session not allowed => `409`
- validation failure => `400`
- missing resource / feature not found => `404`
- unhandled error => `500`

### Common error response
```json
{
  "success": false,
  "code": "INVALID_TOKEN",
  "message": "Invalid or expired auth token"
}
```

---

## 16. Frontend Interaction Flow

## Initial page load
1. load saved filters from cookies
2. call `/api/analytics/config`
3. call `/api/analytics/dashboard` or:
   - `/api/analytics/features`
   - `/api/analytics/features/{defaultFeatureId}/trend`

## Filter change
1. save filter values in cookies
2. call `POST /api/track`
3. refresh feature totals
4. refresh selected feature trend

## Bar chart click
1. call `POST /api/track`
2. call `/api/analytics/features/{clickedFeatureId}/trend`

---

## 17. Migration Plan

Recommended Flyway migration sequence:

- `V1__create_app_schema.sql`
- `V2__create_user_table.sql`
- `V3__create_auth_token_table.sql`
- `V4__create_gender_table.sql`
- `V5__create_age_bucket_table.sql`
- `V6__create_feature_table.sql`
- `V7__create_event_type_table.sql`
- `V8__create_feature_event_table.sql`
- `V9__create_feature_event_daily_agg_table.sql`
- `V10__create_feature_event_hourly_agg_table.sql`
- `V11__seed_gender_age_bucket_feature_event_type.sql`
- `V12__create_indexes.sql`

---

## 18. Implementation Order

### Phase 1
- complete auth flow
- database-backed token validation
- browser-aware login
- `/api/auth/me`

### Phase 2
- create feature/event master tables
- implement `POST /api/track`
- insert raw event

### Phase 3
- add daily/hourly aggregate writes
- add analytics config API
- add feature totals API
- add trend API

### Phase 4
- optional combined dashboard API
- seed dummy analytics data
- Postman collection updates
- docs update

---

## 19. Notes

- keep all analytics filtering out of `meta_info`
- do not aggregate in Kotlin memory
- keep active rows with `delete_info = null`
- make mapper SQL explicitly include `delete_info is null`
- prefer integer ids in hot fact/agg tables
- use aggregate tables for frontend charts
- keep `pingService` unchanged as the single application entry point

---

## 20. Final Summary

This design keeps the current project structure intact while extending the `auth` module into a broader domain module that handles:
- authentication
- session validation
- event tracking
- analytics APIs

It is optimized for:
- single deployable application
- PostgreSQL-only querying
- soft delete support
- millisecond analytics responses
- frontend-ready chart payloads