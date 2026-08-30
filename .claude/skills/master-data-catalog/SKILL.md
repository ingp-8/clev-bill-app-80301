---
name: master-data-catalog
description: Use this skill for anything involving items, categories, brands, tax rates, price lists, customers, or suppliers — the `masters` module. Trigger this whenever the user mentions catalog, item master, menu, product data, price list, customer master, or CRUD on any reference/master data, even if they don't name the module explicitly.
---

# Master Data / Catalog Module

Owns: items, categories, brands, tax rates, price lists, customers, suppliers. Runs on the single backend, directly read-write — see `docs/ARCHITECTURE.md` for the overall system design.

## Straightforward CRUD, one source of truth

There's exactly one backend and one database, so master data management here is standard: a `masters` service layer backed by JPA repositories, exposed via REST endpoints under `/api/v1/masters/...`. No sync, no delta pull, no property scoping — none of that applies in this architecture (see `docs/ARCHITECTURE.md` §11 for why, if it's ever relevant again later).

## Versioning still worth keeping

Even without sync to anything, keep an `updated_at TIMESTAMPTZ` column on every master table (trigger or `@PreUpdate`) — cheap, and useful for audit trails, "recently changed items" views, and frontend cache invalidation.

## IDs

Standard auto-increment (`BIGSERIAL` / JPA `@GeneratedValue`) primary keys — see `docs/ARCHITECTURE.md` §5.

## What NOT to do here

- Don't add sync/delta-pull endpoints for masters — there's nothing on the other end to sync with.
- Don't add a `property_id` column — this system is scoped to one property per installation.
