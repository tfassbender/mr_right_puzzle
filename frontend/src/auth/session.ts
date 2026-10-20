import { effect, signal } from '@preact/signals';

export interface SessionUser {
  userId: string;
  email: string;
  displayName: string;
  emailVerified: boolean;
  language: string | null;
}

export interface Session {
  token: string;
  user: SessionUser;
}

const STORAGE_KEY = 'mrright.session';

/** The logged-in user, persisted in localStorage so the login survives reloads and offline use. */
export const session = signal<Session | null>(loadSession());

effect(() => {
  try {
    if (session.value) localStorage.setItem(STORAGE_KEY, JSON.stringify(session.value));
    else localStorage.removeItem(STORAGE_KEY);
  } catch {
    // Storage blocked (e.g. private mode): the session just doesn't survive a reload.
  }
});

function loadSession(): Session | null {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    return stored ? (JSON.parse(stored) as Session) : null;
  } catch {
    return null;
  }
}

export function startSession(token: string, user: SessionUser): void {
  session.value = { token, user };
}

export function updateSessionUser(user: SessionUser): void {
  if (session.value) session.value = { ...session.value, user };
}

export function endSession(): void {
  session.value = null;
}
