# Clevbill — Project Overview

**What it is:** A single-property retail billing/POS system — client-server, no head office, no multi-property sync. Three functional areas: **Masters** (catalog/reference data), **Transactions** (billing/sales), **Reports** (analytics).

**Location:** `D:\Prashant\Work\Retail\Repository\clev-bill-app`

## Deployment model

One dedicated Linux server per store runs everything — API, database, and the served frontend build. Tills are just browsers on the LAN (no local install beyond a small print-agent for receipt printer/cash drawer hardware). One JAR, one systemd service, one Postgres database — that's the whole system.

## Tech stack

| Layer | Choice |
|---|---|
| Backend | Java 25, Spring Boot 4.1.1 (Spring Framework 7), Maven |
| Frontend | React 19 + Vite 7 + TypeScript |
| Data fetching | TanStack Query, Axios, React Router |
| Auth | Spring Security + JWT (jjwt) |
| Database | PostgreSQL 17, Flyway migrations, Spring Data JPA/Hibernate |

## Repo layout

```
clev-bill-app/
├── backend/     Spring Boot API — com.clevstack.clevbill, packaged by layer
│                (controller/service/model/dto/repository/config/security/exception)
├── frontend/    React app — src/{api,pages,components,hooks,auth}
├── docs/        ARCHITECTURE.md — canonical design reference
├── .claude/     skills/ (billing-transactions, master-data-catalog, gst-compliance,
│                reporting, frontend-app)
└── CLAUDE.md    project memory — rules, conventions, build/run
```

## Key design decisions

(from `docs/ARCHITECTURE.md`)

- Money always `BigDecimal`/`DECIMAL(12,2)`; quantities `DECIMAL(12,3)` — never `float`/`double`.
- Auto-increment `BIGSERIAL` IDs everywhere — no UUIDs, since there's one DB/one writer.
- Bill numbers generated locally, synchronously, inside the sale's own DB transaction; format `{SERIES_PREFIX}-{FY}-{seq}`, GST Rule 46(b) compliant.
- E-invoice/IRN calls happen async, near billing time — never block the printed receipt.
- Inventory is an append-only ledger, never overwritten.
- Multi-property/HO support is explicitly out of scope for now (§11 of ARCHITECTURE.md) — a deliberate future decision, not a gap to quietly fill in.

## Current state

Bare-bones scaffold — build files, package layout, and a placeholder JWT config are in place; no entities, migrations, or real endpoints yet. Both `mvn compile` (backend) and `npm run build` (frontend) succeed as of the last check.

## Not yet done (natural next steps)

- Flyway migration `V1__init.sql` for the core tables (users/roles, items/categories/tax_rates, sales/sale_items/payments, invoice_counters, inventory_transactions).
- JWT auth flow (login endpoint + Spring Security filter chain).
- First real module: masters (items) CRUD, as the reference pattern for the rest.
- `GET /api/system/config` endpoint.
- Frontend: routing, auth context, and the first API client.
