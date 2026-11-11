-- V2: seed 20+ venues + events + slots (idempotent via ON CONFLICT DO NOTHING)

-- Venues with PostGIS geography points [lon, lat]
INSERT INTO venue (id, name, address, location) VALUES
  (1,  'Jawaharlal Nehru Stadium',     'Lodhi Road, New Delhi',           ST_GeographyFromText('SRID=4326;POINT(77.2373 28.5665)')),
  (2,  'Indira Gandhi Arena',          'Indraprastha Estate, New Delhi',   ST_GeographyFromText('SRID=4326;POINT(77.2517 28.6285)')),
  (3,  'NSCI Dome',                    'Worli, Mumbai',                    ST_GeographyFromText('SRID=4326;POINT(72.8186 19.0116)')),
  (4,  'Andheri Sports Complex',       'Andheri East, Mumbai',             ST_GeographyFromText('SRID=4326;POINT(72.8697 19.1177)')),
  (5,  'Kanteerava Stadium',           'Kasturba Road, Bengaluru',         ST_GeographyFromText('SRID=4326;POINT(77.5966 12.9757)')),
  (6,  'Chinnaswamy Stadium',          'MG Road, Bengaluru',               ST_GeographyFromText('SRID=4326;POINT(77.5992 12.9788)')),
  (7,  'Netaji Indoor Stadium',        'Eden Gardens, Kolkata',            ST_GeographyFromText('SRID=4326;POINT(88.3426 22.5645)')),
  (8,  'Salt Lake Stadium',            'Bidhannagar, Kolkata',             ST_GeographyFromText('SRID=4326;POINT(88.3831 22.5778)')),
  (9,  'Rajiv Gandhi Indoor Stadium',  'Kundannoor, Kochi',                ST_GeographyFromText('SRID=4326;POINT(76.3110 9.9781)')),
  (10, 'Nehru Stadium Kochi',          'Kaloor, Kochi',                    ST_GeographyFromText('SRID=4326;POINT(76.2885 9.9973)')),
  (11, 'HICC Hyderabad',               'Madhapur, Hyderabad',              ST_GeographyFromText('SRID=4326;POINT(78.3810 17.4353)')),
  (12, 'Gachibowli Stadium',           'Gachibowli, Hyderabad',            ST_GeographyFromText('SRID=4326;POINT(78.3487 17.4400)')),
  (13, 'Chennai Trade Centre',         'Nandambakkam, Chennai',            ST_GeographyFromText('SRID=4326;POINT(80.1627 13.0067)')),
  (14, 'Jawaharlal Nehru Stadium Chennai', 'Periamet, Chennai',            ST_GeographyFromText('SRID=4326;POINT(80.2693 13.0843)')),
  (15, 'Sardar Patel Stadium',         'Motera, Ahmedabad',                ST_GeographyFromText('SRID=4326;POINT(72.6015 23.0921)'))
ON CONFLICT (id) DO NOTHING;

-- Events
INSERT INTO event (id, title, category, status, venue_id, description) VALUES
  (1,  'Arijit Singh Live',            'CONCERT',    'ON_SALE',   1,  'An evening with Arijit Singh'),
  (2,  'Pro Kabaddi Finals',           'SPORTS',     'ON_SALE',   2,  'Pro Kabaddi League final match'),
  (3,  'Coldplay World Tour Mumbai',   'CONCERT',    'SOLD_OUT',  3,  'Coldplay Music of the Spheres'),
  (4,  'Mumbai Marathon',              'SPORTS',     'ON_SALE',   4,  'Annual Mumbai Marathon'),
  (5,  'Tech Summit Bangalore',        'CONFERENCE', 'ON_SALE',   5,  'Annual developer conference'),
  (6,  'IPL Playoffs RCB',             'SPORTS',     'ON_SALE',   6,  'IPL playoff match'),
  (7,  'Durga Puja Cultural Night',    'CULTURAL',   'ON_SALE',   7,  'Cultural performances'),
  (8,  'ISL East Bengal vs ATKMB',     'SPORTS',     'ON_SALE',   8,  'ISL derby match'),
  (9,  'Kochi Jazz Fest',              'CONCERT',    'ON_SALE',   9,  'International jazz artists'),
  (10, 'Kerala Blasters Open Training','SPORTS',     'DRAFT',     10, 'Open training session'),
  (11, 'Hyderabad Comic Con',          'EXPO',       'ON_SALE',   11, 'Pop culture expo'),
  (12, 'Hyderabad Half Marathon',      'SPORTS',     'ON_SALE',   12, 'Half marathon through the city'),
  (13, 'Chennai Music Festival',       'CULTURAL',   'ON_SALE',   13, 'Classical Carnatic music'),
  (14, 'Chennai Super Kings Farewell', 'SPORTS',     'SOLD_OUT',  14, 'CSK legends match'),
  (15, 'Narendra Modi Stadium T20',    'SPORTS',     'ON_SALE',   15, 'India vs Australia T20'),
  (16, 'Delhi Literature Festival',    'CONFERENCE', 'ON_SALE',   1,  'Annual lit fest'),
  (17, 'Bengaluru Food Expo',          'EXPO',       'ON_SALE',   5,  'Food & beverage expo'),
  (18, 'Kolkata Film Festival',        'CULTURAL',   'ON_SALE',   7,  'International film screenings'),
  (19, 'New Year Countdown Kochi',     'CONCERT',    'ON_SALE',   9,  'New Year live concert'),
  (20, 'Hyderabad Tech Hackathon',     'CONFERENCE', 'ON_SALE',   11, '48-hour hackathon'),
  (21, 'Pink Floyd Tribute Night',     'CONCERT',    'CANCELLED', 3,  'Tribute concert'),
  (22, 'Ahmedabad Kite Festival',      'CULTURAL',   'ON_SALE',   15, 'Makar Sankranti kite fest')
ON CONFLICT (id) DO NOTHING;

-- Event slots (every event gets at least one slot with coords non-null via venue)
INSERT INTO event_slot (id, event_id, starts_at, ends_at, capacity, booked) VALUES
  (1,  1,  '2026-12-05T18:00:00Z', '2026-12-05T22:00:00Z', 50000, 42000),
  (2,  2,  '2026-12-10T15:00:00Z', '2026-12-10T18:00:00Z', 15000, 14000),
  (3,  3,  '2026-12-12T19:00:00Z', '2026-12-12T23:00:00Z', 80000, 80000),
  (4,  4,  '2027-01-15T05:00:00Z', '2027-01-15T12:00:00Z', 40000, 38000),
  (5,  5,  '2026-12-18T09:00:00Z', '2026-12-18T18:00:00Z', 3000,  1500),
  (6,  6,  '2026-12-20T14:00:00Z', '2026-12-20T18:00:00Z', 40000, 35000),
  (7,  7,  '2026-12-25T17:00:00Z', '2026-12-25T22:00:00Z', 10000, 7000),
  (8,  8,  '2026-12-27T16:00:00Z', '2026-12-27T19:00:00Z', 60000, 55000),
  (9,  9,  '2026-12-28T19:00:00Z', '2026-12-28T23:00:00Z', 5000,  3200),
  (10, 10, '2027-01-05T10:00:00Z', '2027-01-05T12:00:00Z', 500,   0),
  (11, 11, '2026-12-13T10:00:00Z', '2026-12-13T20:00:00Z', 8000,  6500),
  (12, 11, '2026-12-14T10:00:00Z', '2026-12-14T20:00:00Z', 8000,  5000),
  (13, 12, '2027-01-10T05:30:00Z', '2027-01-10T09:30:00Z', 20000, 18000),
  (14, 13, '2026-12-15T17:00:00Z', '2026-12-15T21:00:00Z', 2000,  1800),
  (15, 14, '2026-12-22T14:00:00Z', '2026-12-22T18:00:00Z', 50000, 50000),
  (16, 15, '2026-12-08T14:00:00Z', '2026-12-08T19:00:00Z', 100000,78000),
  (17, 16, '2026-12-19T10:00:00Z', '2026-12-19T18:00:00Z', 1000,  450),
  (18, 17, '2027-01-20T10:00:00Z', '2027-01-20T19:00:00Z', 5000,  2200),
  (19, 18, '2026-12-11T11:00:00Z', '2026-12-11T23:00:00Z', 3000,  2700),
  (20, 19, '2026-12-31T20:00:00Z', '2027-01-01T01:00:00Z', 8000,  7500),
  (21, 20, '2027-01-17T09:00:00Z', '2027-01-19T18:00:00Z', 200,   180),
  (22, 21, '2026-12-01T19:00:00Z', '2026-12-01T23:00:00Z', 10000, 0),
  (23, 22, '2027-01-14T07:00:00Z', '2027-01-14T18:00:00Z', 100000,50000)
ON CONFLICT (id) DO NOTHING;

-- Ticket types (price range data for min_price/max_price filters)
INSERT INTO ticket_type (slot_id, name, price) VALUES
  (1,  'General',   999.00),
  (1,  'Premium',  2999.00),
  (1,  'VIP',      7999.00),
  (2,  'Standard', 500.00),
  (2,  'Premium',  1500.00),
  (5,  'Standard', 4999.00),
  (5,  'Workshop', 1999.00),
  (6,  'General',  800.00),
  (6,  'Premium',  2500.00),
  (9,  'General',  1200.00),
  (9,  'VIP',      4500.00),
  (11, 'Entry',    399.00),
  (11, 'Premium',  999.00),
  (16, 'General',  1500.00),
  (16, 'Premium',  5000.00),
  (16, 'VIP',      15000.00)
ON CONFLICT DO NOTHING;

-- Reset sequences
SELECT setval('venue_id_seq',      (SELECT MAX(id) FROM venue));
SELECT setval('event_id_seq',      (SELECT MAX(id) FROM event));
SELECT setval('event_slot_id_seq', (SELECT MAX(id) FROM event_slot));
