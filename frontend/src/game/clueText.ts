import type { Clue } from '../api/gamesApi';
import { getLocale, m } from '../i18n/i18n';
import { BOOK } from './ruleSets';

type MessageFn = (params?: Record<string, unknown>) => string;

/** Looks up a message by a key built at runtime (e.g. from an enum value). */
function message(key: string): MessageFn {
  const fn = (m as unknown as Record<string, MessageFn | undefined>)[key];
  if (!fn) throw new Error(`Missing message ${key}`);
  return fn;
}

/** A height in cm as metres, e.g. 191 → "1.91" (in German "1,91"). */
export function formatHeight(cm: number): string {
  return (cm / 100).toLocaleString(getLocale(), { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

/**
 * Suitors store their birth year, so a puzzle doesn't change over time; the age follows the current year.
 * Birthdays are ignored – the clue's age range is computed the same way, so both shift together.
 */
export function ageOf(birthYear: number, now: Date = new Date()): number {
  return now.getFullYear() - birthYear;
}

/** The translated name of an attribute value, e.g. ("EYE_COLOR", "GREEN") → "green", ("HEIGHT", "191") → "1.91 m". */
export function valueText(attribute: string, value: string): string {
  if (attribute === 'HEIGHT') return m.value_height({ height: formatHeight(Number(value)) });
  if (attribute === 'BIRTH_YEAR') return m.value_age({ age: ageOf(Number(value)) });
  return message(`value_${attribute}_${value}`)();
}

/** The short form shown in a directory row instead of an icon, e.g. "1.91 m" or "31". */
export function numberText(attribute: string, value: string): string {
  // A narrow no-break space keeps "1.91 m" short and on one line.
  return attribute === 'HEIGHT' ? `${formatHeight(Number(value))}\u202Fm` : String(ageOf(Number(value)));
}

export function attributeName(attribute: string): string {
  return message(`attr_${attribute}`)();
}

/** "a or b" / "a, b or c" – one-of clues have 2 or 3 values. */
function joinOr(values: string[]): string {
  if (values.length === 2) return m.list_or2({ a: values[0], b: values[1] });
  return m.list_or3({ a: values[0], b: values[1], c: values[2] });
}

/**
 * Rule sets other than the book have one text per criterion (`clue_<ruleSet>_<attribute>`), e.g. Eva's
 * "He has a car." The book's clues are built from templates with random parameters.
 */
function ruleSetClueText(clue: Clue, ruleSet: string): string {
  if (!('attribute' in clue)) throw new Error(`Unexpected clue ${clue.type} in rule set ${ruleSet}`);
  return message(`clue_${ruleSet}_${clue.attribute}`)(clue.type === 'ATTRIBUTE_BETWEEN' ? rangeParams(clue) : {});
}

function rangeParams(clue: Extract<Clue, { type: 'ATTRIBUTE_BETWEEN' }>): Record<string, unknown> {
  if (clue.attribute === 'HEIGHT') return { min: formatHeight(clue.min), max: formatHeight(clue.max) };
  // A later birth year means a younger man.
  return { min: ageOf(clue.max), max: ageOf(clue.min) };
}

export function clueText(clue: Clue, ruleSet: string = BOOK): string {
  if (ruleSet !== BOOK) return ruleSetClueText(clue, ruleSet);
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
    case 'ATTRIBUTE_IS':
    case 'ATTRIBUTE_BETWEEN':
      return ruleSetClueText(clue, ruleSet);
  }
}
