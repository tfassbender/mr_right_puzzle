import { afterEach, describe, expect, it, vi } from 'vitest';
import type { Clue } from '../api/gamesApi';
import { ageOf, clueText, formatHeight, valueText } from './clueText';

describe('clueText', () => {
  it('renders every clue type with its parameters', () => {
    const clues: [Clue, string][] = [
      [{ type: 'INCOME_AT_LEAST', level: 'AT_LEAST_4K' }, 'He earns at least £4,000 a year.'],
      [{ type: 'ATTRIBUTE_IS_NOT', attribute: 'HAIR_COLOR', value: 'BLOND' }, 'His hair is not blond.'],
      [{ type: 'ATTRIBUTE_IS_ONE_OF', attribute: 'EYE_COLOR', values: ['GREEN', 'BLUE'] }, 'His eyes are green or blue.'],
      [
        { type: 'ATTRIBUTE_IS_ONE_OF', attribute: 'TEMPERAMENT', values: ['BROODING', 'RESERVED', 'DASHING'] },
        'He is brooding, reserved or dashing.',
      ],
      [{ type: 'INITIALS_DIFFER' }, 'The initials of his first name and surname differ.'],
      [{ type: 'INITIALS_IN_RANGE', range: 'A_TO_M' }, 'Both his initials lie between A and M.'],
      [{ type: 'SURNAME_START_AND_END_DIFFER' }, 'His surname does not start and end with the same letter.'],
      [{ type: 'FIRST_NAME_VOWEL_COUNT', count: 3 }, 'His first name has exactly 3 vowels.'],
      [{ type: 'SURNAME_VOWEL_COUNT', count: 2 }, 'His surname has exactly 2 vowels.'],
      [{ type: 'NAME_EXCLUDES_LETTER', letter: 'H' }, 'His full name does not contain the letter H.'],
    ];

    clues.forEach(([clue, text]) => expect(clueText(clue)).toBe(text));
  });

  describe("Eva's rules", () => {
    afterEach(() => vi.useRealTimers());

    it('has one text per criterion', () => {
      expect(clueText({ type: 'ATTRIBUTE_IS', attribute: 'VEHICLE', value: 'CAR' }, 'EVA')).toBe('He has a car.');
      expect(clueText({ type: 'ATTRIBUTE_IS_NOT', attribute: 'JOB', value: 'NONE' }, 'EVA')).toBe('He has a job.');
      expect(clueText({ type: 'ATTRIBUTE_BETWEEN', attribute: 'HEIGHT', min: 188, max: 195 }, 'EVA')).toBe(
        'He is between 1.88 and 1.95 m tall.',
      );
    });

    it('turns birth years into ages of the current year', () => {
      vi.useFakeTimers();
      vi.setSystemTime(new Date('2026-11-03T12:00:00Z'));

      // Born 1984–1997: between 29 and 42 in 2026, a year older in 2027.
      const age = { type: 'ATTRIBUTE_BETWEEN', attribute: 'BIRTH_YEAR', min: 1984, max: 1997 } as const;
      expect(clueText(age, 'EVA')).toBe('He is between 29 and 42 years old.');
      expect(valueText('BIRTH_YEAR', '1995')).toBe('31 years');

      vi.setSystemTime(new Date('2027-01-01T12:00:00Z'));
      expect(clueText(age, 'EVA')).toBe('He is between 30 and 43 years old.');
    });

    it('shows heights in metres', () => {
      expect(formatHeight(191)).toBe('1.91');
      expect(formatHeight(200)).toBe('2.00');
      expect(valueText('HEIGHT', '188')).toBe('1.88 m');
      expect(ageOf(1994, new Date('2026-06-01'))).toBe(32);
    });
  });
});
