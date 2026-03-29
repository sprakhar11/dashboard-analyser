create schema if not exists app;

alter table app."user"
    add column if not exists age integer;
