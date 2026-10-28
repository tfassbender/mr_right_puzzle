import { m } from '../i18n/i18n';

/** The story and the rules of the game, opened from the game list. */
export function IntroDialog({ onClose }: { onClose: () => void }) {
  const rules = [
    m.intro_rule_clues(),
    m.intro_rule_cross_out(),
    m.intro_rule_star(),
    m.intro_rule_check(),
    m.intro_rule_tiebreak(),
    m.intro_rule_pick(),
    m.intro_rule_letters(),
    m.intro_rule_saving(),
  ];

  return (
    <div class="dialog-backdrop" onClick={onClose}>
      <div
        class="dialog card intro-dialog"
        role="dialog"
        aria-modal="true"
        aria-label={m.intro_title()}
        onClick={(e) => e.stopPropagation()}
      >
        <h2>{m.intro_title()}</h2>
        <div class="intro-content">
          <h3>{m.intro_story_title()}</h3>
          <p>{m.intro_story_1()}</p>
          <p>{m.intro_story_2()}</p>
          <h3>{m.intro_rules_title()}</h3>
          <ol>
            {rules.map((rule) => (
              <li key={rule}>{rule}</li>
            ))}
          </ol>
        </div>
        <div class="dialog-actions">
          <button type="button" class="primary" onClick={onClose}>
            {m.intro_close()}
          </button>
        </div>
      </div>
    </div>
  );
}
