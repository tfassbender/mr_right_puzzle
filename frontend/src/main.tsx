import { render } from 'preact';
import { App } from './app';
import { getLocale } from './i18n/i18n';
import './styles.css';

document.documentElement.lang = getLocale();
render(<App />, document.getElementById('app')!);
