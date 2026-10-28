import { describe, expect, it } from 'vitest';
import { itemAt, itemCount, remainingPerSection, sectionHeaderIndex, sectionRange } from './directoryLayout';

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

  it('the last section can be shorter', () => {
    expect(sectionRange(2, 250)).toEqual([200, 249]);
    expect(remainingPerSection(250, new Set([0, 1, 210]))).toEqual([98, 100, 49]);
  });
});
