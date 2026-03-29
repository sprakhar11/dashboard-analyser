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
