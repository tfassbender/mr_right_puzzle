import { describe, expect, it } from 'vitest';
import type { GameDto } from '../api/gamesApi';
import { upgradeStoredGame } from './upgradeStoredGame';

const oldSuitor = {
  id: 0,
  firstName: 'Edward',
  lastName: 'Ashby',
  income: 'AT_LEAST_4K',
  element: 'AIR',
  eyeColor: 'GREEN',
  hairColor: 'BROWN',
  temperament: 'BROODING',
  interest: 'INTELLECTUAL',
};

describe('upgradeStoredGame', () => {
  it('turns a game stored before rule sets into a book game with attribute maps', () => {
    const stored = { id: 'g1', suitors: [oldSuitor] } as unknown as GameDto;

    const upgraded = upgradeStoredGame(stored);

    expect(upgraded.ruleSet).toBe('BOOK');
    expect(upgraded.suitors[0]).toEqual({
      id: 0,
      firstName: 'Edward',
      lastName: 'Ashby',
      attributes: {
        INCOME: 'AT_LEAST_4K',
        ELEMENT: 'AIR',
        EYE_COLOR: 'GREEN',
        HAIR_COLOR: 'BROWN',
        TEMPERAMENT: 'BROODING',
        INTEREST: 'INTELLECTUAL',
      },
    });
  });

  it('leaves current games unchanged', () => {
    const current = { id: 'g2', ruleSet: 'BOOK', suitors: [] } as unknown as GameDto;

    expect(upgradeStoredGame(current)).toBe(current);
  });
});
