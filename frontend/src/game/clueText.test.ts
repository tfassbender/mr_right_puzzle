import { describe, expect, it } from 'vitest';
import type { Clue } from '../api/gamesApi';
import { clueText } from './clueText';

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
});
