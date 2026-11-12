# APIBP003 — Events & Ticketing API

Course: <FILL:course code & name> | Section: <FILL:section> | Instructor: <FILL:instructor>

## Team Details

| # | Name          | Roll No              | GitHub ID     | Email                    |
|---|---------------|----------------------|---------------|--------------------------|
| 1 | Dipto Das     | <FILL:rollno-1>      | 2024sl70039   | <FILL:email-1>           |
| 2 | Deepak Singh  | <FILL:rollno-2>      | deepaks1507   | <FILL:email-2>           |
| 3 | Hritam Kar    | <FILL:rollno-3>      | hritamkar     | <FILL:email-3>           |

- Team ID: `APIBP003` (must equal the repository name and `team.json.team_id`)
- Chosen Approach: `A` — REST-First + GraphQL Layer (must equal `team.json.approach`)

## Quick start

```bash
git clone https://github.com/SAP2024-4Y/APIBP003
cd APIBP003
docker compose up
# Server:      http://localhost:8080
# REST docs:   http://localhost:8080/docs
# GraphQL IDE: http://localhost:8080/graphql
```

## Demo credentials

```
username: demo
password: demo-password
```

Obtain a token:
```bash
curl -s -X POST http://localhost:8080/api/v1/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo-password"}'
# → {"token":"<JWT>"}
```

Use the token:
```bash
TOKEN=<JWT from above>

# List events
curl -s "http://localhost:8080/api/v1/events?limit=5" | jq .

# Events by category (with auth)
curl -s "http://localhost:8080/api/v1/events?category=CONCERT&limit=5" \
  -H "Authorization: Bearer $TOKEN" | jq .

# Nearby venues
curl -s "http://localhost:8080/api/v1/events/nearby?latitude=28.6&longitude=77.2&radius_km=10" | jq .

# Calendar slots
curl -s "http://localhost:8080/api/v1/slots?starts_at=2026-12-01T00:00:00Z&ends_at=2026-12-31T23:59:59Z" | jq .
```

## Run tests locally

```bash
mvn verify
```

## Health check

```bash
curl http://localhost:8080/health
# → {"status":"ok"}
```

## Developer workflow

### Adding a new entity (per PR)

Each PR that introduces a new JPA entity follows this flow:

**1. Write the entity class** under `src/main/java/.../model/`

**2. Generate the DDL automatically:**
```bash
mvn -Pgenerate-ddl
# → writes target/schema.sql based on current JPA entities
```

**3. Copy the relevant `CREATE TABLE` statement** from `target/schema.sql` into a new Flyway migration:
```
src/main/resources/db/migration/V2__add_event.sql   # PR #2 example
```
Review the generated SQL, add `IF NOT EXISTS`, tweak constraints as needed, then commit it.

**4. GraphQL types are auto-generated** from `docs/schema.graphql` at build time:
```bash
mvn generate-sources
# → writes Java DTOs to target/generated-sources/graphql/
```
Update `docs/schema.graphql` when you add new types, then run the above — the Java types regenerate automatically on every build.

### Migration naming convention
```
V1__initial_schema.sql      # PR #1 — app_user only
V2__add_event.sql           # PR #2 — event, ticket_type
V3__add_venue.sql           # PR #3 — venue + venue_id FK on event
V4__add_slot_booking.sql    # PR #4 — event_slot, booking
```
Always use `IF NOT EXISTS` so `docker compose down && up` never crashes on existing tables.