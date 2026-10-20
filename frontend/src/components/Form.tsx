import type { ComponentChildren } from 'preact';
import { useState } from 'preact/hooks';
import { errorText } from '../i18n/i18n';

interface FieldProps {
  label: string;
  type: 'email' | 'text' | 'password';
  value: string;
  onInput: (value: string) => void;
  autocomplete: string;
  hint?: string;
}

export function Field({ label, type, value, onInput, autocomplete, hint }: FieldProps) {
  return (
    <label class="field">
      <span>{label}</span>
      <input
        type={type}
        value={value}
        autocomplete={autocomplete}
        required
        onInput={(e) => onInput(e.currentTarget.value)}
      />
      {hint && <small>{hint}</small>}
    </label>
  );
}

interface FormProps {
  title: string;
  submitLabel: string;
  onSubmit: () => Promise<void>;
  children: ComponentChildren;
  footer?: ComponentChildren;
}

/** A card with a form that disables itself while submitting and shows the translated error on failure. */
export function Form({ title, submitLabel, onSubmit, children, footer }: FormProps) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submit = async (event: Event) => {
    event.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await onSubmit();
    } catch (e) {
      setError(errorText(e));
    } finally {
      setBusy(false);
    }
  };

  return (
    <form class="card" onSubmit={submit}>
      <h1>{title}</h1>
      <fieldset disabled={busy}>
        {children}
        {error && (
          <p class="error" role="alert">
            {error}
          </p>
        )}
        <button type="submit" class="primary">
          {submitLabel}
        </button>
      </fieldset>
      {footer && <div class="card-footer">{footer}</div>}
    </form>
  );
}
