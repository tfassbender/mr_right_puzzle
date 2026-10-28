import type { ComponentType } from 'preact';
import { useEffect } from 'preact/hooks';
import { LocationProvider, Route, Router, useLocation } from 'preact-iso';
import { ApiError } from './api/client';
import { fetchMe, loadAuthConfig } from './auth/authApi';
import { endSession, session, updateSessionUser } from './auth/session';
import { Layout } from './components/Layout';
import { ForgotPasswordPage } from './pages/ForgotPasswordPage';
import { GamesPage } from './pages/GamesPage';
import { LoginPage } from './pages/LoginPage';
import { NewGamePage } from './pages/NewGamePage';
import { NotFoundPage } from './pages/NotFoundPage';
import { RegisterPage } from './pages/RegisterPage';
import { ResetPasswordPage } from './pages/ResetPasswordPage';
import { VerifyEmailPage } from './pages/VerifyEmailPage';

/** Renders [page] only for logged-in users, everyone else is sent to the login. */
function loginRequired(page: ComponentType) {
  return function LoginRequired() {
    const { route } = useLocation();
    const loggedIn = session.value !== null;
    useEffect(() => {
      if (!loggedIn) route('/login', true);
    }, [loggedIn]);
    if (!loggedIn) return null;
    const Page = page;
    return <Page />;
  };
}

const ProtectedGamesPage = loginRequired(GamesPage);
const ProtectedNewGamePage = loginRequired(NewGamePage);

/** Refreshes the stored user on start; an invalid or expired token ends the session. Offline, the stored one is kept. */
async function refreshSession(): Promise<void> {
  if (!session.value) return;
  try {
    updateSessionUser(await fetchMe());
  } catch (e) {
    if (e instanceof ApiError && (e.status === 401 || e.status === 404)) endSession();
  }
}

export function App() {
  useEffect(() => {
    loadAuthConfig();
    refreshSession();
  }, []);

  return (
    <LocationProvider>
      <Layout>
        <Router>
          <Route path="/" component={ProtectedGamesPage} />
          <Route path="/new" component={ProtectedNewGamePage} />
          <Route path="/login" component={LoginPage} />
          <Route path="/register" component={RegisterPage} />
          <Route path="/verify-email" component={VerifyEmailPage} />
          <Route path="/forgot-password" component={ForgotPasswordPage} />
          <Route path="/reset-password" component={ResetPasswordPage} />
          <Route default component={NotFoundPage} />
        </Router>
      </Layout>
    </LocationProvider>
  );
}
