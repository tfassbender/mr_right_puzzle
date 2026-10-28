/**
 * The directory as a flat list of fixed-height items: every section of 100 suitors starts with a header item.
 * Fixed heights let the virtualized list compute positions without measuring.
 */
export const SECTION_SIZE = 100;
const ITEMS_PER_SECTION = SECTION_SIZE + 1; // header + suitors

export type DirectoryItem = { kind: 'header'; section: number } | { kind: 'suitor'; id: number };

export function sectionCount(suitorCount: number): number {
  return Math.ceil(suitorCount / SECTION_SIZE);
}

export function itemCount(suitorCount: number): number {
  return suitorCount + sectionCount(suitorCount);
}

export function itemAt(index: number): DirectoryItem {
  const section = Math.floor(index / ITEMS_PER_SECTION);
  const offset = index % ITEMS_PER_SECTION;
  return offset === 0 ? { kind: 'header', section } : { kind: 'suitor', id: section * SECTION_SIZE + offset - 1 };
}

export function sectionHeaderIndex(section: number): number {
  return section * ITEMS_PER_SECTION;
}

/** The first and last suitor id (inclusive) of a section. */
export function sectionRange(section: number, suitorCount: number): [number, number] {
  const first = section * SECTION_SIZE;
  return [first, Math.min(first + SECTION_SIZE, suitorCount) - 1];
}

/** Suitors not crossed out, per section. */
export function remainingPerSection(suitorCount: number, crossedOut: ReadonlySet<number>): number[] {
  const counts = Array.from({ length: sectionCount(suitorCount) }, (_, section) => {
    const [first, last] = sectionRange(section, suitorCount);
    return last - first + 1;
  });
  crossedOut.forEach((id) => counts[Math.floor(id / SECTION_SIZE)]--);
  return counts;
}
