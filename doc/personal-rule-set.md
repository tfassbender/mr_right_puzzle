# Personal Rule Set – Planning

Planning for the personal rule set (Phase 8c/8d in [implementation_plan.md](implementation_plan.md)).
**Status:** implemented (Phase 8c) – values, icons and texts may still be polished. Open points are marked **Q**.
Rule set id `EVA`, shown as **"Eva's rules"** / **"Evas Regeln"**.

## The criteria (as given)

1. Body height: 1.88 m – 1.95 m
2. Age: −3 to +10 years from her age (born 1994) – must follow the current date
3. Has humor (can be treated as a character type)
4. Is spontaneous
5. Has a car (icons for car, bike, …)
6. Has a job (money icons, but with € signs)
7. Has his own friends and integrates her
8. Good relationship with his family
9. Likes good food and going out
10. Cares about nutrition
11. Open-minded, communicative, self-assured
12. Prefers short trips and activities over long trips and expensive hotels
13. Is loyal
14. Good looking / sporty
15. Doesn't want kids
16. No drugs or cigarettes
17. Makes rules for the relationship and sticks to them

## Constraints from the puzzle mechanics

- **One value per attribute.** Every suitor has exactly one value per attribute, and the clues are AND filters on
  them. Two criteria that Mr Right must *both* meet can't be two values of the same attribute – either each gets
  its own attribute, or they are merged into one value (e.g. "friends *and* family").
- **Space on a phone.** Today a directory row has 6 icons. 16 attributes don't fit on ~390 px. Proposal: merge to
  **12 attributes = 12 clues, like the book**, and show the icons in **two lines** per row (taller rows).
- **Single-value clues need weighted values.** Most of her criteria accept exactly one value. With evenly spread
  values the first clue would already remove ~75 % of the directory. So her attributes get **weights**: most
  suitors have the "right" value for most attributes and fail only one or two criteria. That keeps the player
  checking every icon – like the book, where each clue removes only part of the directory.
  The planted near misses (one per criterion) still make every criterion count.
- **Names:** the name catalog stays. Proposal: **no name clues** – all 12 clues are her real criteria, the names
  are just names. The tie-break may still use the names (open, see below).

## The 12 attributes

| #  | Attribute (id)          | Values (icon)                                                                                  | Eva's criterion → clue                | Covers |
|----|-------------------------|------------------------------------------------------------------------------------------------|---------------------------------------|--------|
| 1  | Height (`HEIGHT`)       | number in cm, 165–205, shown as text "1.91"                                                    | between 188 and 195 (inclusive)       | 1      |
| 2  | Age (`BIRTH_YEAR`)      | birth year, 1975–2003, **shown as age** (current year − birth year)                            | born 1984–1997 (= her age −3 … +10)   | 2      |
| 3  | Income (`INCOME`)       | none (—), €, €€, €€€                                                                           | not "none"                            | 6      |
| 4  | Vehicle (`VEHICLE`)     | car 🚗, motorbike 🏍️, bike 🚲, bus 🚌                                                          | car                                   | 5      |
| 5  | Character (`CHARACTER`) | faces like the book's temperament: loyal 🥰, flirty 😏, moody 😤, aloof 🥶                       | loyal                                 | 13     |
| 6  | Manner (`MANNER`)       | open & funny 😄, shy 🙈, arrogant 🦚, boring 😑                                                 | open & funny                          | 3, 11  |
| 7  | Social (`SOCIAL`)       | friends & family 👨‍👩‍👦, only friends 🎉, only family 🏠, loner 🐺                              | friends & family                      | 7, 8   |
| 8  | Food (`FOOD`)           | healthy foodie 🥗, fast food 🍔, diet fanatic (never eats out) 🥦, can't be bothered 🥫         | healthy foodie                        | 9, 10  |
| 9  | Leisure (`LEISURE`)     | spontaneous short trips 🎒, luxury hotels 🏨, long-haul travel ✈️, homebody 🛋️                 | spontaneous short trips               | 4, 12  |
| 10 | Fitness (`FITNESS`)     | athletic 💪, fit 🏃, average 🚶, couch potato 🥔                                               | athletic or fit                       | 14     |
| 11 | Kids (`KIDS`)           | no kids 🧑🏻‍🤝‍🧑🏻, wants kids 👶, undecided 🤷                                                         | no kids                               | 15     |
| 12 | Vices (`VICES`)         | none ✨, a little alcohol 🍺, a lot of alcohol 🍻, cigarettes 🚬, drugs 💊                     | none or a little alcohol              | 16     |

Merges, so the criteria fit into 12 attributes:

- **Manner** = criteria 3 + 11: humor goes together with open-minded, communicative and self-assured
  ("open & funny"); "boring" is the value without humor. This keeps **Character** free for loyalty, with faces
  like the book's temperament icons.
- **Social** = criteria 7 + 8: only "friends & family" fits ("his own friends, and he brings her into them").
- **Food** = criteria 9 + 10: the "healthy foodie" enjoys good food and going out *and* cares about nutrition.
- **Leisure** = criteria 4 + 12: spontaneity is modelled as "spontaneous short trips and activities".
- **Vices** = criterion 16. A suitor has at most one vice. A little alcohol (one beer) is fine, a lot (two beers) is not.
- **"Makes rules and sticks to them"** (criterion 17) is **not a clue**. It may come back in the finale
  (see below).

The values and icons are a first pick and can be improved later.

## Tie-break and finale

- **Tie-break between the finalists:** fewest consonants in the full name, like the book.
- **Mr Right has at least 9 consonants** – as many as "Tobias Faßbender" (T, b, s + F, ß, b, n, d, r = 9).
  Decided before the finale dropped the consonant comparison; kept, it does no harm.
- **Finale:** after the correct pick, one more suitor arrives: **Tobias Faßbender**. He is not in the directory.
  His values match every criterion:

  | Height | Born | Job | Vehicle | Kids    | Vices            | Character | Manner       | Social           | Food           | Leisure                 | Fitness |
  |--------|------|-----|---------|---------|------------------|-----------|--------------|------------------|----------------|-------------------------|---------|
  | 1.88 m | 1995 | €€  | car     | no kids | a little alcohol | loyal     | open & funny | friends & family | healthy foodie | spontaneous short trips | fit     |

- **The only tie-breaker in the finale: who treats her better**, shown as a progress bar from 1 to 10.
  Tobias has 10, Mr Right a value between 5 and 8 (fixed per game, derived from its seed). No consonant comparison.

### Finale flow (implemented)

1. The player picks Mr Right correctly → "Congratulations" as today, and the finale dialog opens:
   "One more suitor" – Eva is about to choose when one last gentleman arrives who meets all her wishes too.
2. Mr Right's and Tobias's entries (name + icons), each with a "how well he treats her" bar (5–8 vs 10)
   and a "Choose him" button – the last tie-break is her own choice.
3. Choosing Mr Right only shows a playful hint ("Are you sure? Have another look …"); it is not a wrong guess
   and she can choose again.
4. Choosing Tobias shows the result: Tobias is Eva's Mr Right, with a closing text (a draft – to be improved).
   The choice isn't saved – reopening the finale starts at the choice again.

The finale opens again from the solved game ("Wait – one more suitor has just arrived …"). Texts in en + de.
The finale shows the names without small caps – they would turn the ß in "Faßbender" into something like a B.
## What is new for the code

- **Clue types:**
  - `ATTRIBUTE_IS` – exactly one value (the book's "one of" always allows 2–3 values).
  - `ATTRIBUTE_BETWEEN` – a range on an ordinal attribute (height, birth year).
  - Income uses the existing `ATTRIBUTE_IS_NOT` ("not none"); fitness and vices use `ATTRIBUTE_IS_ONE_OF`.
- **Number attributes:** height and birth year are ordinal attributes with many values (one per cm / year),
  shown as text instead of an icon. The age is computed in the frontend from the current year, and so is the
  clue text ("between 29 and 42 years old" in 2026). The puzzle itself stores birth years, so it doesn't change
  over time – a saved game stays valid on her next birthday.
- **Weights per value**, used only by Eva's rules (the book stays evenly distributed, so its puzzles don't change).
  About **80 %** of the suitors have a right value per attribute, so a random suitor fails ~2–3 of the 12 criteria.
- **No name clues:** all 12 clues are her criteria, the names are just names.
- **Tie-break constraint:** the generator picks Mr Right's name among the names with at least 9 consonants.
- **Clue texts per criterion:** the book renders clues from generic templates; her clues get their own texts
  (en + de), e.g. "He drives a car – no more waiting for the bus."
- **Two-line icon rows** for her rule set, laid out as a grid so every attribute keeps its column; the
  directory's row height depends on the rule set.
- Number of suitors and finalists stay configurable.

## Still open

- Intro text: done – "Eva's search for Mr Right" in the "Story & rules" dialog (texts `intro_EVA_*`).
