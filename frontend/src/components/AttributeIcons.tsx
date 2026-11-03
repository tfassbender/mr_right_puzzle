import type { Suitor } from '../api/gamesApi';
import { attributeName, valueText } from '../game/clueText';
import { attributesOf } from '../game/ruleSets';

// Placeholder icons (emoji and coloured shapes) until the Regency look & feel gets its own icons.

const ELEMENT_ICONS: Record<string, string> = { AIR: '💨', FIRE: '🔥', WATER: '💧', EARTH: '🏔️' };
const TEMPERAMENT_ICONS: Record<string, string> = {
  BROODING: '🤔',
  CHARMING: '😊',
  RESERVED: '😶',
  AMIABLE: '🙂',
  DASHING: '😎',
};
const INTEREST_ICONS: Record<string, string> = { INTELLECTUAL: '📚', MUSICAL: '🎻', SPORTY: '🏇', SOCIABLE: '🥂' };
const EYE_COLORS: Record<string, string> = {
  BROWN: '#6b4226',
  BROWN_GREEN: '#77723a',
  AMBER: '#d99a1e',
  GREEN: '#2e8b57',
  BLUE: '#3a6fd8',
};
const HAIR_COLORS: Record<string, string> = { BLOND: '#e6c56e', BROWN: '#7b4a26', BLACK: '#222', AUBURN: '#a0412d' };
const INCOME_POUNDS: Record<string, string> = { AT_LEAST_1K: '£', AT_LEAST_2K: '££', AT_LEAST_4K: '£££', AT_LEAST_10K: '££££' };

// Attributes shown as an emoji per value. Eye and hair colour are swatches, income is a row of pounds.
const EMOJI_ICONS: Record<string, Record<string, string>> = {
  ELEMENT: ELEMENT_ICONS,
  TEMPERAMENT: TEMPERAMENT_ICONS,
  INTEREST: INTEREST_ICONS,
};

/** One attribute value as an icon with an accessible label, e.g. "Eyes: green". */
export function AttributeIcon({ attribute, value }: { attribute: string; value: string }) {
  const label = `${attributeName(attribute)}: ${valueText(attribute, value)}`;
  const common = { role: 'img' as const, 'aria-label': label, title: label };
  switch (attribute) {
    case 'INCOME':
      return <span {...common} class="icon icon-income">{INCOME_POUNDS[value]}</span>;
    case 'EYE_COLOR':
      return <span {...common} class="icon icon-eye" style={{ '--swatch': EYE_COLORS[value] }} />;
    case 'HAIR_COLOR':
      return <span {...common} class="icon icon-hair" style={{ '--swatch': HAIR_COLORS[value] }} />;
    default:
      return <span {...common} class="icon">{EMOJI_ICONS[attribute]?.[value] ?? '?'}</span>;
  }
}

/** The icon row of a directory entry, in the rule set's display order (book: income, element, eyes, hair, …). */
export function AttributeIcons({ suitor, ruleSet }: { suitor: Suitor; ruleSet: string }) {
  return (
    <span class="icons">
      {attributesOf(ruleSet).map(({ attribute }) => (
        <AttributeIcon key={attribute} attribute={attribute} value={suitor.attributes[attribute]} />
      ))}
    </span>
  );
}
