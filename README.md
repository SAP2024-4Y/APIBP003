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