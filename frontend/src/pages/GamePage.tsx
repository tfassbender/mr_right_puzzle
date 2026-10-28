import { useEffect, useState } from 'preact/hooks';
import { useRoute } from 'preact-iso';
import { loadGame } from '../api/gamesApi';
import { CluePanel } from '../components/CluePanel';
import { Directory } from '../components/Directory';
import { FinalistsPanel } from '../components/FinalistsPanel';
import { GameStore } from '../game/gameStore';
import { fromDto } from '../game/playState';
import { errorText, formatNumber, m } from '../i18n/i18n';
import { SyncManager } from '../sync/syncManager';

export function GamePage() {
  const { params } = useRoute();
  const [store, setStore] = useState<GameStore | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let sync: SyncManager | null = null;
    const flush = () => {
      if (document.visibilityState === 'hidden') sync?.flush();
    };
    loadGame(params.id).then((game) => {
      sync = new SyncManager(game.id, game.state.version);
      setStore(new GameStore(game, fromDto(game.state), (state) => sync!.changed(state)));
    }, (e) => setError(errorText(e)));
    document.addEventListener('visibilitychange', flush);
    return () => {
      document.removeEventListener('visibilitychange', flush);
      sync?.flush();
    };
  }, [params.id]);

  if (error) {
    return (
      <div class="card">
        <p class="error" role="alert">{error}</p>
        <a href="/">{m.game_back()}</a>
      </div>
    );
  }
  if (!store) return <p class="muted">{m.game_loading()}</p>;
  return <GameView store={store} />;
}

function GameView({ store }: { store: GameStore }) {
  const [cluesOpen, setCluesOpen] = useState(false);
  const state = store.state.value;
  const hint = store.hint.value;

  return (
    <div class="game-page">
      <div class="game-main">
        <div class="game-toolbar">
          <a href="/" class="game-back" aria-label={m.game_back()}>
            ←
          </a>
          <span class="remaining" data-testid="remaining">
            {m.game_remaining({
              remaining: formatNumber(store.remaining.value.length),
              total: formatNumber(store.game.suitorCount),
            })}
          </span>
          <button type="button" class="secondary small" disabled={!store.canUndo.value} onClick={() => store.undo()}>
            {m.game_undo()}
          </button>
          <button type="button" class="primary small clues-button" onClick={() => setCluesOpen(true)}>
            {m.game_clues()}
          </button>
        </div>
        {hint && (
          <p class="hint" role="alert">
            {m[`game_hint_${hint}`]()}
          </p>
        )}
        {state.finalistsFound && <FinalistsPanel store={store} />}
        <Directory store={store} />
      </div>
      <CluePanel game={store.game} open={cluesOpen} onClose={() => setCluesOpen(false)} />
    </div>
  );
}
