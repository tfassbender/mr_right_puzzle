package com.tfassbender.mrright.puzzle

internal data class FullName(val firstName: String, val lastName: String) {
    val consonantCount: Int = Names.consonantCount(firstName) + Names.consonantCount(lastName)
}

/**
 * All 14,400 catalog names, grouped by the set of name clues they fail (a bit mask over the name clues).
 * Finalists need a name failing nothing, a name clue's near miss a name failing only that clue.
 */
internal class NameGroups(private val clues: List<Clue>) {
    private val groups: Map<Int, List<FullName>>

    init {
        val allNames = NameCatalog.firstNames.flatMap { first -> NameCatalog.surnames.map { last -> FullName(first, last) } }
        groups = allNames.groupBy(::failedClueMask)
    }

    val passingAll: List<FullName> get() = groups[0].orEmpty()

    fun failingOnly(clueIndex: Int): List<FullName> = groups[1 shl clueIndex].orEmpty()

    /** Every group needed for finalists and near misses exists, and the finalists' tie-break can be unique. */
    fun supports(finalistCount: Int): Boolean =
        clues.indices.all { failingOnly(it).isNotEmpty() } && solutionCandidates(finalistCount).isNotEmpty()

    /** Names Mr Right can have: at least `finalistCount - 1` passing names have strictly more consonants. */
    fun solutionCandidates(finalistCount: Int): List<FullName> =
        passingAll.filter { candidate -> namesWithMoreConsonants(candidate).size >= finalistCount - 1 }

    fun namesWithMoreConsonants(name: FullName): List<FullName> =
        passingAll.filter { it.consonantCount > name.consonantCount }

    private fun failedClueMask(name: FullName): Int {
        // Name clues only look at the name, so this probe needs no attribute values.
        val probe = Suitor(0, name.firstName, name.lastName, emptyMap())
        return clues.foldIndexed(0) { index, mask, clue -> if (clue.matches(probe)) mask else mask or (1 shl index) }
    }
}
