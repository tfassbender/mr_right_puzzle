import { useEffect, useState } from 'preact/hooks';
import { deleteGame, listGames, type GameSummary } from '../api/gamesApi';
import { session } from '../auth/session';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { errorText, m } from '../i18n/i18n';

/** The player's puzzles, newest first, with their progress. */
export function GamesPage() {
  const user = session.value?.user;
  const [games, setGames] = useState<GameSummary[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [toDelete, setToDelete] = useState<GameSummary | null>(null);

  useEffect(() => {
    listGames().then(setGames, (e) => setError(errorText(e)));
  }, []);

  if (!user) return null;

  const confirmDelete = async () => {
    const game = toDelete!;
    setToDelete(null);
    try {
      await deleteGame(game.id);
      setGames((current) => current?.filter((g) => g.id !== game.id) ?? null);
    } catch (e) {
      setError(errorText(e));
    }
  };

  return (
    <div class="stack">
      <div class="page-title">
        <h1>{m.games_title()}</h1>
        <a href="/new" class="primary button-link">
          {m.games_new()}
        </a>
      </div>
      <p class="muted">{m.games_welcome({ name: user.displayName })}</p>
      {error && (
        <p class="error" role="alert">
          {error}
        </p>
      )}
      {games?.length === 0 && <p class="muted">{m.games_empty()}</p>}
      <ul class="game-list">
        {games?.map((game) => <GameItem key={game.id} game={game} onDelete={() => setToDelete(game)} />)}
      </ul>
      {toDelete && (
        <ConfirmDialog
          title={m.games_delete_title()}
          confirmLabel={m.games_delete()}
          onConfirm={confirmDelete}
          onCancel={() => setToDelete(null)}
        >
          <p>{m.games_delete_text({ seed: toDelete.seed })}</p>
        </ConfirmDialog>
      )}
    </div>
  );
}

function GameItem({ game, onDelete }: { game: GameSummary; onDelete: () => void }) {
  const percent = Math.round((game.crossedOutCount / game.suitorCount) * 100);
  const title = (
    <>
      <strong>{m.games_seed({ seed: game.seed })}</strong>
      <span class="muted">
        {m.games_size({ suitors: game.suitorCount, finalists: game.finalistCount })}
      </span>
    </>
  );

  return (
    <li class="card game-item" data-testid="game-item">
      {game.outdated ? (
        <div class="game-item-title">{title}</div>
      ) : (
        <a href={`/games/${game.id}`} class="game-item-title">
          {title}
        </a>
      )}
      <div class="progress" aria-hidden="true">
        <div class="progress-bar" style={{ width: `${percent}%` }} />
      </div>
      <div class="game-item-footer">
        <span class="muted">
          {game.outdated
            ? m.games_outdated()
            : game.solved
              ? m.games_solved()
              : m.games_progress({ crossedOut: game.crossedOutCount, total: game.suitorCount })}
          {game.wrongGuesses > 0 && ` · ${m.games_wrong_guesses({ count: game.wrongGuesses })}`}
        </span>
        <button type="button" class="link-button" onClick={onDelete}>
          {m.games_delete()}
        </button>
      </div>
    </li>
  );
}
