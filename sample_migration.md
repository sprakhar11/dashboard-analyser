# Flyway SQL Files: V4 to V12

## Assumptions

These migrations assume:

- `V1` to `V3` already created:
  - `app` schema
  - `app."user"`
  - `app.auth_token`
- `app."user"` already contains:
  - `id`
  - `username`
  - `password_hash`
  - `age`
  - `gender`
  - `delete_info`
- `app.auth_token` already contains:
  - `id`
  - `user_id`
  - `token`
  - `add_date`
  - `log_out_date`
  - `expiry_date`
  - `delete_info`

These migrations extend auth and add tracking + analytics storage.

---

## V4__alter_user_add_multiple_session_allowed.sql

```sql
create schema if not exists app;

alter table app."user"
    add column if not exists multiple_session_allowed boolean not null default false;
```

---

## V5__create_gender.sql

```sql
create schema if not exists app;

create table if not exists app.gender (
    id smallserial primary key,
    code varchar(20) not null,
    name varchar(30) not null,
    sort_order smallint not null,
    delete_info jsonb null
);

create unique index if not exists ux_gender_code_active
    on app.gender (code)
    where delete_info is null;

create unique index if not exists ux_gender_name_active
    on app.gender ((lower(name)))
    where delete_info is null;

create index if not exists ix_gender_sort_order_active
    on app.gender (sort_order)
    where delete_info is null;

insert into app.gender (code, name, sort_order, delete_info)
values
    ('MALE', 'Male', 1, null),
    ('FEMALE', 'Female', 2, null),
    ('OTHER', 'Other', 3, null)
on conflict do nothing;
```

---

## V6__create_age_bucket.sql

```sql
create schema if not exists app;

create table if not exists app.age_bucket (
    id smallserial primary key,
    code varchar(20) not null,
    name varchar(30) not null,
    min_age integer null,
    max_age integer null,
    sort_order smallint not null,
    delete_info jsonb null
);

create unique index if not exists ux_age_bucket_code_active
    on app.age_bucket (code)
    where delete_info is null;

create unique index if not exists ux_age_bucket_name_active
    on app.age_bucket ((lower(name)))
    where delete_info is null;

create index if not exists ix_age_bucket_sort_order_active
    on app.age_bucket (sort_order)
    where delete_info is null;

insert into app.age_bucket (code, name, min_age, max_age, sort_order, delete_info)
values
    ('LT_18', '<18', null, 17, 1, null),
    ('BETWEEN_18_40', '18-40', 18, 40, 2, null),
    ('GT_40', '>40', 41, null, 3, null)
on conflict do nothing;
```

---

## V7__alter_user_add_dimension_columns.sql

```sql
create schema if not exists app;

alter table app."user"
    add column if not exists gender_id smallint;

alter table app."user"
    add column if not exists age_bucket_id smallint;

update app."user" u
set gender_id = g.id
from app.gender g
where u.gender_id is null
  and u.gender is not null
  and lower(trim(u.gender)) = lower(g.name)
  and g.delete_info is null;

update app."user" u
set age_bucket_id = ab.id
from app.age_bucket ab
where u.age_bucket_id is null
  and (
        (ab.min_age is null or u.age >= ab.min_age)
    and (ab.max_age is null or u.age <= ab.max_age)
  )
  and ab.delete_info is null;

alter table app."user"
    alter column gender_id set not null;

alter table app."user"
    alter column age_bucket_id set not null;

alter table app."user"
    add constraint fk_user_gender
    foreign key (gender_id) references app.gender(id);

alter table app."user"
    add constraint fk_user_age_bucket
    foreign key (age_bucket_id) references app.age_bucket(id);

create index if not exists ix_user_gender_id_active
    on app."user" (gender_id)
    where delete_info is null;

create index if not exists ix_user_age_bucket_id_active
    on app."user" (age_bucket_id)
    where delete_info is null;
```

---

## V8__alter_auth_token_add_browser_id.sql

```sql
create schema if not exists app;

alter table app.auth_token
    add column if not exists browser_id varchar(100);

update app.auth_token
set browser_id = 'UNKNOWN'
where browser_id is null;

alter table app.auth_token
    alter column browser_id set not null;

create unique index if not exists ux_auth_token_token_active
    on app.auth_token (token)
    where delete_info is null;

create index if not exists ix_auth_token_user_id_active
    on app.auth_token (user_id)
    where delete_info is null;

create index if not exists ix_auth_token_user_browser_active
    on app.auth_token (user_id, browser_id)
    where delete_info is null;

create index if not exists ix_auth_token_expiry_date_active
    on app.auth_token (expiry_date)
    where delete_info is null;

create index if not exists ix_auth_token_user_browser_expiry_active
    on app.auth_token (user_id, browser_id, expiry_date)
    where delete_info is null;
```

---

## V9__create_feature_and_event_type.sql

```sql
create schema if not exists app;

create table if not exists app.feature (
    id smallserial primary key,
    name varchar(100) not null,
    delete_info jsonb null
);

create table if not exists app.event_type (
    id smallserial primary key,
    name varchar(50) not null,
    delete_info jsonb null
);

create unique index if not exists ux_feature_name_active
    on app.feature ((lower(name)))
    where delete_info is null;

create unique index if not exists ux_event_type_name_active
    on app.event_type ((lower(name)))
    where delete_info is null;

insert into app.feature (name, delete_info)
values
    ('date_picker', null),
    ('filter_age', null),
    ('filter_gender', null),
    ('chart_bar', null)
on conflict do nothing;

insert into app.event_type (name, delete_info)
values
    ('clicked', null),
    ('changed', null),
    ('selected', null),
    ('applied', null)
on conflict do nothing;
```

---

## V10__create_feature_event.sql

```sql
create schema if not exists app;

create table if not exists app.feature_event (
    id bigserial primary key,
    user_id bigint not null,
    auth_token_id bigint not null,
    feature_id smallint not null,
    event_type_id smallint not null,
    browser_id varchar(100) not null,
    gender_id smallint not null,
    age_bucket_id smallint not null,
    event_time timestamptz not null default now(),
    event_date date generated always as ((timezone('UTC', event_time))::date) stored,
    event_hour smallint generated always as ((extract(hour from timezone('UTC', event_time)))::smallint) stored,
    meta_info jsonb null,
    delete_info jsonb null,
    constraint fk_feature_event_user
        foreign key (user_id) references app."user"(id),
    constraint fk_feature_event_auth_token
        foreign key (auth_token_id) references app.auth_token(id),
    constraint fk_feature_event_feature
        foreign key (feature_id) references app.feature(id),
    constraint fk_feature_event_event_type
        foreign key (event_type_id) references app.event_type(id),
    constraint fk_feature_event_gender
        foreign key (gender_id) references app.gender(id),
    constraint fk_feature_event_age_bucket
        foreign key (age_bucket_id) references app.age_bucket(id)
);

create index if not exists ix_feature_event_user_event_time_active
    on app.feature_event (user_id, event_time desc)
    where delete_info is null;

create index if not exists ix_feature_event_auth_token_active
    on app.feature_event (auth_token_id)
    where delete_info is null;

create index if not exists ix_feature_event_feature_date_age_gender_active
    on app.feature_event (feature_id, event_date, age_bucket_id, gender_id)
    where delete_info is null;

create index if not exists ix_feature_event_date_age_gender_active
    on app.feature_event (event_date, age_bucket_id, gender_id)
    where delete_info is null;

create index if not exists ix_feature_event_browser_date_active
    on app.feature_event (browser_id, event_date)
    where delete_info is null;

create index if not exists ix_feature_event_event_type_date_active
    on app.feature_event (event_type_id, event_date)
    where delete_info is null;

create index if not exists ix_feature_event_event_time_brin_active
    on app.feature_event using brin (event_time)
    where delete_info is null;
```

---

## V11__create_feature_event_daily_agg.sql

```sql
create schema if not exists app;

create table if not exists app.feature_event_daily_agg (
    id bigserial primary key,
    event_date date not null,
    feature_id smallint not null,
    gender_id smallint not null,
    age_bucket_id smallint not null,
    total_count bigint not null default 0,
    delete_info jsonb null,
    constraint fk_feature_event_daily_agg_feature
        foreign key (feature_id) references app.feature(id),
    constraint fk_feature_event_daily_agg_gender
        foreign key (gender_id) references app.gender(id),
    constraint fk_feature_event_daily_agg_age_bucket
        foreign key (age_bucket_id) references app.age_bucket(id)
);

create unique index if not exists ux_feature_event_daily_agg_active
    on app.feature_event_daily_agg (event_date, feature_id, gender_id, age_bucket_id)
    where delete_info is null;

create index if not exists ix_feature_event_daily_agg_filter_active
    on app.feature_event_daily_agg (age_bucket_id, gender_id, event_date, feature_id)
    where delete_info is null;

create index if not exists ix_feature_event_daily_agg_feature_date_active
    on app.feature_event_daily_agg (feature_id, event_date)
    where delete_info is null;
```

---

## V12__create_feature_event_hourly_agg.sql

```sql
create schema if not exists app;

create table if not exists app.feature_event_hourly_agg (
    id bigserial primary key,
    event_date date not null,
    event_hour smallint not null,
    feature_id smallint not null,
    gender_id smallint not null,
    age_bucket_id smallint not null,
    total_count bigint not null default 0,
    delete_info jsonb null,
    constraint fk_feature_event_hourly_agg_feature
        foreign key (feature_id) references app.feature(id),
    constraint fk_feature_event_hourly_agg_gender
        foreign key (gender_id) references app.gender(id),
    constraint fk_feature_event_hourly_agg_age_bucket
        foreign key (age_bucket_id) references app.age_bucket(id)
);

create unique index if not exists ux_feature_event_hourly_agg_active
    on app.feature_event_hourly_agg (event_date, event_hour, feature_id, gender_id, age_bucket_id)
    where delete_info is null;

create index if not exists ix_feature_event_hourly_agg_feature_filter_active
    on app.feature_event_hourly_agg (feature_id, age_bucket_id, gender_id, event_date, event_hour)
    where delete_info is null;

create index if not exists ix_feature_event_hourly_agg_filter_active
    on app.feature_event_hourly_agg (age_bucket_id, gender_id, event_date, event_hour, feature_id)
    where delete_info is null;
```

---

## Notes for service/query layer

### 1. Active row rule
All hot queries must include:

```sql
delete_info is null
```

so PostgreSQL can use the partial indexes.

### 2. Track API write flow
For `POST /track`:

- insert into `app.feature_event`
- upsert into `app.feature_event_daily_agg`
- upsert into `app.feature_event_hourly_agg`

### 3. Example upsert for daily agg

```sql
insert into app.feature_event_daily_agg (
    event_date,
    feature_id,
    gender_id,
    age_bucket_id,
    total_count,
    delete_info
)
values (
    #{eventDate},
    #{featureId},
    #{genderId},
    #{ageBucketId},
    1,
    null
)
on conflict (event_date, feature_id, gender_id, age_bucket_id)
where delete_info is null
do update set total_count = app.feature_event_daily_agg.total_count + 1;
```

### 4. Example upsert for hourly agg

```sql
insert into app.feature_event_hourly_agg (
    event_date,
    event_hour,
    feature_id,
    gender_id,
    age_bucket_id,
    total_count,
    delete_info
)
values (
    #{eventDate},
    #{eventHour},
    #{featureId},
    #{genderId},
    #{ageBucketId},
    1,
    null
)
on conflict (event_date, event_hour, feature_id, gender_id, age_bucket_id)
where delete_info is null
do update set total_count = app.feature_event_hourly_agg.total_count + 1;
```

### 5. Example bar chart query

```sql
select
    f.id as feature_id,
    f.name as feature_name,
    sum(d.total_count) as total_count
from app.feature_event_daily_agg d
join app.feature f
    on f.id = d.feature_id
   and f.delete_info is null
where d.delete_info is null
  and d.event_date between #{fromDate} and #{toDate}
  and (#{ageBucketId} is null or d.age_bucket_id = #{ageBucketId})
  and (#{genderId} is null or d.gender_id = #{genderId})
group by f.id, f.name
order by total_count desc, f.id asc;
```

### 6. Example line chart query

```sql
select
    make_timestamp(
        extract(year from h.event_date)::int,
        extract(month from h.event_date)::int,
        extract(day from h.event_date)::int,
        h.event_hour::int,
        0,
        0
    ) at time zone 'UTC' as bucket_time,
    h.total_count as total_count
from app.feature_event_hourly_agg h
where h.delete_info is null
  and h.feature_id = #{featureId}
  and h.event_date between #{fromDate} and #{toDate}
  and (#{ageBucketId} is null or h.age_bucket_id = #{ageBucketId})
  and (#{genderId} is null or h.gender_id = #{genderId})
order by bucket_time asc;
```