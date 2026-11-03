import { useEffect, useState } from 'preact/hooks';
import type { Finale, Suitor } from '../api/gamesApi';
import { m } from '../i18n/i18n';
import { AttributeIcons } from './AttributeIcons';
import { suitorName } from './Directory';

interface FinaleDialogProps {
  ruleSet: string;
  mrRight: Suitor;
  finale: Finale;
  onClose: () => void;
}

type Choice = 'none' | 'mrRight' | 'finaleSuitor';

/**
 * Eva's rules end with one more suitor: after Mr Right is found, another gentleman arrives who meets every wish
 * too. The last tie-break – who treats her better – is the player's own choice between the two. Choosing
 * Mr Right only earns a playful hint (no wrong guess); the result is shown once she chooses the finale suitor.
 */
export function FinaleDialog({ ruleSet, mrRight, finale, onClose }: FinaleDialogProps) {
  const [choice, setChoice] = useState<Choice>('none');

  useEffect(() => {
    const closeOnEscape = (e: KeyboardEvent) => e.key === 'Escape' && onClose();
    window.addEventListener('keydown', closeOnEscape);
    return () => window.removeEventListener('keydown', closeOnEscape);
  }, [onClose]);

  const decided = choice === 'finaleSuitor';

  return (
    <div class="dialog-backdrop" onClick={onClose}>
      <div
        class="dialog card intro-dialog finale-dialog"
        role="dialog"
        aria-modal="true"
        aria-label={m.finale_title()}
        onClick={(e) => e.stopPropagation()}
      >
        <h2>{m.finale_title()}</h2>
        <div class="intro-content">
          <p>{m.finale_intro()}</p>
          <p>{m.finale_tiebreak()}</p>
          <ul class="finale-suitors">
            <FinaleEntry
              suitor={mrRight}
              ruleSet={ruleSet}
              rating={finale.mrRightRating}
              onChoose={decided ? undefined : () => setChoice('mrRight')}
            />
            <FinaleEntry
              suitor={finale.suitor}
              ruleSet={ruleSet}
              rating={finale.suitorRating}
              chosen={decided}
              onChoose={decided ? undefined : () => setChoice('finaleSuitor')}
            />
          </ul>
          {choice === 'mrRight' && (
            <p class="error" role="alert">
              {m.finale_wrong_choice()}
            </p>
          )}
          {decided && (
            <div role="status">
              <h3>{m.finale_result_title()}</h3>
              <p>{m.finale_result({ name: suitorName(finale.suitor) })}</p>
            </div>
          )}
        </div>
        <div class="dialog-actions">
          <button type="button" class={decided ? 'primary' : 'secondary'} onClick={onClose}>
            {m.finale_close()}
          </button>
        </div>
      </div>
    </div>
  );
}

interface FinaleEntryProps {
  suitor: Suitor;
  ruleSet: string;
  rating: number;
  chosen?: boolean;
  onChoose?: () => void; // no button once the choice is made
}

function FinaleEntry({ suitor, ruleSet, rating, chosen = false, onChoose }: FinaleEntryProps) {
  const label = `${m.finale_rating()}: ${m.finale_rating_value({ value: rating })}`;
  return (
    <li class={chosen ? 'finale-entry chosen' : 'finale-entry'} data-testid="finale-entry">
      <span class="suitor-name">Mr {suitorName(suitor)}</span>
      <AttributeIcons suitor={suitor} ruleSet={ruleSet} />
      <span class="finale-rating-label">{label}</span>
      <span class="rating" role="meter" aria-valuemin={1} aria-valuemax={10} aria-valuenow={rating} aria-label={label}>
        <span class="rating-bar" style={{ width: `${rating * 10}%` }} />
      </span>
      {onChoose && (
        <button type="button" class="primary finale-choose" onClick={onChoose}>
          {m.finale_choose()}
        </button>
      )}
    </li>
  );
}
