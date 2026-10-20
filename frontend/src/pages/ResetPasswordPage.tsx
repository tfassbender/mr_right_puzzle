import { useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { resetPassword } from '../auth/authApi';
import { Field, Form } from '../components/Form';
import { m } from '../i18n/i18n';

/** Target of the link in the password reset email (`/reset-password?token=…`). */
export function ResetPasswordPage() {
  const { query, route } = useLocation();
  const [password, setPassword] = useState('');

  const submit = async () => {
    await resetPassword(query.token ?? '', password);
    route('/');
  };

  return (
    <Form
      title={m.reset_title()}
      submitLabel={m.reset_submit()}
      onSubmit={submit}
      footer={<a href="/login">{m.back_to_login()}</a>}
    >
      <Field
        label={m.new_password()}
        type="password"
        value={password}
        onInput={setPassword}
        autocomplete="new-password"
        hint={m.password_hint()}
      />
    </Form>
  );
}
