-- V3: venue table + venue_id FK on event (PR #3 scope)
CREATE TABLE IF NOT EXISTS venue (
    id       BIGSERIAL    PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    address  TEXT         NOT NULL,
    location geography(Point, 4326) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_venue_location ON venue USING GIST (location);

ALTER TABLE event ADD COLUMN IF NOT EXISTS venue_id BIGINT REFERENCES venue(id);

-- Seed: 25 venues across Indian cities with real coordinates
INSERT INTO venue (name, address, location) VALUES
  ('Indira Gandhi Arena',        'Pragati Maidan, New Delhi',          ST_SetSRID(ST_MakePoint(77.2090, 28.6139), 4326)),
  ('JLN Stadium',                'Lodhi Road, New Delhi',              ST_SetSRID(ST_MakePoint(77.2373, 28.5832), 4326)),
  ('Talkatora Stadium',          'Talkatora Rd, New Delhi',            ST_SetSRID(ST_MakePoint(77.2025, 28.6271), 4326)),
  ('Siri Fort Auditorium',       'Siri Fort, New Delhi',               ST_SetSRID(ST_MakePoint(77.2167, 28.5494), 4326)),
  ('Kingdom of Dreams',          'Sector 29, Gurugram',                ST_SetSRID(ST_MakePoint(77.0598, 28.4595), 4326)),
  ('Bandra Kurla Complex',       'BKC, Mumbai',                        ST_SetSRID(ST_MakePoint(72.8680, 19.0607), 4326)),
  ('Wankhede Stadium',           'Marine Lines, Mumbai',               ST_SetSRID(ST_MakePoint(72.8252, 18.9388), 4326)),
  ('MMRDA Grounds',              'BKC, Mumbai',                        ST_SetSRID(ST_MakePoint(72.8656, 19.0633), 4326)),
  ('Nehru Centre',               'Worli, Mumbai',                      ST_SetSRID(ST_MakePoint(72.8196, 19.0131), 4326)),
  ('Phoenix Palladium',          'Lower Parel, Mumbai',                ST_SetSRID(ST_MakePoint(72.8296, 18.9967), 4326)),
  ('Palace Grounds',             'Mekhri Circle, Bengaluru',           ST_SetSRID(ST_MakePoint(77.5915, 13.0031), 4326)),
  ('NIMHANS Convention Centre',  'Hosur Rd, Bengaluru',                ST_SetSRID(ST_MakePoint(77.5946, 12.9352), 4326)),
  ('Chinnaswamy Stadium',        'MG Road, Bengaluru',                 ST_SetSRID(ST_MakePoint(77.5993, 12.9791), 4326)),
  ('Phoenix Marketcity',         'Whitefield, Bengaluru',              ST_SetSRID(ST_MakePoint(77.7480, 12.9958), 4326)),
  ('ITC Grand Chola',            'Mount Road, Chennai',                ST_SetSRID(ST_MakePoint(80.2205, 13.0067), 4326)),
  ('YMCA Grounds',               'Nandanam, Chennai',                  ST_SetSRID(ST_MakePoint(80.2524, 13.0228), 4326)),
  ('Chennai Trade Centre',       'Nandambakkam, Chennai',              ST_SetSRID(ST_MakePoint(80.1784, 13.0101), 4326)),
  ('Biswa Bangla Convention',    'New Town, Kolkata',                  ST_SetSRID(ST_MakePoint(88.4623, 22.5726), 4326)),
  ('Eden Gardens',               'BBD Bagh, Kolkata',                  ST_SetSRID(ST_MakePoint(88.3432, 22.5647), 4326)),
  ('Science City Grounds',       'EM Bypass, Kolkata',                 ST_SetSRID(ST_MakePoint(88.3994, 22.5103), 4326)),
  ('Hyderabad International',    'Madhapur, Hyderabad',                ST_SetSRID(ST_MakePoint(78.3809, 17.4310), 4326)),
  ('HITEX Exhibition Centre',    'Kondapur, Hyderabad',                ST_SetSRID(ST_MakePoint(78.3337, 17.4600), 4326)),
  ('Lal Bahadur Stadium',        'Fateh Maidan, Hyderabad',            ST_SetSRID(ST_MakePoint(78.4717, 17.3850), 4326)),
  ('Jawaharlal Nehru Stadium',   'Kochi',                              ST_SetSRID(ST_MakePoint(76.2711, 9.9312),  4326)),
  ('Brabourne Stadium',          'Marine Lines, Mumbai',               ST_SetSRID(ST_MakePoint(72.8252, 18.9388), 4326));

-- Link events to venues (round-robin so every event has a non-null venue)
UPDATE event SET venue_id = (
    SELECT v.id FROM venue v
    ORDER BY v.id
    LIMIT 1 OFFSET ((event.id - 1) % 25)
)
WHERE venue_id IS NULL;
