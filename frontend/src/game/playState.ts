import type { GameStateDto, StateUpdateDto } from '../api/gamesApi';

/**
 * The player's progress in memory. Like the server's GameState, minus the version (the SyncManager tracks it).
 * A suitor is unmarked, crossed out (no match) or starred (possible match) – never both.
 * Once [finalistsFound] is reached the directory is locked: only picking Mr Right is left.
 */
export interface PlayState {
  crossedOut: ReadonlySet<number>;
  possibleMatches: ReadonlySet<number>;
  finalistsFound: boolean;
  mrRightId: number | null;
  wrongGuesses: number;
  updatedAt: string; // client time of the last change
}

export type Marker = 'none' | 'noMatch' | 'possibleMatch';

export function fromDto(dto: GameStateDto): PlayState {
  return {
    crossedOut: new Set(dto.crossedOut),
    possibleMatches: new Set(dto.possibleMatches ?? []),
    finalistsFound: dto.finalistsFound,
    mrRightId: dto.mrRightId,
    wrongGuesses: dto.wrongGuesses,
    updatedAt: dto.updatedAt,
  };
}

/** States stored on the device before possible matches existed don't have them. */
export function withPossibleMatches(state: PlayState): PlayState {
  return state.possibleMatches ? state : { ...state, possibleMatches: new Set() };
}

const sorted = (ids: ReadonlySet<number>) => [...ids].sort((a, b) => a - b);

export function toUpdateDto(state: PlayState): StateUpdateDto {
  return {
    crossedOut: sorted(state.crossedOut),
    possibleMatches: sorted(state.possibleMatches),
    finalistsFound: state.finalistsFound,
    mrRightId: state.mrRightId,
    wrongGuesses: state.wrongGuesses,
    updatedAt: state.updatedAt,
  };
}

function sameIds(a: ReadonlySet<number>, b: ReadonlySet<number>): boolean {
  return a.size === b.size && [...a].every((id) => b.has(id));
}

/** Same progress, ignoring when it happened – e.g. to skip a conflict dialog for identical states. */
export function sameProgress(a: PlayState, b: PlayState): boolean {
  return (
    a.finalistsFound === b.finalistsFound &&
    a.mrRightId === b.mrRightId &&
    a.wrongGuesses === b.wrongGuesses &&
    sameIds(a.crossedOut, b.crossedOut) &&
    sameIds(a.possibleMatches, b.possibleMatches)
  );
}

export function markerOf(state: PlayState, id: number): Marker {
  if (state.crossedOut.has(id)) return 'noMatch';
  return state.possibleMatches.has(id) ? 'possibleMatch' : 'none';
}

/** Sets a suitor's marker, removing the other one. No-op once the finalists are found. */
export function withMarker(state: PlayState, id: number, marker: Marker, now: string): PlayState {
  if (state.finalistsFound || markerOf(state, id) === marker) return state;
  const crossedOut = new Set(state.crossedOut);
  const possibleMatches = new Set(state.possibleMatches);
  crossedOut.delete(id);
  possibleMatches.delete(id);
  if (marker === 'noMatch') crossedOut.add(id);
  if (marker === 'possibleMatch') possibleMatches.add(id);
  return { ...state, crossedOut, possibleMatches, updatedAt: now };
}

/** The finalists are found: everybody else is crossed out, the finalists are starred. */
export function withFinalists(state: PlayState, finalists: number[], suitorCount: number, now: string): PlayState {
  const possibleMatches = new Set(finalists);
  const crossedOut = new Set<number>();
  for (let id = 0; id < suitorCount; id++) {
    if (!possibleMatches.has(id)) crossedOut.add(id);
  }
  return { ...state, crossedOut, possibleMatches, finalistsFound: true, updatedAt: now };
}

/** Suitors not crossed out – unmarked and starred ones. */
export function remainingIds(state: PlayState, suitorCount: number): number[] {
  const remaining: number[] = [];
  for (let id = 0; id < suitorCount; id++) {
    if (!state.crossedOut.has(id)) remaining.push(id);
  }
  return remaining;
}

/** The first suitor with no marker at all, or null when everybody is marked. */
export function firstUnmarkedId(state: PlayState, suitorCount: number): number | null {
  for (let id = 0; id < suitorCount; id++) {
    if (markerOf(state, id) === 'none') return id;
  }
  return null;
}

export function withWrongGuess(state: PlayState, now: string): PlayState {
  return { ...state, wrongGuesses: state.wrongGuesses + 1, updatedAt: now };
}
