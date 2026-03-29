# Registration & Profile Changes — Age and Gender

## Summary

Registration now requires `age` (integer) and `genderId`. The age is saved directly in the `app.user` table. The `ageBucketId` is auto-resolved from the age value internally — the caller never needs to know about age buckets.

The `/api/auth/me` endpoint now returns `age` and `gender` details.

---

## What Changed

### Database

New Flyway migration `V13__alter_user_add_age.sql`:

```sql
alter table app."user" add column if not exists age integer;
```

The `app.user` table now has:

| Column       | Type    | Nullable | Description                        |
|-------------|---------|----------|------------------------------------|
| age         | integer | yes      | User's actual age                  |
| gender_id   | smallint| no       | FK to app.gender                   |
| age_bucket_id| smallint| no      | FK to app.age_bucket (auto-resolved)|

### Registration Request

`POST /api/auth/register` now requires `age` and `genderId`.

**Before:**
```json
{ "name": "...", "email": "...", "password": "..." }
```

**After:**
```json
{ "name": "...", "email": "...", "password": "...", "genderId": 1, "age": 25 }
```

### Registration Response

Now includes `age` and `gender` object.

**Before:**
```json
{ "userId": 1, "email": "...", "message": "..." }
```

**After:**
```json
{
  "userId": 1,
  "name": "John Smith",
  "email": "john@example.com",
  "age": 25,
  "gender": { "id": 1, "code": "MALE", "name": "Male" },
  "message": "User registered successfully"
}
```

### /me Response

Now includes `age` and `gender` object.

**Before:**
```json
{ "userId": 1, "name": "...", "email": "..." }
```

**After:**
```json
{
  "userId": 1,
  "name": "John Smith",
  "email": "john@example.com",
  "age": 25,
  "gender": { "id": 1, "code": "MALE", "name": "Male" }
}
```

---

## API Reference

### POST /api/auth/register

#### Request Body

| Field    | Type   | Required | Constraints                          |
|---------|--------|----------|--------------------------------------|
| name    | string | yes      | 1–100 characters                     |
| email   | string | yes      | Valid email format                   |
| password| string | yes      | 6–100 characters                     |
| genderId| number | yes      | Must be a valid active gender ID     |
| age     | number | yes      | Integer, 1–150                       |

#### Valid Gender IDs

| ID | Code   | Name   |
|----|--------|--------|
| 1  | MALE   | Male   |
| 2  | FEMALE | Female |
| 3  | OTHER  | Other  |

Use `GET /api/analytics/config` to fetch the latest list.

---

## Sample Cases

### 1. Successful Registration — Male, age 25

**Request:**
```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Smith",
    "email": "john@example.com",
    "password": "secret123",
    "genderId": 1,
    "age": 25
  }'
```

**Response — 200:**
```json
{
  "userId": 9,
  "name": "John Smith",
  "email": "john@example.com",
  "age": 25,
  "gender": {
    "id": 1,
    "code": "MALE",
    "name": "Male"
  },
  "message": "User registered successfully"
}
```

### 2. Successful Registration — Female, age 15

**Request:**
```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Doe",
    "email": "jane@example.com",
    "password": "secret123",
    "genderId": 2,
    "age": 15
  }'
```

**Response — 200:**
```json
{
  "userId": 10,
  "name": "Jane Doe",
  "email": "jane@example.com",
  "age": 15,
  "gender": {
    "id": 2,
    "code": "FEMALE",
    "name": "Female"
  },
  "message": "User registered successfully"
}
```

### 3. Missing age

**Request:**
```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test",
    "email": "test@example.com",
    "password": "secret123",
    "genderId": 1
  }'
```

**Response — 400:**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "age: Age is required"
}
```

### 4. Age below minimum (0)

**Request:**
```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test",
    "email": "test@example.com",
    "password": "secret123",
    "genderId": 1,
    "age": 0
  }'
```

**Response — 400:**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "age: Age must be at least 1"
}
```

### 5. Age above maximum (200)

**Request:**
```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test",
    "email": "test@example.com",
    "password": "secret123",
    "genderId": 1,
    "age": 200
  }'
```

**Response — 400:**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "age: Age must be at most 150"
}
```

### 6. Negative age

**Request:**
```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test",
    "email": "test@example.com",
    "password": "secret123",
    "genderId": 1,
    "age": -5
  }'
```

**Response — 400:**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "age: Age must be at least 1"
}
```

### 7. Invalid genderId

**Request:**
```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test",
    "email": "test@example.com",
    "password": "secret123",
    "genderId": 99,
    "age": 25
  }'
```

**Response — 400:**
```json
{
  "error": "REGISTRATION_VALIDATION_ERROR",
  "message": "Invalid gender ID: 99. Use GET /api/analytics/config to see valid options."
}
```

### 8. Missing genderId

**Request:**
```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test",
    "email": "test@example.com",
    "password": "secret123",
    "age": 25
  }'
```

**Response — 400:**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "genderId: Gender ID is required"
}
```

### 9. Empty body

**Request:**
```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Response — 400:**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "name: Name is required; password: Password is required; name: Name must be between 1 and 100 characters; genderId: Gender ID is required; email: Email is required; password: Password must be between 6 and 100 characters; age: Age is required"
}
```

### 10. Duplicate email

**Request:**
```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Smith",
    "email": "john@example.com",
    "password": "secret123",
    "genderId": 1,
    "age": 25
  }'
```

**Response — 409:**
```json
{
  "error": "EMAIL_ALREADY_EXISTS",
  "message": "Email is already registered"
}
```

### 11. GET /api/auth/me — Returns age and gender

**Request:**
```bash
curl -s -H "auth-token: <TOKEN>" http://localhost:8080/api/auth/me
```

**Response — 200:**
```json
{
  "userId": 9,
  "name": "John Smith",
  "email": "john@example.com",
  "age": 25,
  "gender": {
    "id": 1,
    "code": "MALE",
    "name": "Male"
  }
}
```

---

## Files Modified

| File | Change |
|------|--------|
| `commons/src/main/resources/db/migration/V13__alter_user_add_age.sql` | New — adds `age` column to user table |
| `auth/.../model/User.kt` | Added `age: Int?` field |
| `auth/.../dto/RegisterRequest.kt` | Replaced `ageBucketId` with `age: Int?` (validated 1–150) |
| `auth/.../dto/RegisterResponse.kt` | Added `age`, removed `ageBucket` |
| `auth/.../dto/MeResponse.kt` | Added `age`, removed `ageBucket` |
| `auth/.../mapper/UserMapper.kt` | Added `age` to SELECT and INSERT queries |
| `auth/.../mapper/AgeBucketMapper.kt` | Added `findByAge(age: Int)` method |
| `auth/.../service/AuthService.kt` | Auto-resolves `ageBucketId` from age; returns age in register/me |
| `auth/pom.xml` | Added `spring-boot-starter-validation` dependency |
