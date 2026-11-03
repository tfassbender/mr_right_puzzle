import { useEffect } from 'preact/hooks';
import { version } from '../../package.json';
import { m } from '../i18n/i18n';

const AUTHOR = 'Tobias Fassbender';
const GITHUB_URL = 'https://github.com/tfassbender/mr_right_puzzle';

/**
 * The About dialog from the game list's header button, modelled on Avalon's: a title bar with a close button,
 * the app emblem, name and version, a one-line description, the author, the source link and the copyright.
 * The version is taken from package.json at build time, so the dialog also works offline.
 */
export function AboutDialog({ onClose }: { onClose: () => void }) {
  useEffect(() => {
    const closeOnEscape = (e: KeyboardEvent) => e.key === 'Escape' && onClose();
    window.addEventListener('keydown', closeOnEscape);
    return () => window.removeEventListener('keydown', closeOnEscape);
  }, [onClose]);

  return (
    <div class="dialog-backdrop" onClick={onClose}>
      <div
        class="dialog card about-dialog"
        role="dialog"
        aria-modal="true"
        aria-label={m.about_title()}
        onClick={(e) => e.stopPropagation()}
      >
        <div class="about-titlebar">
          <h2>
            <span aria-hidden="true">ⓘ</span> {m.about_title()}
          </h2>
          <button type="button" class="about-close" aria-label={m.game_close()} title={m.game_close()} onClick={onClose}>
            ✕
          </button>
        </div>
        <div class="about-body">
          <img src="/favicon.svg" alt="" class="about-emblem" />
          <div>
            <h3 class="about-name">{m.app_title()}</h3>
            <p class="about-version">{m.about_version({ version })}</p>
          </div>
          <p>{m.about_description()}</p>
          <p>
            <span class="muted">{m.about_author()}</span> {AUTHOR}
          </p>
          <p>
            <a href={GITHUB_URL} target="_blank" rel="noopener noreferrer">
              {m.about_source()}
            </a>
          </p>
          <p class="about-copyright muted">{m.about_copyright({ year: new Date().getFullYear(), author: AUTHOR })}</p>
        </div>
      </div>
    </div>
  );
}
