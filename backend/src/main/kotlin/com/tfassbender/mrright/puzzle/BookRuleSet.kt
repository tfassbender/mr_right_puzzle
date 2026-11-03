package com.tfassbender.mrright.puzzle

import kotlin.random.Random

/**
 * The book's puzzle: six attributes and 12 clues with random parameters, in the book's order of subjects
 * (see doc/puzzle-analysis.md). Every order below drives the random draws – changing one changes all book
 * puzzles, which [BookGoldenTest] catches.
 */
object BookRuleSet : RuleSet {
    const val INCOME: String = "INCOME"
    const val ELEMENT: String = "ELEMENT"
    const val EYE_COLOR: String = "EYE_COLOR"
    const val HAIR_COLOR: String = "HAIR_COLOR"
    const val TEMPERAMENT: String = "TEMPERAMENT"
    const val INTEREST: String = "INTEREST"

    private const val MAX_NAME_CLUE_ROLLS = 100
    private val INCOME_LEVELS = listOf(Income.AT_LEAST_2K, Income.AT_LEAST_4K)

    override val id: String = RuleSets.BOOK
    override val generatorVersion: Int = 1

    /** The attributes with "is not" / "one of" clues; income has its own clue. */
    val nonOrdinalAttributes: List<AttributeDefinition> = listOf(
        AttributeDefinition(ELEMENT, listOf("AIR", "FIRE", "WATER", "EARTH")),
        AttributeDefinition(EYE_COLOR, listOf("BROWN", "BROWN_GREEN", "AMBER", "GREEN", "BLUE")),
        AttributeDefinition(HAIR_COLOR, listOf("BLOND", "BROWN", "BLACK", "AUBURN")),
        AttributeDefinition(TEMPERAMENT, listOf("BROODING", "CHARMING", "RESERVED", "AMIABLE", "DASHING")),
        AttributeDefinition(INTEREST, listOf("INTELLECTUAL", "MUSICAL", "SPORTY", "SOCIABLE")),
    )

    override val attributes: List<AttributeDefinition> =
        nonOrdinalAttributes + AttributeDefinition(INCOME, Income.entries.map { it.name })

    override fun clues(random: Random, finalistCount: Int): ClueSet {
        val income = IncomeAtLeast(INCOME_LEVELS.random(random))
        val attributeClues = randomAttributeClues(random)
        repeat(MAX_NAME_CLUE_ROLLS) {
            val clueSet = clueSet(income, attributeClues, NameClues.random(random))
            if (clueSet.nameGroups.supports(finalistCount)) return clueSet
        }
        error("No usable name clues after $MAX_NAME_CLUE_ROLLS rolls – the name catalog doesn't fit the clue types")
    }

    /** Like the book: one random attribute gets an "is not" clue, the other four a "one of" clue. */
    private fun randomAttributeClues(random: Random): Map<String, AttributeClue> {
        val isNotAttribute = nonOrdinalAttributes.random(random)
        return nonOrdinalAttributes.associate { attribute ->
            val clue = if (attribute == isNotAttribute) {
                AttributeIsNot(attribute.id, attribute.values.random(random))
            } else {
                oneOf(attribute, random)
            }
            attribute.id to clue
        }
    }

    private fun oneOf(attribute: AttributeDefinition, random: Random): AttributeIsOneOf {
        val chosen = attribute.values.shuffled(random).take(random.nextInt(2, 4)).toSet()
        // Keep the legend's order, so the clue text lists the values like the legend does.
        return AttributeIsOneOf(attribute.id, attribute.values.filter { it in chosen }.toSet())
    }

    private fun clueSet(income: IncomeAtLeast, attributeClues: Map<String, AttributeClue>, names: NameClues): ClueSet {
        val clue = { attribute: String -> attributeClues.getValue(attribute) }
        return ClueSet(
            attributes = attributes,
            attributeClues = listOf(income) + nonOrdinalAttributes.map { clue(it.id) },
            nameClues = names.all,
            ordered = listOf(
                clue(HAIR_COLOR), income, names.initialsDiffer, clue(TEMPERAMENT), names.initialsInRange,
                clue(INTEREST), clue(ELEMENT), names.surnameStartAndEndDiffer, names.firstNameVowelCount,
                clue(EYE_COLOR), names.excludedLetter, names.surnameVowelCount,
            ),
        )
    }
}

/** The six name clues, one per name clue type, with random parameters. */
internal data class NameClues(
    val initialsDiffer: InitialsDiffer,
    val initialsInRange: InitialsInRange,
    val surnameStartAndEndDiffer: SurnameStartAndEndDiffer,
    val firstNameVowelCount: FirstNameVowelCount,
    val excludedLetter: NameExcludesLetter,
    val surnameVowelCount: SurnameVowelCount,
) {
    val all: List<Clue> = listOf(
        initialsDiffer, initialsInRange, surnameStartAndEndDiffer, firstNameVowelCount, excludedLetter, surnameVowelCount,
    )

    companion object {
        private val VOWEL_COUNTS = listOf(2, 3)
        private val EXCLUDABLE_LETTERS = listOf('B', 'H', 'K', 'P', 'W')

        fun random(random: Random): NameClues = NameClues(
            InitialsDiffer,
            InitialsInRange(LetterRange.entries.random(random)),
            SurnameStartAndEndDiffer,
            FirstNameVowelCount(VOWEL_COUNTS.random(random)),
            NameExcludesLetter(EXCLUDABLE_LETTERS.random(random)),
            SurnameVowelCount(VOWEL_COUNTS.random(random)),
        )
    }
}
