package com.tfassbender.mrright.puzzle

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PuzzleGeneratorTest {

    @Test
    fun `same seed and config give the same puzzle`() {
        val config = PuzzleConfig(500, 2)

        assertEquals(PuzzleGenerator.generate("emma", config), PuzzleGenerator.generate("emma", config))
    }

    @Test
    fun `different seeds give different puzzles`() {
        val config = PuzzleConfig(500, 2)

        assertNotEquals(PuzzleGenerator.generate("emma", config).suitors, PuzzleGenerator.generate("emmy", config).suitors)
    }

    /** The core property test: the verifier checks all guarantees, so it must pass for any seed and config. */
    @ParameterizedTest
    @CsvSource("100, 1", "100, 5", "250, 2", "1000, 3", "3000, 4")
    fun `every seed gives a valid puzzle`(suitorCount: Int, finalistCount: Int) {
        val config = PuzzleConfig(suitorCount, finalistCount)
        repeat(100) { index ->
            PuzzleVerifier.verify(PuzzleGenerator.generate("seed-$index", config))
        }
    }

    @Test
    fun `exactly the finalists match all clues and Mr Right has the fewest consonants`() {
        val puzzle = PuzzleGenerator.generate("emma", PuzzleConfig(1000, 3))

        val matching = puzzle.suitors.filter { suitor -> puzzle.clues.all { it.matches(suitor) } }
        assertEquals(puzzle.finalistIds, matching.map { it.id }.toSet())
        val solution = puzzle.suitors[puzzle.solutionId]
        assertTrue(solution.id in puzzle.finalistIds)
        matching.filter { it != solution }.forEach {
            assertTrue(Names.fullNameConsonantCount(it) > Names.fullNameConsonantCount(solution))
        }
    }

    @Test
    fun `clues follow the book's order of subjects`() {
        val clues = PuzzleGenerator.generate("emma", PuzzleConfig(100, 2)).clues

        assertEquals(12, clues.size)
        assertEquals(Attribute.HAIR_COLOR, attributeOf(clues[0]))
        assertTrue(clues[1] is IncomeAtLeast)
        assertEquals(InitialsDiffer, clues[2])
        assertEquals(Attribute.TEMPERAMENT, attributeOf(clues[3]))
        assertTrue(clues[4] is InitialsInRange)
        assertEquals(Attribute.INTEREST, attributeOf(clues[5]))
        assertEquals(Attribute.ELEMENT, attributeOf(clues[6]))
        assertEquals(SurnameStartAndEndDiffer, clues[7])
        assertTrue(clues[8] is FirstNameVowelCount)
        assertEquals(Attribute.EYE_COLOR, attributeOf(clues[9]))
        assertTrue(clues[10] is NameExcludesLetter)
        assertTrue(clues[11] is SurnameVowelCount)
    }

    @Test
    fun `clue parameters stay within the decided limits`() {
        repeat(200) { index ->
            val clues = PuzzleGenerator.generate("p-$index", PuzzleConfig(100, 1)).clues

            assertEquals(1, clues.count { it is AttributeIsNot })
            assertEquals(4, clues.count { it is AttributeIsOneOf })
            clues.filterIsInstance<AttributeIsOneOf>().forEach { assertTrue(it.values.size in 2..3) }
            assertTrue(clues.filterIsInstance<IncomeAtLeast>().single().level in setOf(Income.AT_LEAST_2K, Income.AT_LEAST_4K))
            assertTrue(clues.filterIsInstance<NameExcludesLetter>().single().letter in "BHKPW")
            assertTrue(clues.filterIsInstance<FirstNameVowelCount>().single().count in 2..3)
            assertTrue(clues.filterIsInstance<SurnameVowelCount>().single().count in 2..3)
        }
    }

    @Test
    fun `10,000 suitors are generated quickly`() {
        PuzzleGenerator.generate("warm-up", PuzzleConfig(10_000, 5))

        val start = System.nanoTime()
        val puzzle = PuzzleGenerator.generate("big", PuzzleConfig(10_000, 5))
        val millis = (System.nanoTime() - start) / 1_000_000

        assertEquals(10_000, puzzle.suitors.size)
        // Generous bound for slow CI machines; locally it takes well under a second.
        assertTrue(millis < 3000, "generation took $millis ms")
    }

    private fun attributeOf(clue: Clue): Attribute = when (clue) {
        is AttributeIsNot -> clue.attribute
        is AttributeIsOneOf -> clue.attribute
        else -> error("$clue is not an attribute clue")
    }
}
