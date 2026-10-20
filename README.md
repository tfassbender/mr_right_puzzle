# Mr Right

A browser-based logic elimination puzzle, inspired by *Emma's Perfect Match*: find the one Mr Right
among hundreds of bachelors by crossing out everyone who doesn't match the clues. Optimized for mobile.

## Status

- ✅ Puzzle analysis and decisions – [`doc/puzzle-analysis.md`](doc/puzzle-analysis.md)
- ✅ Architecture – [`doc/architecture.md`](doc/architecture.md)
- ✅ Project setup: Gradle monorepo, Quarkus backend with login, Preact frontend with i18n (en/de) and PWA
- ⬜ Puzzle rules (waiting for the rule pages of the book), generator, game view, sync

## Tech Stack

| Area     | Choice                                                       |
|----------|--------------------------------------------------------------|
| Backend  | Kotlin + Quarkus, file-based JSON storage, JWT login         |
| Frontend | Preact + TypeScript + Vite, Preact Signals, preact-iso       |
| i18n     | Paraglide JS (`frontend/messages/{en,de}.json`)              |
| Offline  | vite-plugin-pwa                                              |
| Tests    | JUnit5 + RestAssured, Vitest, Playwright (Java)              |

## Development

Requires Java 21 and Node 22.

| Task                     | Command                                         |
|--------------------------|-------------------------------------------------|
| Backend dev (port 4720)  | `./gradlew :backend:quarkusDev`                 |
| Frontend dev (port 5174) | `cd frontend && npm run dev`                    |
| Both in new windows      | `./gradlew :e2e:startE2eServers`                |
| Backend tests            | `./gradlew :backend:test`                       |
| Frontend tests           | `cd frontend && npm test`                       |
| E2E tests (servers up)   | `./gradlew :e2e:e2eTest`                        |
| Playwright browser       | `./gradlew :e2e:installBrowsers` (once)         |
| Production build         | `./gradlew buildFrontend :backend:quarkusBuild` |

Open http://localhost:5174 in dev. The dev backend skips email verification, so registration logs in directly.

## Deployment

The production build is one Quarkus JAR that also serves the SPA. Server-specific settings (data directory,
JWT key, SMTP, link URLs) go into `config/application.properties` next to the JAR –
see [`backend/config.example/application.properties`](backend/config.example/application.properties).
