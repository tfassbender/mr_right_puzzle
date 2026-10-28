import { signal } from '@preact/signals';
import { ApiError } from '../api/client';
import type { GameStateDto, StateUpdateDto } from '../api/gamesApi';
import { fromDto, sameProgress, toUpdateDto, type PlayState } from '../game/playState';
import type { LocalGameState } from '../storage/gameStorage';

const DEBOUNCE_MS = 3_000;
const MAX_WAIT_MS = 30_000;
const RETRY_MS = 30_000;

/**
 * - `synced`: the server has everything
 * - `pending`: local changes wait for the debounced save
 * - `offline`: the server is unreachable, changes are only stored on this device (retried)
 * - `error`: the server rejected the state (e.g. the game was deleted) – not retried automatically
 */
export type SyncStatus = 'synced' | 'pending' | 'offline' | 'error';

/** Both sides changed since the last sync: the player picks one. */
export interface Conflict {
  local: PlayState;
  server: PlayState;
  serverVersion: number;
}

/** The outside world of the SyncManager, injected so it can be tested without a server or IndexedDB. */
export interface SyncDeps {
  saveState(gameId: string, baseVersion: number, state: StateUpdateDto, keepalive: boolean): Promise<GameStateDto>;
  loadState(gameId: string): Promise<GameStateDto>;
  storeLocal(gameId: string, local: LocalGameState): Promise<void>;
}

/**
 * Keeps one game's state in sync (see doc/architecture.md, "Saving & Sync"):
 * every change is stored locally at once and saved to the server debounced (3 s after the last change,
 * at the latest every 30 s). Optimistic locking via the server version; a 409 or a newer server state
 * is taken over silently when there are no local changes, otherwise it becomes a [conflict].
 */
export class SyncManager {
  readonly status = signal<SyncStatus>('synced');
  readonly conflict = signal<Conflict | null>(null);

  private debounceTimer: ReturnType<typeof setTimeout> | undefined;
  private retryTimer: ReturnType<typeof setTimeout> | undefined;
  private firstPendingChangeAt: number | null = null;
  private saving = false;

  constructor(
    private readonly gameId: string,
    private local: LocalGameState,
    private readonly deps: SyncDeps,
    /** Called when the server state replaces the local one (other device, conflict resolution). */
    private readonly onServerState: (state: PlayState) => void,
  ) {
    if (local.dirty) this.status.value = 'pending';
  }

  changed(state: PlayState): void {
    this.setLocal({ ...this.local, state, dirty: true });
    if (this.status.value !== 'offline') this.status.value = 'pending';
    this.firstPendingChangeAt ??= Date.now();
    const waited = Date.now() - this.firstPendingChangeAt;
    clearTimeout(this.debounceTimer);
    this.debounceTimer = setTimeout(() => this.flush(), Math.max(0, Math.min(DEBOUNCE_MS, MAX_WAIT_MS - waited)));
  }

  /** Sends local changes now. [keepalive] lets the request survive a closing tab. */
  async flush(keepalive = false): Promise<void> {
    clearTimeout(this.debounceTimer);
    if (!this.local.dirty || this.saving || this.conflict.value) return;
    this.firstPendingChangeAt = null;
    this.saving = true;
    const sent = this.local.state;
    try {
      const saved = await this.deps.saveState(this.gameId, this.local.baseVersion, toUpdateDto(sent), keepalive);
      // Changes made while saving stay dirty and are sent with the next flush.
      const unchanged = this.local.state === sent;
      this.setLocal({ ...this.local, baseVersion: saved.version, dirty: !unchanged });
      this.status.value = unchanged ? 'synced' : 'pending';
      if (!unchanged) this.changed(this.local.state);
    } catch (e) {
      this.handleFailure(e);
    } finally {
      this.saving = false;
    }
  }

  /** Picks up changes from another device (on open, tab return, reconnect). Local changes are flushed. */
  async refresh(): Promise<void> {
    if (this.saving || this.conflict.value) return;
    try {
      this.takeServerState(await this.deps.loadState(this.gameId));
      if (this.local.dirty) await this.flush();
    } catch (e) {
      this.handleFailure(e);
    }
  }

  resolveConflict(choice: 'local' | 'server'): void {
    const conflict = this.conflict.value;
    if (!conflict) return;
    this.conflict.value = null;
    if (choice === 'server') {
      this.adopt(conflict.server, conflict.serverVersion);
    } else {
      this.setLocal({ ...this.local, baseVersion: conflict.serverVersion, dirty: true });
      this.flush();
    }
  }

  dispose(): void {
    clearTimeout(this.debounceTimer);
    clearTimeout(this.retryTimer);
  }

  private takeServerState(dto: GameStateDto): void {
    if (dto.version === this.local.baseVersion) return; // nothing new on the server
    const server = fromDto(dto);
    if (!this.local.dirty) {
      this.adopt(server, dto.version);
    } else if (sameProgress(this.local.state, server)) {
      this.setLocal({ ...this.local, baseVersion: dto.version, dirty: false });
      this.status.value = 'synced';
    } else {
      this.conflict.value = { local: this.local.state, server, serverVersion: dto.version };
    }
  }

  private adopt(state: PlayState, version: number): void {
    this.setLocal({ state, baseVersion: version, dirty: false });
    this.status.value = 'synced';
    this.onServerState(state);
  }

  private handleFailure(error: unknown): void {
    if (error instanceof ApiError && error.code === 'VERSION_CONFLICT') {
      this.takeServerState((error.body as { state: GameStateDto }).state);
    } else if (error instanceof ApiError && error.code !== 'NETWORK') {
      this.status.value = 'error';
    } else {
      this.status.value = 'offline';
      clearTimeout(this.retryTimer);
      this.retryTimer = setTimeout(() => this.refresh(), RETRY_MS);
    }
  }

  private setLocal(local: LocalGameState): void {
    this.local = local;
    this.deps.storeLocal(this.gameId, local);
  }
}
