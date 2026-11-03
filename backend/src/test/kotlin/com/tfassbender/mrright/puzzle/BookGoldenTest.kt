package com.tfassbender.mrright.puzzle

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.security.MessageDigest
import kotlin.test.assertEquals

/**
 * Pins the book puzzles to the output of generator version 1: existing games are regenerated from their seed,
 * so any change here would silently swap their puzzle. A failure means: restore the old output, or increase the
 * book's generator version on purpose (which marks all existing book games as outdated).
 */
class BookGoldenTest {

    private val mapper = jacksonObjectMapper()

    @ParameterizedTest
    @CsvSource(
        "emma, 100, 1, 7e2b4690e1c764ee7a08b7a9f572a8bfbe3298988820252547ea2739e1b99dc6",
        "emma, 6400, 2, 108e9fec946595f3749c0c245a91c1724adeffcb65f6905fbd680a6d2901c727",
        "k3j9x0q2ab, 250, 5, 53fb0d21042415aa14c312af86014712f86f622f5181df3ad1ee8bb632ded42c",
        "Mr Right, 10000, 3, 95c89327fd99456aa9cd8cccc7c281381227bf0e9a76b257f50bc3fd4da3c3da",
    )
    fun `book puzzles stay the same for the same seed`(seed: String, suitorCount: Int, finalistCount: Int, hash: String) {
        val puzzle = PuzzleGenerator.generate(BookRuleSet, seed, PuzzleConfig(suitorCount, finalistCount))
        assertEquals(hash, sha256(fingerprint(puzzle)))
    }

    /** Everything a player sees or is checked against, independent of how suitors are modelled in Kotlin. */
    private fun fingerprint(puzzle: Puzzle): String = buildString {
        puzzle.suitors.forEach { suitor ->
            append(suitor.id).append(' ').append(suitor.firstName).append(' ').append(suitor.lastName)
            bookValues(suitor).forEach { append(' ').append(it) }
            append('\n')
        }
        append(mapper.writeValueAsString(puzzle.clues)).append('\n')
        append(puzzle.finalistIds.sorted()).append(' ').append(puzzle.solutionId)
    }

    private fun bookValues(suitor: Suitor): List<String> = with(BookRuleSet) {
        listOf(INCOME, ELEMENT, EYE_COLOR, HAIR_COLOR, TEMPERAMENT, INTEREST).map(suitor::value)
    }

    private fun sha256(text: String): String =
        MessageDigest.getInstance("SHA-256").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }
}
