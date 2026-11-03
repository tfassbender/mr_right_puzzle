package com.tfassbender.mrright.puzzle

/**
 * One attribute of a rule set, e.g. eye colour. Values are referenced by name (plain strings in the JSON),
 * in legend order – ascending for ordinal attributes like income.
 */
data class AttributeDefinition(val id: String, val values: List<String>) {
    init {
        require(values.size >= 2) { "Attribute $id needs at least 2 values" }
        require(values.toSet().size == values.size) { "Attribute $id has duplicate values" }
    }
}

/** Annual income in the book, ordinal: £ .. ££££. An enum, because [IncomeAtLeast] compares the levels. */
enum class Income { AT_LEAST_1K, AT_LEAST_2K, AT_LEAST_4K, AT_LEAST_10K }
