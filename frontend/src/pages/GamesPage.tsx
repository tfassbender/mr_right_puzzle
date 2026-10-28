import { useEffect, useState } from 'preact/hooks';
import { ApiError } from '../api/client';
import { deleteGame, listGames, type GameSummary } from '../api/gamesApi';
import { session } from '../auth/session';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { errorText, m } from '../i18n/i18n';
import { deleteStoredGame, loadLocalState, loadStoredGameList, storeGameList } from '../storage/gameStorage';

/**
 * Shows this device's progress where the server may not have it yet: unsaved (dirty) local states,
 * and – when offline, from the stored list – every local state.
 */
async function withLocalProgress(games: GameSummary[], offline: boolean): Promise<GameSummary[]> {
  return Promise.all(
    games.map(async (game) => {
      const local = await loadLocalState(game.id);
      if (!local || (!local.dirty && !offline)) return game;
      const { state } = local;
      return {
        ...game,
        crossedOutCount: state.crossedOut.size,
        finalistsFound: state.finalistsFound,
        solved: state.mrRightId !== null,
        wrongGuesses: state.wrongGuesses,
      };
    }),
  );
}

/** The game list from the server, or the last stored one when offline (then `offline` is true). */
async function fetchGames(): Promise<{ games: GameSummary[]; offline: boolean }> {
  try {
    const games = await listGames();
    await storeGameList(games);
    return { games: await withLocalProgress(games, false), offline: false };
  } catch (e) {
    const stored = e instanceof ApiError && e.code === 'NETWORK' ? await loadStoredGameList() : undefined;
    if (!stored) throw e;
    return { games: await withLocalProgress(stored, true), offline: true };
  }
}

/** The player's puzzles, newest first, with their progress. */
export function GamesPage() {
  const user = session.value?.user;
  const [games, setGames] = useState<GameSummary[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [toDelete, setToDelete] = useState<GameSummary | null>(null);
  const [offline, setOffline] = useState(false);

  useEffect(() => {
    fetchGames().then(
      (result) => {
        setGames(result.games);
        setOffline(result.offline);
      },
      (e) => setError(errorText(e)),
    );
  }, []);

  if (!user) return null;

  const confirmDelete = async () => {
    const game = toDelete!;
    setToDelete(null);
    try {
      await deleteGame(game.id);
      await deleteStoredGame(game.id);
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
      {offline && (
        <p class="sync-note offline" role="status">
          <span aria-hidden="true">📝</span> {m.games_offline()}
        </p>
      )}
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
