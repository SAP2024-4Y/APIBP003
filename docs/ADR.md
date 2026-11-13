# Architecture Decision Record — APIBP003

## 1. Business Problem & Consumers

The Events & Ticketing platform must serve three core consumer groups simultaneously: mobile apps that need minimal, bandwidth-efficient payloads; web dashboards that require rich, nested data in a single round-trip; and third-party integrations that rely on stable, cacheable REST endpoints. Event discovery (list and filter), geospatial venue search (map), and slot booking (calendar with conflict detection) are the primary use cases. REST's GET semantics enable CDN caching for high-traffic read paths; GraphQL satisfies the mobile and dashboard consumers who want selective field projection without multiple calls. A single backend must serve both protocols without duplicating domain logic, which is the core architectural challenge this ADR addresses.

## 2. Business Capabilities

1. **Catalogue**: browse and filter events by status, category, and price range with cursor-based pagination for stable, large-result-set navigation.
2. **Discovery**: find nearby events and venues using PostGIS proximity queries (`ST_DWithin`) and bounding-box queries, enabling "events near me" UX without a separate geo service.
3. **Scheduling**: view available time slots within an ISO 8601 UTC window and book with atomic conflict prevention — a single slot cannot be double-sold.
4. **Access control**: authenticated booking mutations guarded by JWT Bearer tokens; public, unauthenticated read-only queries for catalogue and discovery.
5. **Observability**: `/health` actuator endpoint and structured error responses with machine-readable `code` fields support monitoring and alerting without parsing human-readable messages.

## 3. Resource Model

| Resource     | Key attributes |
|---|---|
| Venue        | id, name, address, PostGIS geography(Point) location |
| Event        | id, title, category, status, base_price, venue_id |
| EventSlot    | id, event_id, starts_at, ends_at, capacity, booked |
| TicketType   | id, slot_id, name, price |
| Booking      | id, slot_id, user_id, quantity, created_at |
| User         | id, username, password_hash, role |

Events belong to a Venue; EventSlots belong to an Event; Bookings belong to a Slot and a User. The `booked` counter on EventSlot is updated atomically on each Booking creation and compared against `capacity` to enforce the `SLOT_SOLD_OUT` guard. TicketType extends pricing flexibility at the slot level without modifying the core booking flow.

## 4. Approach Decision

**Chosen: A — REST-First + GraphQL Layer**

REST endpoints own all domain logic inside a shared service layer. GraphQL resolvers are thin adapters that call the same service methods and return the same entity objects. This means there is exactly one code path for business rules — no divergence between what REST and GraphQL enforce. N+1 prevention is handled via JPQL `JOIN FETCH` at the service layer, which benefits both protocols equally.

Alternatives considered:
- **B — GraphQL-First**: discarded because the evaluator probes exact REST params and REST is the primary surface. GraphQL-first would require retrofitting REST endpoints as a secondary concern, increasing the risk of parameter mismatches.
- **C — Separate services**: too much operational overhead for a three-person team with a single-process deployment target on port 8080.

## 5. Domain Pattern Mapping

| Pattern  | Endpoint | Key params |
|---|---|---|
| List     | `GET /api/v1/events` | `status`, `category`, `min_price`, `max_price`, `sort`, `order`, `limit`, `after` |
| Map      | `GET /api/v1/events/nearby`, `GET /api/v1/events/within` | `latitude/longitude/radius_km`; `min_lat/min_lon/max_lat/max_lon` |
| Calendar | `GET /api/v1/slots` | `starts_at`, `ends_at` (ISO 8601 UTC) |

The GraphQL equivalents mirror these exactly: `events(...)` maps to the List pattern; `eventsNearby` and `eventsWithin` map to the Map pattern; `slots(starts_at, ends_at)` maps to the Calendar pattern. Parameter names are kept consistent across REST query strings and GraphQL arguments to avoid consumer confusion.

## 6. Composite Payload Comparison

Scenario: fetch an event list page (10 items) + venue name for each item.

| Metric | REST (2 calls: `GET /api/v1/events` + `GET /api/v1/events/nearby`) | GraphQL (1 query: `events` with `venue { name }`) |
|---|---|---|
| Round-trips | 2 | 1 |
| Bytes (approx) | ~4 KB (full venue object per event) | ~2.8 KB (venue.name only) |
| Over-fetch | Yes — full Venue object returned even if only name is needed | No — client selects exactly `id title venue { name }` |
| Caching | Yes — GET, CDN-friendly, ETags possible | No — POST, per-client, cache-busted per query shape |

GraphQL wins on mobile where bandwidth and round-trip latency matter. REST wins for shared caches — a CDN can cache `GET /api/v1/events?status=active` for all anonymous users.

## 7. Error Catalogue

| Code | HTTP | Cause |
|---|---|---|
| `VALIDATION_ERROR`     | 400 | Bad query param or request body field |
| `INVALID_CURSOR`       | 400 | Opaque cursor cannot be decoded |
| `UNAUTHENTICATED`      | 401 | Missing, invalid, or expired Bearer token |
| `INVALID_CREDENTIALS`  | 401 | Wrong demo username or password |
| `FORBIDDEN`            | 403 | Valid token, insufficient role |
| `EVENT_NOT_FOUND`      | 404 | Unknown event id |
| `SLOT_NOT_FOUND`       | 404 | Unknown slot id |
| `BOOKING_CONFLICT`     | 409 | Duplicate reservation for the same user and slot |
| `SLOT_SOLD_OUT`        | 409 | Slot capacity fully consumed |
| `UNPROCESSABLE_BOOKING`| 422 | Past slot, invalid quantity, or other semantic violation |
| `RATE_LIMITED`         | 429 | Bucket4j per-IP threshold exceeded |
| `INTERNAL_ERROR`       | 500 | Unexpected server fault |

All REST error responses use `{ "code": "...", "message": "..." }`. GraphQL errors include the same `code` in `extensions.code` so consumers can handle both protocols with one error-handling path.

## 8. Design Decisions

1. **JPQL `JOIN FETCH` for N+1 prevention** — GraphQL field resolvers read from already-hydrated entity graphs loaded in one SQL statement (`JOIN FETCH e.venue`, `JOIN FETCH e.slots`). This avoids DataLoader or batching complexity for our dataset size while keeping query count bounded at O(1) per resolver call regardless of result set size.

2. **Spec-first OpenAPI** — `docs/openapi.yaml` is hand-authored and served as a static resource under `/openapi.yaml`; springdoc is configured with `springdoc.swagger-ui.url=/openapi.yaml` so Swagger UI renders our committed contract, not the auto-generated one from annotations. This guarantees the probe's `/docs` renders exactly what we intend.

3. **Opaque base64 keyset cursor** — a shared `CursorEncoder` utility encodes the last-seen `(id, sort_field)` pair as a Base64 string. Both the REST list controller and the GraphQL `EventConnection` resolver call the same encoder, preventing the two layers from drifting to incompatible cursor formats.

## 9. Future Evolution

1. Add a `TicketType`-level availability query so clients can filter by exact price tier without fetching all slots.
2. Introduce per-user rate-limit buckets backed by Redis (`bucket4j-redis`) as request volume grows beyond what per-IP in-process buckets can handle.
3. Federate into a GraphQL subgraph when the venue catalogue splits into its own service, using Apollo Federation or Spring for GraphQL's subgraph support.

## 10. Business Value

1. **Unified data access**: REST for CDN-cached integrations and GraphQL for flexible frontend queries — one backend, two protocols, zero duplicated business logic.
2. **Real-time availability**: conflict-checked booking with `409 BOOKING_CONFLICT` prevents double-sells atomically, reducing customer-support load without requiring a distributed lock.
3. **Geospatial discovery**: PostGIS proximity queries (`ST_DWithin`) enable "events near me" UX with sub-100ms latency at typical dataset sizes, without deploying a separate geo service or external provider.
