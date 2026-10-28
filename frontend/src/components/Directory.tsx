import { useComputed } from '@preact/signals';
import { useEffect, useRef, useState } from 'preact/hooks';
import type { Suitor } from '../api/gamesApi';
import {
  itemAt,
  itemCount,
  remainingPerSection,
  sectionCount,
  sectionHeaderIndex,
  sectionRange,
} from '../game/directoryLayout';
import type { GameStore } from '../game/gameStore';
import { formatNumber, m } from '../i18n/i18n';
import { AttributeIcons } from './AttributeIcons';

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

  return { ref, scrollTop, height, onScroll: () => setScrollTop(ref.current?.scrollTop ?? 0) };
}

/** The virtualized directory: numbered sections of 100 suitors, tap a row to cross out / restore. */
export function Directory({ store }: { store: GameStore }) {
  const { suitorCount } = store.game;
  const viewport = useViewport();
  const perSection = useComputed(() => remainingPerSection(suitorCount, store.state.value.crossedOut));
  const state = store.state.value;

  const total = itemCount(suitorCount);
  const first = Math.max(0, Math.floor(viewport.scrollTop / ITEM_HEIGHT) - OVERSCAN);
  const last = Math.min(total - 1, Math.ceil((viewport.scrollTop + viewport.height) / ITEM_HEIGHT) + OVERSCAN);

  const jumpTo = (section: number) => {
    viewport.ref.current?.scrollTo({ top: sectionHeaderIndex(section) * ITEM_HEIGHT });
  };

  const items = [];
  for (let index = first; index <= last; index++) {
    const item = itemAt(index);
    const style = { top: `${index * ITEM_HEIGHT}px`, height: `${ITEM_HEIGHT}px` };
    if (item.kind === 'header') {
      items.push(<SectionHeader key={`s${item.section}`} section={item.section} left={perSection.value[item.section]} suitorCount={suitorCount} style={style} />);
    } else if (item.id < suitorCount) {
      const suitor = store.game.suitors[item.id];
      const crossedOut = state.crossedOut.has(item.id);
      items.push(
        <button
          key={item.id}
          type="button"
          class={crossedOut ? 'suitor-row crossed-out' : 'suitor-row'}
          style={style}
          aria-pressed={crossedOut}
          disabled={state.finalistsFound}
          onClick={() => store.toggle(item.id)}
          data-suitor-id={item.id}
        >
          <span class="suitor-number">{item.id + 1}</span>
          <span class="suitor-main">
            <span class="suitor-name">Mr {suitorName(suitor)}</span>
            <AttributeIcons suitor={suitor} />
          </span>
        </button>,
      );
    }
  }

  return (
    <div class="directory">
      {sectionCount(suitorCount) > 1 && (
        <select class="section-jump" aria-label={m.game_jump()} onChange={(e) => jumpTo(Number(e.currentTarget.value))}>
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
