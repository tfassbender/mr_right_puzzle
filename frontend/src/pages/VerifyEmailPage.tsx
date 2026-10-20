import { useEffect, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { verifyEmail } from '../auth/authApi';
import { errorText, m } from '../i18n/i18n';

/** Target of the link in the verification email (`/verify-email?token=…`). */
export function VerifyEmailPage() {
  const { query, route } = useLocation();
  const token = query.token;
  const [error, setError] = useState<string | null>(token ? null : m.verify_missing_token());

  useEffect(() => {
    if (!token) return;
    verifyEmail(token)
      .then(() => route('/', true))
      .catch((e) => setError(errorText(e)));
  }, [token]);

  return (
    <div class="card">
      <h1>{m.verify_title()}</h1>
      {error ? (
        <p class="error" role="alert">
          {error}
        </p>
      ) : (
        <p>{m.verify_running()}</p>
      )}
      <a href="/login">{m.back_to_login()}</a>
    </div>
  );
}
