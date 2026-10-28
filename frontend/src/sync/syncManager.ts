import { saveState } from '../api/gamesApi';
import { toUpdateDto, type PlayState } from '../game/playState';

const DEBOUNCE_MS = 3_000;
const MAX_WAIT_MS = 30_000;

/**
 * Saves the state of one game to the server: debounced 3 s after the last change, at the latest every 30 s
 * while changes keep coming. [baseVersion] is the server version the local state builds on (optimistic locking).
 */
export class SyncManager {
  private pending: PlayState | null = null;
  private debounceTimer: ReturnType<typeof setTimeout> | undefined;
  private firstChangeAt: number | null = null;

  constructor(
    private readonly gameId: string,
    private baseVersion: number,
  ) {}

  changed(state: PlayState): void {
    this.pending = state;
    this.firstChangeAt ??= Date.now();
    clearTimeout(this.debounceTimer);
    const waited = Date.now() - this.firstChangeAt;
    this.debounceTimer = setTimeout(() => this.flush(), Math.min(DEBOUNCE_MS, MAX_WAIT_MS - waited));
  }

  async flush(): Promise<void> {
    clearTimeout(this.debounceTimer);
    const state = this.pending;
    if (!state) return;
    this.pending = null;
    this.firstChangeAt = null;
    try {
      const saved = await saveState(this.gameId, this.baseVersion, toUpdateDto(state));
      this.baseVersion = saved.version;
    } catch {
      // Keep it for the next attempt, unless something newer came in meanwhile.
      this.pending ??= state;
    }
  }
}
