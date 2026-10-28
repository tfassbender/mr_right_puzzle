package com.tfassbender.mrright.puzzle

import kotlin.random.Random

/** The six name clues, one per name clue type, in the order they appear in the book. */
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

/**
 * The 12 clues of a puzzle, split by subject: the generator needs to know which values each attribute clue
 * allows, to build finalists and near misses from them.
 */
internal data class ClueSet(
    val income: IncomeAtLeast,
    val attributeClues: Map<Attribute, Clue>, // AttributeIsNot or AttributeIsOneOf
    val nameClues: NameClues,
) {
    /** The fixed order of the book's subjects – attribute and name clues mixed. */
    fun inBookOrder(): List<Clue> = listOf(
        attributeClues.getValue(Attribute.HAIR_COLOR),
        income,
        nameClues.initialsDiffer,
        attributeClues.getValue(Attribute.TEMPERAMENT),
        nameClues.initialsInRange,
        attributeClues.getValue(Attribute.INTEREST),
        attributeClues.getValue(Attribute.ELEMENT),
        nameClues.surnameStartAndEndDiffer,
        nameClues.firstNameVowelCount,
        attributeClues.getValue(Attribute.EYE_COLOR),
        nameClues.excludedLetter,
        nameClues.surnameVowelCount,
    )

    fun allowedIncomes(): List<Income> = Income.entries.filter { it >= income.level }

    fun disallowedIncomes(): List<Income> = Income.entries.filter { it < income.level }

    fun allowedValues(attribute: Attribute): List<String> = attribute.valueNames.filter { isAllowed(attribute, it) }

    fun disallowedValues(attribute: Attribute): List<String> = attribute.valueNames.filterNot { isAllowed(attribute, it) }

    private fun isAllowed(attribute: Attribute, value: String): Boolean = when (val clue = attributeClues.getValue(attribute)) {
        is AttributeIsNot -> value != clue.value
        is AttributeIsOneOf -> value in clue.values
        else -> error("Unexpected attribute clue $clue")
    }

    companion object {
        private val INCOME_LEVELS = listOf(Income.AT_LEAST_2K, Income.AT_LEAST_4K)

        /** Like the book: one random attribute gets an "is not" clue, the other four a "one of" clue. */
        fun randomAttributeClues(random: Random): Pair<IncomeAtLeast, Map<Attribute, Clue>> {
            val income = IncomeAtLeast(INCOME_LEVELS.random(random))
            val isNotAttribute = Attribute.entries.random(random)
            val clues = Attribute.entries.associateWith { attribute ->
                if (attribute == isNotAttribute) {
                    AttributeIsNot(attribute, attribute.valueNames.random(random))
                } else {
                    oneOf(attribute, random)
                }
            }
            return income to clues
        }

        private fun oneOf(attribute: Attribute, random: Random): AttributeIsOneOf {
            val chosen = attribute.valueNames.shuffled(random).take(random.nextInt(2, 4)).toSet()
            // Keep the legend's order, so the clue text lists the values like the legend does.
            return AttributeIsOneOf(attribute, attribute.valueNames.filter { it in chosen }.toSet())
        }
    }
}
