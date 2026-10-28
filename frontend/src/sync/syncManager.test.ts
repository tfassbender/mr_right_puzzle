import { afterEach, beforeEach, describe, expect, it, vi, type Mock } from 'vitest';
import { ApiError } from '../api/client';
import type { GameStateDto } from '../api/gamesApi';
import type { PlayState } from '../game/playState';
import type { LocalGameState } from '../storage/gameStorage';
import { SyncManager, type SyncDeps } from './syncManager';

function play(crossedOut: number[], updatedAt = '2026-10-28T10:00:00Z'): PlayState {
  return { crossedOut: new Set(crossedOut), finalistsFound: false, mrRightId: null, wrongGuesses: 0, updatedAt };
}

function dto(crossedOut: number[], version: number): GameStateDto {
  return {
    crossedOut,
    finalistsFound: false,
    mrRightId: null,
    solved: false,
    wrongGuesses: 0,
    version,
    updatedAt: '2026-10-28T09:00:00Z',
  };
}

const NETWORK_ERROR = new ApiError(0, 'NETWORK', 'offline');

function conflictError(state: GameStateDto) {
  return new ApiError(409, 'VERSION_CONFLICT', 'conflict', { code: 'VERSION_CONFLICT', state });
}

describe('SyncManager', () => {
  let deps: { saveState: ReturnType<typeof vi.fn>; loadState: ReturnType<typeof vi.fn>; storeLocal: ReturnType<typeof vi.fn> };
  let onServerState: Mock<(state: PlayState) => void>;

  beforeEach(() => {
    vi.useFakeTimers();
    deps = {
      saveState: vi.fn((_id: string, baseVersion: number) => Promise.resolve(dto([], baseVersion + 1))),
      loadState: vi.fn(),
      storeLocal: vi.fn(() => Promise.resolve()),
    };
    onServerState = vi.fn<(state: PlayState) => void>();
  });

  afterEach(() => vi.useRealTimers());

  function newSync(local: LocalGameState = { state: play([]), baseVersion: 5, dirty: false }) {
    return new SyncManager('g1', local, deps as unknown as SyncDeps, onServerState);
  }

  it('stores every change locally at once and saves to the server debounced', async () => {
    const sync = newSync();

    sync.changed(play([1]));
    sync.changed(play([1, 2]));
    expect(deps.storeLocal).toHaveBeenLastCalledWith('g1', { state: play([1, 2]), baseVersion: 5, dirty: true });
    expect(sync.status.value).toBe('pending');
    expect(deps.saveState).not.toHaveBeenCalled();

    await vi.advanceTimersByTimeAsync(3_000);
    expect(deps.saveState).toHaveBeenCalledTimes(1);
    expect(deps.saveState.mock.calls[0][1]).toBe(5);
    expect(deps.saveState.mock.calls[0][2].crossedOut).toEqual([1, 2]);
    expect(sync.status.value).toBe('synced');
    expect(deps.storeLocal).toHaveBeenLastCalledWith('g1', { state: play([1, 2]), baseVersion: 6, dirty: false });
  });

  it('saves at the latest every 30 s while changes keep coming', async () => {
    const sync = newSync();

    for (let i = 0; i < 15; i++) {
      sync.changed(play([i]));
      await vi.advanceTimersByTimeAsync(2_000);
    }

    expect(deps.saveState).toHaveBeenCalledTimes(1);
  });

  it('goes offline on a network error and retries later', async () => {
    deps.saveState.mockRejectedValueOnce(NETWORK_ERROR);
    deps.loadState.mockResolvedValue(dto([], 5));
    const sync = newSync();

    sync.changed(play([1]));
    await vi.advanceTimersByTimeAsync(3_000);
    expect(sync.status.value).toBe('offline');

    await vi.advanceTimersByTimeAsync(30_000);
    expect(deps.saveState).toHaveBeenCalledTimes(2);
    expect(sync.status.value).toBe('synced');
  });

  it('takes a newer server state silently when there are no local changes', async () => {
    deps.loadState.mockResolvedValue(dto([7], 8));
    const sync = newSync();

    await sync.refresh();

    expect(onServerState).toHaveBeenCalledWith(expect.objectContaining({ crossedOut: new Set([7]) }));
    expect(deps.storeLocal).toHaveBeenLastCalledWith('g1', expect.objectContaining({ baseVersion: 8, dirty: false }));
  });

  it('a 409 with different progress on both sides becomes a conflict', async () => {
    deps.saveState.mockRejectedValueOnce(conflictError(dto([9], 6)));
    const sync = newSync();

    sync.changed(play([1]));
    await sync.flush();

    expect(sync.conflict.value?.serverVersion).toBe(6);
    expect(sync.conflict.value?.local.crossedOut).toEqual(new Set([1]));
    expect(sync.conflict.value?.server.crossedOut).toEqual(new Set([9]));
  });

  it('a 409 with the same progress just takes over the server version', async () => {
    deps.saveState.mockRejectedValueOnce(conflictError(dto([1], 6)));
    const sync = newSync();

    sync.changed(play([1]));
    await sync.flush();

    expect(sync.conflict.value).toBeNull();
    expect(sync.status.value).toBe('synced');
    expect(deps.storeLocal).toHaveBeenLastCalledWith('g1', expect.objectContaining({ baseVersion: 6, dirty: false }));
  });

  it('keeping the local version overwrites the server based on its version', async () => {
    deps.saveState.mockRejectedValueOnce(conflictError(dto([9], 6)));
    const sync = newSync();
    sync.changed(play([1]));
    await sync.flush();

    sync.resolveConflict('local');
    await vi.runAllTimersAsync();

    expect(deps.saveState).toHaveBeenLastCalledWith('g1', 6, expect.objectContaining({ crossedOut: [1] }), false);
    expect(sync.status.value).toBe('synced');
  });

  it('taking the server version replaces the local state', async () => {
    deps.saveState.mockRejectedValueOnce(conflictError(dto([9], 6)));
    const sync = newSync();
    sync.changed(play([1]));
    await sync.flush();

    sync.resolveConflict('server');

    expect(onServerState).toHaveBeenCalledWith(expect.objectContaining({ crossedOut: new Set([9]) }));
    expect(sync.conflict.value).toBeNull();
    expect(deps.storeLocal).toHaveBeenLastCalledWith('g1', expect.objectContaining({ baseVersion: 6, dirty: false }));
  });

  it('a rejected state is an error and not retried', async () => {
    deps.saveState.mockRejectedValueOnce(new ApiError(400, 'INVALID_GAME_STATE', 'nope'));
    const sync = newSync();

    sync.changed(play([1]));
    await vi.advanceTimersByTimeAsync(60_000);

    expect(sync.status.value).toBe('error');
    expect(deps.saveState).toHaveBeenCalledTimes(1);
  });
});
