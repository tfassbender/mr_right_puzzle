package com.tfassbender.mrright.puzzle

/** Size and difficulty of a puzzle. The clue count is always 12, so it is not configurable. */
data class PuzzleConfig(val suitorCount: Int, val finalistCount: Int) {
    init {
        require(suitorCount in SUITOR_COUNT_RANGE) { "suitorCount must be in $SUITOR_COUNT_RANGE, got $suitorCount" }
        require(finalistCount in FINALIST_COUNT_RANGE) {
            "finalistCount must be in $FINALIST_COUNT_RANGE, got $finalistCount"
        }
    }

    companion object {
        val SUITOR_COUNT_RANGE: IntRange = 100..10_000
        val FINALIST_COUNT_RANGE: IntRange = 1..5

        fun isValid(suitorCount: Int, finalistCount: Int): Boolean =
            suitorCount in SUITOR_COUNT_RANGE && finalistCount in FINALIST_COUNT_RANGE
    }
}

/**
 * A generated directory. [finalistIds] are the suitors matching all clues, [solutionId] is Mr Right:
 * the finalist with the fewest consonants in his full name (unique by construction).
 */
data class Puzzle(
    val config: PuzzleConfig,
    val suitors: List<Suitor>,
    val clues: List<Clue>,
    val finalistIds: Set<Int>,
    val solutionId: Int,
)
