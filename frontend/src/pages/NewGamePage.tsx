import { useEffect, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { createGame, FINALIST_COUNT_MAX, listGames, SUITOR_COUNT_MAX, SUITOR_COUNT_MIN } from '../api/gamesApi';
import { Form } from '../components/Form';
import { defaultGameName, GAME_NAME_MAX_LENGTH } from '../game/gameName';
import { randomSeed } from '../game/randomSeed';
import { formatNumber, getLocale, m } from '../i18n/i18n';

const SUITOR_PRESETS = [100, 500, 1000, 6400];
const FINALIST_COUNTS = Array.from({ length: FINALIST_COUNT_MAX }, (_, i) => i + 1);

/** "Mr Right - <today>", unique among the player's games. Without the game list, just today's name. */
function useSuggestedName(): string {
  const today = new Date().toLocaleDateString(getLocale(), { dateStyle: 'medium' });
  const [existingNames, setExistingNames] = useState<Array<string | null>>([]);
  useEffect(() => {
    listGames().then(
      (games) => setExistingNames(games.map((game) => game.name)),
      () => {},
    );
  }, []);
  return defaultGameName(today, existingNames);
}

export function NewGamePage() {
  const { route } = useLocation();
  const [suitorCount, setSuitorCount] = useState(500);
  const [finalistCount, setFinalistCount] = useState(2);
  const [advanced, setAdvanced] = useState(false);
  const [seed, setSeed] = useState('');
  const [name, setName] = useState('');
  const suggestedName = useSuggestedName();

  const submit = async () => {
    // Without the advanced options the server picks a random seed and the game gets the suggested name.
    const chosenSeed = advanced ? seed.trim() || undefined : undefined;
    const chosenName = (advanced && name.trim()) || suggestedName;
    const game = await createGame({ suitorCount, finalistCount, seed: chosenSeed, name: chosenName });
    route(`/games/${game.id}`);
  };

  const toggleAdvanced = (checked: boolean) => {
    setAdvanced(checked);
    if (checked && !seed) setSeed(randomSeed());
    if (checked && !name) setName(suggestedName);
  };

  return (
    <Form title={m.new_game_title()} submitLabel={m.new_game_submit()} onSubmit={submit}>
      <div class="field">
        <label for="suitorCount">{m.new_game_suitors()}</label>
        <input
          id="suitorCount"
          type="number"
          name="suitorCount"
          min={SUITOR_COUNT_MIN}
          max={SUITOR_COUNT_MAX}
          value={suitorCount}
          required
          onInput={(e) => setSuitorCount(e.currentTarget.valueAsNumber)}
        />
        <div class="chips">
          {SUITOR_PRESETS.map((preset) => (
            <button
              key={preset}
              type="button"
              class={preset === suitorCount ? 'chip selected' : 'chip'}
              onClick={() => setSuitorCount(preset)}
            >
              {preset}
            </button>
          ))}
        </div>
        <small>{m.new_game_suitors_hint({ min: formatNumber(SUITOR_COUNT_MIN), max: formatNumber(SUITOR_COUNT_MAX) })}</small>
      </div>
      <div class="field">
        <label for="finalistCount">{m.new_game_finalists()}</label>
        <select
          id="finalistCount"
          name="finalistCount"
          value={finalistCount}
          onChange={(e) => setFinalistCount(Number(e.currentTarget.value))}
        >
          {FINALIST_COUNTS.map((count) => (
            <option key={count} value={count}>
              {count}
            </option>
          ))}
        </select>
        <small>{m.new_game_finalists_hint()}</small>
      </div>
      <label class="checkbox">
        <input type="checkbox" checked={advanced} onChange={(e) => toggleAdvanced(e.currentTarget.checked)} />
        {m.new_game_advanced()}
      </label>
      {advanced && (
        <div class="field">
          <label for="name">{m.new_game_name()}</label>
          <input
            id="name"
            type="text"
            name="name"
            value={name}
            maxLength={GAME_NAME_MAX_LENGTH}
            onInput={(e) => setName(e.currentTarget.value)}
          />
        </div>
      )}
      {advanced && (
        <div class="field">
          <label for="seed">{m.new_game_seed()}</label>
          <div class="input-with-button">
            <input id="seed" type="text" name="seed" value={seed} maxLength={64} onInput={(e) => setSeed(e.currentTarget.value)} />
            <button type="button" class="secondary" onClick={() => setSeed(randomSeed())}>
              {m.new_game_seed_random()}
            </button>
          </div>
          <small>{m.new_game_seed_hint()}</small>
        </div>
      )}
    </Form>
  );
}
