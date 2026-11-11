-- V1: initial schema
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS app_user (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL DEFAULT 'USER'
);

CREATE TABLE IF NOT EXISTS venue (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    address     TEXT,
    location    GEOGRAPHY(Point, 4326) NOT NULL
);

CREATE TABLE IF NOT EXISTS event (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    category    VARCHAR(100) NOT NULL,
    status      VARCHAR(50)  NOT NULL DEFAULT 'ON_SALE',
    venue_id    BIGINT       NOT NULL REFERENCES venue(id),
    description TEXT
);

CREATE TABLE IF NOT EXISTS event_slot (
    id          BIGSERIAL PRIMARY KEY,
    event_id    BIGINT       NOT NULL REFERENCES event(id),
    starts_at   TIMESTAMPTZ  NOT NULL,
    ends_at     TIMESTAMPTZ  NOT NULL,
    capacity    INT          NOT NULL DEFAULT 100,
    booked      INT          NOT NULL DEFAULT 0,
    CONSTRAINT slot_end_after_start CHECK (ends_at > starts_at)
);

CREATE TABLE IF NOT EXISTS ticket_type (
    id          BIGSERIAL PRIMARY KEY,
    slot_id     BIGINT       NOT NULL REFERENCES event_slot(id),
    name        VARCHAR(100) NOT NULL,
    price       NUMERIC(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS booking (
    id          BIGSERIAL PRIMARY KEY,
    slot_id     BIGINT       NOT NULL REFERENCES event_slot(id),
    user_id     BIGINT       NOT NULL REFERENCES app_user(id),
    quantity    INT          NOT NULL DEFAULT 1,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT booking_unique_user_slot UNIQUE (slot_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_event_status   ON event(status);
CREATE INDEX IF NOT EXISTS idx_event_category ON event(category);
CREATE INDEX IF NOT EXISTS idx_slot_window    ON event_slot(starts_at, ends_at);
CREATE INDEX IF NOT EXISTS idx_venue_location ON venue USING GIST(location);
