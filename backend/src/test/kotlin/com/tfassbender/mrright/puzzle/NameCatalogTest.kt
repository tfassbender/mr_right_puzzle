package com.tfassbender.mrright.puzzle

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NameCatalogTest {

    private val lists = mapOf("first names" to NameCatalog.firstNames, "surnames" to NameCatalog.surnames)

    @Test
    fun `both lists have 120 unique names`() {
        lists.forEach { (label, names) ->
            assertEquals(120, names.size, label)
            assertEquals(names.size, names.toSet().size, "$label contain duplicates")
        }
    }

    @Test
    fun `names are capitalized and contain only the letters A to Z`() {
        val pattern = Regex("[A-Z][a-z]+")
        lists.values.flatten().forEach { assertTrue(pattern.matches(it), "invalid name: $it") }
    }

    // The generator picks clue parameters from these options, so each must leave enough names on both sides.
    @Test
    fun `both letter ranges are well represented`() {
        lists.forEach { (label, names) ->
            val shareNtoZ = names.count { Names.initial(it) in LetterRange.N_TO_Z }.toDouble() / names.size
            assertTrue(shareNtoZ in 0.35..0.65, "$label: N–Z share $shareNtoZ")
        }
    }

    @Test
    fun `vowel counts 2 and 3 are common`() {
        lists.forEach { (label, names) ->
            listOf(2, 3).forEach { count ->
                assertTrue(names.count { Names.vowelCount(it) == count } >= 30, "$label with $count vowels")
            }
        }
    }

    @Test
    fun `some surnames start and end with the same letter`() {
        assertTrue(NameCatalog.surnames.count { !SurnameStartAndEndDiffer.matches(suitor(lastName = it)) } >= 5)
    }
}
