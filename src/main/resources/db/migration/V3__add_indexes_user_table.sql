CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SCHEMA IF NOT EXISTS users;
SET search_path TO users;

CREATE INDEX IF NOT EXISTS idx_users_user_name ON users (user_name);
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_kcid ON users (kcid);
CREATE INDEX IF NOT EXISTS idx_users_role ON users (role);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users (created_at);