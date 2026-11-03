import { apiRequest } from './client';

// DTO types mirroring backend/src/main/kotlin/com/tfassbender/mrright/rest/GameDtos.kt

export type Income = 'AT_LEAST_1K' | 'AT_LEAST_2K' | 'AT_LEAST_4K' | 'AT_LEAST_10K';

/** [attributes] maps every attribute id of the game's rule set to the suitor's value, e.g. EYE_COLOR → GREEN. */
export interface Suitor {
  id: number;
  firstName: string;
  lastName: string;
  attributes: Record<string, string>;
}

/** Language-neutral clues: a type plus parameters, the texts are rendered from the messages. */
export type Clue =
  | { type: 'INCOME_AT_LEAST'; level: Income }
  | { type: 'ATTRIBUTE_IS_NOT'; attribute: string; value: string }
  | { type: 'ATTRIBUTE_IS_ONE_OF'; attribute: string; values: string[] }
  | { type: 'ATTRIBUTE_IS'; attribute: string; value: string }
  | { type: 'ATTRIBUTE_BETWEEN'; attribute: string; min: number; max: number }
  | { type: 'INITIALS_DIFFER' }
  | { type: 'INITIALS_IN_RANGE'; range: 'A_TO_M' | 'N_TO_Z' }
  | { type: 'SURNAME_START_AND_END_DIFFER' }
  | { type: 'FIRST_NAME_VOWEL_COUNT'; count: number }
  | { type: 'SURNAME_VOWEL_COUNT'; count: number }
  | { type: 'NAME_EXCLUDES_LETTER'; letter: string };

/** The state as the server stores it. [version] is assigned by the server on every save. */
export interface GameStateDto {
  crossedOut: number[];
  possibleMatches?: number[]; // missing in games stored on the device before possible matches existed
  finalistsFound: boolean;
  mrRightId: number | null;
  solved: boolean;
  wrongGuesses: number;
  version: number;
  updatedAt: string;
}

/** The state the client sends – the server assigns the version. */
export interface StateUpdateDto {
  crossedOut: number[];
  possibleMatches: number[];
  finalistsFound: boolean;
  mrRightId: number | null;
  wrongGuesses: number;
  updatedAt: string;
}

export interface GameSummary {
  id: string;
  name: string | null; // null for games created before names existed
  ruleSet: string;
  seed: string;
  suitorCount: number;
  finalistCount: number;
  createdAt: string;
  outdated: boolean;
  crossedOutCount: number;
  finalistsFound: boolean;
  solved: boolean;
  wrongGuesses: number;
  updatedAt: string;
}

/** Eva's rules: the suitor who arrives after Mr Right is found, and both ratings for "who treats her better". */
export interface Finale {
  suitor: Suitor; // not in the directory (id -1)
  suitorRating: number;
  mrRightRating: number;
}

export interface GameDto {
  id: string;
  name: string | null;
  ruleSet: string;
  seed: string;
  suitorCount: number;
  finalistCount: number;
  generatorVersion: number;
  createdAt: string;
  suitors: Suitor[];
  clues: Clue[];
  finalistsHash: string;
  solutionHash: string;
  state: GameStateDto;
  finale?: Finale | null; // missing in games stored on the device before the finale existed
}

export interface NewGame {
  suitorCount: number;
  finalistCount: number;
  seed?: string;
  name?: string;
  ruleSet?: string; // default: the book
}

export const SUITOR_COUNT_MIN = 100;
export const SUITOR_COUNT_MAX = 10_000;
export const FINALIST_COUNT_MAX = 5;

export function listGames(): Promise<GameSummary[]> {
  return apiRequest<GameSummary[]>('GET', '/games');
}

export function createGame(game: NewGame): Promise<GameSummary> {
  return apiRequest<GameSummary>('POST', '/games', game);
}

export function loadGame(id: string): Promise<GameDto> {
  return apiRequest<GameDto>('GET', `/games/${id}`);
}

export function loadState(id: string): Promise<GameStateDto> {
  return apiRequest<GameStateDto>('GET', `/games/${id}/state`);
}

/** Rejects with an ApiError `VERSION_CONFLICT` (409) whose body carries the current server `state`. */
export function saveState(id: string, baseVersion: number, state: StateUpdateDto, keepalive = false): Promise<GameStateDto> {
  return apiRequest<GameStateDto>('PUT', `/games/${id}/state`, { baseVersion, state }, keepalive);
}

export function deleteGame(id: string): Promise<void> {
  return apiRequest<void>('DELETE', `/games/${id}`);
}
