---
name: billing-transactions
description: Use this skill for any work on the checkout/billing flow — creating sales, generating bill numbers, recording payments, handling returns, or anything touching the `transactions` module. Trigger this whenever the user mentions billing, checkout, POS screen, invoice/bill generation, sale creation, payments, split payment, returns/refunds, or the sale_items/payments/returns tables, even if they don't name the module directly.
---

# Billing & Transactions Module

Owns: `sales`, `sale_items`, `payments`, `returns`, `return_items`, and bill number generation. Runs on the single backend — this is the primary write path of the whole system. See `docs/ARCHITECTURE.md` §4–6 for the design rationale; this file is the implementation reference.

## Core rule: everything happens in one local DB transaction

A checkout is a single atomic unit: bill number allocation, `sales` row, `sale_items` rows, `payments` row(s), and stock decrement (via the `inventory` module's service interface) — all in one `@Transactional` method, against the one database this system has.

## Bill number generation

```sql
CREATE TABLE invoice_counters (
  financial_year    TEXT PRIMARY KEY,    -- e.g. '2026-27'
  next_number        BIGINT NOT NULL DEFAULT 1
);
```

Algorithm (inside the same transaction as the sale insert):
1. `SELECT next_number FROM invoice_counters WHERE financial_year = ? FOR UPDATE` (row lock prevents concurrent double-allocation across simultaneous checkouts).
2. Use it, then `UPDATE invoice_counters SET next_number = next_number + 1 WHERE ...`.
3. Bill number = `{series_prefix}-{financial_year}-{next_number, fixed width}` — `series_prefix` comes from business settings (`/api/system/config`), not per-property, since there's only one property. Full compliance rules in the `gst-compliance` skill.
4. If the counter row doesn't exist yet for this FY, create it starting at 1 — handles both the first-ever bill and the April 1 rollover.

## Split payments

`payments` is a separate table from `sales`, one-to-many, so a single sale can have multiple payment rows (part cash, part card, part gift card). Sum of `payments.amount` for a sale must equal `sales.total_amount` before the sale can be marked complete — validate this in the service layer, not just trust the UI.

## Returns

- `returns` references the original `sales.id`.
- `return_items` references specific `sale_items.id`, not just a product — this is what prevents a customer returning more units than they bought, or at a different price than they paid.
- A return is its own transaction, its own row, never a mutation of the original sale.

## What NOT to do here

- Don't call the GST/e-invoice IRP synchronously inside the checkout transaction — see `gst-compliance` skill for the correct async-but-near-real-time pattern.
- Don't add a blocking stock check that can refuse a sale over a technicality — see `docs/ARCHITECTURE.md` §5 on inventory handling.
- Don't use `float`/`double` anywhere in this module. `BigDecimal` only, always.
