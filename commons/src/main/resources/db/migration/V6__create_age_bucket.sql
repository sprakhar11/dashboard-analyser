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
