package com.tfassbender.mrright.puzzle

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RuleSetTest {

    private val colour = AttributeDefinition("COLOUR", listOf("RED", "GREEN", "BLUE"))
    private val size = AttributeDefinition("SIZE", listOf("SMALL", "LARGE"))

    @Test
    fun `the book is always available, other rule sets only when unlocked`() {
        assertEquals(listOf(RuleSets.BOOK), RuleSets.availableFor(emptySet()).map { it.id })
        assertEquals(listOf(RuleSets.BOOK), RuleSets.availableFor(setOf("UNKNOWN")).map { it.id })
        assertEquals(BookRuleSet, RuleSets.byId(RuleSets.BOOK))
        assertEquals(listOf(RuleSets.BOOK, "EVA"), RuleSets.availableFor(setOf("EVA")).map { it.id })
    }

    @Test
    fun `weighted values follow their weights`() {
        val weighted = AttributeDefinition("COIN", listOf("HEADS", "TAILS"), listOf(0.9, 0.1))
        val random = Seeds.toRandom("weights")
        val heads = (1..10_000).count { weighted.randomValue(random) == "HEADS" }

        assertTrue(heads in 8_700..9_300, "heads: $heads")
        assertThrows<IllegalArgumentException> { AttributeDefinition("COIN", listOf("HEADS", "TAILS"), listOf(1.0)) }
    }

    @Test
    fun `attributes without a clue allow every value`() {
        val clue = AttributeIsNot("COLOUR", "RED")
        val clueSet = ClueSet(listOf(colour, size), listOf(clue), emptyList(), listOf(clue))

        assertEquals(listOf("GREEN", "BLUE"), clueSet.allowedValues("COLOUR"))
        assertEquals(listOf("RED"), clueSet.disallowedValues("COLOUR"))
        assertEquals(listOf("SMALL", "LARGE"), clueSet.allowedValues("SIZE"))
    }

    @Test
    fun `clue sets reject clues that don't fit the attributes`() {
        assertInvalid(AttributeIsNot("SHAPE", "ROUND"))
        assertInvalid(AttributeIsNot("COLOUR", "PURPLE"))
        assertInvalid(AttributeIsOneOf("COLOUR", setOf("RED", "PURPLE")))
        // A clue that allows every value can't have near misses.
        assertInvalid(AttributeIsOneOf("SIZE", setOf("SMALL", "LARGE")))
    }

    @Test
    fun `clue sets reject a second clue on the same attribute or a mismatching order`() {
        val first = AttributeIsNot("COLOUR", "RED")
        val second = AttributeIsNot("COLOUR", "BLUE")

        assertThrows<IllegalArgumentException> {
            ClueSet(listOf(colour), listOf(first, second), emptyList(), listOf(first, second))
        }
        assertThrows<IllegalArgumentException> { ClueSet(listOf(colour), listOf(first), emptyList(), emptyList()) }
        assertThrows<IllegalArgumentException> {
            ClueSet(listOf(colour), listOf(first), listOf(InitialsDiffer), listOf(first))
        }
    }

    private fun assertInvalid(clue: AttributeClue) {
        assertThrows<IllegalArgumentException> { ClueSet(listOf(colour, size), listOf(clue), emptyList(), listOf(clue)) }
    }
}
