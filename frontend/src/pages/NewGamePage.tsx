import { useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { createGame, FINALIST_COUNT_MAX, SUITOR_COUNT_MAX, SUITOR_COUNT_MIN } from '../api/gamesApi';
import { Form } from '../components/Form';
import { formatNumber, m } from '../i18n/i18n';

const SUITOR_PRESETS = [100, 500, 1000, 6400];
const FINALIST_COUNTS = Array.from({ length: FINALIST_COUNT_MAX }, (_, i) => i + 1);

export function NewGamePage() {
  const { route } = useLocation();
  const [suitorCount, setSuitorCount] = useState(500);
  const [finalistCount, setFinalistCount] = useState(2);
  const [seed, setSeed] = useState('');

  const submit = async () => {
    const game = await createGame({ suitorCount, finalistCount, seed: seed.trim() || undefined });
    route(`/games/${game.id}`);
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
      <div class="field">
        <label for="seed">{m.new_game_seed()}</label>
        <input id="seed" type="text" name="seed" value={seed} maxLength={64} onInput={(e) => setSeed(e.currentTarget.value)} />
        <small>{m.new_game_seed_hint()}</small>
      </div>
    </Form>
  );
}
