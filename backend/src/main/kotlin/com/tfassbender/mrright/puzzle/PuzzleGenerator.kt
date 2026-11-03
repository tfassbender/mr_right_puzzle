package com.tfassbender.mrright.puzzle

import kotlin.random.Random

/**
 * Builds a puzzle constructively (see doc/architecture.md, "Puzzle Generation"): clues and finalists first,
 * then near misses that make every clue count, then random suitors around them.
 * Deterministic: same rule set (and its generator version) + seed + config → same puzzle.
 */
object PuzzleGenerator {

    fun generate(ruleSet: RuleSet, seed: String, config: PuzzleConfig): Puzzle {
        val random = Seeds.toRandom(seed)
        val clueSet = ruleSet.clues(random, config.finalistCount)
        check(clueSet.nameGroups.supports(config.finalistCount)) {
            "The name clues of rule set ${ruleSet.id} don't support ${config.finalistCount} finalists"
        }

        val builder = DirectoryBuilder(random, ruleSet.attributes, clueSet)
        builder.addFinalists(config.finalistCount)
        builder.addNearMisses(nearMissesPerClue(config.suitorCount))
        builder.fillWithRandomSuitors(config.suitorCount)
        val suitors = builder.shuffledWithIds()

        val clues = clueSet.ordered
        val finalists = suitors.filter { suitor -> clues.all { it.matches(suitor) } }
        val solution = finalists.minBy(Names::fullNameConsonantCount)
        val puzzle = Puzzle(config, suitors, clues, finalists.map { it.id }.toSet(), solution.id)
        PuzzleVerifier.verify(puzzle)
        return puzzle
    }

    /** Larger directories get a few more near misses, so they don't all sit in the first sections. */
    internal fun nearMissesPerClue(suitorCount: Int): Int = 1 + suitorCount / 2000
}

/** Collects the suitors of one directory and guarantees that no two are identical (name + all attributes). */
private class DirectoryBuilder(
    private val random: Random,
    private val attributes: List<AttributeDefinition>,
    private val clueSet: ClueSet,
) {
    // Suitors without ids (all 0), so data class equality compares name and attributes only.
    private val suitors = LinkedHashSet<Suitor>()
    private val clues = clueSet.ordered
    private val nameGroups = clueSet.nameGroups

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
            clueSet.attributeClues.forEach { clue ->
                addUnique {
                    val suitor = finalist(passingName())
                    val disallowed = clueSet.disallowedValues(clue.attribute).random(random)
                    suitor.copy(attributes = suitor.attributes + (clue.attribute to disallowed))
                }
            }
            clueSet.nameClues.indices.forEach { clueIndex ->
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
    private fun finalist(name: FullName): Suitor =
        suitorOf(name, attributes.associate { it.id to clueSet.allowedValues(it.id).random(random) })

    private fun randomSuitor(): Suitor {
        val name = FullName(NameCatalog.firstNames.random(random), NameCatalog.surnames.random(random))
        return suitorOf(name, attributes.associate { it.id to it.values.random(random) })
    }

    private fun suitorOf(name: FullName, values: Map<String, String>): Suitor =
        Suitor(id = 0, firstName = name.firstName, lastName = name.lastName, attributes = values)

    companion object {
        private const val MAX_ATTEMPTS = 1000
    }
}
