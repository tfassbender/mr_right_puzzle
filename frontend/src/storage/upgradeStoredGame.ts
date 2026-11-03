import type { GameDto, Suitor } from '../api/gamesApi';
import { BOOK } from '../game/ruleSets';

/** A suitor as stored on the device before rule sets existed: the book's attributes as fixed fields. */
interface BookSuitorV1 {
  id: number;
  firstName: string;
  lastName: string;
  income: string;
  element: string;
  eyeColor: string;
  hairColor: string;
  temperament: string;
  interest: string;
}

/**
 * Puzzles are stored once on the device and opened from there, also offline. Copies stored before rule sets
 * existed are book games with fixed suitor fields – converted here, so they keep working without a reload.
 */
export function upgradeStoredGame(game: GameDto): GameDto {
  if (game.ruleSet) return game;
  const suitors = game.suitors as unknown as BookSuitorV1[];
  return { ...game, ruleSet: BOOK, suitors: suitors.map(toAttributes) };
}

function toAttributes(suitor: BookSuitorV1): Suitor {
  return {
    id: suitor.id,
    firstName: suitor.firstName,
    lastName: suitor.lastName,
    attributes: {
      INCOME: suitor.income,
      ELEMENT: suitor.element,
      EYE_COLOR: suitor.eyeColor,
      HAIR_COLOR: suitor.hairColor,
      TEMPERAMENT: suitor.temperament,
      INTEREST: suitor.interest,
    },
  };
}
