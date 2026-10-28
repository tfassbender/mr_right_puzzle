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
    state: { crossedOut: [], possibleMatches: [], finalistsFound: false, mrRightId: null, solved: false, wrongGuesses: 0, version: 0, updatedAt: '' },
  };
  const initial: PlayState = { crossedOut: new Set(), possibleMatches: new Set(), finalistsFound: false, mrRightId: null, wrongGuesses: 0, updatedAt: '' };
  return new GameStore(game, initial, onChange, () => '2026-10-28T12:00:00Z');
}

function crossOutAllBut(store: GameStore, keep: number[]) {
  for (let id = 0; id < 10; id++) {
    if (!keep.includes(id)) store.toggleNoMatch(id);
  }
}

describe('GameStore', () => {
  it('crosses out and restores suitors, reporting every change', async () => {
    const onChange = vi.fn();
    const store = await newStore(onChange);

    store.toggleNoMatch(1);
    expect(store.state.value.crossedOut.has(1)).toBe(true);
    expect(store.remaining.value).toHaveLength(9);

    store.toggleNoMatch(1);
    expect(store.state.value.crossedOut.has(1)).toBe(false);
    expect(onChange).toHaveBeenCalledTimes(2);
    expect(onChange.mock.calls[0][0].updatedAt).toBe('2026-10-28T12:00:00Z');
  });

  it('a suitor is either a possible match or crossed out, never both', async () => {
    const store = await newStore();

    store.togglePossibleMatch(1);
    expect(store.state.value.possibleMatches.has(1)).toBe(true);

    store.toggleNoMatch(1);
    expect(store.state.value.crossedOut.has(1)).toBe(true);
    expect(store.state.value.possibleMatches.has(1)).toBe(false);

    store.togglePossibleMatch(1);
    expect(store.state.value.crossedOut.has(1)).toBe(false);
    store.togglePossibleMatch(1);
    expect(store.state.value.possibleMatches.size).toBe(0);
  });

  it('undo restores the previous marker, step by step', async () => {
    const store = await newStore();
    store.toggleNoMatch(1);
    store.togglePossibleMatch(2);
    store.toggleNoMatch(2);

    store.undo();
    expect([...store.state.value.possibleMatches]).toEqual([2]);
    expect([...store.state.value.crossedOut]).toEqual([1]);
    store.undo();
    store.undo();
    expect(store.state.value.crossedOut.size + store.state.value.possibleMatches.size).toBe(0);
    expect(store.canUndo.value).toBe(false);
  });

  it('the first unmarked suitor skips crossed out and starred ones', async () => {
    const store = await newStore();
    store.toggleNoMatch(0);
    store.togglePossibleMatch(1);
    expect(store.firstUnmarked.value).toBe(2);

    crossOutAllBut(store, [0, 1]);
    expect(store.firstUnmarked.value).toBe(null);
  });

  it('starring as many suitors as there are finalists allows submitting them', async () => {
    const store = await newStore();
    store.togglePossibleMatch(3);
    expect(store.candidates.value).toBe(null);
    store.togglePossibleMatch(7);
    expect(store.candidates.value).toEqual([3, 7]);

    expect(await store.submit()).toBe(true);
    const state = store.state.value;
    expect(state.finalistsFound).toBe(true);
    expect(state.crossedOut.size).toBe(8); // everybody else is crossed out
    expect(store.remaining.value).toEqual([3, 7]);
    expect(store.canUndo.value).toBe(false);

    store.toggleNoMatch(3);
    expect(store.state.value.crossedOut.has(3)).toBe(false); // locked
  });

  it('crossing out all but the finalists allows submitting the rest', async () => {
    const store = await newStore();
    crossOutAllBut(store, FINALISTS);

    expect(store.candidates.value).toEqual(FINALISTS);
    expect(await store.submit()).toBe(true);
    expect(store.state.value.finalistsFound).toBe(true);
  });

  it('wrong finalists count as a wrong guess and show a hint', async () => {
    const store = await newStore();
    store.togglePossibleMatch(3);
    store.togglePossibleMatch(8);

    expect(await store.submit()).toBe(false);
    expect(store.state.value.finalistsFound).toBe(false);
    expect(store.state.value.wrongGuesses).toBe(1);
    expect(store.hint.value).toBe('WRONG_FINALISTS');

    store.togglePossibleMatch(8);
    expect(store.hint.value).toBe(null);
  });

  it('warns as soon as fewer suitors are left than finalists', async () => {
    const store = await newStore();
    crossOutAllBut(store, [3]);
    expect(store.hint.value).toBe('TOO_FEW_LEFT');
    expect(store.candidates.value).toBe(null);
  });

  it('picking Mr Right checks the solution hash', async () => {
    const store = await newStore();
    crossOutAllBut(store, FINALISTS);
    await store.submit();

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
