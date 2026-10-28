import { describe, expect, it } from 'vitest';
import { defaultGameName } from './gameName';

describe('defaultGameName', () => {
  it('is "Mr Right - <date>" when that name is free', () => {
    expect(defaultGameName('28.10.2026', ['Mr Right - 27.10.2026', null])).toBe('Mr Right - 28.10.2026');
  });

  it('counts up when the name is taken', () => {
    expect(defaultGameName('28.10.2026', ['Mr Right - 28.10.2026'])).toBe('Mr Right - 28.10.2026 (2)');
    expect(defaultGameName('28.10.2026', ['Mr Right - 28.10.2026', 'Mr Right - 28.10.2026 (2)'])).toBe(
      'Mr Right - 28.10.2026 (3)',
    );
  });
});
