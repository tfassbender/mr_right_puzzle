import type { Suitor } from '../api/gamesApi';
import { attributeName, numberText, valueText } from '../game/clueText';
import { iconRowsOf } from '../game/ruleSets';

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

// Eva's rules
const JOB_EUROS: Record<string, string> = { NONE: '—', EURO_1: '€', EURO_2: '€€', EURO_3: '€€€' };
const EVA_ICONS: Record<string, Record<string, string>> = {
  VEHICLE: { CAR: '🚗', MOTORBIKE: '🏍️', BIKE: '🚲', BUS: '🚌' },
  KIDS: { NO_KIDS: '🧑🏻‍🤝‍🧑🏻', WANTS_KIDS: '👶', UNDECIDED: '🤷' },
  VICES: { NONE: '✨', ALCOHOL_LITTLE: '🍺', ALCOHOL_LOT: '🍻', CIGARETTES: '🚬', DRUGS: '💊' },
  CHARACTER: { LOYAL: '🥰', FLIRTY: '😏', MOODY: '😤', ALOOF: '🥶' },
  MANNER: { OPEN_AND_FUNNY: '😄', SHY: '🙈', ARROGANT: '🦚', BORING: '😑' },
  SOCIAL: { FRIENDS_AND_FAMILY: '🫂', ONLY_FRIENDS: '🎉', ONLY_FAMILY: '🏠', LONER: '🐺' },
  FOOD: { HEALTHY_FOODIE: '🥗', FAST_FOOD: '🍔', DIET_FANATIC: '🥦', CANT_BE_BOTHERED: '🥫' },
  LEISURE: { SPONTANEOUS_TRIPS: '🎒', LUXURY_HOTELS: '🏨', LONG_HAUL: '✈️', HOMEBODY: '🛋️' },
  FITNESS: { ATHLETIC: '💪', FIT: '🏃', AVERAGE: '🚶', COUCH_POTATO: '🥔' },
};

// Attributes shown as an emoji per value. Eye and hair colour are swatches, money is a row of £ or €,
// height and age are numbers.
const EMOJI_ICONS: Record<string, Record<string, string>> = {
  ELEMENT: ELEMENT_ICONS,
  TEMPERAMENT: TEMPERAMENT_ICONS,
  INTEREST: INTEREST_ICONS,
  ...EVA_ICONS,
};

/** One attribute value as an icon with an accessible label, e.g. "Eyes: green". */
export function AttributeIcon({ attribute, value }: { attribute: string; value: string }) {
  const label = `${attributeName(attribute)}: ${valueText(attribute, value)}`;
  const common = { role: 'img' as const, 'aria-label': label, title: label };
  switch (attribute) {
    case 'INCOME':
      return <span {...common} class="icon icon-income">{INCOME_POUNDS[value]}</span>;
    case 'JOB':
      return <span {...common} class="icon icon-income">{JOB_EUROS[value]}</span>;
    case 'HEIGHT':
    case 'BIRTH_YEAR':
      return <span {...common} class="icon icon-number">{numberText(attribute, value)}</span>;
    case 'EYE_COLOR':
      return <span {...common} class="icon icon-eye" style={{ '--swatch': EYE_COLORS[value] }} />;
    case 'HAIR_COLOR':
      return <span {...common} class="icon icon-hair" style={{ '--swatch': HAIR_COLORS[value] }} />;
    default:
      return <span {...common} class="icon">{EMOJI_ICONS[attribute]?.[value] ?? '?'}</span>;
  }
}

/**
 * The icons of a directory entry, in the rule set's display order. Several lines (Eva's rules) form a grid,
 * so the same attribute sits in the same column in every row – easier to scan down the directory.
 */
export function AttributeIcons({ suitor, ruleSet }: { suitor: Suitor; ruleSet: string }) {
  const rows = iconRowsOf(ruleSet);
  const gridClass = rows.length > 1 ? 'icons icons-grid' : 'icons';
  return (
    <span class={gridClass} style={{ '--columns': Math.max(0, ...rows.map((row) => row.length)) - 1 }}>
      {rows.flat().map((attribute) => (
        <AttributeIcon key={attribute} attribute={attribute} value={suitor.attributes[attribute]} />
      ))}
    </span>
  );
}
