import { loadGame, loadState, saveState } from '../api/gamesApi';
import { GameStore } from '../game/gameStore';
import { fromDto, withPossibleMatches } from '../game/playState';
import { loadLocalState, loadStoredGame, storeGame, storeLocalState } from '../storage/gameStorage';
import { SyncManager, type SyncDeps } from './syncManager';

const syncDeps: SyncDeps = { saveState, loadState, storeLocal: storeLocalState };

export interface GameSession {
  store: GameStore;
  sync: SyncManager;
}

/**
 * Opens a game from the local copy if there is one (works offline), otherwise from the server, which then
 * becomes the local copy. The caller refreshes from the server afterwards to pick up other devices' changes.
 */
export async function openGameSession(gameId: string): Promise<GameSession> {
  let game = await loadStoredGame(gameId);
  if (!game) {
    game = await loadGame(gameId);
    await storeGame(game);
  }
  const stored = await loadLocalState(gameId);
  const local = stored
    ? { ...stored, state: withPossibleMatches(stored.state) }
    : { state: fromDto(game.state), baseVersion: game.state.version, dirty: false };

  // The store and the sync manager report to each other: player changes go to the sync,
  // server states coming in through the sync replace the store's state.
  let store: GameStore | null = null;
  const sync = new SyncManager(gameId, local, syncDeps, (state) => store?.replaceState(state));
  store = new GameStore(game, local.state, (state) => sync.changed(state));
  return { store, sync };
}
