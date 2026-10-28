# Mr Right – Implementation Plan

A short, phased roadmap. Details live in [puzzle-analysis.md](puzzle-analysis.md) (rules, decisions) and
[architecture.md](architecture.md) (how we build it).

- Tick items as they're done; add a one-line **Status** note to a finished phase if something is worth remembering.
- This is a starting point, not a contract – split, add or reorder items as we learn.
- **Done** means: tests pass, both languages have their texts, it works on a phone, and `doc/` is still accurate.

---

## Phase 0 – Project setup ✅

- [x] Gradle monorepo (`backend`, `frontend`, `e2e`), Quarkus + Kotlin, Preact + Vite
- [x] Auth (register, login, JWT), file storage, i18n (en/de), PWA shell
- [x] Playwright e2e setup (desktop + mobile)
- [x] Puzzle rules and architecture documented

## Phase 1 – Puzzle model ✅

**Exit criteria:** all attributes, clues and name rules exist as plain Kotlin and are unit-tested.

- [x] Attribute enums and `Suitor`
- [x] Name helpers: initials, vowel and consonant counts (y is a consonant)
- [x] All clue types (9 types → 12 clues per puzzle) with `matches()`, plus their JSON form
- [x] Name catalog: 120 first names + 120 surnames (A–Z only)

## Phase 2 – Generator ✅

**Exit criteria:** any valid seed + config produces a correct puzzle, verified over many seeds.

- [x] Seed hashing → `Random`, config validation, `GENERATOR_VERSION`
- [x] Clue selection with random parameters, in the fixed book order
- [x] Name groups, finalists with a unique tie-break, near misses, random suitors, shuffle
- [x] Property tests: deterministic, exactly the finalists match, unique tie-break, every clue needed
- [x] Performance check with 10,000 suitors

**Status:** a 10,000-suitor puzzle takes ~0.2 s. `PuzzleVerifier` runs after every generation.

## Phase 3 – Games backend

**Exit criteria:** games can be created, loaded, saved and deleted via REST (RestAssured tests).

- [ ] `Game`, `GameState`, `GameRepository` (one JSON file per game), owner index
- [ ] `GameService`: create, list with progress, load (regenerate + cache), save state, delete
- [ ] Finalists and solution hashes; server re-checks `finalistsFound` / `solved`
- [ ] Optimistic locking (`baseVersion` → 409), outdated-generator games
- [ ] REST endpoints + HTTP compression

## Phase 4 – Game list & new game (frontend)

**Exit criteria:** a logged-in player can create a game and see it in the list.

- [ ] API client + DTO types
- [ ] Game list with progress, outdated marker, delete
- [ ] New game page: suitor count, finalist count, optional seed

## Phase 5 – Game view

**Exit criteria:** a puzzle can be played to the end on a phone, online.

- [ ] Virtualized directory with sections of 100, tap to cross out / restore
- [ ] Attribute icons (placeholders) and clue texts in en/de
- [ ] Clue panel: bottom sheet on mobile, side panel on desktop, finalist count
- [ ] Finalists check (hint + wrong guess on mismatch), tie-break reveal, pick Mr Right with confirmation
- [ ] Basic player aids: remaining count, undo
- [ ] E2E test: play a small puzzle to the end (desktop + mobile)

## Phase 6 – Saving, sync & offline

**Exit criteria:** progress survives device switches and offline play.

- [ ] IndexedDB storage of puzzle + state, immediate local save
- [ ] SyncManager: debounced server save, flush on tab hide, refresh on open/return
- [ ] Offline indicator, retry when the connection is back
- [ ] Conflict dialog (both versions with a summary)
- [ ] Vitest tests for sync and conflict logic

## Phase 7 – Polish & release

**Exit criteria:** deployed on the server and nice to play.

- [ ] Look & feel: Regency theme, own icons (decide first – postponed point)
- [ ] Clue flavour texts (optional)
- [ ] Production build and deployment

## Later (optional)

- [ ] Sharing a puzzle with another player (owner wins, lock while the owner plays)
- [ ] More player aids (decide first – postponed point)
