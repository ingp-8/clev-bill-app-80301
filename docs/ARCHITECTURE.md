# Architecture Reference — Retail Billing System

This is the canonical reference for how this system is built. Decisions here were made deliberately after reviewing trade-offs — don't silently re-decide them mid-implementation. If a decision genuinely needs to change, flag it explicitly rather than drifting away from it in code.

---

## 1. System Overview

A retail billing/POS system for a **single property per installation** — client-server architecture, no head office, no multi-property sync. Three functional modules: **Masters** (catalog/reference data), **Transactions** (billing/sales), **Reports** (analytics, filterable by till/date/item/etc). All three run on the same single backend, against the same single database.

## 2. Deployment Topology

```
        ┌───────────────────────────────────────┐
        │ One server per property — dedicated     │
        │ Linux box, on UPS                        │
        │  • Backend: Spring Boot (API + DB access) │
        │  • Frontend: React build, served as static  │
        │    assets by the same Spring Boot process     │
        │  • PostgreSQL                                  │
        └──┬───┬───┬───┬───────────────────────────────┘
           │   │   │   │
         till1 till2 till3 till4
       (browsers on the LAN — no local DB, no local install
        beyond the browser + a small print-agent for the
        receipt printer / cash drawer)
```

One JAR, one systemd service, one database. This is the entire system for a given property — there is nothing else to deploy.

**Hardware notes:**
- Dedicated Linux machine, not a checkout till — every till is an equal browser client.
- UPS on the server *and* the network switch.
- Static local IP/hostname for the server, not DHCP-leased, or till browsers break on lease renewal.
- Tills run a small local print-agent process for the receipt printer/cash drawer, since browsers can't drive that hardware directly. Barcode scanners need no special handling (keyboard-emulated).

## 3. Tech Stack

| Layer | Choice | Notes |
|---|---|---|
| Backend language | Java 25 (LTS) | |
| Backend framework | Spring Boot 4.1.x (Spring Framework 7) | Pure REST/JSON API |
| Backend build | Maven | |
| Frontend | React + Vite + TypeScript | Talks to backend only via REST |
| Frontend data fetching | TanStack Query | |
| Auth | Spring Security issuing JWTs | |
| Database | PostgreSQL 17 | |
| ORM / migrations | Spring Data JPA + Hibernate, Flyway | |
| Packaging | Executable Spring Boot JAR, frontend build bundled as static resources | Runs under systemd |
| Testing | JUnit 5 + Testcontainers (Postgres), React Testing Library | |

## 4. Repository & Module Structure

**One repository, two codebases**: `/backend` (Spring Boot) and `/frontend` (React), so the API contract between them can't silently drift, and one `CLAUDE.md` + skill set covers the whole system.

The backend runs as **one process, organized as a modular monolith** — deliberately not microservices or a distributed system of any kind. There's exactly one database and one writer, which makes most of the complexity that a multi-node system would need (sync, idempotency across nodes, conflict resolution) simply not apply here. Module boundaries are enforced by package structure and public service interfaces, not network calls.

| Module | Owns |
|---|---|
| `masters` | items, categories, brands, tax rates, price lists, customers, suppliers — directly read-write |
| `inventory` | stock levels, immutable inventory ledger |
| `transactions` | sales, sale_items, payments, returns, bill numbering — the primary write path |
| `gst` | tax calc, e-invoice/IRN client, HSN validation |
| `reports` | filterable reporting, reads from the same live database |
| `auth` | users, roles, JWT issuance |

### API contract (`/backend` ↔ `/frontend`)

- REST, JSON, versioned under `/api/v1/...`.
- Auth: JWT bearer tokens issued by Spring Security on login, sent as `Authorization: Bearer <token>` on every subsequent call.
- `GET /api/system/config` — returns business-level settings (store name, GSTIN, address, invoice series prefix, tax defaults) for the frontend to render. Fetched once at app startup.

## 5. Data Model Principles

- **Money:** always `BigDecimal`, DB type `DECIMAL(12,2)`. Never `float`/`double`.
- **Quantities:** `DECIMAL(12,3)` to support weight/volume-sold items.
- **IDs:** standard auto-increment (`BIGSERIAL`) primary keys everywhere. One database, one writer — no multi-node collision risk to design around (contrast with §7 below).
- **Inventory is an append-only ledger** (`inventory_transactions`), never an overwrite — this is good practice for auditability regardless of topology, not something tied to multi-property sync. The live `inventories.quantity` is a derived/cached number.

## 6. Bill Numbering & GST Compliance

- Format: `{SERIES_PREFIX}-{FY}-{sequence}`, generated **locally, synchronously**, inside the same DB transaction as the sale insert. `SERIES_PREFIX` is a configurable business code, set once at setup — a compliance/readability convention here, not a collision-avoidance mechanism.
- Compliant with CGST Rule 46(b): a consecutive serial number, unique within a financial year, up to 16 characters.
- Sequence resets each financial year (April 1).
- **E-invoicing (IRN/QR)** — the backend calls the government IRP directly, near billing time, without blocking the printed receipt. See the `gst-compliance` skill for the full flow.

## 7. Security Notes

- Tills talk to the backend over the in-store LAN only — no DB credentials on any till, since tills never touch the DB directly.
- JWT-based auth for all API calls.
- POS network should eventually be segmented from guest/general Wi-Fi (PCI DSS expectation once card payments are involved) — deferred, see §9.

## 8. Backup & Disaster Recovery

- Scheduled + manual DB backup, writing to cloud object storage — not email (dumps contain PII/financial data and often exceed attachment size limits). Email is used only for a pass/fail notification.
- **This is the property's only protection against total data loss** — there is no second copy of anything anywhere else in this architecture. Backup discipline here isn't optional hardening, it's the whole safety net.
- Restore procedure must be tested at least once before go-live.

## 9. Deferred / Known Gaps

Explicitly discussed and intentionally deferred — don't "fix" without checking with the team first:

- LAN redundancy at the property — single network path from tills to the server, no failover designed yet.
- PCI-style network segmentation (POS traffic isolated from guest Wi-Fi) — not yet implemented.
- Server hardware redundancy — one dedicated box; UPS + backups are the current mitigation, not a replacement for a hot standby.

## 10. Open Decisions

- Whether `reports` ever needs a read replica — revisit only if report queries visibly start slowing down checkout, not preemptively.

## 11. Explicitly out of scope (for now)

Multi-property / head-office support was designed in an earlier revision of this document and then deliberately dropped in favor of the simpler single-property architecture described above. If it's ever needed again, it is **not** a small add-on — it requires reintroducing:

- A sync layer (outbox pattern for transactions, delta-pull for masters) between each property's server and a central HO.
- UUID-based primary keys (or another collision-safe ID scheme) for anything that would need to merge across nodes, replacing the plain auto-increment IDs this version uses.
- Property-scoped access control and data partitioning.

This isn't a checklist to build now — it's here so a future decision to add multi-property support starts from an accurate picture of the cost, instead of being treated as a small config change.
