import { m } from '../i18n/i18n';

export const BOOK = 'BOOK';

export interface AttributeLegend {
  attribute: string;
  values: string[]; // in legend order
}

/**
 * How each rule set's attributes are shown: the order of the icon row and the legend, and the values per
 * attribute. The backend's attribute order drives its random draws and is independent of this one.
 */
const RULE_SET_ATTRIBUTES: Record<string, AttributeLegend[]> = {
  // The book's order: income, element, eyes, hair, temperament, interest.
  [BOOK]: [
    { attribute: 'INCOME', values: ['AT_LEAST_1K', 'AT_LEAST_2K', 'AT_LEAST_4K', 'AT_LEAST_10K'] },
    { attribute: 'ELEMENT', values: ['AIR', 'FIRE', 'WATER', 'EARTH'] },
    { attribute: 'EYE_COLOR', values: ['BROWN', 'BROWN_GREEN', 'AMBER', 'GREEN', 'BLUE'] },
    { attribute: 'HAIR_COLOR', values: ['BLOND', 'BROWN', 'BLACK', 'AUBURN'] },
    { attribute: 'TEMPERAMENT', values: ['BROODING', 'CHARMING', 'RESERVED', 'AMIABLE', 'DASHING'] },
    { attribute: 'INTEREST', values: ['INTELLECTUAL', 'MUSICAL', 'SPORTY', 'SOCIABLE'] },
  ],
};

/** Unknown rule sets (a newer server than this cached app) fall back to no icons rather than crashing. */
export function attributesOf(ruleSet: string): AttributeLegend[] {
  return RULE_SET_ATTRIBUTES[ruleSet] ?? [];
}

const RULE_SET_NAMES: Record<string, () => string> = {
  [BOOK]: m.rule_set_BOOK,
};

export function ruleSetName(ruleSet: string): string {
  return RULE_SET_NAMES[ruleSet]?.() ?? ruleSet;
}
