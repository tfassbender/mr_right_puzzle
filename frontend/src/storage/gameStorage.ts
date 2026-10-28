import { del, get, set } from 'idb-keyval';
import type { GameDto, GameSummary } from '../api/gamesApi';
import { session } from '../auth/session';
import type { PlayState } from '../game/playState';

/**
 * The local copy of a game's progress. [baseVersion] is the server version [state] builds on;
 * [dirty] means it has changes the server doesn't know yet.
 */
export interface LocalGameState {
  state: PlayState;
  baseVersion: number;
  dirty: boolean;
}

// Keys are namespaced by user, so two accounts on one device never see each other's games.
function key(...parts: string[]): string {
  return [session.value?.user.userId ?? 'anonymous', ...parts].join(':');
}

// IndexedDB can be unavailable (private mode, old browsers): then the game just isn't kept offline.
async function safely<T>(action: () => Promise<T>, fallback: T): Promise<T> {
  try {
    return await action();
  } catch {
    return fallback;
  }
}

/** The puzzle itself never changes, so it is stored once when a game is first opened. */
export function storeGame(game: GameDto): Promise<void> {
  return safely(() => set(key('game', game.id), game), undefined);
}

export function loadStoredGame(id: string): Promise<GameDto | undefined> {
  return safely(() => get<GameDto>(key('game', id)), undefined);
}

export function storeLocalState(gameId: string, local: LocalGameState): Promise<void> {
  return safely(() => set(key('state', gameId), local), undefined);
}

export function loadLocalState(gameId: string): Promise<LocalGameState | undefined> {
  return safely(() => get<LocalGameState>(key('state', gameId)), undefined);
}

export function deleteStoredGame(id: string): Promise<void> {
  return safely(async () => {
    await del(key('game', id));
    await del(key('state', id));
  }, undefined);
}

/** The last game list from the server, shown when offline. */
export function storeGameList(games: GameSummary[]): Promise<void> {
  return safely(() => set(key('list'), games), undefined);
}

export function loadStoredGameList(): Promise<GameSummary[] | undefined> {
  return safely(() => get<GameSummary[]>(key('list')), undefined);
}
