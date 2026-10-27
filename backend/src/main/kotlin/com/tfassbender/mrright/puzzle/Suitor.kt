package com.tfassbender.mrright.puzzle

/**
 * One bachelor in the directory. Name-derived properties (initials, vowel and consonant counts) are
 * computed via [Names], not stored.
 */
data class Suitor(
    val id: Int, // position in the directory, 0-based
    val firstName: String,
    val lastName: String,
    val income: Income,
    val element: Element,
    val eyeColor: EyeColor,
    val hairColor: HairColor,
    val temperament: Temperament,
    val interest: Interest,
)
