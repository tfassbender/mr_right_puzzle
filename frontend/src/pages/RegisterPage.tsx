import { useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { register } from '../auth/authApi';
import { Field, Form } from '../components/Form';
import { m } from '../i18n/i18n';

export function RegisterPage() {
  const { route } = useLocation();
  const [email, setEmail] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [password, setPassword] = useState('');
  const [mustConfirmEmail, setMustConfirmEmail] = useState(false);

  const submit = async () => {
    const loggedIn = await register(email, displayName, password);
    if (loggedIn) route('/');
    else setMustConfirmEmail(true);
  };

  if (mustConfirmEmail) {
    return (
      <div class="card">
        <h1>{m.register_title()}</h1>
        <p>{m.register_check_email()}</p>
        <a href="/login">{m.back_to_login()}</a>
      </div>
    );
  }

  const footer = (
    <span>
      {m.register_have_account()} <a href="/login">{m.login_title()}</a>
    </span>
  );

  return (
    <Form title={m.register_title()} submitLabel={m.register_submit()} onSubmit={submit} footer={footer}>
      <Field label={m.email()} type="email" value={email} onInput={setEmail} autocomplete="email" />
      <Field
        label={m.display_name()}
        type="text"
        value={displayName}
        onInput={setDisplayName}
        autocomplete="username"
        hint={m.display_name_hint()}
      />
      <Field
        label={m.password()}
        type="password"
        value={password}
        onInput={setPassword}
        autocomplete="new-password"
        hint={m.password_hint()}
      />
    </Form>
  );
}
