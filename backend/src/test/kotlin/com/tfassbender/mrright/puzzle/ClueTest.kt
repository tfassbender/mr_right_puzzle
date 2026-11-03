package com.tfassbender.mrright.puzzle

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClueTest {

    @Test
    fun `income at least includes the level itself`() {
        val clue = IncomeAtLeast(Income.AT_LEAST_4K)

        assertTrue(clue.matches(suitor(income = Income.AT_LEAST_4K)))
        assertTrue(clue.matches(suitor(income = Income.AT_LEAST_10K)))
        assertFalse(clue.matches(suitor(income = Income.AT_LEAST_2K)))
    }

    @Test
    fun `attribute is not excludes exactly one value`() {
        val clue = AttributeIsNot(BookRuleSet.HAIR_COLOR, "BLOND")

        assertFalse(clue.matches(suitor(hairColor = "BLOND")))
        assertTrue(clue.matches(suitor(hairColor = "AUBURN")))
    }

    @Test
    fun `attribute is one of allows the listed values only`() {
        val clue = AttributeIsOneOf(BookRuleSet.EYE_COLOR, setOf("GREEN", "BLUE"))

        assertTrue(clue.matches(suitor(eyeColor = "GREEN")))
        assertTrue(clue.matches(suitor(eyeColor = "BLUE")))
        assertFalse(clue.matches(suitor(eyeColor = "AMBER")))
    }

    @Test
    fun `one of clues need 2 or 3 values`() {
        assertThrows<IllegalArgumentException> { AttributeIsOneOf(BookRuleSet.INTEREST, setOf("SPORTY")) }
        assertThrows<IllegalArgumentException> {
            AttributeIsOneOf(BookRuleSet.TEMPERAMENT, setOf("BROODING", "CHARMING", "RESERVED", "AMIABLE"))
        }
    }

    @Test
    fun `initials differ`() {
        assertTrue(InitialsDiffer.matches(suitor(firstName = "Edward", lastName = "Ashby")))
        assertFalse(InitialsDiffer.matches(suitor(firstName = "Edward", lastName = "Elliot")))
    }

    @Test
    fun `both initials must be in the letter range`() {
        val clue = InitialsInRange(LetterRange.A_TO_M)

        assertTrue(clue.matches(suitor(firstName = "Miles", lastName = "Abbott")))
        assertFalse(clue.matches(suitor(firstName = "Miles", lastName = "Nash")))
        assertFalse(clue.matches(suitor(firstName = "Noel", lastName = "Abbott")))
        assertTrue(InitialsInRange(LetterRange.N_TO_Z).matches(suitor(firstName = "Noel", lastName = "York")))
    }

    @Test
    fun `surname must not start and end with the same letter`() {
        assertFalse(SurnameStartAndEndDiffer.matches(suitor(lastName = "Kirk")))
        assertFalse(SurnameStartAndEndDiffer.matches(suitor(lastName = "Dodd")))
        assertTrue(SurnameStartAndEndDiffer.matches(suitor(lastName = "Ashby")))
    }

    @Test
    fun `vowel counts are exact`() {
        assertTrue(FirstNameVowelCount(3).matches(suitor(firstName = "Gabriel")))
        assertFalse(FirstNameVowelCount(2).matches(suitor(firstName = "Gabriel")))
        assertTrue(SurnameVowelCount(2).matches(suitor(lastName = "Dalton")))
        assertFalse(SurnameVowelCount(3).matches(suitor(lastName = "Dalton")))
    }

    @Test
    fun `excluded letter is checked in the full name, case-insensitive`() {
        val clue = NameExcludesLetter('H')

        assertFalse(clue.matches(suitor(firstName = "Hugh", lastName = "Dalton")))
        assertFalse(clue.matches(suitor(firstName = "Edward", lastName = "Ashby")))
        assertTrue(clue.matches(suitor(firstName = "Edward", lastName = "Dalton")))
        assertThrows<IllegalArgumentException> { NameExcludesLetter('h') }
    }
}
