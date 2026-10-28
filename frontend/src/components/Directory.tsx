import { useComputed } from '@preact/signals';
import { useEffect, useRef, useState } from 'preact/hooks';
import type { Suitor } from '../api/gamesApi';
import {
  itemAt,
  itemCount,
  remainingPerSection,
  sectionCount,
  sectionHeaderIndex,
  contextRowsAbove,
  sectionRange,
  suitorIndex,
} from '../game/directoryLayout';
import type { GameStore } from '../game/gameStore';
import { markerOf } from '../game/playState';
import { formatNumber, m } from '../i18n/i18n';
import { AttributeIcons } from './AttributeIcons';
import { headerAction } from './headerSlot';

const ITEM_HEIGHT = 60;
const OVERSCAN = 10;

export function suitorName(suitor: Suitor): string {
  return `${suitor.firstName} ${suitor.lastName}`;
}

/** Tracks the scroll position and height of a scroll container, so only the visible rows are rendered. */
function useViewport() {
  const ref = useRef<HTMLDivElement>(null);
  const [scrollTop, setScrollTop] = useState(0);
  const [height, setHeight] = useState(800);

  useEffect(() => {
    const element = ref.current;
    if (!element || typeof ResizeObserver === 'undefined') return;
    const observer = new ResizeObserver(() => setHeight(element.clientHeight));
    observer.observe(element);
    return () => observer.disconnect();
  }, []);

  const scrollToIndex = (index: number) => ref.current?.scrollTo({ top: index * ITEM_HEIGHT });
  // Read the height directly: on the first render the ResizeObserver may not have reported it yet.
  const visibleRows = () => Math.floor((ref.current?.clientHeight ?? height) / ITEM_HEIGHT);
  return { ref, scrollTop, height, scrollToIndex, visibleRows, onScroll: () => setScrollTop(ref.current?.scrollTop ?? 0) };
}

/**
 * The virtualized directory: numbered sections of 100 suitors. Only the rows in view (plus a few around them)
 * are rendered, so even 10,000 suitors stay fast. Tap a row to cross out / restore, the star marks a possible match.
 */
export function Directory({ store }: { store: GameStore }) {
  const { suitorCount } = store.game;
  const viewport = useViewport();
  const perSection = useComputed(() => remainingPerSection(suitorCount, store.state.value.crossedOut));

  const jumpToFirstUnmarked = () => {
    const id = store.firstUnmarked.peek();
    if (id === null) return;
    // A few rows above stay in view, so the player sees where in the list they are.
    viewport.scrollToIndex(Math.max(0, suitorIndex(id) - contextRowsAbove(viewport.visibleRows())));
  };
  useEffect(() => {
    // Opening a game continues where the player left off.
    jumpToFirstUnmarked();
    headerAction.value = <NextOpenButton store={store} onClick={jumpToFirstUnmarked} />;
    return () => {
      headerAction.value = null;
    };
  }, []);

  const total = itemCount(suitorCount);
  const first = Math.max(0, Math.floor(viewport.scrollTop / ITEM_HEIGHT) - OVERSCAN);
  const last = Math.min(total - 1, Math.ceil((viewport.scrollTop + viewport.height) / ITEM_HEIGHT) + OVERSCAN);

  const items = [];
  for (let index = first; index <= last; index++) {
    const item = itemAt(index);
    const style = { top: `${index * ITEM_HEIGHT}px`, height: `${ITEM_HEIGHT}px` };
    if (item.kind === 'header') {
      items.push(<SectionHeader key={`s${item.section}`} section={item.section} left={perSection.value[item.section]} suitorCount={suitorCount} style={style} />);
    } else if (item.id < suitorCount) {
      items.push(<SuitorRow key={item.id} store={store} suitor={store.game.suitors[item.id]} style={style} />);
    }
  }

  return (
    <div class="directory">
      {sectionCount(suitorCount) > 1 && (
        <select
          class="section-jump"
          aria-label={m.game_jump()}
          onChange={(e) => viewport.scrollToIndex(sectionHeaderIndex(Number(e.currentTarget.value)))}
        >
          {perSection.value.map((left, section) => (
            <option key={section} value={section}>
              {m.game_section({ number: section + 1 })} · {m.game_section_left({ count: left })}
            </option>
          ))}
        </select>
      )}
      <div class="directory-scroll" ref={viewport.ref} onScroll={viewport.onScroll} data-item-height={ITEM_HEIGHT}>
        <div class="directory-content" style={{ height: `${total * ITEM_HEIGHT}px` }}>
          {items}
        </div>
      </div>
    </div>
  );
}

/** In the header: jumps to the first suitor without a marker. Phones show only the arrow, with a tooltip. */
function NextOpenButton({ store, onClick }: { store: GameStore; onClick: () => void }) {
  const label = m.game_next_unmarked();
  return (
    <button
      type="button"
      class="header-button"
      title={label}
      aria-label={label}
      disabled={store.firstUnmarked.value === null}
      onClick={onClick}
    >
      <span aria-hidden="true">↓</span>
      <span class="wide-only" aria-hidden="true">
        {label}
      </span>
    </button>
  );
}

interface SuitorRowProps {
  store: GameStore;
  suitor: Suitor;
  style: Record<string, string>;
}

/** A suitor: the row itself crosses him out (or restores him), the star marks him as possible match. */
function SuitorRow({ store, suitor, style }: SuitorRowProps) {
  const state = store.state.value;
  const marker = markerOf(state, suitor.id);
  const name = `Mr ${suitorName(suitor)}`;
  const rowClass = { none: 'suitor-row', noMatch: 'suitor-row crossed-out', possibleMatch: 'suitor-row possible-match' }[marker];

  return (
    <div class={rowClass} style={style}>
      <button
        type="button"
        class="suitor-select"
        aria-pressed={marker === 'noMatch'}
        disabled={state.finalistsFound}
        onClick={() => store.toggleNoMatch(suitor.id)}
        data-suitor-id={suitor.id}
      >
        <span class="suitor-number">{suitor.id + 1}</span>
        <span class="suitor-main">
          <span class="suitor-name">{name}</span>
          <AttributeIcons suitor={suitor} />
        </span>
      </button>
      <button
        type="button"
        class="star-button"
        aria-pressed={marker === 'possibleMatch'}
        aria-label={m.game_possible_match({ name })}
        disabled={state.finalistsFound}
        onClick={() => store.togglePossibleMatch(suitor.id)}
        data-star-id={suitor.id}
      >
        <span aria-hidden="true">{marker === 'possibleMatch' ? '★' : '☆'}</span>
      </button>
    </div>
  );
}

interface SectionHeaderProps {
  section: number;
  left: number;
  suitorCount: number;
  style: Record<string, string>;
}

function SectionHeader({ section, left, suitorCount, style }: SectionHeaderProps) {
  const [firstId, lastId] = sectionRange(section, suitorCount);
  return (
    <div class="section-header" style={style}>
      <strong>{m.game_section({ number: section + 1 })}</strong>
      <span class="muted">
        {formatNumber(firstId + 1)}–{formatNumber(lastId + 1)} · {m.game_section_left({ count: left })}
      </span>
    </div>
  );
}
