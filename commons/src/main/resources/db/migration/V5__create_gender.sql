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
