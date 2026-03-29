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
