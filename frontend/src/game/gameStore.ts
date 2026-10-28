import { computed, signal, type Signal } from '@preact/signals';
import type { GameDto } from '../api/gamesApi';
import { finalistsHash, solutionHash } from './hashing';
import {
  firstUnmarkedId,
  markerOf,
  remainingIds,
  withFinalists,
  withMarker,
  withWrongGuess,
  type Marker,
  type PlayState,
} from './playState';

/** Feedback shown after a check: wrong finalists, a finalist crossed out by mistake, or a wrong pick. */
export type Hint = 'WRONG_FINALISTS' | 'TOO_FEW_LEFT' | 'WRONG_PICK' | null;

/** One step to undo: the suitor and the marker it had before. */
interface UndoStep {
  id: number;
  before: Marker;
}

/**
 * The game being played: the puzzle plus the player's state as signals. Every change made by the player is
 * reported to [onChange] (saving); [replaceState] takes a state from elsewhere (server, other device) without it.
 */
export class GameStore {
  readonly state: Signal<PlayState>;
  readonly hint = signal<Hint>(null);
  private readonly undoStack = signal<UndoStep[]>([]);

  readonly remaining = computed(() => remainingIds(this.state.value, this.game.suitorCount));
  readonly firstUnmarked = computed(() => firstUnmarkedId(this.state.value, this.game.suitorCount));
  readonly canUndo = computed(() => this.undoStack.value.length > 0 && !this.state.value.finalistsFound);

  /**
   * The suitors a submit would check as the finalists: the starred ones once there are exactly as many as
   * finalists, or everybody left once only that many are left. Null when neither is the case.
   */
  readonly candidates = computed<number[] | null>(() => {
    const state = this.state.value;
    const finalistCount = this.game.finalistCount;
    if (state.finalistsFound) return null;
    if (state.possibleMatches.size === finalistCount) return [...state.possibleMatches].sort((a, b) => a - b);
    return this.remaining.value.length === finalistCount ? this.remaining.value : null;
  });

  constructor(
    readonly game: GameDto,
    initialState: PlayState,
    private readonly onChange: (state: PlayState) => void,
    private readonly now: () => string = () => new Date().toISOString(),
  ) {
    this.state = signal(initialState);
  }

  /** Tapping a suitor: crosses him out, or restores him if he already is. */
  toggleNoMatch(id: number): void {
    this.mark(id, markerOf(this.state.value, id) === 'noMatch' ? 'none' : 'noMatch');
  }

  /** The star: marks a suitor as possible match, or removes the mark again. */
  togglePossibleMatch(id: number): void {
    this.mark(id, markerOf(this.state.value, id) === 'possibleMatch' ? 'none' : 'possibleMatch');
  }

  undo(): void {
    const stack = this.undoStack.value;
    if (!this.canUndo.value) return;
    const step = stack[stack.length - 1];
    this.undoStack.value = stack.slice(0, -1);
    this.update(withMarker(this.state.value, step.id, step.before, this.now()));
    this.updateHint();
  }

  /** Submits the [candidates] as the finalists. A wrong set counts as a wrong guess. */
  async submit(): Promise<boolean> {
    const candidates = this.candidates.value;
    if (!candidates) return false;
    const stateBefore = this.state.value;
    const matches = (await finalistsHash(this.game.id, candidates)) === this.game.finalistsHash;
    // The player may have changed something while hashing – then this result is outdated.
    if (this.state.value !== stateBefore) return false;
    if (matches) {
      this.hint.value = null;
      this.update(withFinalists(stateBefore, candidates, this.game.suitorCount, this.now()));
    } else {
      this.hint.value = 'WRONG_FINALISTS';
      this.update(withWrongGuess(stateBefore, this.now()));
    }
    return matches;
  }

  /** Picks Mr Right among the finalists. Returns whether it was him; a wrong pick counts as a wrong guess. */
  async pick(id: number): Promise<boolean> {
    const state = this.state.value;
    if (!state.finalistsFound || state.mrRightId !== null) return false;
    const correct = (await solutionHash(this.game.id, id)) === this.game.solutionHash;
    if (correct) {
      this.hint.value = null;
      this.update({ ...this.state.value, mrRightId: id, updatedAt: this.now() });
    } else {
      this.hint.value = 'WRONG_PICK';
      this.update(withWrongGuess(this.state.value, this.now()));
    }
    return correct;
  }

  replaceState(state: PlayState): void {
    this.state.value = state;
    this.undoStack.value = [];
    this.hint.value = null;
  }

  private mark(id: number, marker: Marker): void {
    const before = this.state.value;
    if (before.finalistsFound) return;
    this.update(withMarker(before, id, marker, this.now()));
    this.undoStack.value = [...this.undoStack.value, { id, before: markerOf(before, id) }];
    this.updateHint();
  }

  private update(state: PlayState): void {
    this.state.value = state;
    this.onChange(state);
  }

  /** Fewer left than finalists means a finalist was crossed out by mistake – worth saying right away. */
  private updateHint(): void {
    this.hint.value = this.remaining.value.length < this.game.finalistCount ? 'TOO_FEW_LEFT' : null;
  }
}
