import { session } from '../auth/session';
import { m } from '../i18n/i18n';

/** Placeholder until the puzzle rules are clarified: the list of the player's puzzles goes here. */
export function GamesPage() {
  const user = session.value?.user;
  if (!user) return null;

  return (
    <div class="card">
      <h1>{m.games_title()}</h1>
      <p>{m.games_welcome({ name: user.displayName })}</p>
      <p class="muted">{m.games_empty()}</p>
    </div>
  );
}
