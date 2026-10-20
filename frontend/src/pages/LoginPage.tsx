import { useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { emailDisabled, login } from '../auth/authApi';
import { session } from '../auth/session';
import { Field, Form } from '../components/Form';
import { applyAccountLanguage, m } from '../i18n/i18n';

export function LoginPage() {
  const { route } = useLocation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const submit = async () => {
    await login(email, password);
    route('/');
    applyAccountLanguage(session.value?.user.language ?? null);
  };

  const footer = (
    <>
      {!emailDisabled.value && <a href="/forgot-password">{m.login_forgot_password()}</a>}
      <span>
        {m.login_no_account()} <a href="/register">{m.register_title()}</a>
      </span>
    </>
  );

  return (
    <Form title={m.login_title()} submitLabel={m.login_submit()} onSubmit={submit} footer={footer}>
      <Field label={m.email()} type="email" value={email} onInput={setEmail} autocomplete="email" />
      <Field label={m.password()} type="password" value={password} onInput={setPassword} autocomplete="current-password" />
    </Form>
  );
}
