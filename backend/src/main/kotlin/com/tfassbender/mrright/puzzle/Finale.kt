package com.tfassbender.mrright.puzzle

/**
 * The end of a rule set after Mr Right is found: one more [suitor] arrives (not part of the directory), who
 * meets every criterion too and wins the last tie-break – "who treats her better", rated from 1 to 10.
 * Mr Right's rating comes from [mrRightRatings], fixed per game by its seed.
 */
data class Finale(val suitor: Suitor, val suitorRating: Int, val mrRightRatings: IntRange) {
    init {
        require(suitorRating in RATING_RANGE && mrRightRatings.first in RATING_RANGE && mrRightRatings.last in RATING_RANGE) {
            "Ratings must be in $RATING_RANGE"
        }
        require(mrRightRatings.last < suitorRating) { "The finale suitor must win the tie-break" }
    }

    /** A separate random stream, so the puzzle itself stays the same. */
    fun mrRightRating(seed: String): Int = Seeds.toRandom("finale:$seed").nextInt(mrRightRatings.first, mrRightRatings.last + 1)

    companion object {
        val RATING_RANGE: IntRange = 1..10

        /** Not in the directory, so no directory position. */
        const val SUITOR_ID: Int = -1
    }
}
