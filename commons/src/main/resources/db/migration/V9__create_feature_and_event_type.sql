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
