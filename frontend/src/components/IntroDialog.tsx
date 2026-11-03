import { useState } from 'preact/hooks';
import { BOOK, EVA, ruleSetName } from '../game/ruleSets';
import { m } from '../i18n/i18n';

interface IntroTexts {
  title: string;
  story: string[];
  firstRule: string; // who the wishes belong to
}

function introTexts(ruleSet: string): IntroTexts {
  if (ruleSet === EVA) {
    return {
      title: m.intro_EVA_title(),
      story: [m.intro_EVA_story_1(), m.intro_EVA_story_2(), m.intro_EVA_story_3()],
      firstRule: m.intro_EVA_rule_clues(),
    };
  }
  return { title: m.intro_title(), story: [m.intro_story_1(), m.intro_story_2()], firstRule: m.intro_rule_clues() };
}

/**
 * The story and the rules of the game, opened from the game list. Players with more than one rule set choose
 * which story to read; Eva's comes first. The rules are the same for all – and don't give the finale away.
 */
export function IntroDialog({ ruleSets, onClose }: { ruleSets: string[]; onClose: () => void }) {
  const [ruleSet, setRuleSet] = useState(ruleSets.includes(EVA) ? EVA : BOOK);
  const texts = introTexts(ruleSet);
  const rules = [
    texts.firstRule,
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
        aria-label={texts.title}
        onClick={(e) => e.stopPropagation()}
      >
        <h2>{texts.title}</h2>
        {ruleSets.length > 1 && (
          <div class="field intro-rule-set">
            <label for="introRuleSet">{m.new_game_rule_set()}</label>
            <select id="introRuleSet" value={ruleSet} onChange={(e) => setRuleSet(e.currentTarget.value)}>
              {ruleSets.map((id) => (
                <option key={id} value={id}>
                  {ruleSetName(id)}
                </option>
              ))}
            </select>
          </div>
        )}
        <div class="intro-content">
          <h3>{m.intro_story_title()}</h3>
          {texts.story.map((paragraph) => (
            <p key={paragraph}>{paragraph}</p>
          ))}
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
