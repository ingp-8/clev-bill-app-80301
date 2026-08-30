---
name: gst-compliance
description: Use this skill for anything involving GST tax calculation, invoice numbering rules, e-invoicing/IRN/QR codes, HSN codes, or GSTR reporting — the `gst` module. Trigger this whenever the user mentions GST, tax invoice, e-invoice, IRN, IRP, HSN, CGST/SGST/IGST, or invoice compliance, even if they don't name the module explicitly.
---

# GST Compliance Module

Owns: tax calculation, e-invoice/IRN client, HSN validation. Runs on the single backend. See `docs/ARCHITECTURE.md` for the overall system design.

## Invoice numbering

CGST Rule 46(b): a tax invoice needs a consecutive serial number, unique within a financial year, up to 16 characters, using only letters, numbers, hyphens, and slashes.

- Format in use: `{SERIES_PREFIX}-{FY}-{sequence}`. `SERIES_PREFIX` is a configurable business/store code, set once during setup and served alongside other business settings via `/api/system/config` — a compliance/readability convention, not a collision-avoidance mechanism (there's only one source of numbers in this architecture).
- Sequence resets to 1 each financial year (April 1) — implement as "counter row doesn't exist for this FY yet → start at 1", not a scheduled job that could be missed.
- Never allow a gap or skipped number without an explicit, logged cancellation — auditors flag gaps.

## Tax calculation

- Calculate tax **per line item**, never one rate applied to the whole bill — a single bill routinely mixes GST rates across items.
- Rate lookup goes through the item's linked tax rate in the `masters` module — never hardcode a rate in the billing flow.
- All tax amounts are `BigDecimal`. Round at the line-item level, then sum — don't sum first and round once, that produces bills that don't reconcile against their own line items.

## E-invoicing / IRN

If the business is above the applicable e-invoice turnover threshold (verify the current threshold — it's been lowered progressively; don't hardcode last year's number), every eligible invoice needs an IRN and signed QR code from the government's Invoice Registration Portal (IRP).

Flow:
1. Bill is generated and printed immediately from local data — never block the customer's receipt on the IRP call.
2. IRN request is queued (a simple status field/queue table — no need for outbox-style idempotency machinery, there's no remote system to coordinate with here) and sent to the IRP asynchronously, ideally within seconds.
3. On success, store the IRN + QR code against the sale, and make the invoice reprintable/re-sendable with it included.
4. Retry with backoff on failure — treat this as a high-priority queue, not best-effort; an invoice without a valid IRN isn't a valid GST document.

## HSN codes

Every item in `masters` needs an HSN code validated against the current GST rate schedule before it can be sold — a missing or invalid HSN code on an eligible item should block that item from being added to the catalog, not just warn.

## What NOT to do here

- Don't sum-then-round tax across line items — round per line, then sum.
- Don't generate bill numbers anywhere except the same local transaction as the sale insert (see `billing-transactions` skill).
