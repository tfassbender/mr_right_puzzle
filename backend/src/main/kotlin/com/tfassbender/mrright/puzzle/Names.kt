package com.tfassbender.mrright.puzzle

/**
 * Letter rules of the book: vowels are a, e, i, o, u – y is a consonant.
 * Names contain only the letters A–Z, so every non-vowel character is a consonant.
 */
object Names {
    private const val VOWELS = "aeiou"

    fun initial(name: String): Char = name.first().uppercaseChar()

    fun vowelCount(name: String): Int = name.count { it.lowercaseChar() in VOWELS }

    fun consonantCount(name: String): Int = name.length - vowelCount(name)

    /** The tie-break value: consonants in first name + surname (the "Mr" prefix is not part of the name). */
    fun fullNameConsonantCount(suitor: Suitor): Int =
        consonantCount(suitor.firstName) + consonantCount(suitor.lastName)
}
