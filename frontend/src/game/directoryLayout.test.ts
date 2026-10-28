import { describe, expect, it } from 'vitest';
import { itemAt, itemCount, remainingPerSection, sectionHeaderIndex, sectionRange, suitorIndex, contextRowsAbove } from './directoryLayout';

describe('directoryLayout', () => {
  it('puts a header in front of every section of 100', () => {
    expect(itemCount(250)).toBe(253);
    expect(itemAt(0)).toEqual({ kind: 'header', section: 0 });
    expect(itemAt(1)).toEqual({ kind: 'suitor', id: 0 });
    expect(itemAt(100)).toEqual({ kind: 'suitor', id: 99 });
    expect(itemAt(101)).toEqual({ kind: 'header', section: 1 });
    expect(itemAt(102)).toEqual({ kind: 'suitor', id: 100 });
    expect(sectionHeaderIndex(2)).toBe(202);
  });

  it('finds the row of a suitor', () => {
    for (const id of [0, 99, 100, 250]) {
      expect(itemAt(suitorIndex(id))).toEqual({ kind: 'suitor', id });
    }
  });

  it('keeps more rows above a jump target the more rows fit on the screen', () => {
    expect(contextRowsAbove(4)).toBe(1);
    expect(contextRowsAbove(9)).toBe(1); // phone
    expect(contextRowsAbove(12)).toBe(2);
    expect(contextRowsAbove(14)).toBe(3); // desktop
    expect(contextRowsAbove(30)).toBe(3);
  });

  it('the last section can be shorter', () => {
    expect(sectionRange(2, 250)).toEqual([200, 249]);
    expect(remainingPerSection(250, new Set([0, 1, 210]))).toEqual([98, 100, 49]);
  });
});
