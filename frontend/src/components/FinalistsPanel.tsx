import { useState } from 'preact/hooks';
import type { Suitor } from '../api/gamesApi';
import type { GameStore } from '../game/gameStore';
import { m } from '../i18n/i18n';
import { AttributeIcons } from './AttributeIcons';
import { ConfirmDialog } from './ConfirmDialog';
import { suitorName } from './Directory';

/** Shown once the finalists are found: the tie-break rule and the pick – or the result once solved. */
export function FinalistsPanel({ store }: { store: GameStore }) {
  const [candidate, setCandidate] = useState<Suitor | null>(null);
  const { game } = store;
  const state = store.state.value;

  if (state.mrRightId !== null) {
    return (
      <section class="card finalists-panel solved" role="status">
        <h2>{m.game_solved_title()}</h2>
        <p>{m.game_solved_text({ name: suitorName(game.suitors[state.mrRightId]) })}</p>
        <p class="muted">{m.game_wrong_guesses({ count: state.wrongGuesses })}</p>
        <a href="/">{m.game_back()}</a>
      </section>
    );
  }

  const finalists = store.remaining.value.map((id) => game.suitors[id]);
  const confirm = () => {
    const picked = candidate!;
    setCandidate(null);
    store.pick(picked.id);
  };

  return (
    <section class="card finalists-panel">
      <h2>{game.finalistCount === 1 ? m.game_finalist_title() : m.game_finalists_title()}</h2>
      <p class="muted">{game.finalistCount === 1 ? m.game_single_finalist() : m.game_tiebreak()}</p>
      <ul class="finalists">
        {finalists.map((suitor) => (
          <li key={suitor.id}>
            <span class="suitor-main">
              <span class="suitor-name">Mr {suitorName(suitor)}</span>
              <AttributeIcons suitor={suitor} />
            </span>
            <button type="button" class="primary" onClick={() => setCandidate(suitor)}>
              {m.game_pick()}
            </button>
          </li>
        ))}
      </ul>
      {candidate && (
        <ConfirmDialog
          title={m.game_pick_title()}
          confirmLabel={m.game_pick_confirm()}
          onConfirm={confirm}
          onCancel={() => setCandidate(null)}
        >
          <p>{m.game_pick_text({ name: suitorName(candidate) })}</p>
        </ConfirmDialog>
      )}
    </section>
  );
}
