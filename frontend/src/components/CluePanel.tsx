import type { GameDto } from '../api/gamesApi';
import { attributeName, clueText, valueText } from '../game/clueText';
import { m } from '../i18n/i18n';
import { attributesOf, hasNameClues } from '../game/ruleSets';
import { AttributeIcon } from './AttributeIcons';

interface CluePanelProps {
  game: GameDto;
  open: boolean;
  onClose: () => void;
}

/** Emma's 12 clues, the finalist count and the icon legend. A bottom sheet on phones, a side panel on wide screens. */
export function CluePanel({ game, open, onClose }: CluePanelProps) {
  return (
    <aside class={open ? 'clue-panel open' : 'clue-panel'} aria-label={m.game_clues()}>
      <div class="clue-panel-header">
        <h2>{m.game_clues()}</h2>
        <button type="button" class="link-button clue-panel-close" onClick={onClose}>
          {m.game_close()}
        </button>
      </div>
      <div class="clue-panel-body">
        <p class="finalist-count">
          {game.finalistCount === 1 ? m.game_finalists_one() : m.game_finalists_many({ count: game.finalistCount })}
        </p>
        <ol class="clue-list">
          {game.clues.map((clue, index) => (
            <li key={index}>{clueText(clue, game.ruleSet)}</li>
          ))}
        </ol>
        {hasNameClues(game.ruleSet) && <p class="muted">{m.clues_vowels_note()}</p>}
        <h2>{m.game_legend()}</h2>
        <dl class="legend">
          {attributesOf(game.ruleSet).map(({ attribute, values, number }) => (
            <div key={attribute}>
              <dt>{attributeName(attribute)}</dt>
              <dd>
                {number && <span class="legend-entry">{legendText(attribute)}</span>}
                {values.map((value) => (
                  <span key={value} class="legend-entry">
                    <AttributeIcon attribute={attribute} value={value} /> {valueText(attribute, value)}
                  </span>
                ))}
              </dd>
            </div>
          ))}
        </dl>
      </div>
    </aside>
  );
}

/** Number attributes have no value list; the legend says how to read them. */
function legendText(attribute: string): string {
  return attribute === 'HEIGHT' ? m.legend_HEIGHT() : m.legend_BIRTH_YEAR();
}
