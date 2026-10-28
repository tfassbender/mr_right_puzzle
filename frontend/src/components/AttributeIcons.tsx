import type { Attribute, Suitor } from '../api/gamesApi';
import { attributeName, valueText } from '../game/clueText';

// Placeholder icons (emoji and coloured shapes) until the Regency look & feel gets its own icons.

const ELEMENT_ICONS: Record<string, string> = { AIR: '💨', FIRE: '🔥', WATER: '💧', EARTH: '🌿' };
const TEMPERAMENT_ICONS: Record<string, string> = {
  BROODING: '😔',
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

/** One attribute value as an icon with an accessible label, e.g. "Eyes: green". */
export function AttributeIcon({ attribute, value }: { attribute: Attribute | 'INCOME'; value: string }) {
  const label = `${attributeName(attribute)}: ${valueText(attribute, value)}`;
  const common = { role: 'img' as const, 'aria-label': label, title: label };
  switch (attribute) {
    case 'INCOME':
      return <span {...common} class="icon icon-income">{INCOME_POUNDS[value]}</span>;
    case 'ELEMENT':
      return <span {...common} class="icon">{ELEMENT_ICONS[value]}</span>;
    case 'EYE_COLOR':
      return <span {...common} class="icon icon-eye" style={{ '--swatch': EYE_COLORS[value] }} />;
    case 'HAIR_COLOR':
      return <span {...common} class="icon icon-hair" style={{ '--swatch': HAIR_COLORS[value] }} />;
    case 'TEMPERAMENT':
      return <span {...common} class="icon">{TEMPERAMENT_ICONS[value]}</span>;
    case 'INTEREST':
      return <span {...common} class="icon">{INTEREST_ICONS[value]}</span>;
  }
}

/** The icon row of a directory entry, in the book's order: income, element, eyes, hair, temperament, interest. */
export function AttributeIcons({ suitor }: { suitor: Suitor }) {
  return (
    <span class="icons">
      <AttributeIcon attribute="INCOME" value={suitor.income} />
      <AttributeIcon attribute="ELEMENT" value={suitor.element} />
      <AttributeIcon attribute="EYE_COLOR" value={suitor.eyeColor} />
      <AttributeIcon attribute="HAIR_COLOR" value={suitor.hairColor} />
      <AttributeIcon attribute="TEMPERAMENT" value={suitor.temperament} />
      <AttributeIcon attribute="INTEREST" value={suitor.interest} />
    </span>
  );
}

export const LEGEND: { attribute: Attribute | 'INCOME'; values: string[] }[] = [
  { attribute: 'INCOME', values: Object.keys(INCOME_POUNDS) },
  { attribute: 'ELEMENT', values: Object.keys(ELEMENT_ICONS) },
  { attribute: 'EYE_COLOR', values: Object.keys(EYE_COLORS) },
  { attribute: 'HAIR_COLOR', values: Object.keys(HAIR_COLORS) },
  { attribute: 'TEMPERAMENT', values: Object.keys(TEMPERAMENT_ICONS) },
  { attribute: 'INTEREST', values: Object.keys(INTEREST_ICONS) },
];
