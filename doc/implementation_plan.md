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

## Phase 3 – Games backend ✅

**Exit criteria:** games can be created, loaded, saved and deleted via REST (RestAssured tests).

- [x] `Game`, `GameState`, `GameRepository` (one JSON file per game), owner index
- [x] `GameService`: create, list with progress, load (regenerate + cache), save state, delete
- [x] Finalists and solution hashes; server re-checks `finalistsFound` / `solved`
- [x] Optimistic locking (`baseVersion` → 409), outdated-generator games
- [x] REST endpoints + HTTP compression

**Status:** `GameState.solved` became `mrRightId` (server-checkable); `finalistsFound` locks the directory.

## Phase 4 – Game list & new game (frontend) ✅

**Exit criteria:** a logged-in player can create a game and see it in the list.

- [x] API client + DTO types
- [x] Game list with progress, outdated marker, delete
- [x] New game page: suitor count, finalist count, optional seed

## Phase 5 – Game view ✅

**Exit criteria:** a puzzle can be played to the end on a phone, online.

- [x] Virtualized directory with sections of 100, tap to cross out / restore
- [x] Attribute icons (placeholders) and clue texts in en/de
- [x] Clue panel: bottom sheet on mobile, side panel on desktop, finalist count
- [x] Finalists check (hint + wrong guess on mismatch), tie-break reveal, pick Mr Right with confirmation
- [x] Basic player aids: remaining count, undo
- [x] E2E test: play a small puzzle to the end (desktop + mobile)

**Status:** the finalists check first ran automatically whenever exactly `finalistCount` suitors were left –
replaced by the explicit "Check" in Phase 6b. Placeholder icons are emoji and coloured shapes. The e2e module depends on `:backend` to regenerate the puzzle
from its seed and know the solution. A minimal debounced server save is in place; Phase 6 completes it.

## Phase 6 – Saving, sync & offline ✅

**Exit criteria:** progress survives device switches and offline play.

- [x] IndexedDB storage of puzzle + state, immediate local save
- [x] SyncManager: debounced server save, flush on tab hide, refresh on open/return
- [x] Offline indicator, retry when the connection is back
- [x] Conflict dialog (both versions with a summary)
- [x] Vitest tests for sync and conflict logic

**Status:** e2e tests cover offline saving (`setOffline`) and the conflict dialog with two browser contexts.
The game list shows unsaved local progress and falls back to the stored list when offline.

## Phase 6b – Playtest feedback ✅

**Exit criteria:** the first round of feedback from playing is in.

- [x] Display names with 3–15 characters (was 4–15)
- [x] New game page: no book references; name and seed only under "Advanced options", random seed button
- [x] Puzzle names: default "Mr Right - <date>" (localized date), " (2)", " (3)" … if taken; seed hidden in the list
- [x] Possible matches: star button per suitor (`GameState.possibleMatches`), a suitor is never starred and
      crossed out at once
- [x] "Check" submits the starred (or the remaining) suitors once there are `finalistCount` of them, with a
      confirmation – replaces the automatic finalists check
- [x] "Next open": jumps to the first suitor without a marker (also when a game is opened), keeping 1 (phone)
      to 3 (desktop) rows above it; lives in the app header, arrow-only on phones
- [x] "Clues" button toggles the clue panel; attribute icons 50% larger
- [x] Story & rules dialog in the game list (original Regency intro, no reference to the book)

**Status:** a correct check crosses out everybody else and stars the finalists, so the server's re-check of
`finalistsFound` stayed unchanged. Older game files without `possibleMatches` or `name` still load.

## Phase 7 – Polish & release ✅

**Exit criteria:** deployed on the server and nice to play.

- [x] Story & rules introduction (done in Phase 6b)
- [x] Production build and deployment

**Status:** the first working version runs at https://mr-right.tobias-fassbender.com (behind Cloudflare).
`./gradlew :backend:quarkusBuild` builds the frontend too; the server config lives next to the JAR in
`config/application.properties`. The look & feel and the clue flavour texts were moved to "Later".
After a deploy, browsers keep the cached (PWA) version until the next reload.

## Phase 8 – Rule sets: a personal puzzle (version 1.1)

A second rule set next to the book's: fixed criteria (her wishes) instead of random clues, mostly its own
attributes, unlocked per user. Decisions so far:

- **Rule sets:** `BOOK` (today's puzzle) is always available. Other rule sets are unlocked per user via
  `unlockedRuleSets` in the user file (edited by hand on the server, no admin UI).
- **Choice in "New puzzle":** the rule set is the first field. The dropdown lists only the user's available
  rule sets, and is not shown at all if `BOOK` is the only one. The server rejects locked rule sets.
- **Personal rule set:** fixed clues (simple AND filters, like the book). Only the directory changes with the seed.
  It replaces most of the book's attributes. The name catalog and name logic stay. Suitor count and finalist
  count stay configurable. Texts in en + de, plus its own intro.
- **Finale ("one more suitor"):** after the correct Mr Right pick, one more suitor "just came in" – the author.
  He is not in the directory and is fixed (not generated). He also matches all criteria and wins a final tie-break.
- **Privacy:** the rule set lives in the (public) code like the book's – accepted.

**Criteria and how they become attributes and clues:** [personal-rule-set.md](personal-rule-set.md).

**Open points (decide with the criteria):** rule set id and display name, attributes with values and icons,
the clues, the tie-break rule, the finale's attributes, tie-break and texts, the intro text.

### 8a – Rule set foundation (no visible change) ✅

**Exit criteria:** book puzzles are byte-identical for the same seeds; all tests pass.

- [x] Golden test first: a hash of the puzzle for a few fixed seeds/configs, recorded before the refactoring
- [x] `RuleSet` in `puzzle`: id, attribute definitions, clue set, generator version
- [x] Generic attributes: `Suitor.attributes` (attribute → value) instead of the typed fields.
      Attribute clues share `AttributeClue`; `IncomeAtLeast` stays a book clue. The clue JSON is unchanged
- [x] `Game.ruleSet` (default `BOOK` for existing files), `outdated` checks the generator version of the game's rule set
- [x] Frontend: icons and legend come from the rule set, not from fixed suitor fields. Puzzles stored on the
      device in the old shape are converted when loaded (works offline, no reload needed)
- [x] Update `architecture.md` (puzzle model, generation, game model)

**Status:** the book puzzles are unchanged (`BookGoldenTest`), so the book stays at generator version 1 and
existing games keep working. The tie-break is still the generator's consonant rule – it becomes part of the
rule set in 8c, once her rule is known.

### 8b – Unlocking and choosing a rule set ✅

**Exit criteria:** an unlocked user can choose the rule set, everybody else sees no difference.

- [x] `User.unlockedRuleSets` (default empty); available = `BOOK` + unlocked
- [x] Available rule sets in the user DTO (login / me), so the frontend also knows them offline
- [x] `ruleSet` in the create request (default `BOOK`); a locked or unknown one → error code `RULE_SET_LOCKED` (403)
- [x] "New puzzle": rule set dropdown first, only if more than one is available
- [x] Game list: show the rule set's name for non-book games
- [x] Tests: RestAssured (locked), e2e (no dropdown for normal users)

**Status:** the book is named "Classic" / "Klassisch". With only the book in the code, the unlocked path
(dropdown shown, game created with another rule set) can't be tested yet – it gets tests with her rule set in 8c.

### 8c – The personal rule set ("Eva's rules") ✅

**Exit criteria:** she can create and solve a puzzle with her criteria on a phone.

- [x] Attributes, values and icons (en + de) – 12 attributes, see [personal-rule-set.md](personal-rule-set.md)
- [x] The fixed clues and their texts; the generator works with fixed clues (finalists and near misses
      possible for every clue – checked by a test, since the parameters can't be re-rolled)
- [x] Tie-break rule: fewest consonants, Mr Right has at least 9
- [x] Rule-set-specific intro ("Story & rules"): a rule set choice in the dialog, Eva's story first
- [x] Generator tests over many seeds, e2e: unlock, create and solve

**Status:** new clue types `ATTRIBUTE_IS` and `ATTRIBUTE_BETWEEN`, weighted attribute values (80 % right per
attribute – a suitor fails ~2.5 criteria on average), and a two-line icon grid for her directory rows.
The book's puzzles are unchanged (`BookGoldenTest`).

### 8d – The finale ✅

**Exit criteria:** after the correct pick, the extra suitor appears and wins.

- [x] The extra suitor as part of the rule set (name + attributes); a test checks he matches all clues and wins
      the final tie-break against every possible Mr Right
- [x] Finale dialog after the correct pick; can be opened again from the solved game
- [x] Texts en + de, e2e test

**Status:** the tie-break is "who treats her better" (Tobias 10, Mr Right 5–8 per game). The closing text is a draft.
Puzzles stored on a device before the finale existed are fetched once more when opened.

## Later (optional)

- [ ] Look & feel: Regency theme, own icons (decide first – postponed point)
- [ ] Clue flavour texts
- [ ] Own JWT key on the server instead of the committed dev key

- [ ] Sharing a puzzle with another player (owner wins, lock while the owner plays)
- [ ] More player aids (decide first – postponed point)
