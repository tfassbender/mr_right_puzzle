package com.tfassbender.mrright.puzzle

import kotlin.random.Random

/**
 * The rules of one kind of puzzle: its attributes and how the clues are chosen. The name catalog and the
 * generator algorithm are shared by all rule sets.
 */
interface RuleSet {
    val id: String

    /** Increase whenever a change produces a different puzzle for the same seed – it outdates existing games. */
    val generatorVersion: Int

    /**
     * The attributes every suitor has. The order is the order of the random draws, so it is part of the
     * generator – the display order is up to the frontend.
     */
    val attributes: List<AttributeDefinition>

    /** The clues of one puzzle, drawn from [random] (or fixed). They must support [finalistCount] finalists. */
    fun clues(random: Random, finalistCount: Int): ClueSet
}

/** All rule sets. [BOOK] is available to everybody, the others only to users who have them unlocked. */
object RuleSets {
    const val BOOK: String = "BOOK"

    val all: List<RuleSet> = listOf(BookRuleSet)

    fun byId(id: String): RuleSet? = all.find { it.id == id }

    /** Unknown ids in [unlocked] (e.g. a typo in the user file) are ignored. */
    fun availableFor(unlocked: Set<String>): List<RuleSet> = all.filter { it.id == BOOK || it.id in unlocked }
}
