---
name: frontend-app
description: Use this skill for anything involving the React frontend — UI screens, API client setup, auth token handling, routing, or the build/serve process. Trigger this whenever the user mentions the frontend, React app, UI screens, till screen, or the checkout/reports/masters UI, even if they don't name the module explicitly.
---

# Frontend Application

React + Vite + TypeScript, living in `/frontend`. Talks to the backend only through its REST API (`/api/v1/...`) — never touches the database directly, and never embeds business logic that belongs in the backend (tax calc, bill numbering, and stock rules all stay server-side; the frontend renders and submits, it doesn't decide).

## Structure

```
frontend/src
├── api/          typed API client functions, one file per backend module (masters, transactions, reports)
├── pages/         one folder per screen (checkout, items, reports, ...)
├── components/     shared UI components
├── hooks/          TanStack Query hooks wrapping the api/ client functions
└── auth/           JWT storage + attach-to-request logic
```

## API client

- All calls go through a single typed client layer (`api/`), not ad-hoc `fetch` calls scattered through components — keeps the REST contract in one place if the backend changes an endpoint shape.
- TanStack Query for all server data — don't hand-roll loading/error state with `useState`/`useEffect` for anything that's really a server fetch.
- JWT stored client-side and attached as `Authorization: Bearer <token>` on every request via a shared API client interceptor, not repeated per call site.

## Business settings

`GET /api/system/config` returns business-level settings — store name, GSTIN, address, invoice series prefix, default tax behavior — for the UI to render correctly. Fetch it once at app startup and keep it in a small shared context/store rather than re-fetching per screen. This is business configuration, not a feature flag — this architecture only has one deployment shape (see `docs/ARCHITECTURE.md`).

## Till-facing UI priorities

The checkout screen is the highest-traffic screen in the whole system and needs to prioritize speed over visual richness: minimal re-renders per scan, keyboard-first interaction (barcode scanners emulate keyboard input — don't let modal/focus management fight this), and no blocking network calls in the scan → add-line-item path (that item data should already be loaded/cached from the masters API, not fetched fresh per scan).

## What NOT to do here

- Don't put tax calculation, bill numbering, or stock-decrement logic in the frontend — these are backend rules (see `gst-compliance` and `billing-transactions` skills). The frontend submits a cart and displays what the backend returns.
- Don't build deployment-mode-aware conditional rendering — this architecture only has one shape, there's nothing to switch between.
