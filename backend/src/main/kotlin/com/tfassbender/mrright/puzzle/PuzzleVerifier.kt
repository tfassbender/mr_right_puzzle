package com.tfassbender.mrright.puzzle

/**
 * The guarantees every generated puzzle must keep. The generator builds puzzles so that they hold;
 * a failed check is a bug, not a retry case.
 */
object PuzzleVerifier {

    fun verify(puzzle: Puzzle) {
        val failedClues: List<List<Int>> = puzzle.suitors.map { suitor ->
            puzzle.clues.indices.filterNot { puzzle.clues[it].matches(suitor) }
        }
        val matchingAll = puzzle.suitors.indices.filter { failedClues[it].isEmpty() }.map { puzzle.suitors[it].id }

        check(puzzle.suitors.size == puzzle.config.suitorCount) { "Expected ${puzzle.config.suitorCount} suitors" }
        check(puzzle.suitors.mapIndexed { index, suitor -> suitor.id == index }.all { it }) { "Ids must be positions" }
        check(puzzle.suitors.map { it.copy(id = 0) }.toSet().size == puzzle.suitors.size) { "Duplicate suitors" }
        check(matchingAll.size == puzzle.config.finalistCount) { "${matchingAll.size} suitors match all clues" }
        check(matchingAll.toSet() == puzzle.finalistIds) { "Finalist ids don't match the clues" }
        verifyTieBreak(puzzle)
        verifyEveryClueNeeded(puzzle, failedClues)
    }

    private fun verifyTieBreak(puzzle: Puzzle) {
        val consonants = puzzle.finalistIds.associateWith { Names.fullNameConsonantCount(puzzle.suitors[it]) }
        val fewest = consonants.values.min()
        check(consonants.values.count { it == fewest } == 1) { "Tie-break is not unique: $consonants" }
        check(consonants.getValue(puzzle.solutionId) == fewest) { "Solution is not the finalist with fewest consonants" }
    }

    /** Dropping any clue must leave more candidates than finalists, i.e. some suitor fails only that clue. */
    private fun verifyEveryClueNeeded(puzzle: Puzzle, failedClues: List<List<Int>>) {
        val onlyFailedClues = failedClues.filter { it.size == 1 }.map { it.single() }.toSet()
        puzzle.clues.indices.forEach { index ->
            check(index in onlyFailedClues) { "Clue ${puzzle.clues[index]} is not needed" }
        }
    }
}
