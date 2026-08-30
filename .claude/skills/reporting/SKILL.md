---
name: reporting
description: Use this skill for anything involving reports, dashboards, analytics, or filterable business queries — the `reports` module. Trigger this whenever the user mentions reports, dashboards, sales analytics, till-wise/date-wise filters, or owner-facing summaries, even if they don't name the module explicitly.
---

# Reporting Module

Owns: reporting and analytics for the property. Runs on the single backend, reading from the same database transactions are written to — see `docs/ARCHITECTURE.md` §4.

## Standard filter dimensions

Design report queries and DTOs around:
- **Till/register** (which checkout lane)
- **Date range**
- **Item / category**
- **Cashier/user**

Build filters as composable query parameters (`/api/v1/reports/sales?tillId=&from=&to=&categoryId=`), not separate hardcoded endpoints per filter combination.

## Query patterns

- Index `created_at`, `product_id`, and `till_id`/`register_id` — these are the columns every report filter touches.
- Reports read from the same live database checkout writes to. That's fine at this scale; if report queries ever visibly start slowing down checkout, that's the trigger to consider a read replica (see `docs/ARCHITECTURE.md` §10) — not something to build preemptively.
- Money fields stay `BigDecimal` end to end, including in aggregation (`SUM`, `AVG`) — don't cast to double for display formatting.

## What NOT to do here

- Don't build any write endpoints in this module — reporting is strictly read-only.
- Don't add property-level filters or cross-location aggregation — this system is scoped to one property per installation (see `docs/ARCHITECTURE.md` §11 if that ever changes).
