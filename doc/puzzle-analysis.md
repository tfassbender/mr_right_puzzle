# Puzzle Analysis: "Emma's Perfect Match"

Source: cover photo ([book_cover.jpg](book_cover.jpg)) plus publisher listings (Hachette, Penguin Random House, Google Books, Quercus, Planeta).
The book was released in September 2026 – there are no reviews, rule pages or solved examples online yet.
Everything marked *(assumption)* still needs to be verified against the actual book.

## The Book

- Title: *Emma's Perfect Match* – "Ein Matchmaking-Rätsel von Mrs Elizabeth Godwin" (German edition by dtv)
- English original: Quercus/Greenfinch (UK), Penguin (US); Spanish: *La pareja ideal para Emma*
- Author is a fictional matchmaker; the pen name belongs to designer/illustrator Clare Faulkner
- Tagline: "6.400 Junggesellen, 12 Hinweise, nur ein »Mr Right«"
- ~224 pages, directory split into 34 sections (according to the Google Books table of contents)

## Puzzle Mechanics

- A directory lists **6,400 bachelors**, each with a name and a set of attributes shown as icons/abbreviations.
- Mrs Godwin gives a list of requirements: **12 clues** (one listing says 11).
- The player eliminates candidates by crossing them out, clue by clue, until **exactly one** suitor remains.
- Pure elimination logic – no hidden information, just careful matching of symbols. The appeal is the calm, methodical crossing out.

### Attributes mentioned by publishers

- Eye colour
- Hair colour
- Star sign (zodiac)
- Size of fortune
- Number of vowels in the first name

### Attributes visible on the cover *(assumption)*

Each entry seems to be a name followed by a row of icons:

| Icon                         | Likely meaning                         | Codes seen                                                  |
|------------------------------|----------------------------------------|-------------------------------------------------------------|
| Eye + abbreviation           | Eye colour                             | Bl, Br, BG, Gr, Bst                                         |
| Head + abbreviation          | Hair colour                            | S, Br, Bl, Rb (Google Books additionally shows Hz, Au, Gn, Bk) |
| £ to ££££                    | Fortune, probably 4 levels             |                                                             |
| Small icons (droplet, flame, …) | Star sign or its element (water, fire, …) |                                                        |
| Face icons (smiley, cat, …)  | Unknown – maybe temperament or hobby   |                                                             |
| "MR" prefix vs. full name    | Unknown – possibly an attribute itself |                                                             |

Notes:
- 6,400 = 80 × 80 – might be 80 first names × 80 surnames *(assumption)*.
- The vowel clue implies that name properties are part of the puzzle, not just decoration.

## Implementation Idea (first draft)

1. **Generate suitors**: combine first names/surnames and random attribute values.
2. **Pick the target** ("Mr Right") at random.
3. **Generate clues** that are true for the target and eliminate others,
   e.g. "fortune at least £££", "fire sign", "not blue eyes", "exactly 3 vowels in his first name".
   Repeat until only the target remains.
4. **Minimality check**: removing any clue must leave more than one candidate – every clue matters.
5. **i18n**: clue texts and attribute labels via message bundles; names stay untranslated.

### Mobile considerations

- 6,400 entries are too many to scroll on a phone → difficulty levels (e.g. 200 / 1,000 / 6,400), paged sections, tap a row to cross it out.

### Backend

- Quarkus is useful for server-side generation (e.g. seeded "puzzle of the day"), saved progress and user handling later.
- Reference project for setup and user handling: `../avalons_final_stand`.

## Open Points

### From the book (photos pending)
- Exact rules, attribute list, symbol legend and clue types
- Are clues independent filters, or are there combined/conditional clues ("if blue eyes, then …")?

### Postponed (decide later)
- **Player aids**: some are needed (undo, remaining count, …), details open. Auto-filtering would remove the actual game, so keep it limited.
- **Look & feel**: Regency theme with our own icons, names and texts – the book's content must not be copied (copyright).
- **Name lists**: English names (not translated); ~80 first names and ~80 surnames; define the vowel rule (does "y" count?).
- **Sharing**: optional feature, details (locking while the owner plays) later.


## Decisions

### Project
- Name: **Mr Right** (`mr-right`) – may change later.
- Backend language: **Kotlin** (like `avalons_final_stand`).

### Game
- **Size and difficulty** are configurable when creating a new game (number of suitors, number of clues).
- **Solving**: the player marks a candidate as Mr Right and confirms it in a dialog.
  A wrong answer is reported and counted. There is no "losing" – the game continues.

### Puzzles & sharing
- Every player can create puzzles; they are persisted on the server.
- A player can have multiple puzzles at once, regardless of their state.
- **Sharing** is an optional feature for later:
  - Both players play on the same puzzle (shared progress).
  - The original player (owner) always wins conflicts.
  - While the owner is playing, the other player cannot play that puzzle.

### Generation
- Puzzles are generated in the backend.
- Generation is driven by a random seed; the player can optionally enter a seed in the frontend.
- The seed is stored with the game data.

### Saving & sync
- Game state is stored on the server, so a player can switch devices (e.g. laptop → phone).
- Saving is automatic, no save button – but throttled/debounced so not every tap is a request.
- **Offline mode**: the game stays playable offline. A small note icon tells the player that changes are only stored locally.
  When the connection is back, the local state is sent to the server – only if it is newer than the server state (timestamps).
- Offline play only covers puzzles that were already created; creating puzzles requires a connection.
- **Sync conflicts** are resolved by the player: both versions are shown with a summary
  (e.g. number of crossed-out names, whether Mr Right was already found) and the player picks one.

### Languages
- German and English, more may follow. Use a lightweight i18n library.
- Suitor names are English and never translated.

### Tech stack
- Backend: Kotlin + Quarkus, hosted as a Quarkus app on the user's own server (deployment is known territory).
- Storage: **file-based JSON**, no database (like `avalons_final_stand`).
- Auth: **email login with a dev/test mode**, reused from `avalons_final_stand`. An account is required.
- Seeds: only the seed (plus size/difficulty config) is stored; the puzzle is regenerated from it.
  The generator will rarely change, so seed stability is accepted as a risk.
- Architecture: keep it simple – generator, game logic, REST API, storage. No hexagonal layering;
  a minimal ArchUnit test with two rules: no package cycles, `puzzle` stays free of Quarkus/REST.
- Testing: JUnit5 + RestAssured (backend), a few Playwright e2e tests including a mobile viewport.
- Repository: git, directory `mr_right`.
- Frontend: **Preact + TypeScript + Vite**, state via **Preact Signals**, virtualized list for the directory.
- i18n: **Paraglide JS** (fallback: i18next).
- Offline/PWA: **vite-plugin-pwa** (service worker, installable).
- Autosave: every change goes to local storage immediately; server save debounced (a few seconds after the last change)
  and on tab hide/close.
- Reference for project structure and user handling: `../avalons_final_stand` (Gradle multi-module: `backend`, `frontend`, `e2e`; frontend served as static resources from the Quarkus JAR).

## Sources

- https://www.hachette.co.uk/titles/elizabeth-godwin/emmas-perfect-match/9781529455588/
- https://www.penguinrandomhouse.com/books/854370/emmas-perfect-match-by-elizabeth-godwin/
- https://books.google.com/books/about/Emma_s_Perfect_Match.html?id=Iwb1EQAAQBAJ
- https://www.quercusbooks.co.uk/titles/elizabeth-godwin/emmas-perfect-match/9781529455588/
- https://www.planetadelibros.com/libro-la-pareja-ideal-para-emma/458902
