# Tracking & Analytics API — Frontend Integration Guide

## Base URL

```
http://localhost:8080
```

## Authentication

All tracking and analytics endpoints require the `auth-token` header. Obtain a token via the login endpoint.

```
auth-token: <token-value>
```

If the header is missing or the token is invalid/expired, the API returns:

```json
HTTP 401
{
  "error": "UNAUTHORIZED",
  "message": "Authentication required"
}
```

---

## 1. Load Filter Configuration

Load all dimension data (features, event types, age buckets, genders) for populating dashboard filter dropdowns.

**Call this once on dashboard mount.**

### Request

```
GET /api/analytics/config
```

### Headers

| Header       | Required | Description        |
|-------------|----------|--------------------|
| auth-token  | Yes      | Authentication token |

### Response — 200 OK

```json
{
  "success": true,
  "data": {
    "features": [
      { "id": 1, "name": "date_picker" },
      { "id": 2, "name": "filter_age" },
      { "id": 3, "name": "filter_gender" },
      { "id": 4, "name": "chart_bar" }
    ],
    "eventTypes": [
      { "id": 1, "name": "clicked" },
      { "id": 2, "name": "changed" },
      { "id": 3, "name": "selected" },
      { "id": 4, "name": "applied" }
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

### cURL

```bash
curl -s -H "auth-token: <TOKEN>" \
  "http://localhost:8080/api/analytics/config"
```

### Frontend Usage

```typescript
// On dashboard mount
const config = await fetch('/api/analytics/config', {
  headers: { 'auth-token': token }
}).then(r => r.json());

// Populate dropdowns
setFeatures(config.data.features);
setEventTypes(config.data.eventTypes);
setAgeBuckets(config.data.ageBuckets);
setGenders(config.data.genders);
```

---

## 2. Track Feature Interaction Event

Record a user interaction with a UI feature. Call this whenever the user interacts with a tracked component (clicks a button, changes a filter, selects an option, etc.).

### Request

```
POST /api/track
Content-Type: application/json
```

### Headers

| Header        | Required | Description          |
|--------------|----------|----------------------|
| auth-token   | Yes      | Authentication token |
| Content-Type | Yes      | application/json     |

### Body

| Field       | Type     | Required | Description                                      |
|------------|----------|----------|--------------------------------------------------|
| featureId  | number   | Yes      | ID of the feature being interacted with           |
| eventTypeId| number   | Yes      | ID of the event type (clicked, changed, etc.)     |
| eventTime  | string   | Yes      | ISO-8601 timestamp of the event                   |
| metaInfo   | object   | No       | Optional JSON object with contextual data         |

### Response — 200 OK

```json
{
  "success": true,
  "message": "Event tracked successfully",
  "data": {
    "eventId": 2,
    "featureId": 1,
    "eventTypeId": 1,
    "eventTime": "2026-03-29T10:30:00Z"
  }
}
```

### Error Responses

**Invalid featureId — 404:**
```json
{
  "error": "FEATURE_NOT_FOUND",
  "message": "Feature not found: 99"
}
```

**Invalid eventTypeId — 404:**
```json
{
  "error": "EVENT_TYPE_NOT_FOUND",
  "message": "Event type not found: 99"
}
```

**No auth token — 401:**
```json
{
  "error": "UNAUTHORIZED",
  "message": "Authentication required"
}
```

### cURL Examples

**Basic track:**
```bash
curl -s -X POST http://localhost:8080/api/track \
  -H "Content-Type: application/json" \
  -H "auth-token: <TOKEN>" \
  -d '{
    "featureId": 1,
    "eventTypeId": 1,
    "eventTime": "2026-03-29T10:30:00Z"
  }'
```

**Track with metaInfo:**
```bash
curl -s -X POST http://localhost:8080/api/track \
  -H "Content-Type: application/json" \
  -H "auth-token: <TOKEN>" \
  -d '{
    "featureId": 2,
    "eventTypeId": 2,
    "eventTime": "2026-03-29T11:00:00Z",
    "metaInfo": { "filter": "age", "value": "18-40" }
  }'
```


### Frontend Usage

```typescript
// Track helper function
async function trackEvent(
  featureId: number,
  eventTypeId: number,
  metaInfo?: Record<string, any>
) {
  await fetch('/api/track', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'auth-token': getToken()
    },
    body: JSON.stringify({
      featureId,
      eventTypeId,
      eventTime: new Date().toISOString(),
      ...(metaInfo && { metaInfo })
    })
  });
}

// Usage examples
trackEvent(1, 1);  // date_picker clicked
trackEvent(2, 2, { filter: 'age', value: '18-40' });  // filter_age changed
trackEvent(3, 3, { selectedGender: 'Male' });  // filter_gender selected
trackEvent(4, 4);  // chart_bar applied
```

---

## 3. Feature Totals (Bar Chart Data)

Get total event counts per feature for a date range. Use this to render the bar chart comparing feature usage.

### Request

```
GET /api/analytics/features?fromDate=YYYY-MM-DD&toDate=YYYY-MM-DD
```

### Query Parameters

| Parameter   | Type   | Required | Description                              |
|------------|--------|----------|------------------------------------------|
| fromDate   | date   | Yes      | Start date (ISO format: YYYY-MM-DD)      |
| toDate     | date   | Yes      | End date (ISO format: YYYY-MM-DD)        |
| ageBucketId| number | No       | Filter by age bucket ID                  |
| genderId   | number | No       | Filter by gender ID                      |

### Response — 200 OK

```json
{
  "success": true,
  "data": {
    "summary": {
      "fromDate": "2026-03-28",
      "toDate": "2026-03-29",
      "ageBucketId": null,
      "genderId": null
    },
    "items": [
      { "featureId": 1, "featureName": "date_picker", "totalCount": 4 },
      { "featureId": 2, "featureName": "filter_age", "totalCount": 1 },
      { "featureId": 3, "featureName": "filter_gender", "totalCount": 1 },
      { "featureId": 4, "featureName": "chart_bar", "totalCount": 1 }
    ]
  }
}
```

Items are ordered by `totalCount` descending, then `featureId` ascending.

### cURL Examples

**No filters:**
```bash
curl -s -H "auth-token: <TOKEN>" \
  "http://localhost:8080/api/analytics/features?fromDate=2026-03-28&toDate=2026-03-29"
```

**With gender filter:**
```bash
curl -s -H "auth-token: <TOKEN>" \
  "http://localhost:8080/api/analytics/features?fromDate=2026-03-28&toDate=2026-03-29&genderId=1"
```

**With age bucket filter:**
```bash
curl -s -H "auth-token: <TOKEN>" \
  "http://localhost:8080/api/analytics/features?fromDate=2026-03-28&toDate=2026-03-29&ageBucketId=2"
```

### Frontend Usage

```typescript
interface FeatureTotalsParams {
  fromDate: string;  // YYYY-MM-DD
  toDate: string;    // YYYY-MM-DD
  ageBucketId?: number;
  genderId?: number;
}

async function getFeatureTotals(params: FeatureTotalsParams) {
  const query = new URLSearchParams({
    fromDate: params.fromDate,
    toDate: params.toDate,
    ...(params.ageBucketId && { ageBucketId: String(params.ageBucketId) }),
    ...(params.genderId && { genderId: String(params.genderId) })
  });

  return fetch(`/api/analytics/features?${query}`, {
    headers: { 'auth-token': getToken() }
  }).then(r => r.json());
}

// Render bar chart
const totals = await getFeatureTotals({
  fromDate: '2026-03-01',
  toDate: '2026-03-31'
});
// totals.data.items → bar chart data
// Each item: { featureId, featureName, totalCount }
```

---

## 4. Feature Trend (Line Chart Data)

Get time-series event counts for a specific feature. Use this to render the line chart showing usage over time.

### Request

```
GET /api/analytics/features/{featureId}/trend?fromDate=YYYY-MM-DD&toDate=YYYY-MM-DD&bucket=day|hour
```

### Path Parameters

| Parameter  | Type   | Required | Description                |
|-----------|--------|----------|----------------------------|
| featureId | number | Yes      | ID of the feature to trend |

### Query Parameters

| Parameter   | Type   | Required | Description                                    |
|------------|--------|----------|------------------------------------------------|
| fromDate   | date   | Yes      | Start date (ISO format: YYYY-MM-DD)            |
| toDate     | date   | Yes      | End date (ISO format: YYYY-MM-DD)              |
| bucket     | string | Yes      | Time granularity: `day` or `hour`              |
| ageBucketId| number | No       | Filter by age bucket ID                        |
| genderId   | number | No       | Filter by gender ID                            |

### Response — 200 OK (bucket=day)

```json
{
  "success": true,
  "data": {
    "featureId": 1,
    "featureName": "date_picker",
    "bucket": "day",
    "points": [
      { "time": "2026-03-28", "count": 1 },
      { "time": "2026-03-29", "count": 3 }
    ]
  }
}
```

### Response — 200 OK (bucket=hour)

```json
{
  "success": true,
  "data": {
    "featureId": 1,
    "featureName": "date_picker",
    "bucket": "hour",
    "points": [
      { "time": "2026-03-28 20:30:00+05:30", "count": 1 },
      { "time": "2026-03-29 15:30:00+05:30", "count": 2 },
      { "time": "2026-03-29 17:30:00+05:30", "count": 1 }
    ]
  }
}
```

Points are ordered by `time` ascending.

### Error Responses

**Invalid featureId — 404:**
```json
{ "error": "FEATURE_NOT_FOUND", "message": "Feature not found: 99" }
```

**Invalid bucket — 400:**
```json
{ "error": "ANALYTICS_VALIDATION_ERROR", "message": "Invalid bucket: invalid. Must be 'day' or 'hour'" }
```

### cURL Examples

**Daily trend:**
```bash
curl -s -H "auth-token: <TOKEN>" \
  "http://localhost:8080/api/analytics/features/1/trend?fromDate=2026-03-28&toDate=2026-03-29&bucket=day"
```

**Hourly trend:**
```bash
curl -s -H "auth-token: <TOKEN>" \
  "http://localhost:8080/api/analytics/features/1/trend?fromDate=2026-03-28&toDate=2026-03-29&bucket=hour"
```

### Frontend Usage

```typescript
interface TrendParams {
  featureId: number;
  fromDate: string;
  toDate: string;
  bucket: 'day' | 'hour';
  ageBucketId?: number;
  genderId?: number;
}

async function getFeatureTrend(params: TrendParams) {
  const { featureId, ...queryParams } = params;
  const query = new URLSearchParams({
    fromDate: queryParams.fromDate,
    toDate: queryParams.toDate,
    bucket: queryParams.bucket,
    ...(queryParams.ageBucketId && { ageBucketId: String(queryParams.ageBucketId) }),
    ...(queryParams.genderId && { genderId: String(queryParams.genderId) })
  });

  return fetch(`/api/analytics/features/${featureId}/trend?${query}`, {
    headers: { 'auth-token': getToken() }
  }).then(r => r.json());
}

// Render line chart
const trend = await getFeatureTrend({
  featureId: 1,
  fromDate: '2026-03-01',
  toDate: '2026-03-31',
  bucket: 'day'
});
// trend.data.points → line chart data
// Each point: { time, count }
```

---

## 5. Dashboard (Combined Bar + Line Chart)

Load both bar chart totals and line chart trend in a single request. Use this on initial dashboard load to reduce round trips.

### Request

```
GET /api/analytics/dashboard?fromDate=YYYY-MM-DD&toDate=YYYY-MM-DD&selectedFeatureId=N
```

### Query Parameters

| Parameter         | Type   | Required | Description                                |
|------------------|--------|----------|--------------------------------------------|
| fromDate         | date   | Yes      | Start date (ISO format: YYYY-MM-DD)        |
| toDate           | date   | Yes      | End date (ISO format: YYYY-MM-DD)          |
| selectedFeatureId| number | Yes      | Feature ID for the line chart trend         |
| ageBucketId      | number | No       | Filter by age bucket ID                    |
| genderId         | number | No       | Filter by gender ID                        |

### Response — 200 OK

```json
{
  "success": true,
  "data": {
    "summary": {
      "fromDate": "2026-03-28",
      "toDate": "2026-03-29",
      "ageBucketId": null,
      "genderId": null,
      "selectedFeatureId": 1
    },
    "barChart": [
      { "featureId": 1, "featureName": "date_picker", "totalCount": 4 },
      { "featureId": 2, "featureName": "filter_age", "totalCount": 1 },
      { "featureId": 3, "featureName": "filter_gender", "totalCount": 1 },
      { "featureId": 4, "featureName": "chart_bar", "totalCount": 1 }
    ],
    "lineChart": {
      "featureId": 1,
      "featureName": "date_picker",
      "bucket": "day",
      "points": [
        { "time": "2026-03-28", "count": 1 },
        { "time": "2026-03-29", "count": 3 }
      ]
    }
  }
}
```

### cURL

```bash
curl -s -H "auth-token: <TOKEN>" \
  "http://localhost:8080/api/analytics/dashboard?fromDate=2026-03-28&toDate=2026-03-29&selectedFeatureId=1"
```

### Frontend Usage

```typescript
interface DashboardParams {
  fromDate: string;
  toDate: string;
  selectedFeatureId: number;
  ageBucketId?: number;
  genderId?: number;
}

async function getDashboard(params: DashboardParams) {
  const query = new URLSearchParams({
    fromDate: params.fromDate,
    toDate: params.toDate,
    selectedFeatureId: String(params.selectedFeatureId),
    ...(params.ageBucketId && { ageBucketId: String(params.ageBucketId) }),
    ...(params.genderId && { genderId: String(params.genderId) })
  });

  return fetch(`/api/analytics/dashboard?${query}`, {
    headers: { 'auth-token': getToken() }
  }).then(r => r.json());
}

// Initial dashboard load
const dashboard = await getDashboard({
  fromDate: '2026-03-01',
  toDate: '2026-03-31',
  selectedFeatureId: 1
});
// dashboard.data.barChart → bar chart component
// dashboard.data.lineChart → line chart component
```

---

## 6. Register User (Updated)

Registration now requires `genderId` and `ageBucketId` for demographic tracking.

### Request

```
POST /api/auth/register
Content-Type: application/json
```

### Body

| Field       | Type   | Required | Default | Description                    |
|------------|--------|----------|---------|--------------------------------|
| name       | string | Yes      |         | User display name              |
| email      | string | Yes      |         | User email (unique)            |
| password   | string | Yes      |         | User password                  |
| genderId   | number | No       | 1       | Gender ID from config endpoint |
| ageBucketId| number | No       | 2       | Age bucket ID from config      |

### Response — 200 OK

```json
{
  "userId": 5,
  "email": "user@example.com",
  "message": "User registered successfully"
}
```

### cURL

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "user@example.com",
    "password": "password123",
    "genderId": 1,
    "ageBucketId": 2
  }'
```

---

## Error Response Format

All errors follow a consistent format:

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable description"
}
```

### Error Codes Reference

| HTTP Status | Error Code                  | When                                          |
|------------|----------------------------|-----------------------------------------------|
| 400        | TRACK_VALIDATION_ERROR     | Missing required fields in track request       |
| 400        | ANALYTICS_VALIDATION_ERROR | Invalid bucket value, missing date params      |
| 400        | VALIDATION_ERROR           | Spring validation failures                     |
| 401        | UNAUTHORIZED               | Missing/invalid/expired auth token             |
| 404        | FEATURE_NOT_FOUND          | featureId references non-existent feature      |
| 404        | EVENT_TYPE_NOT_FOUND       | eventTypeId references non-existent event type |
| 409        | EMAIL_ALREADY_EXISTS       | Registration with duplicate email              |
| 500        | INTERNAL_ERROR             | Unexpected server error                        |

---

## Frontend Integration Patterns

### Dashboard Page Flow

```
1. Mount dashboard
   ├── GET /api/analytics/config          → populate filter dropdowns
   └── GET /api/analytics/dashboard       → render bar chart + line chart

2. User changes date range or filters
   └── GET /api/analytics/dashboard       → re-render both charts

3. User clicks a bar in the bar chart
   └── GET /api/analytics/features/{id}/trend  → update line chart only

4. User toggles hourly view
   └── GET /api/analytics/features/{id}/trend?bucket=hour  → hourly line chart
```

### Event Tracking Integration

Track events at the component level. Fire-and-forget — don't block UI on the response.

```typescript
// DatePicker component
<DatePicker onChange={(date) => {
  setDate(date);
  trackEvent(1, 2, { selectedDate: date });  // feature=date_picker, type=changed
}} />

// Age Filter component
<Select onChange={(value) => {
  setAgeFilter(value);
  trackEvent(2, 3, { selectedAge: value });  // feature=filter_age, type=selected
}} />

// Gender Filter component
<Select onChange={(value) => {
  setGenderFilter(value);
  trackEvent(3, 3, { selectedGender: value });  // feature=filter_gender, type=selected
}} />

// Apply Filters button
<Button onClick={() => {
  applyFilters();
  trackEvent(4, 4);  // feature=chart_bar, type=applied
}} />
```

### TypeScript Interfaces

```typescript
// Config response types
interface AnalyticsConfig {
  success: boolean;
  data: {
    features: Array<{ id: number; name: string }>;
    eventTypes: Array<{ id: number; name: string }>;
    ageBuckets: Array<{ id: number; name: string }>;
    genders: Array<{ id: number; name: string }>;
  };
}

// Feature totals types
interface FeatureTotalsResponse {
  success: boolean;
  data: {
    summary: {
      fromDate: string;
      toDate: string;
      ageBucketId: number | null;
      genderId: number | null;
    };
    items: Array<{
      featureId: number;
      featureName: string;
      totalCount: number;
    }>;
  };
}

// Trend types
interface FeatureTrendResponse {
  success: boolean;
  data: {
    featureId: number;
    featureName: string;
    bucket: 'day' | 'hour';
    points: Array<{ time: string; count: number }>;
  };
}

// Dashboard types
interface DashboardResponse {
  success: boolean;
  data: {
    summary: {
      fromDate: string;
      toDate: string;
      ageBucketId: number | null;
      genderId: number | null;
      selectedFeatureId: number;
    };
    barChart: Array<{
      featureId: number;
      featureName: string;
      totalCount: number;
    }>;
    lineChart: {
      featureId: number;
      featureName: string;
      bucket: string;
      points: Array<{ time: string; count: number }>;
    };
  };
}

// Track types
interface TrackRequest {
  featureId: number;
  eventTypeId: number;
  eventTime: string;  // ISO-8601
  metaInfo?: Record<string, any>;
}

interface TrackResponse {
  success: boolean;
  message: string;
  data?: {
    eventId: number;
    featureId: number;
    eventTypeId: number;
    eventTime: string;
  };
}
```

---

## Seed Data Reference

### Features (featureId → name)

| ID | Name           | Description                    |
|----|---------------|--------------------------------|
| 1  | date_picker   | Date range selector component  |
| 2  | filter_age    | Age filter dropdown            |
| 3  | filter_gender | Gender filter dropdown         |
| 4  | chart_bar     | Bar chart component            |

### Event Types (eventTypeId → name)

| ID | Name     | When to use                          |
|----|---------|--------------------------------------|
| 1  | clicked | User clicks/taps a component         |
| 2  | changed | User changes a value (date, input)   |
| 3  | selected| User selects from a dropdown/list    |
| 4  | applied | User applies filters or submits      |

### Genders (genderId → name)

| ID | Name   |
|----|--------|
| 1  | Male   |
| 2  | Female |
| 3  | Other  |

### Age Buckets (ageBucketId → name)

| ID | Name  | Range    |
|----|-------|----------|
| 1  | <18   | 0–17     |
| 2  | 18-40 | 18–40    |
| 3  | >40   | 41+      |
