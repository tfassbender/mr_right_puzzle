import type { GameStateDto, StateUpdateDto } from '../api/gamesApi';

/**
 * The player's progress in memory. Like the server's GameState, minus the version (the SyncManager tracks it).
 * Once [finalistsFound] is reached the directory is locked: only picking Mr Right is left.
 */
export interface PlayState {
  crossedOut: ReadonlySet<number>;
  finalistsFound: boolean;
  mrRightId: number | null;
  wrongGuesses: number;
  updatedAt: string; // client time of the last change
}

export function fromDto(dto: GameStateDto): PlayState {
  return {
    crossedOut: new Set(dto.crossedOut),
    finalistsFound: dto.finalistsFound,
    mrRightId: dto.mrRightId,
    wrongGuesses: dto.wrongGuesses,
    updatedAt: dto.updatedAt,
  };
}

export function toUpdateDto(state: PlayState): StateUpdateDto {
  return {
    crossedOut: [...state.crossedOut].sort((a, b) => a - b),
    finalistsFound: state.finalistsFound,
    mrRightId: state.mrRightId,
    wrongGuesses: state.wrongGuesses,
    updatedAt: state.updatedAt,
  };
}

/** Same progress, ignoring when it happened – e.g. to skip a conflict dialog for identical states. */
export function sameProgress(a: PlayState, b: PlayState): boolean {
  return (
    a.finalistsFound === b.finalistsFound &&
    a.mrRightId === b.mrRightId &&
    a.wrongGuesses === b.wrongGuesses &&
    a.crossedOut.size === b.crossedOut.size &&
    [...a.crossedOut].every((id) => b.crossedOut.has(id))
  );
}

/** Crosses out or restores a suitor. No-op once the finalists are found. */
export function toggleSuitor(state: PlayState, id: number, now: string): PlayState {
  if (state.finalistsFound) return state;
  const crossedOut = new Set(state.crossedOut);
  if (!crossedOut.delete(id)) crossedOut.add(id);
  return { ...state, crossedOut, updatedAt: now };
}

export function remainingIds(state: PlayState, suitorCount: number): number[] {
  const remaining: number[] = [];
  for (let id = 0; id < suitorCount; id++) {
    if (!state.crossedOut.has(id)) remaining.push(id);
  }
  return remaining;
}

export function withWrongGuess(state: PlayState, now: string): PlayState {
  return { ...state, wrongGuesses: state.wrongGuesses + 1, updatedAt: now };
}
