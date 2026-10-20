import { m } from '../paraglide/messages.js';
import { getLocale, isLocale, locales, setLocale } from '../paraglide/runtime.js';
import { ApiError } from '../api/client';
import { saveLanguage } from '../auth/authApi';
import { session } from '../auth/session';

export { m, getLocale, locales };

export const languageNames: Record<string, string> = { en: 'English', de: 'Deutsch' };

/** Switches the UI language (reloads the page) and saves it to the account, so it follows the user to other devices. */
export async function changeLanguage(locale: string): Promise<void> {
  if (!isLocale(locale)) return;
  if (session.value) {
    try {
      await saveLanguage(locale);
    } catch {
      // Offline: the choice is still remembered locally.
    }
  }
  setLocale(locale);
}

/** Applies the language saved on the account, e.g. after logging in on a new device. */
export function applyAccountLanguage(language: string | null): void {
  if (language && isLocale(language) && language !== getLocale()) setLocale(language);
}

/** The translated text for an error from the API (or anything else that was thrown). */
export function errorText(error: unknown): string {
  const code = error instanceof ApiError ? error.code : 'UNKNOWN';
  const message = (m as unknown as Record<string, (() => string) | undefined>)[`error_${code}`];
  return message ? message() : m.error_UNKNOWN();
}
