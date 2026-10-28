import { useState } from 'preact/hooks';
import type { GameStore } from '../game/gameStore';
import { m } from '../i18n/i18n';
import { ConfirmDialog } from './ConfirmDialog';
import { suitorName } from './Directory';

/**
 * How many possible matches are starred, and the submit: once as many are starred as there are finalists
 * (or only that many are left), the player can check them without crossing out everybody else first.
 */
export function MatchBar({ store }: { store: GameStore }) {
  const [confirming, setConfirming] = useState(false);
  const { game } = store;
  const candidates = store.candidates.value;

  const submit = () => {
    setConfirming(false);
    store.submit();
  };

  return (
    <div class="match-bar">
      <span class="match-count" data-testid="match-count">
        <span aria-hidden="true">★ </span>
        {m.game_possible_matches({ count: store.state.value.possibleMatches.size, total: game.finalistCount })}
      </span>
      <button type="button" class="primary small" disabled={!candidates} onClick={() => setConfirming(true)}>
        {m.game_submit()}
      </button>
      {confirming && candidates && (
        <ConfirmDialog title={m.game_submit_title()} confirmLabel={m.game_submit_confirm()} onConfirm={submit} onCancel={() => setConfirming(false)}>
          <ul>
            {candidates.map((id) => (
              <li key={id}>Mr {suitorName(game.suitors[id])}</li>
            ))}
          </ul>
          <p class="muted">{m.game_submit_text()}</p>
        </ConfirmDialog>
      )}
    </div>
  );
}
