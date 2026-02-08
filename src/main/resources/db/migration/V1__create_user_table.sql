CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SCHEMA IF NOT EXISTS users;
SET search_path TO users;

CREATE TABLE users (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    avatar varchar(200),
    fist_name varchar(200),
    last_name varchar(200),
    user_name varchar(200) NOT NULL ,
    email varchar(200) NOT NULL UNIQUE,
    birth_date date,
    phone_number varchar(50),
    created_at timestamp default current_timestamp,
    kcId uuid
);
