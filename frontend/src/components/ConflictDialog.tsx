import type { PlayState } from '../game/playState';
import { getLocale, m } from '../i18n/i18n';
import type { Conflict } from '../sync/syncManager';

function Summary({ title, state }: { title: string; state: PlayState }) {
  const changedAt = new Date(state.updatedAt).toLocaleString(getLocale(), { dateStyle: 'medium', timeStyle: 'short' });
  return (
    <div class="conflict-version">
      <h3>{title}</h3>
      <ul>
        <li>{m.conflict_crossed_out({ count: state.crossedOut.size })}</li>
        <li>{state.mrRightId !== null ? m.conflict_solved() : m.conflict_not_solved()}</li>
        <li>{m.game_wrong_guesses({ count: state.wrongGuesses })}</li>
        <li>{m.conflict_changed({ time: changedAt })}</li>
      </ul>
    </div>
  );
}

/** Both this device and another one changed the game: the player decides which version to keep. */
export function ConflictDialog({ conflict, onResolve }: { conflict: Conflict; onResolve: (choice: 'local' | 'server') => void }) {
  return (
    <div class="dialog-backdrop">
      <div class="dialog card" role="dialog" aria-modal="true" aria-label={m.conflict_title()}>
        <h2>{m.conflict_title()}</h2>
        <p>{m.conflict_text()}</p>
        <div class="conflict-versions">
          <Summary title={m.conflict_this_device()} state={conflict.local} />
          <Summary title={m.conflict_other_device()} state={conflict.server} />
        </div>
        <div class="dialog-actions">
          <button type="button" class="secondary" onClick={() => onResolve('local')}>
            {m.conflict_keep_local()}
          </button>
          <button type="button" class="primary" onClick={() => onResolve('server')}>
            {m.conflict_take_server()}
          </button>
        </div>
      </div>
    </div>
  );
}
