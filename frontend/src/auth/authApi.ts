import { signal } from '@preact/signals';
import { apiRequest } from '../api/client';
import { startSession, type SessionUser } from './session';

/** Response of register/login/verify/reset. [token] is null while the email still has to be confirmed. */
interface AuthResponse extends SessionUser {
  token: string | null;
}

/** Whether the server has email features turned off (dev): then verify/forgot/reset are hidden. */
export const emailDisabled = signal(false);

export async function loadAuthConfig(): Promise<void> {
  try {
    const config = await apiRequest<{ emailDisabled: boolean }>('GET', '/auth/config');
    emailDisabled.value = config.emailDisabled;
  } catch {
    // Offline: keep the default, the email links are just shown.
  }
}

/** Returns true if the user is logged in afterwards, false if the email must be confirmed first. */
export async function register(email: string, displayName: string, password: string): Promise<boolean> {
  return applyAuthResponse(await apiRequest<AuthResponse>('POST', '/auth/register', { email, displayName, password }));
}

export async function login(email: string, password: string): Promise<void> {
  applyAuthResponse(await apiRequest<AuthResponse>('POST', '/auth/login', { email, password }));
}

export async function verifyEmail(token: string): Promise<void> {
  applyAuthResponse(await apiRequest<AuthResponse>('POST', '/auth/verify-email', { token }));
}

export async function forgotPassword(email: string): Promise<void> {
  await apiRequest<void>('POST', '/auth/forgot-password', { email });
}

export async function resetPassword(token: string, newPassword: string): Promise<void> {
  applyAuthResponse(await apiRequest<AuthResponse>('POST', '/auth/reset-password', { token, newPassword }));
}

export function fetchMe(): Promise<SessionUser> {
  return apiRequest<SessionUser>('GET', '/auth/me');
}

export function saveLanguage(language: string): Promise<SessionUser> {
  return apiRequest<SessionUser>('PUT', '/auth/language', { language });
}

function applyAuthResponse({ token, ...user }: AuthResponse): boolean {
  if (!token) return false;
  startSession(token, user);
  return true;
}
