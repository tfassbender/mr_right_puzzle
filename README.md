# Mr Right

A browser-based logic elimination puzzle, inspired by *Emma's Perfect Match*: find the one Mr Right
among hundreds of bachelors by crossing out everyone who doesn't match the clues. Optimized for mobile.

**🎉 First working version** – deployed at https://mr-right.tobias-fassbender.com (playable on phone and
desktop, online and offline).

## Status

- ✅ Puzzle analysis and decisions – [`doc/puzzle-analysis.md`](doc/puzzle-analysis.md)
- ✅ Architecture – [`doc/architecture.md`](doc/architecture.md)
- 📋 Implementation plan – [`doc/implementation_plan.md`](doc/implementation_plan.md)
- ✅ Project setup: Gradle monorepo, Quarkus backend with login, Preact frontend with i18n (en/de) and PWA
- ✅ Puzzle rules analysed and decided (`doc/puzzle-analysis.md`)
- ✅ Puzzle generator, games REST API, game list, game view (playable on phone and desktop)
- ✅ Offline play with IndexedDB, debounced sync with conflict dialog
- ✅ Playtest feedback: possible matches (star) with "Check", jump to the next open suitor, puzzle names,
  story & rules dialog
- ✅ Production build and deployment
- ✅ 1.1: rule sets – "Eva's rules", a personal puzzle with fixed criteria and a finale, unlocked per user
- ⬜ Later: look & feel (Regency theme, own icons), clue flavour texts, sharing puzzles

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
| Production build         | `./gradlew :backend:quarkusBuild` (also builds the frontend) |

Open http://localhost:5174 in dev. The dev backend skips email verification, so registration logs in directly.

Right after a backend change, the first e2e test may fail with "Something went wrong" while Quarkus dev
live-reloads – run it again before looking for a bug.

## Deployment

The production build is one Quarkus JAR that also serves the SPA. Server-specific settings (data directory,
JWT key, SMTP, link URLs) go into `config/application.properties` next to the JAR –
see [`backend/config.example/application.properties`](backend/config.example/application.properties).

### Unlocking rule sets

Everybody plays the book's rule set ("Classic"). Other rule sets – currently only `EVA` ("Eva's rules",
see [`doc/personal-rule-set.md`](doc/personal-rule-set.md)) – are unlocked per user by hand; there is no admin UI.

1. Find the user's file (named by the user id) in the data directory:
   `grep -il "<email>" <data-dir>/users/*.json`
2. Set the rule set ids in it: `"unlockedRuleSets": ["EVA"]` (users created since 1.1.0 already have an empty
   `"unlockedRuleSets": []`; older files don't have the field yet – just add it).
3. The user reloads the app: "New puzzle" now offers a rule set dropdown, and "Story & rules" shows the story.

Unknown ids are ignored. Only edit the file on a server running 1.1.0 or later – older versions drop the field
the next time they save the user.
