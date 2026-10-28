import type { Attribute, Clue } from '../api/gamesApi';
import { m } from '../i18n/i18n';

type MessageFn = (params?: Record<string, unknown>) => string;

/** Looks up a message by a key built at runtime (e.g. from an enum value). */
function message(key: string): MessageFn {
  const fn = (m as unknown as Record<string, MessageFn | undefined>)[key];
  if (!fn) throw new Error(`Missing message ${key}`);
  return fn;
}

/** The translated name of an attribute value, e.g. ("EYE_COLOR", "GREEN") → "green". */
export function valueText(attribute: Attribute | 'INCOME', value: string): string {
  return message(`value_${attribute}_${value}`)();
}

export function attributeName(attribute: Attribute | 'INCOME'): string {
  return message(`attr_${attribute}`)();
}

/** "a or b" / "a, b or c" – one-of clues have 2 or 3 values. */
function joinOr(values: string[]): string {
  if (values.length === 2) return m.list_or2({ a: values[0], b: values[1] });
  return m.list_or3({ a: values[0], b: values[1], c: values[2] });
}

export function clueText(clue: Clue): string {
  switch (clue.type) {
    case 'INCOME_AT_LEAST':
      return m.clue_INCOME_AT_LEAST({ level: valueText('INCOME', clue.level) });
    case 'ATTRIBUTE_IS_NOT':
      return message(`clue_not_${clue.attribute}`)({ value: valueText(clue.attribute, clue.value) });
    case 'ATTRIBUTE_IS_ONE_OF':
      return message(`clue_oneOf_${clue.attribute}`)({
        values: joinOr(clue.values.map((value) => valueText(clue.attribute, value))),
      });
    case 'INITIALS_DIFFER':
      return m.clue_INITIALS_DIFFER();
    case 'INITIALS_IN_RANGE':
      return clue.range === 'A_TO_M'
        ? m.clue_INITIALS_IN_RANGE({ from: 'A', to: 'M' })
        : m.clue_INITIALS_IN_RANGE({ from: 'N', to: 'Z' });
    case 'SURNAME_START_AND_END_DIFFER':
      return m.clue_SURNAME_START_AND_END_DIFFER();
    case 'FIRST_NAME_VOWEL_COUNT':
      return m.clue_FIRST_NAME_VOWEL_COUNT({ count: clue.count });
    case 'SURNAME_VOWEL_COUNT':
      return m.clue_SURNAME_VOWEL_COUNT({ count: clue.count });
    case 'NAME_EXCLUDES_LETTER':
      return m.clue_NAME_EXCLUDES_LETTER({ letter: clue.letter });
  }
}
