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
