# Clevbill — Project Memory

Project name: **Clevbill** (domain: clevbill.com / clevbill.in).

Retail billing/POS system for a single property, client-server architecture:

- **One server** per property — a dedicated on-prem Linux box running both the backend (API + database) and serving the frontend build.
- **Clients (tills)** are plain browsers on the local network — no local install beyond the browser and a small print-agent for receipt printer/cash drawer hardware.

No head office, no multi-property sync, no deployment-mode switching — this system is scoped to **one property per installation**. If multi-property/HO support is needed later, that's a deliberate future scope change requiring a sync layer and a revisit of the ID strategy (see `docs/ARCHITECTURE.md` §11) — not something this codebase is built for today.

Full design in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — read it before proposing an architectural change.

## Repository layout — one repo, two codebases

```
/backend    Java 25, Spring Boot 4.1.x — REST/JSON API + database access
/frontend   React + Vite + TypeScript — talks to the backend only via REST
/docs       ARCHITECTURE.md
/.claude    skills/
```

Kept as one repository so the API contract between `/backend` and `/frontend` can't silently drift.

**Deployment:** the frontend's production build is served as static assets by the same Spring Boot process — one JAR, one systemd service, on the one server per property. No separate frontend hosting.

## Stack

- Backend: Java 25 (LTS), Spring Boot 4.1.1 (Spring Framework 7), Maven
- Frontend: React 19 + Vite 7 + TypeScript, TanStack Query for API calls, React Router, Axios
- Auth: Spring Security issuing JWTs (jjwt), consumed by the frontend
- DB: PostgreSQL 17, Flyway migrations, Spring Data JPA / Hibernate

## Build & run

Backend:
```
cd backend
mvn clean install
mvn spring-boot:run
mvn test
```
Needs a running PostgreSQL reachable via the `spring.datasource.*` settings in `backend/src/main/resources/application.yml` (defaults to `clevbill`/`clevbill` on `localhost:5432` — override in an untracked `application-local.yml` or env vars, never commit real credentials). `docker-compose up -d db` at the repo root starts a matching local Postgres 17 instance.

Frontend:
```
cd frontend
npm install
npm run dev      # local dev server on :5173, proxies /api to localhost:8080
npm run build     # production build — consumed by the backend's static resources
```

## Package layout (backend) — by layer, not by feature

Base package `com.clevstack.clevbill`, packaged **by technical layer** rather than by domain module:

```
com.clevstack.clevbill
├── controller/     REST controllers (/api/v1/...), classes named by domain
├── service/         business logic / transaction boundaries, classes named by domain
├── model/           JPA entities
├── dto/             request/response payloads — never expose entities over the API
├── repository/       Spring Data JPA repositories
├── config/           DB, Flyway, business settings (/api/system/config), cross-cutting beans
├── security/         Spring Security config + JWT issuance/validation (the `auth` domain)
└── exception/         shared exception types + global @ControllerAdvice
```

This is a deliberate deviation from `docs/ARCHITECTURE.md` §4, which describes package-by-feature module boundaries (`masters`, `inventory`, `transactions`, `gst`, `reports`, `auth`). Here, domain separation is expressed through **class naming** within each layer package (e.g. `ItemController`/`ItemService` for masters, `SaleController`/`SaleService` for transactions) instead of through package structure. Keep class names domain-prefixed so the module boundaries described in the architecture doc stay visible even though the folders don't nest by domain. Don't silently drift back to package-by-feature or introduce a third convention without updating this file.

## Non-negotiable rules — never violate without flagging it to the user first

1. Money is always `BigDecimal` (`DECIMAL(12,2)` in DB) — never `float`/`double`, anywhere, no exceptions.
2. Quantities that can be fractional (weight-sold items) are `DECIMAL(12,3)`.
3. Standard auto-increment (`BIGSERIAL` / JPA `@GeneratedValue`) primary keys everywhere. There's one database and one writer in this architecture, so there's no ID-collision problem to design around — don't add UUIDs "just in case."
4. Masters are directly read-write — no read-only mirror concept, no central-authoring restriction.
5. Bill numbers are generated **locally, synchronously, in the same DB transaction as the sale insert**, format `{SERIES_PREFIX}-{FY}-{seq}` — `SERIES_PREFIX` is a configurable business/store code set once during setup, purely a compliance/readability convention (not collision avoidance, since there's only one source of numbers).
6. All timestamps stored in UTC. Convert at display time only.
7. E-invoice/IRN calls go directly from the backend to the government IRP, at/near billing time — never block the customer's printed receipt on this call.
8. `GET /api/system/config` serves genuinely dynamic business settings (store name, GSTIN, address, invoice series prefix, tax defaults) for the frontend to render — it is not a deployment-mode flag; this architecture only has one shape.

## Skills

| Skill | Load it for |
|---|---|
| `billing-transactions` | checkout, sale/payment/return flow, bill numbering |
| `master-data-catalog` | item/customer/price master CRUD |
| `gst-compliance` | tax calc, invoice numbering rules, e-invoice/IRN, HSN |
| `reporting` | reports/dashboards, filters, aggregation queries |
| `frontend-app` | React app structure, API client, till UI, build/serve strategy |

## Current state

This is a bare-bones scaffold: build files, package/folder layout, and a JWT config placeholder — no entities, migrations, or endpoints implemented yet. Both `mvn compile` (backend) and `npm run build` (frontend) succeed as of scaffolding. Node.js on this machine is 20.18.0, slightly below Vite 7's recommended minimum (20.19+) — builds work but expect a warning; upgrade Node when convenient.
