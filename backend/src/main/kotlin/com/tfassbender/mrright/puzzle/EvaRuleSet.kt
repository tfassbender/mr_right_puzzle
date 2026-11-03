package com.tfassbender.mrright.puzzle

import kotlin.random.Random

/**
 * Eva's rules: her own 12 criteria as fixed clues on her own attributes, no name clues
 * (see doc/personal-rule-set.md). Only the directory changes with the seed.
 *
 * The values are weighted: about [RIGHT_SHARE] of the random suitors have a right value per attribute, so a
 * random suitor fails only a few criteria and the player has to look at every icon. With evenly spread values,
 * the first single-value clue alone would remove three quarters of the directory.
 */
object EvaRuleSet : RuleSet {
    const val HEIGHT: String = "HEIGHT" // cm
    const val BIRTH_YEAR: String = "BIRTH_YEAR" // shown as the age in the frontend
    const val JOB: String = "JOB"
    const val VEHICLE: String = "VEHICLE"
    const val CHARACTER: String = "CHARACTER"
    const val MANNER: String = "MANNER"
    const val SOCIAL: String = "SOCIAL"
    const val FOOD: String = "FOOD"
    const val LEISURE: String = "LEISURE"
    const val FITNESS: String = "FITNESS"
    const val KIDS: String = "KIDS"
    const val VICES: String = "VICES"

    private const val RIGHT_SHARE = 0.8

    override val id: String = "EVA"
    override val generatorVersion: Int = 1

    /** As many consonants as "Tobias Faßbender", so he never loses the tie-break against Mr Right in the finale. */
    override val minSolutionConsonants: Int = 9

    // Eva was born in 1994 and wants a man between 3 years younger and 10 years older: born 1984–1997.
    private val clues: List<AttributeClue> = listOf(
        AttributeBetween(HEIGHT, 188, 195),
        AttributeBetween(BIRTH_YEAR, 1984, 1997),
        AttributeIs(MANNER, "OPEN_AND_FUNNY"),
        AttributeIs(LEISURE, "SPONTANEOUS_TRIPS"),
        AttributeIs(VEHICLE, "CAR"),
        AttributeIsNot(JOB, "NONE"),
        AttributeIs(SOCIAL, "FRIENDS_AND_FAMILY"),
        AttributeIs(FOOD, "HEALTHY_FOODIE"),
        AttributeIs(CHARACTER, "LOYAL"),
        AttributeIsOneOf(FITNESS, setOf("ATHLETIC", "FIT")),
        AttributeIs(KIDS, "NO_KIDS"),
        AttributeIsOneOf(VICES, setOf("NONE", "ALCOHOL_LITTLE")),
    )

    private val valuesByAttribute: Map<String, List<String>> = mapOf(
        HEIGHT to (165..205).map(Int::toString),
        BIRTH_YEAR to (1975..2003).map(Int::toString),
        JOB to listOf("NONE", "EURO_1", "EURO_2", "EURO_3"),
        VEHICLE to listOf("CAR", "MOTORBIKE", "BIKE", "BUS"),
        CHARACTER to listOf("LOYAL", "FLIRTY", "MOODY", "ALOOF"),
        MANNER to listOf("OPEN_AND_FUNNY", "SHY", "ARROGANT", "BORING"),
        SOCIAL to listOf("FRIENDS_AND_FAMILY", "ONLY_FRIENDS", "ONLY_FAMILY", "LONER"),
        FOOD to listOf("HEALTHY_FOODIE", "FAST_FOOD", "DIET_FANATIC", "CANT_BE_BOTHERED"),
        LEISURE to listOf("SPONTANEOUS_TRIPS", "LUXURY_HOTELS", "LONG_HAUL", "HOMEBODY"),
        FITNESS to listOf("ATHLETIC", "FIT", "AVERAGE", "COUCH_POTATO"),
        KIDS to listOf("NO_KIDS", "WANTS_KIDS", "UNDECIDED"),
        VICES to listOf("NONE", "ALCOHOL_LITTLE", "ALCOHOL_LOT", "CIGARETTES", "DRUGS"),
    )

    override val attributes: List<AttributeDefinition> = clues.map { clue ->
        val values = valuesByAttribute.getValue(clue.attribute)
        AttributeDefinition(clue.attribute, values, weights(values, clue))
    }

    private val clueSet: ClueSet = ClueSet(attributes, clues, nameClues = emptyList(), ordered = clues)

    override fun clues(random: Random, finalistCount: Int): ClueSet = clueSet

    /** The right values share [RIGHT_SHARE] of the weight, the wrong ones the rest – evenly within each group. */
    private fun weights(values: List<String>, clue: AttributeClue): List<Double> {
        val rightCount = values.count(clue::allows)
        return values.map { if (clue.allows(it)) RIGHT_SHARE / rightCount else (1 - RIGHT_SHARE) / (values.size - rightCount) }
    }
}
