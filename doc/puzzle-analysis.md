# Puzzle Analysis: "Emma's Perfect Match"

Sources: photos of the German edition – cover ([book_cover.jpg](book_cover.jpg)), Emma's criteria
([book_criteria.jpg](book_criteria.jpg)), the legend ([book_legend.jpg](book_legend.jpg)), a directory page
([book_directory_page.jpg](book_directory_page.jpg)) and the tie-break on page 215 ([book_tiebreak.jpg](book_tiebreak.jpg)) – plus publisher listings
(Hachette, Penguin Random House, Google Books, Quercus, Planeta).
Everything marked *(assumption)* still needs to be verified against the actual book.

## The Book

- Title: *Emma's Perfect Match* – "Ein Matchmaking-Rätsel von Mrs Elizabeth Godwin" (German edition by dtv)
- English original: Quercus/Greenfinch (UK), Penguin (US); Spanish: *La pareja ideal para Emma*
- Author is a fictional matchmaker; the pen name belongs to designer/illustrator Clare Faulkner
- Tagline: "6.400 Junggesellen, 12 Hinweise, nur ein »Mr Right«"
- ~224 pages, directory split into 34 sections (according to the Google Books table of contents)

## Puzzle Mechanics

- A directory lists **6,400 bachelors**, each with a name and a row of attribute icons/abbreviations.
- "Emmas Kriterien": **12 clues**, all of which must hold for the right gentleman.
- The player uses the legend to cross out unsuitable bachelors one after another.
- After all 12 clues **two candidates remain** ("das Verzeichnis hält gleich zwei potenzielle Verehrer bereit").
- **Tie-break (page 215):** Mr Right is the one of the two with the **fewest consonants in his full name**
  (first name + surname, **y counts as a consonant** here). The player writes his name onto the page.
- Pure elimination logic – no hidden information, just careful matching of symbols.

### Attributes (legend)

Six attributes, each bachelor has **exactly one value per attribute** (verified on a directory page):

| Attribute (German)              | Values                                                                                 | Count |
|---------------------------------|----------------------------------------------------------------------------------------|-------|
| Income (Jährliches Einkommen)   | £ = £1,000+, ££ = £2,000+, £££ = £4,000+, ££££ = £10,000+ (ordinal)                      | 4     |
| Star sign element (Sternzeichen-Element) | air (Luft), fire (Feuer), water (Wasser), earth (Erde) – only the element, not the sign | 4     |
| Eye colour (Augenfarbe)         | Br brown, BG brown-green, Bst amber (Bernstein), Gn green, Bl blue                      | 5     |
| Hair colour (Haarfarbe)         | Bl blond, Br brown, S black (Schwarz), Rb auburn (Rotbraun)                             | 4     |
| Temperament (Naturell)          | brooding (grüblerisch), charming, reserved, amiable (liebenswürdig), dashing (verwegen) – face icons | 5     |
| Interests (Interessen)          | intellectual, musical (musisch), sporty, sociable (gesellig)                            | 4     |

Plus the **name** (first name + surname), which several clues refer to.

Notes:
- The extra Google Books hair codes (Hz, Au, Gn, Bk) are probably English edition abbreviations –
  the German legend has exactly the values above.
- 6,400 = 80 × 80 – might be 80 first names × 80 surnames *(assumption)*.

### Directory layout (page 37)

- Two columns of 15 entries → 30 per page; 6,400 / 30 ≈ 213 pages, which fits the reveal on page 215.
- Each entry: `MR FIRSTNAME SURNAME` in capitals (the "MR" is just a fixed prefix), below it one icon row in a
  fixed order: income, element, eye colour, hair colour, temperament, interest.
- Entries are **not sorted** – neither by name nor by attribute.
- First names and surnames repeat a lot (three Alistairs and three Cornwallis on one page), so names are
  combined freely from two lists. Whether a full name can appear twice in the directory is unknown.

### Emma's criteria (the 12 clues of the German edition)

| #  | Clue (paraphrased)                                         | Clue type                        |
|----|------------------------------------------------------------|----------------------------------|
| 1  | No preference for body build, but **not blond** hair         | attribute is not X               |
| 2  | Income **at least £4,000** (£££ or ££££)                     | ordinal attribute at least X     |
| 3  | Initials of first name and surname are **not identical**     | name: initials differ            |
| 4  | Temperament **brooding or reserved**                         | attribute is one of {X, Y}       |
| 5  | **Both initials** in the first half of the alphabet (A–M)    | name: initials in letter range   |
| 6  | Interests **intellectual or sociable**                       | attribute is one of {X, Y}       |
| 7  | Star sign element **air or fire** (Emma is Aquarius)         | attribute is one of {X, Y}       |
| 8  | Surname does **not start and end with the same letter**      | name: first ≠ last letter        |
| 9  | First name has **exactly three vowels**                      | name: vowel count                |
| 10 | Eye colour **green or blue** (Emma has green eyes)           | attribute is one of {X, Y}       |
| 11 | Full name contains **no H**                                  | name: letter not contained       |
| 12 | Surname has **exactly two vowels**                           | name: vowel count                |

Vowels are **a, e, i, o, u – not y** (footnote in the book).

### Observations

- **Every clue is an independent filter**; all clues are combined with AND. A clue may allow several values of
  one attribute (OR). There are **no conditional or cross-attribute clues** ("if blue eyes, then …").
- 6 of 12 clues are about the **name** – name properties are a core part of the puzzle, not decoration.
- Clues carry **flavour text** (Emma's own star sign, her green eyes, why she likes intellectual men).
  Clue 1 even mentions body build, which is not an attribute at all – a deliberate distraction.
- Attribute clues and name clues are mixed in the list, not grouped.
- Rough pass rates: the attribute clues let ~1.5 % through (3/4 · 2/4 · 2/5 · 2/4 · 2/4 · 2/5), the name clues
  roughly another 2–3 % – which fits ~2 survivors out of 6,400. The book's clues look like plain random filters;
  no single clue needs to be "the one" that removes a specific bachelor.

## Implementation

The puzzle model and the generation algorithm are described in [architecture.md](architecture.md#puzzle-model).

### Mobile considerations

- 6,400 entries are too many to scroll on a phone → difficulty levels (e.g. 200 / 1,000 / 6,400), paged sections, tap a row to cross it out.

### Backend

- Quarkus is useful for server-side generation (e.g. seeded "puzzle of the day"), saved progress and user handling later.
- Reference project for setup and user handling: `../avalons_final_stand`.

## Open Points

### From the book
- None left from the book pages.

### Postponed (decide later)
- **Player aids**: some are needed (undo, remaining count, …), details open. Auto-filtering would remove the actual game, so keep it limited.
- **Look & feel**: Regency theme with our own icons, names and texts – the book's content must not be copied (copyright).
- **Name lists**: the actual 120 first names and 120 surnames (English, not translated).
- **Clue flavour texts** (like Emma's star sign or green eyes in the book).
- **Sharing**: optional feature, details (locking while the owner plays) later.


## Decisions

### Project
- Name: **Mr Right** (`mr-right`) – may change later.
- Backend language: **Kotlin** (like `avalons_final_stand`).

### Game
- **Attributes**: the book's six attributes and their values are used 1:1, with our own icons
  (income, star sign element, eye colour, hair colour, temperament, interest).
- **Ending**: the number of suitors left after all clues (*finalists*) is configurable from **1 to 5**
  (book: 2). With more than one finalist, the book's tie-break picks Mr Right: fewest consonants in the
  full name (y counts as a consonant). The generator guarantees that this minimum is unique.
  With one finalist there is no tie-break step.
- **Clue types**: only the book's clue types (see table above), with randomised parameters – e.g. a different
  attribute value, letter range, vowel count or forbidden letter per puzzle.
- **Clue set**: always **12 clues like the book** – one per attribute (6) and one per name clue type (6).
  The order is **fixed** (the book's order of subjects); the variety comes from the parameters.
  A shuffled order and a random ratio of attribute and name clues were considered and dropped: shuffling adds
  little, and a random ratio would need two clues on the same attribute (redundant or contradicting) or new
  clue types beyond the book.
- **Clue parameters** (keep it simple, not too difficult):
  - "one of" clues allow **2 or 3 values**, never 1; "is not" excludes exactly one value.
    Like the book, exactly one of the five non-income attributes gets an "is not" clue, the others "one of".
  - Income: "at least ££" or "at least £££".
  - Initials in a letter range: only **A–M** or **N–Z** (both initials).
  - Vowel counts (first name, surname): exactly 2 or 3.
  - Forbidden letter: one of B, H, K, P, W.
- **Letters**: vowels are a, e, i, o, u; **y is a consonant** – for the vowel clues and the tie-break.
  Names contain only the letters A–Z (no hyphens or apostrophes); the "Mr" prefix is not part of the name.
- **Names**: catalog of **120 first names × 120 surnames** (14,400 combinations). A full name may appear more than
  once in a directory, but no two suitors are completely identical (name + all attributes).
- **Directory order**: unsorted, like the book. Sorting/filtering belongs to the postponed player aids.
- **Directory sections**: the directory is split into numbered sections of 100 suitors for navigation and
  progress (the book has 34 sections) – derived from the position, not stored.
- **Size and difficulty** are configurable when creating a new game: number of suitors (**100–10,000**) and
  number of finalists (1–5). The number of clues is always 12. No further difficulty settings for now
  (e.g. the share of near misses – maybe later, probably not).
- **Finalists in the UI**: the finalist count is shown from the start. When exactly that many suitors are left
  and they are the correct finalists, the tie-break is revealed and the player picks Mr Right among them.
  If they are not the correct finalists, a hint says that not all remaining suitors match the clues, the
  tie-break stays hidden and it **counts as a wrong guess**.
- **Solving**: the player marks a candidate as Mr Right and confirms it in a dialog.
  A wrong answer is reported and counted. There is no "losing" – the game continues.
- **Cheating is accepted**: the solution check runs offline on hashes of the finalist and Mr Right ids
  (see architecture). With at most 10,000 suitors these hashes can be brute-forced. That is fine –
  it's a single-player game with nothing to win; whoever cheats only spoils their own puzzle.

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
  The generator will rarely change, so seed stability is accepted as a risk. Each game also stores the
  **`generatorVersion`** it was created with, so a breaking generator change can be detected. Kept simple:
  breaking changes are rare, outdated games are marked as such and can only be deleted – no migration.
- Login: JWT lifetime **30 days**, so offline sessions and device switches don't keep forcing a new login.
- Generation: **constructive** – clues and finalists first, then near misses and random suitors around them
  (details in architecture.md).
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
