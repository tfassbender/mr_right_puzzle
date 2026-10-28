export const GAME_NAME_MAX_LENGTH = 60;

/** "Mr Right - <date>", with " (2)", " (3)" … appended if the player already has a game with that name. */
export function defaultGameName(date: string, existingNames: ReadonlyArray<string | null>): string {
  const base = `Mr Right - ${date}`;
  const taken = new Set(existingNames);
  if (!taken.has(base)) return base;
  let count = 2;
  while (taken.has(`${base} (${count})`)) count++;
  return `${base} (${count})`;
}
