package com.tfassbender.mrright.puzzle

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvaRuleSetTest {

    private fun generate(seed: String, config: PuzzleConfig): Puzzle = PuzzleGenerator.generate(EvaRuleSet, seed, config)

    /** Her clues are fixed, so they can't be re-rolled – every config must work with them as they are. */
    @ParameterizedTest
    @CsvSource("100, 1", "100, 5", "250, 2", "1000, 3", "3000, 4")
    fun `every seed gives a valid puzzle`(suitorCount: Int, finalistCount: Int) {
        repeat(50) { index ->
            PuzzleVerifier.verify(generate("eva-$index", PuzzleConfig(suitorCount, finalistCount)))
        }
    }

    @Test
    fun `the clues are her 12 fixed criteria, without name clues`() {
        val clues = generate("one", PuzzleConfig(100, 2)).clues

        assertEquals(12, clues.size)
        assertTrue(clues.all { it is AttributeClue })
        assertEquals(clues, generate("other", PuzzleConfig(500, 3)).clues)
        assertEquals(EvaRuleSet.attributes.map { it.id }.toSet(), clues.map { (it as AttributeClue).attribute }.toSet())
    }

    @Test
    fun `Mr Right has at least 9 consonants`() {
        repeat(100) { index ->
            val puzzle = generate("consonants-$index", PuzzleConfig(100, 1 + index % 5))
            assertTrue(Names.fullNameConsonantCount(puzzle.suitors[puzzle.solutionId]) >= 9)
        }
    }

    @Test
    fun `the height and age criteria include both ends`() {
        val clues = EvaRuleSet.clues(Seeds.toRandom("any"), 1).attributeClues.associateBy { it.attribute }

        assertTrue(clues.getValue(EvaRuleSet.HEIGHT).allows("188"))
        assertTrue(clues.getValue(EvaRuleSet.HEIGHT).allows("195"))
        assertTrue(!clues.getValue(EvaRuleSet.HEIGHT).allows("196"))
        assertTrue(clues.getValue(EvaRuleSet.BIRTH_YEAR).allows("1984"))
        assertTrue(clues.getValue(EvaRuleSet.BIRTH_YEAR).allows("1997"))
        assertTrue(!clues.getValue(EvaRuleSet.BIRTH_YEAR).allows("1983"))
    }

    @Test
    fun `Tobias meets every criterion and treats her better than any Mr Right`() {
        val finale = EvaRuleSet.finale
        val clues = generate("finale", PuzzleConfig(100, 1)).clues

        assertTrue(clues.all { it.matches(finale.suitor) })
        assertEquals(EvaRuleSet.attributes.map { it.id }.toSet(), finale.suitor.attributes.keys)
        assertEquals(10, finale.suitorRating)
        repeat(200) { index ->
            assertTrue(finale.mrRightRating("seed-$index") in 5..8)
        }
        assertEquals(finale.mrRightRating("fixed"), finale.mrRightRating("fixed"))
        assertEquals(setOf(5, 6, 7, 8), (0 until 200).map { finale.mrRightRating("s-$it") }.toSet())
    }

    @Test
    fun `the book has no finale`() {
        assertEquals(null, BookRuleSet.finale)
    }

    /** The weights keep the puzzle interesting: a suitor typically fails only a few criteria, not most of them. */
    @Test
    fun `most suitors fail only a few criteria`() {
        val puzzle = generate("difficulty", PuzzleConfig(10_000, 2))
        val failed = puzzle.suitors.map { suitor -> puzzle.clues.count { !it.matches(suitor) } }
        val nonFinalists = failed.filter { it > 0 }

        val average = nonFinalists.average()
        assertTrue(average in 2.0..3.2, "average failed criteria: $average")
        assertTrue(nonFinalists.count { it == 1 } > nonFinalists.size / 5, "few suitors fail exactly one criterion")
    }

    @Test
    fun `10,000 suitors are generated quickly`() {
        generate("warm-up", PuzzleConfig(10_000, 5))

        val start = System.nanoTime()
        generate("big", PuzzleConfig(10_000, 5))
        val millis = (System.nanoTime() - start) / 1_000_000

        // Generous bound for slow CI machines.
        assertTrue(millis < 3000, "generation took $millis ms")
    }
}
