# CLAUDE.md

## What this repository is

**Mr Right** – a browser-based logic elimination puzzle inspired by the book *Emma's Perfect Match*:
find the one Mr Right among hundreds of bachelors by crossing out everyone who doesn't match the clues.
Mobile first, German + English, playable offline.

- `doc/puzzle-analysis.md` – how the book's puzzle works and **all product decisions** (read first)
- `doc/architecture.md` – packages, data model, REST API, saving/sync, offline, sharing
- `doc/implementation_plan.md` – phased roadmap with checkboxes (tick items when done)
- `README.md` – status and dev commands

**Status:** Phases 0–6 and the playtest feedback round (6b) of `doc/implementation_plan.md` are done – the game
is playable end to end, online and offline. Next: Phase 7 (look & feel, flavour texts, deployment) – its open points need the user's decisions first.

## Build, run & test

| Task                     | Command                                         |
|--------------------------|-------------------------------------------------|
| Backend dev (port 4720)  | `./gradlew :backend:quarkusDev`                 |
| Frontend dev (port 5174) | `cd frontend && npm run dev` (proxies `/api` to 4720) |
| Backend tests            | `./gradlew :backend:test`                       |
| Frontend tests / types   | `cd frontend && npm test` / `npm run typecheck` |
| E2E (both servers up)    | `./gradlew :e2e:e2eTest` (`:e2e:test` alone is skipped on purpose); filter: `./gradlew :e2e:test --tests '*GameE2eTest' :e2e:e2eTest` |
| Production build         | `./gradlew :backend:quarkusBuild` (also builds the frontend) |

- Windows: stopping a background dev server often leaves `node`/`java` running and holding the port –
  check with `Get-NetTCPConnection -LocalPort 4720,5174` and kill the process tree.
- The dev/test backend has email disabled: registration logs in directly, verify/reset return 503.
- `frontend/src/paraglide/` is generated (gitignored) by `npm run i18n`, which all npm scripts run first.
  It loads the inlang message-format plugin from a CDN, so it needs a network connection.

## Tech stack & key decisions

- **Backend:** Kotlin + Quarkus 3.35, Gradle Kotlin DSL, JVM 21. Package root `com.tfassbender.mrright`.
- **Storage:** file-based JSON under `mrright.data.dir` (no database), always written via `AtomicJsonFile`.
- **Auth:** taken over from `../avalons_final_stand` – email + password, JWT (HS256, 30 days), `@RolesAllowed("user")`.
- **Frontend:** Preact 10 + TypeScript + Vite, Preact Signals for state, `preact-iso` for routing.
  Plain CSS with tokens in `src/styles.css` – the look & feel is still a placeholder.
- **i18n:** Paraglide JS, messages in `frontend/messages/{en,de}.json`. Suitor names are never translated.
- **Offline:** vite-plugin-pwa for the app shell; game data/state will live in IndexedDB.
- **Vitest 5, not 4:** npm 10 crashes (`edgesOut`) resolving Vitest 4's optional peer dependencies.

## Architecture rules

- Keep it simple: packaged by feature (`auth`, `storage`, `rest`, later `puzzle`, `game`). **No hexagonal
  layering, no ports/adapters** – the user decided against it for this small project.
- `ArchitectureTest` has exactly two rules: no dependency cycles between packages, and `puzzle` must not
  depend on Quarkus/Jakarta/Vert.x. `storage` is plain infrastructure and never depends on a feature package.
- Repositories are plain classes (unit-testable against a temp dir); the feature package produces the bean.
- Frontend: pure game logic in `src/game/` (Vitest), `src/sync/` for SyncManager + game session,
  `src/storage/` for IndexedDB. The e2e module depends on `:backend` to regenerate a puzzle from its seed.
- The backend sends **language-neutral data** (error codes, later clue types + parameters); all texts are
  rendered in the frontend. A backend error code `X` is shown via the message key `error_X`.

## Conventions

- Every user-facing text goes into **both** `en.json` and `de.json`.
- Every UI feature must work on a phone (~390 px wide); e2e tests run desktop and mobile viewports.
- Tests: JUnit5 + RestAssured (backend), Vitest (frontend), Playwright Java (e2e).
- Update `doc/` when a decision changes; open points are listed there – don't silently resolve them.
- `../avalons_final_stand` is a read-only reference. Never edit it from this project.
- Git: solo project – commit directly to `main`, no branches or PRs unless asked.
