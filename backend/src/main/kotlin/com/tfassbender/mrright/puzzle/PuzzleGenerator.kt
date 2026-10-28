package com.tfassbender.mrright.puzzle

import kotlin.random.Random

/**
 * Builds a puzzle constructively (see doc/architecture.md, "Puzzle Generation"): clues and finalists first,
 * then near misses that make every clue count, then random suitors around them.
 * Deterministic: same seed + config + [GENERATOR_VERSION] → same puzzle.
 */
object PuzzleGenerator {

    /** Increase whenever a change produces a different puzzle for the same seed (also: name catalog changes). */
    const val GENERATOR_VERSION: Int = 1

    private const val MAX_NAME_CLUE_ROLLS = 100

    fun generate(seed: String, config: PuzzleConfig): Puzzle {
        val random = Seeds.toRandom(seed)
        val (income, attributeClues) = ClueSet.randomAttributeClues(random)
        val (nameClues, nameGroups) = rollNameClues(random, config.finalistCount)
        val clueSet = ClueSet(income, attributeClues, nameClues)

        val builder = DirectoryBuilder(random, clueSet, nameGroups)
        builder.addFinalists(config.finalistCount)
        builder.addNearMisses(nearMissesPerClue(config.suitorCount))
        builder.fillWithRandomSuitors(config.suitorCount)
        val suitors = builder.shuffledWithIds()

        val clues = clueSet.inBookOrder()
        val finalists = suitors.filter { suitor -> clues.all { it.matches(suitor) } }
        val solution = finalists.minBy(Names::fullNameConsonantCount)
        val puzzle = Puzzle(config, suitors, clues, finalists.map { it.id }.toSet(), solution.id)
        PuzzleVerifier.verify(puzzle)
        return puzzle
    }

    /** Larger directories get a few more near misses, so they don't all sit in the first sections. */
    internal fun nearMissesPerClue(suitorCount: Int): Int = 1 + suitorCount / 2000

    /** Re-rolls the name clue parameters until every needed name group exists. */
    private fun rollNameClues(random: Random, finalistCount: Int): Pair<NameClues, NameGroups> {
        repeat(MAX_NAME_CLUE_ROLLS) {
            val nameClues = NameClues.random(random)
            val groups = NameGroups(nameClues)
            if (groups.supports(finalistCount)) return nameClues to groups
        }
        error("No usable name clues after $MAX_NAME_CLUE_ROLLS rolls – the name catalog doesn't fit the clue types")
    }
}

/** Collects the suitors of one directory and guarantees that no two are identical (name + all attributes). */
private class DirectoryBuilder(
    private val random: Random,
    private val clueSet: ClueSet,
    private val nameGroups: NameGroups,
) {
    // Suitors without ids (all 0), so data class equality compares name and attributes only.
    private val suitors = LinkedHashSet<Suitor>()
    private val clues = clueSet.inBookOrder()

    /** Mr Right gets the fewest consonants, the other finalists strictly more – a unique tie-break. */
    fun addFinalists(finalistCount: Int) {
        val solutionName = nameGroups.solutionCandidates(finalistCount).random(random)
        addUnique { finalist(solutionName) }
        val otherNames = nameGroups.namesWithMoreConsonants(solutionName)
        repeat(finalistCount - 1) {
            addUnique { finalist(otherNames.random(random)) }
        }
    }

    /** For every clue: suitors failing only that clue, so dropping any clue leaves extra candidates. */
    fun addNearMisses(perClue: Int) {
        repeat(perClue) {
            addUnique { finalist(passingName()).copy(income = clueSet.disallowedIncomes().random(random)) }
            Attribute.entries.forEach { attribute ->
                addUnique { withValue(finalist(passingName()), attribute, clueSet.disallowedValues(attribute).random(random)) }
            }
            clueSet.nameClues.all.indices.forEach { clueIndex ->
                addUnique { finalist(nameGroups.failingOnly(clueIndex).random(random)) }
            }
        }
    }

    fun fillWithRandomSuitors(suitorCount: Int) {
        while (suitors.size < suitorCount) {
            val candidate = randomSuitor()
            if (!clues.all { it.matches(candidate) }) suitors.add(candidate)
        }
    }

    fun shuffledWithIds(): List<Suitor> = suitors.shuffled(random).mapIndexed { id, suitor -> suitor.copy(id = id) }

    private fun addUnique(create: () -> Suitor) {
        repeat(MAX_ATTEMPTS) {
            if (suitors.add(create())) return
        }
        error("No unique suitor after $MAX_ATTEMPTS attempts")
    }

    private fun passingName(): FullName = nameGroups.passingAll.random(random)

    /** A suitor with the given name and only allowed attribute values. */
    private fun finalist(name: FullName): Suitor {
        val values = Attribute.entries.associateWith { clueSet.allowedValues(it).random(random) }
        return suitorOf(name, clueSet.allowedIncomes().random(random), values)
    }

    private fun randomSuitor(): Suitor {
        val name = FullName(NameCatalog.firstNames.random(random), NameCatalog.surnames.random(random))
        val values = Attribute.entries.associateWith { it.valueNames.random(random) }
        return suitorOf(name, Income.entries.random(random), values)
    }

    private fun withValue(suitor: Suitor, attribute: Attribute, value: String): Suitor = when (attribute) {
        Attribute.ELEMENT -> suitor.copy(element = Element.valueOf(value))
        Attribute.EYE_COLOR -> suitor.copy(eyeColor = EyeColor.valueOf(value))
        Attribute.HAIR_COLOR -> suitor.copy(hairColor = HairColor.valueOf(value))
        Attribute.TEMPERAMENT -> suitor.copy(temperament = Temperament.valueOf(value))
        Attribute.INTEREST -> suitor.copy(interest = Interest.valueOf(value))
    }

    private fun suitorOf(name: FullName, income: Income, values: Map<Attribute, String>): Suitor = Suitor(
        id = 0,
        firstName = name.firstName,
        lastName = name.lastName,
        income = income,
        element = Element.valueOf(values.getValue(Attribute.ELEMENT)),
        eyeColor = EyeColor.valueOf(values.getValue(Attribute.EYE_COLOR)),
        hairColor = HairColor.valueOf(values.getValue(Attribute.HAIR_COLOR)),
        temperament = Temperament.valueOf(values.getValue(Attribute.TEMPERAMENT)),
        interest = Interest.valueOf(values.getValue(Attribute.INTEREST)),
    )

    companion object {
        private const val MAX_ATTEMPTS = 1000
    }
}
