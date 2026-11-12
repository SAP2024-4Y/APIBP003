-- V4: event_slot + booking tables (PR #4 scope)
CREATE TABLE IF NOT EXISTS event_slot (
    id         BIGSERIAL PRIMARY KEY,
    event_id   BIGINT    NOT NULL REFERENCES event(id),
    starts_at  TIMESTAMPTZ NOT NULL,
    ends_at    TIMESTAMPTZ NOT NULL,
    capacity   INT       NOT NULL CHECK (capacity > 0),
    UNIQUE (event_id, starts_at)
);

CREATE INDEX IF NOT EXISTS idx_slot_event   ON event_slot (event_id);
CREATE INDEX IF NOT EXISTS idx_slot_window  ON event_slot (starts_at, ends_at);

CREATE TABLE IF NOT EXISTS booking (
    id         BIGSERIAL PRIMARY KEY,
    slot_id    BIGINT    NOT NULL REFERENCES event_slot(id),
    user_id    BIGINT    NOT NULL REFERENCES app_user(id),
    booked_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (slot_id, user_id)
);

-- Seed: 2 slots per event (all 20 events from V2 → 40 slots total)
-- Slot 1: 2026-12-01 10:00–13:00 UTC  Slot 2: 2026-12-02 15:00–18:00 UTC
INSERT INTO event_slot (event_id, starts_at, ends_at, capacity)
SELECT e.id,
       '2026-12-01T10:00:00Z'::timestamptz,
       '2026-12-01T13:00:00Z'::timestamptz,
       200
FROM   event e
ON CONFLICT DO NOTHING;

INSERT INTO event_slot (event_id, starts_at, ends_at, capacity)
SELECT e.id,
       '2026-12-02T15:00:00Z'::timestamptz,
       '2026-12-02T18:00:00Z'::timestamptz,
       150
FROM   event e
ON CONFLICT DO NOTHING;
