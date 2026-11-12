-- V2: event + ticket_type tables + seed (PR #2 scope)
CREATE TABLE IF NOT EXISTS event (
    id          BIGSERIAL    PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    category    VARCHAR(100) NOT NULL,
    status      VARCHAR(50)  NOT NULL DEFAULT 'ON_SALE',
    description TEXT
);

CREATE TABLE IF NOT EXISTS ticket_type (
    id       BIGSERIAL      PRIMARY KEY,
    event_id BIGINT         NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    name     VARCHAR(100)   NOT NULL,
    price    NUMERIC(10, 2) NOT NULL
);

-- Seed: 25 events across 5 categories, all ON_SALE unless noted
INSERT INTO event (title, category, status, description) VALUES
  ('Rock Fest 2026',          'MUSIC',       'ON_SALE',   'Annual rock music festival'),
  ('Jazz Nights Vol. 3',      'MUSIC',       'ON_SALE',   'Smooth jazz under the stars'),
  ('Classical Evening',       'MUSIC',       'SOLD_OUT',  'Symphony orchestra performance'),
  ('EDM Underground',         'MUSIC',       'ON_SALE',   'Electronic dance music event'),
  ('Folk & Roots Fair',       'MUSIC',       'ON_SALE',   'Celebrating folk traditions'),
  ('Premier League Watch',    'SPORTS',      'ON_SALE',   'Live screening with big screens'),
  ('City Marathon 2026',      'SPORTS',      'ON_SALE',   '42 km through the city'),
  ('Tennis Open Finals',      'SPORTS',      'SOLD_OUT',  'Grand slam final match'),
  ('Basketball Showdown',     'SPORTS',      'ON_SALE',   'Inter-city basketball clash'),
  ('Cricket T20 Night',       'SPORTS',      'ON_SALE',   'T20 match under floodlights'),
  ('Dev Summit 2026',         'CONFERENCE',  'ON_SALE',   'Software engineering conference'),
  ('AI & ML Expo',            'CONFERENCE',  'ON_SALE',   'Artificial intelligence showcase'),
  ('Product Design Days',     'CONFERENCE',  'ON_SALE',   'UX and product design talks'),
  ('Cloud Native Con',        'CONFERENCE',  'CANCELLED', 'Cloud infrastructure conference'),
  ('Open Source Summit',      'CONFERENCE',  'ON_SALE',   'Community open source event'),
  ('Stand-Up Comedy Night',   'ARTS',        'ON_SALE',   'Live comedy performances'),
  ('Contemporary Dance Show', 'ARTS',        'ON_SALE',   'Modern dance ensemble'),
  ('Street Art Festival',     'ARTS',        'ON_SALE',   'Urban street art exhibition'),
  ('Theatre Improv Night',    'ARTS',        'SOLD_OUT',  'Improvised theatre show'),
  ('Opera Gala',              'ARTS',        'DRAFT',     'Grand opera performance'),
  ('Food & Wine Festival',    'FOOD',        'ON_SALE',   'Culinary arts showcase'),
  ('Street Food Carnival',    'FOOD',        'ON_SALE',   'International street food'),
  ('Craft Beer Fest',         'FOOD',        'ON_SALE',   'Artisan brews and tastings'),
  ('Farm to Table Dinner',    'FOOD',        'SOLD_OUT',  'Exclusive farm dining experience'),
  ('Vegan Food Fair',         'FOOD',        'ON_SALE',   'Plant-based food celebration')
ON CONFLICT DO NOTHING;

-- One ticket type per event so min_price/max_price filters return data
INSERT INTO ticket_type (event_id, name, price)
SELECT e.id, 'General', (500 + (e.id * 73) % 4500)::numeric(10,2)
FROM event e
ON CONFLICT DO NOTHING;

-- Second tier for some events
INSERT INTO ticket_type (event_id, name, price)
SELECT e.id, 'VIP', (2000 + (e.id * 137) % 8000)::numeric(10,2)
FROM event e WHERE e.id % 3 = 0
ON CONFLICT DO NOTHING;
