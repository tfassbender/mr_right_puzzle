package com.tfassbender.mrright.puzzle

import kotlin.random.Random

/**
 * One attribute of a rule set, e.g. eye colour. Values are referenced by name (plain strings in the JSON),
 * in legend order – ascending for ordinal attributes like income. Number attributes (height, birth year) have
 * one value per number.
 *
 * [weights] (one per value) make some values more common among the random suitors; `null` means evenly
 * distributed, which the book relies on (a weighted draw would change its puzzles).
 */
data class AttributeDefinition(val id: String, val values: List<String>, val weights: List<Double>? = null) {
    init {
        require(values.size >= 2) { "Attribute $id needs at least 2 values" }
        require(values.toSet().size == values.size) { "Attribute $id has duplicate values" }
        require(weights == null || (weights.size == values.size && weights.all { it > 0 })) {
            "Attribute $id needs one positive weight per value"
        }
    }

    private val totalWeight: Double = weights?.sum() ?: 0.0

    /** A value for a random suitor. */
    fun randomValue(random: Random): String {
        if (weights == null) return values.random(random)
        var remaining = random.nextDouble(totalWeight)
        values.forEachIndexed { index, value ->
            remaining -= weights[index]
            if (remaining < 0) return value
        }
        return values.last() // only reached through rounding
    }

    companion object {
        /** A number attribute: one value per number in [range], e.g. heights in cm. */
        fun numbers(id: String, range: IntRange, weights: List<Double>? = null): AttributeDefinition =
            AttributeDefinition(id, range.map(Int::toString), weights)
    }
}

/** Annual income in the book, ordinal: £ .. ££££. An enum, because [IncomeAtLeast] compares the levels. */
enum class Income { AT_LEAST_1K, AT_LEAST_2K, AT_LEAST_4K, AT_LEAST_10K }
