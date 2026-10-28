import { computed, signal, type Signal } from '@preact/signals';
import type { GameDto } from '../api/gamesApi';
import { finalistsHash, solutionHash } from './hashing';
import { remainingIds, toggleSuitor, withWrongGuess, type PlayState } from './playState';

/** Feedback shown after a check: wrong finalists, a finalist crossed out by mistake, or a wrong pick. */
export type Hint = 'WRONG_FINALISTS' | 'TOO_FEW_LEFT' | 'WRONG_PICK' | null;

/**
 * The game being played: the puzzle plus the player's state as signals. Every change made by the player is
 * reported to [onChange] (saving); [replaceState] takes a state from elsewhere (server, other device) without it.
 */
export class GameStore {
  readonly state: Signal<PlayState>;
  readonly hint = signal<Hint>(null);
  private readonly undoStack = signal<number[]>([]);

  readonly remaining = computed(() => remainingIds(this.state.value, this.game.suitorCount));
  readonly canUndo = computed(() => this.undoStack.value.length > 0 && !this.state.value.finalistsFound);

  constructor(
    readonly game: GameDto,
    initialState: PlayState,
    private readonly onChange: (state: PlayState) => void,
    private readonly now: () => string = () => new Date().toISOString(),
  ) {
    this.state = signal(initialState);
  }

  /** Crosses out or restores a suitor and runs the finalists check when exactly the finalist count is left. */
  async toggle(id: number): Promise<void> {
    const before = this.state.value;
    if (before.finalistsFound) return;
    this.update(toggleSuitor(before, id, this.now()));
    this.undoStack.value = [...this.undoStack.value, id];
    await this.checkRemaining();
  }

  async undo(): Promise<void> {
    const stack = this.undoStack.value;
    if (!this.canUndo.value) return;
    const id = stack[stack.length - 1];
    this.undoStack.value = stack.slice(0, -1);
    this.update(toggleSuitor(this.state.value, id, this.now()));
    await this.checkRemaining();
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

  private update(state: PlayState): void {
    this.state.value = state;
    this.onChange(state);
  }

  /** Whenever exactly the finalist count is left, that's a guess at the finalists. */
  private async checkRemaining(): Promise<void> {
    const remaining = this.remaining.value;
    const finalistCount = this.game.finalistCount;
    if (remaining.length > finalistCount) {
      this.hint.value = null;
    } else if (remaining.length < finalistCount) {
      this.hint.value = 'TOO_FEW_LEFT';
    } else {
      await this.checkFinalists(remaining);
    }
  }

  private async checkFinalists(remaining: number[]): Promise<void> {
    const crossedOutBefore = this.state.value.crossedOut;
    const matches = (await finalistsHash(this.game.id, remaining)) === this.game.finalistsHash;
    // The player may have tapped again while hashing – then this result is outdated.
    if (this.state.value.crossedOut !== crossedOutBefore) return;
    if (matches) {
      this.hint.value = null;
      this.update({ ...this.state.value, finalistsFound: true, updatedAt: this.now() });
    } else {
      this.hint.value = 'WRONG_FINALISTS';
      this.update(withWrongGuess(this.state.value, this.now()));
    }
  }
}
