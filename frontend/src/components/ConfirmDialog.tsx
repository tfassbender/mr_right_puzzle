import type { ComponentChildren } from 'preact';
import { m } from '../i18n/i18n';

interface ConfirmDialogProps {
  title: string;
  children?: ComponentChildren;
  confirmLabel: string;
  onConfirm: () => void;
  onCancel: () => void;
}

/** A modal yes/no question. Rendered only while open – the parent decides when. */
export function ConfirmDialog({ title, children, confirmLabel, onConfirm, onCancel }: ConfirmDialogProps) {
  return (
    <div class="dialog-backdrop" onClick={onCancel}>
      <div
        class="dialog card"
        role="dialog"
        aria-modal="true"
        aria-label={title}
        onClick={(e) => e.stopPropagation()}
      >
        <h2>{title}</h2>
        {children}
        <div class="dialog-actions">
          <button type="button" class="secondary" onClick={onCancel}>
            {m.cancel()}
          </button>
          <button type="button" class="primary" onClick={onConfirm}>
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
