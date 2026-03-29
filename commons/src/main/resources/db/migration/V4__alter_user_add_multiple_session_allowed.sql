create schema if not exists app;

alter table app."user"
    add column if not exists multiple_session_allowed boolean not null default false;
