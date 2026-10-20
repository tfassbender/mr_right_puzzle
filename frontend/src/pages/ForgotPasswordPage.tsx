import { useState } from 'preact/hooks';
import { forgotPassword } from '../auth/authApi';
import { Field, Form } from '../components/Form';
import { m } from '../i18n/i18n';

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [sent, setSent] = useState(false);

  const submit = async () => {
    await forgotPassword(email);
    setSent(true);
  };

  if (sent) {
    return (
      <div class="card">
        <h1>{m.forgot_title()}</h1>
        <p>{m.forgot_sent()}</p>
        <a href="/login">{m.back_to_login()}</a>
      </div>
    );
  }

  return (
    <Form
      title={m.forgot_title()}
      submitLabel={m.forgot_submit()}
      onSubmit={submit}
      footer={<a href="/login">{m.back_to_login()}</a>}
    >
      <Field label={m.email()} type="email" value={email} onInput={setEmail} autocomplete="email" />
    </Form>
  );
}
