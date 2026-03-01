CREATE EXTENSION IF NOT EXISTS pgcrypto;

SET search_path TO users;

alter table users add column provisioning_status varchar(100);