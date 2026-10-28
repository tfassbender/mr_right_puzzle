import type { ComponentChildren } from 'preact';
import { signal } from '@preact/signals';

/** A page-specific action shown in the app header, before the language selection. Pages clear it on unmount. */
export const headerAction = signal<ComponentChildren>(null);
