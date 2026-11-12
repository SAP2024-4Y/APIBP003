-- V1: app_user table only (PR #1 scope)
-- Each subsequent PR adds its own migration file.
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS app_user (
    id       BIGSERIAL    PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(50)  NOT NULL DEFAULT 'USER'
);

INSERT INTO app_user (username, password, role)
VALUES ('demo', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER')
ON CONFLICT (username) DO NOTHING;
