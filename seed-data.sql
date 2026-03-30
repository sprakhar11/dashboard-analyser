-- ============================================================
-- Seed Data Script: 5 users + auth tokens + 80 feature events
-- ============================================================
-- Password for all users: "password123"
-- SHA-256 hash: ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f
-- ============================================================

-- 1. Insert 5 dummy users (gender_id: 1=Male, 2=Female, 3=Other | age_bucket_id: 1=<18, 2=18-40, 3=>40)
INSERT INTO app."user" (name, email, password_hash, multiple_session_allowed, age, gender_id, age_bucket_id)
VALUES
    ('Alice Johnson',  'alice@example.com',  'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', false, 28, 2, 2),
    ('Bob Williams',   'bob@example.com',    'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', false, 35, 1, 2),
    ('Charlie Brown',  'charlie@example.com','ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', false, 16, 1, 1),
    ('Diana Prince',   'diana@example.com',  'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', false, 45, 2, 3),
    ('Eve Martinez',   'eve@example.com',    'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', false, 22, 3, 2)
ON CONFLICT DO NOTHING;

-- 2. Create auth tokens for each user (expire in 30 days)
INSERT INTO app.auth_token (user_id, token, browser_id, expiry_date)
SELECT u.id, 'seed-token-' || lower(split_part(u.email, '@', 1)), 'seed-browser', now() + interval '30 days'
FROM app."user" u
WHERE u.email IN ('alice@example.com','bob@example.com','charlie@example.com','diana@example.com','eve@example.com')
  AND u.delete_info IS NULL
ON CONFLICT DO NOTHING;

-- 3. Insert 80 feature events spread across the last 7 days
--    Features: 1=date_picker, 2=filter_age, 3=filter_gender, 4=chart_bar
--    Event types: 1=clicked, 2=changed, 3=selected, 4=applied

-- Helper: get user/token pairs into a temp table
CREATE TEMP TABLE seed_ctx AS
SELECT u.id AS user_id, t.id AS auth_token_id, t.browser_id, u.gender_id, u.age_bucket_id
FROM app."user" u
JOIN app.auth_token t ON t.user_id = u.id AND t.token LIKE 'seed-token-%'
WHERE u.email IN ('alice@example.com','bob@example.com','charlie@example.com','diana@example.com','eve@example.com')
  AND u.delete_info IS NULL
  AND t.delete_info IS NULL;

-- Alice (28, Female, 18-40) — heavy date_picker and filter_age user
INSERT INTO app.feature_event (user_id, auth_token_id, feature_id, event_type_id, browser_id, gender_id, age_bucket_id, event_time)
SELECT c.user_id, c.auth_token_id, feat, evt, c.browser_id, c.gender_id, c.age_bucket_id, ts
FROM seed_ctx c, (VALUES
    (1, 1, now() - interval '6 days' + interval '9 hours'),
    (1, 1, now() - interval '6 days' + interval '10 hours'),
    (1, 2, now() - interval '5 days' + interval '11 hours'),
    (2, 2, now() - interval '5 days' + interval '14 hours'),
    (2, 3, now() - interval '4 days' + interval '9 hours'),
    (1, 1, now() - interval '4 days' + interval '15 hours'),
    (1, 1, now() - interval '3 days' + interval '8 hours'),
    (2, 2, now() - interval '3 days' + interval '10 hours'),
    (3, 3, now() - interval '3 days' + interval '16 hours'),
    (4, 4, now() - interval '2 days' + interval '11 hours'),
    (1, 1, now() - interval '2 days' + interval '14 hours'),
    (2, 2, now() - interval '1 day'  + interval '9 hours'),
    (1, 1, now() - interval '1 day'  + interval '13 hours'),
    (4, 4, now() - interval '1 day'  + interval '17 hours'),
    (1, 2, now() - interval '0 days' + interval '8 hours'),
    (3, 3, now() - interval '0 days' + interval '12 hours')
) AS v(feat, evt, ts)
WHERE c.user_id = (SELECT id FROM app."user" WHERE email = 'alice@example.com' AND delete_info IS NULL);

-- Bob (35, Male, 18-40) — heavy chart_bar and filter_gender user
INSERT INTO app.feature_event (user_id, auth_token_id, feature_id, event_type_id, browser_id, gender_id, age_bucket_id, event_time)
SELECT c.user_id, c.auth_token_id, feat, evt, c.browser_id, c.gender_id, c.age_bucket_id, ts
FROM seed_ctx c, (VALUES
    (4, 1, now() - interval '6 days' + interval '8 hours'),
    (4, 1, now() - interval '6 days' + interval '11 hours'),
    (4, 4, now() - interval '5 days' + interval '10 hours'),
    (3, 3, now() - interval '5 days' + interval '15 hours'),
    (3, 3, now() - interval '4 days' + interval '9 hours'),
    (4, 1, now() - interval '4 days' + interval '14 hours'),
    (4, 4, now() - interval '3 days' + interval '10 hours'),
    (3, 3, now() - interval '3 days' + interval '13 hours'),
    (1, 1, now() - interval '2 days' + interval '9 hours'),
    (4, 1, now() - interval '2 days' + interval '16 hours'),
    (4, 4, now() - interval '1 day'  + interval '11 hours'),
    (3, 3, now() - interval '1 day'  + interval '14 hours'),
    (4, 1, now() - interval '0 days' + interval '9 hours'),
    (4, 4, now() - interval '0 days' + interval '15 hours'),
    (1, 2, now() - interval '0 days' + interval '17 hours'),
    (2, 2, now() - interval '0 days' + interval '18 hours')
) AS v(feat, evt, ts)
WHERE c.user_id = (SELECT id FROM app."user" WHERE email = 'bob@example.com' AND delete_info IS NULL);

-- Charlie (16, Male, <18) — mostly clicks on date_picker and chart_bar
INSERT INTO app.feature_event (user_id, auth_token_id, feature_id, event_type_id, browser_id, gender_id, age_bucket_id, event_time)
SELECT c.user_id, c.auth_token_id, feat, evt, c.browser_id, c.gender_id, c.age_bucket_id, ts
FROM seed_ctx c, (VALUES
    (1, 1, now() - interval '6 days' + interval '15 hours'),
    (4, 1, now() - interval '5 days' + interval '16 hours'),
    (1, 1, now() - interval '5 days' + interval '18 hours'),
    (4, 1, now() - interval '4 days' + interval '10 hours'),
    (1, 1, now() - interval '4 days' + interval '19 hours'),
    (4, 4, now() - interval '3 days' + interval '15 hours'),
    (1, 1, now() - interval '3 days' + interval '20 hours'),
    (4, 1, now() - interval '2 days' + interval '16 hours'),
    (1, 2, now() - interval '2 days' + interval '18 hours'),
    (4, 4, now() - interval '1 day'  + interval '15 hours'),
    (1, 1, now() - interval '1 day'  + interval '19 hours'),
    (4, 1, now() - interval '0 days' + interval '10 hours'),
    (1, 1, now() - interval '0 days' + interval '16 hours'),
    (2, 3, now() - interval '0 days' + interval '17 hours')
) AS v(feat, evt, ts)
WHERE c.user_id = (SELECT id FROM app."user" WHERE email = 'charlie@example.com' AND delete_info IS NULL);

-- Diana (45, Female, >40) — filter_age and filter_gender heavy
INSERT INTO app.feature_event (user_id, auth_token_id, feature_id, event_type_id, browser_id, gender_id, age_bucket_id, event_time)
SELECT c.user_id, c.auth_token_id, feat, evt, c.browser_id, c.gender_id, c.age_bucket_id, ts
FROM seed_ctx c, (VALUES
    (2, 3, now() - interval '6 days' + interval '7 hours'),
    (3, 3, now() - interval '6 days' + interval '9 hours'),
    (2, 2, now() - interval '5 days' + interval '8 hours'),
    (3, 3, now() - interval '5 days' + interval '12 hours'),
    (2, 3, now() - interval '4 days' + interval '10 hours'),
    (2, 2, now() - interval '4 days' + interval '14 hours'),
    (3, 3, now() - interval '3 days' + interval '9 hours'),
    (2, 2, now() - interval '3 days' + interval '11 hours'),
    (3, 3, now() - interval '2 days' + interval '8 hours'),
    (2, 3, now() - interval '2 days' + interval '13 hours'),
    (4, 4, now() - interval '1 day'  + interval '10 hours'),
    (2, 2, now() - interval '1 day'  + interval '15 hours'),
    (3, 3, now() - interval '0 days' + interval '9 hours'),
    (2, 3, now() - interval '0 days' + interval '11 hours'),
    (4, 4, now() - interval '0 days' + interval '14 hours'),
    (1, 1, now() - interval '0 days' + interval '16 hours')
) AS v(feat, evt, ts)
WHERE c.user_id = (SELECT id FROM app."user" WHERE email = 'diana@example.com' AND delete_info IS NULL);

-- Eve (22, Other, 18-40) — balanced across all features
INSERT INTO app.feature_event (user_id, auth_token_id, feature_id, event_type_id, browser_id, gender_id, age_bucket_id, event_time)
SELECT c.user_id, c.auth_token_id, feat, evt, c.browser_id, c.gender_id, c.age_bucket_id, ts
FROM seed_ctx c, (VALUES
    (1, 1, now() - interval '6 days' + interval '10 hours'),
    (2, 2, now() - interval '6 days' + interval '13 hours'),
    (3, 3, now() - interval '6 days' + interval '16 hours'),
    (4, 4, now() - interval '5 days' + interval '9 hours'),
    (1, 2, now() - interval '5 days' + interval '12 hours'),
    (2, 3, now() - interval '4 days' + interval '11 hours'),
    (3, 1, now() - interval '4 days' + interval '14 hours'),
    (4, 4, now() - interval '4 days' + interval '17 hours'),
    (1, 1, now() - interval '3 days' + interval '8 hours'),
    (2, 2, now() - interval '3 days' + interval '12 hours'),
    (3, 3, now() - interval '2 days' + interval '10 hours'),
    (4, 1, now() - interval '2 days' + interval '15 hours'),
    (1, 2, now() - interval '1 day'  + interval '9 hours'),
    (2, 3, now() - interval '1 day'  + interval '12 hours'),
    (3, 1, now() - interval '1 day'  + interval '16 hours'),
    (4, 4, now() - interval '0 days' + interval '10 hours'),
    (1, 1, now() - interval '0 days' + interval '13 hours'),
    (2, 2, now() - interval '0 days' + interval '16 hours')
) AS v(feat, evt, ts)
WHERE c.user_id = (SELECT id FROM app."user" WHERE email = 'eve@example.com' AND delete_info IS NULL);

-- Cleanup temp table
DROP TABLE IF EXISTS seed_ctx;

-- 4. Verify counts
SELECT 'users' AS entity, count(*) FROM app."user" WHERE email IN ('alice@example.com','bob@example.com','charlie@example.com','diana@example.com','eve@example.com') AND delete_info IS NULL
UNION ALL
SELECT 'tokens', count(*) FROM app.auth_token WHERE token LIKE 'seed-token-%' AND delete_info IS NULL
UNION ALL
SELECT 'events', count(*) FROM app.feature_event WHERE browser_id = 'seed-browser' AND delete_info IS NULL;
