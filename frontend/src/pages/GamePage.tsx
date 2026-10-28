import { useEffect, useState } from 'preact/hooks';
import { useRoute } from 'preact-iso';
import { CluePanel } from '../components/CluePanel';
import { ConflictDialog } from '../components/ConflictDialog';
import { Directory } from '../components/Directory';
import { FinalistsPanel } from '../components/FinalistsPanel';
import { errorText, formatNumber, m } from '../i18n/i18n';
import { openGameSession, type GameSession } from '../sync/gameSession';

/** Opens the game and keeps it in sync: refresh on tab return and reconnect, save when the tab is hidden. */
function useGameSession(gameId: string) {
  const [session, setSession] = useState<GameSession | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let current: GameSession | null = null;
    let closed = false;
    openGameSession(gameId).then(
      (opened) => {
        if (closed) return opened.sync.dispose();
        current = opened;
        setSession(opened);
        opened.sync.refresh();
      },
      (e) => setError(errorText(e)),
    );

    const onVisibilityChange = () => {
      if (document.visibilityState === 'hidden') current?.sync.flush(true);
      else current?.sync.refresh();
    };
    const onOnline = () => current?.sync.refresh();
    document.addEventListener('visibilitychange', onVisibilityChange);
    window.addEventListener('online', onOnline);
    return () => {
      closed = true;
      document.removeEventListener('visibilitychange', onVisibilityChange);
      window.removeEventListener('online', onOnline);
      current?.sync.flush();
      current?.sync.dispose();
    };
  }, [gameId]);

  return { session, error };
}

export function GamePage() {
  const { params } = useRoute();
  const { session, error } = useGameSession(params.id);

  if (error) {
    return (
      <div class="card">
        <p class="error" role="alert">
          {error}
        </p>
        <a href="/">{m.game_back()}</a>
      </div>
    );
  }
  if (!session) return <p class="muted">{m.game_loading()}</p>;
  return <GameView session={session} />;
}

function GameView({ session: { store, sync } }: { session: GameSession }) {
  const [cluesOpen, setCluesOpen] = useState(false);
  const state = store.state.value;
  const hint = store.hint.value;
  const status = sync.status.value;
  const conflict = sync.conflict.value;

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
        {(status === 'offline' || status === 'error') && (
          <p class={`sync-note ${status}`} role="status" data-testid="sync-note">
            <span aria-hidden="true">📝</span> {status === 'offline' ? m.sync_offline() : m.sync_error()}
          </p>
        )}
        {hint && (
          <p class="hint" role="alert">
            {m[`game_hint_${hint}`]()}
          </p>
        )}
        {state.finalistsFound && <FinalistsPanel store={store} />}
        <Directory store={store} />
      </div>
      <CluePanel game={store.game} open={cluesOpen} onClose={() => setCluesOpen(false)} />
      {conflict && <ConflictDialog conflict={conflict} onResolve={(choice) => sync.resolveConflict(choice)} />}
    </div>
  );
}
