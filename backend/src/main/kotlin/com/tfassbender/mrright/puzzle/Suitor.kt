package com.tfassbender.mrright.puzzle

/**
 * One bachelor in the directory. [attributes] maps every attribute id of the rule set to its value.
 * Name-derived properties (initials, vowel and consonant counts) are computed via [Names], not stored.
 */
data class Suitor(
    val id: Int, // position in the directory, 0-based
    val firstName: String,
    val lastName: String,
    val attributes: Map<String, String>,
) {
    fun value(attribute: String): String = attributes.getValue(attribute)
}
