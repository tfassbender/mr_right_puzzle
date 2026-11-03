import { m } from '../i18n/i18n';

export const BOOK = 'BOOK';
export const EVA = 'EVA';

/** An attribute in the legend. Number attributes (height, age) have no value list – the legend explains them. */
export interface AttributeLegend {
  attribute: string;
  values: string[]; // in legend order; empty for number attributes
  number?: boolean;
}

interface RuleSetDisplay {
  attributes: AttributeLegend[]; // legend order
  iconRows: string[][]; // attribute ids per line of a directory row
}

const BOOK_DISPLAY: RuleSetDisplay = {
  attributes: [
    { attribute: 'INCOME', values: ['AT_LEAST_1K', 'AT_LEAST_2K', 'AT_LEAST_4K', 'AT_LEAST_10K'] },
    { attribute: 'ELEMENT', values: ['AIR', 'FIRE', 'WATER', 'EARTH'] },
    { attribute: 'EYE_COLOR', values: ['BROWN', 'BROWN_GREEN', 'AMBER', 'GREEN', 'BLUE'] },
    { attribute: 'HAIR_COLOR', values: ['BLOND', 'BROWN', 'BLACK', 'AUBURN'] },
    { attribute: 'TEMPERAMENT', values: ['BROODING', 'CHARMING', 'RESERVED', 'AMIABLE', 'DASHING'] },
    { attribute: 'INTEREST', values: ['INTELLECTUAL', 'MUSICAL', 'SPORTY', 'SOCIABLE'] },
  ],
  // The book's order: income, element, eyes, hair, temperament, interest.
  iconRows: [['INCOME', 'ELEMENT', 'EYE_COLOR', 'HAIR_COLOR', 'TEMPERAMENT', 'INTEREST']],
};

const EVA_DISPLAY: RuleSetDisplay = {
  attributes: [
    { attribute: 'HEIGHT', values: [], number: true },
    { attribute: 'BIRTH_YEAR', values: [], number: true },
    { attribute: 'JOB', values: ['NONE', 'EURO_1', 'EURO_2', 'EURO_3'] },
    { attribute: 'VEHICLE', values: ['CAR', 'MOTORBIKE', 'BIKE', 'BUS'] },
    { attribute: 'KIDS', values: ['NO_KIDS', 'WANTS_KIDS', 'UNDECIDED'] },
    { attribute: 'VICES', values: ['NONE', 'ALCOHOL_LITTLE', 'ALCOHOL_LOT', 'CIGARETTES', 'DRUGS'] },
    { attribute: 'CHARACTER', values: ['LOYAL', 'FLIRTY', 'MOODY', 'ALOOF'] },
    { attribute: 'MANNER', values: ['OPEN_AND_FUNNY', 'SHY', 'ARROGANT', 'BORING'] },
    { attribute: 'SOCIAL', values: ['FRIENDS_AND_FAMILY', 'ONLY_FRIENDS', 'ONLY_FAMILY', 'LONER'] },
    { attribute: 'FOOD', values: ['HEALTHY_FOODIE', 'FAST_FOOD', 'DIET_FANATIC', 'CANT_BE_BOTHERED'] },
    { attribute: 'LEISURE', values: ['SPONTANEOUS_TRIPS', 'LUXURY_HOTELS', 'LONG_HAUL', 'HOMEBODY'] },
    { attribute: 'FITNESS', values: ['ATHLETIC', 'FIT', 'AVERAGE', 'COUCH_POTATO'] },
  ],
  // 12 icons don't fit into one line on a phone: the facts first, then the character.
  iconRows: [
    ['HEIGHT', 'BIRTH_YEAR', 'JOB', 'VEHICLE', 'KIDS', 'VICES'],
    ['CHARACTER', 'MANNER', 'SOCIAL', 'FOOD', 'LEISURE', 'FITNESS'],
  ],
};

const DISPLAYS: Record<string, RuleSetDisplay> = { [BOOK]: BOOK_DISPLAY, [EVA]: EVA_DISPLAY };

// Unknown rule sets (a newer server than this cached app) fall back to no icons rather than crashing.
const EMPTY_DISPLAY: RuleSetDisplay = { attributes: [], iconRows: [] };

export function attributesOf(ruleSet: string): AttributeLegend[] {
  return (DISPLAYS[ruleSet] ?? EMPTY_DISPLAY).attributes;
}

export function iconRowsOf(ruleSet: string): string[][] {
  return (DISPLAYS[ruleSet] ?? EMPTY_DISPLAY).iconRows;
}

/** Only the book has name clues, so only its players need the note on vowels. */
export function hasNameClues(ruleSet: string): boolean {
  return ruleSet === BOOK;
}

const RULE_SET_NAMES: Record<string, () => string> = {
  [BOOK]: m.rule_set_BOOK,
  [EVA]: m.rule_set_EVA,
};

export function ruleSetName(ruleSet: string): string {
  return RULE_SET_NAMES[ruleSet]?.() ?? ruleSet;
}
