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
