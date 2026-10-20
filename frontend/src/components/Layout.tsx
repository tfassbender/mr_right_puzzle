import type { ComponentChildren } from 'preact';
import { useLocation } from 'preact-iso';
import { endSession, session } from '../auth/session';
import { changeLanguage, getLocale, languageNames, locales, m } from '../i18n/i18n';

export function Layout({ children }: { children: ComponentChildren }) {
  const { route } = useLocation();

  const logout = () => {
    endSession();
    route('/login');
  };

  return (
    <div class="app">
      <header class="app-header">
        <a href="/" class="app-title">
          {m.app_title()}
        </a>
        <div class="header-actions">
          <select
            aria-label={m.language_label()}
            value={getLocale()}
            onChange={(e) => changeLanguage(e.currentTarget.value)}
          >
            {locales.map((locale) => (
              <option key={locale} value={locale}>
                {languageNames[locale] ?? locale}
              </option>
            ))}
          </select>
          {session.value && (
            <button type="button" class="link-button" onClick={logout}>
              {m.logout()}
            </button>
          )}
        </div>
      </header>
      <main class="app-main">{children}</main>
    </div>
  );
}
