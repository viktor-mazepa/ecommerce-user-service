CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SCHEMA IF NOT EXISTS users;
SET search_path TO users;

alter table users add column role varchar(50);
alter table users add column updated_at timestamp;
