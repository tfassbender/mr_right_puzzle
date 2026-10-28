import { describe, expect, it, vi } from 'vitest';
import type { GameDto, Suitor } from '../api/gamesApi';
import { GameStore } from './gameStore';
import { finalistsHash, solutionHash } from './hashing';
import type { PlayState } from './playState';

const GAME_ID = 'game-1';
const FINALISTS = [3, 7];
const SOLUTION = 7;

function suitor(id: number): Suitor {
  return {
    id,
    firstName: 'Edward',
    lastName: 'Ashby',
    income: 'AT_LEAST_4K',
    element: 'AIR',
    eyeColor: 'GREEN',
    hairColor: 'BROWN',
    temperament: 'BROODING',
    interest: 'INTELLECTUAL',
  };
}

async function newStore(onChange: (state: PlayState) => void = () => {}) {
  const game: GameDto = {
    id: GAME_ID,
    seed: 'emma',
    suitorCount: 10,
    finalistCount: 2,
    generatorVersion: 1,
    createdAt: '2026-10-28T10:00:00Z',
    suitors: Array.from({ length: 10 }, (_, id) => suitor(id)),
    clues: [],
    finalistsHash: await finalistsHash(GAME_ID, FINALISTS),
    solutionHash: await solutionHash(GAME_ID, SOLUTION),
    state: { crossedOut: [], finalistsFound: false, mrRightId: null, solved: false, wrongGuesses: 0, version: 0, updatedAt: '' },
  };
  const initial: PlayState = { crossedOut: new Set(), finalistsFound: false, mrRightId: null, wrongGuesses: 0, updatedAt: '' };
  return new GameStore(game, initial, onChange, () => '2026-10-28T12:00:00Z');
}

async function crossOutAllBut(store: GameStore, keep: number[]) {
  for (let id = 0; id < 10; id++) {
    if (!keep.includes(id)) await store.toggle(id);
  }
}

describe('GameStore', () => {
  it('crosses out and restores suitors, reporting every change', async () => {
    const onChange = vi.fn();
    const store = await newStore(onChange);

    await store.toggle(1);
    expect(store.state.value.crossedOut.has(1)).toBe(true);
    expect(store.remaining.value).toHaveLength(9);

    await store.toggle(1);
    expect(store.state.value.crossedOut.has(1)).toBe(false);
    expect(onChange).toHaveBeenCalledTimes(2);
    expect(onChange.mock.calls[0][0].updatedAt).toBe('2026-10-28T12:00:00Z');
  });

  it('undo reverts the last toggles in order', async () => {
    const store = await newStore();
    await store.toggle(1);
    await store.toggle(2);

    await store.undo();
    expect([...store.state.value.crossedOut]).toEqual([1]);
    await store.undo();
    expect(store.state.value.crossedOut.size).toBe(0);
    expect(store.canUndo.value).toBe(false);
  });

  it('finds the finalists when exactly the right suitors are left and locks the directory', async () => {
    const store = await newStore();
    await crossOutAllBut(store, FINALISTS);

    expect(store.state.value.finalistsFound).toBe(true);
    expect(store.state.value.wrongGuesses).toBe(0);
    expect(store.canUndo.value).toBe(false);

    await store.toggle(0);
    expect(store.state.value.crossedOut.has(0)).toBe(true); // still crossed out: locked
  });

  it('wrong finalists count as a wrong guess and show a hint', async () => {
    const store = await newStore();
    await crossOutAllBut(store, [3, 8]);

    expect(store.state.value.finalistsFound).toBe(false);
    expect(store.state.value.wrongGuesses).toBe(1);
    expect(store.hint.value).toBe('WRONG_FINALISTS');

    await store.toggle(8);
    expect(store.hint.value).toBe('TOO_FEW_LEFT');
    await store.toggle(7); // restoring to exactly two left is a new guess – the right one this time
    expect(store.hint.value).toBe(null);
    expect(store.state.value.finalistsFound).toBe(true);
  });

  it('picking Mr Right checks the solution hash', async () => {
    const store = await newStore();
    await crossOutAllBut(store, FINALISTS);

    expect(await store.pick(3)).toBe(false);
    expect(store.state.value.wrongGuesses).toBe(1);
    expect(store.hint.value).toBe('WRONG_PICK');

    expect(await store.pick(SOLUTION)).toBe(true);
    expect(store.state.value.mrRightId).toBe(SOLUTION);
    expect(store.hint.value).toBe(null);
  });
});

describe('hashing', () => {
  it('matches the backend format: "<gameId>:<sorted ids joined by commas>" as hex SHA-256', async () => {
    expect(await finalistsHash(GAME_ID, [7, 3])).toBe('2a96f952896e885e04f54533e6dbe571204315e61dc6c9f3b7760f64e3d793b8');
    expect(await solutionHash('x', 1)).toBe('0b788078937c4c6ed6f98b641b7a9129ce9d6cc993f1000bb8024e7adb48c280');
  });
});
