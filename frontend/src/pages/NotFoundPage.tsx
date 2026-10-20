import { m } from '../i18n/i18n';

export function NotFoundPage() {
  return (
    <div class="card">
      <p>{m.not_found()}</p>
      <a href="/">{m.to_start()}</a>
    </div>
  );
}
