package com.tfassbender.mrright.puzzle

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * One of Emma's criteria: a predicate every finalist fulfils. Clues are language-neutral – the JSON carries
 * a `type` plus parameters, the frontend renders the text.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(IncomeAtLeast::class, name = "INCOME_AT_LEAST"),
    JsonSubTypes.Type(AttributeIsNot::class, name = "ATTRIBUTE_IS_NOT"),
    JsonSubTypes.Type(AttributeIsOneOf::class, name = "ATTRIBUTE_IS_ONE_OF"),
    JsonSubTypes.Type(InitialsDiffer::class, name = "INITIALS_DIFFER"),
    JsonSubTypes.Type(InitialsInRange::class, name = "INITIALS_IN_RANGE"),
    JsonSubTypes.Type(SurnameStartAndEndDiffer::class, name = "SURNAME_START_AND_END_DIFFER"),
    JsonSubTypes.Type(FirstNameVowelCount::class, name = "FIRST_NAME_VOWEL_COUNT"),
    JsonSubTypes.Type(SurnameVowelCount::class, name = "SURNAME_VOWEL_COUNT"),
    JsonSubTypes.Type(NameExcludesLetter::class, name = "NAME_EXCLUDES_LETTER"),
)
sealed interface Clue {
    fun matches(suitor: Suitor): Boolean
}

data class IncomeAtLeast(val level: Income) : Clue {
    override fun matches(suitor: Suitor): Boolean = suitor.income >= level
}

data class AttributeIsNot(val attribute: Attribute, val value: String) : Clue {
    init {
        require(value in attribute.valueNames) { "$value is not a value of $attribute" }
    }

    override fun matches(suitor: Suitor): Boolean = attribute.valueName(suitor) != value
}

/** "One of" always allows 2 or 3 values – a single value would make the clue too strong. */
data class AttributeIsOneOf(val attribute: Attribute, val values: Set<String>) : Clue {
    init {
        require(values.size in 2..3) { "one-of clues need 2 or 3 values, got $values" }
        require(attribute.valueNames.containsAll(values)) { "$values are not all values of $attribute" }
    }

    override fun matches(suitor: Suitor): Boolean = attribute.valueName(suitor) in values
}

data object InitialsDiffer : Clue {
    override fun matches(suitor: Suitor): Boolean =
        Names.initial(suitor.firstName) != Names.initial(suitor.lastName)
}

enum class LetterRange(val first: Char, val last: Char) {
    A_TO_M('A', 'M'),
    N_TO_Z('N', 'Z');

    operator fun contains(letter: Char): Boolean = letter.uppercaseChar() in first..last
}

/** Both initials (first name and surname) lie in the range. */
data class InitialsInRange(val range: LetterRange) : Clue {
    override fun matches(suitor: Suitor): Boolean =
        Names.initial(suitor.firstName) in range && Names.initial(suitor.lastName) in range
}

data object SurnameStartAndEndDiffer : Clue {
    override fun matches(suitor: Suitor): Boolean =
        suitor.lastName.first().lowercaseChar() != suitor.lastName.last().lowercaseChar()
}

data class FirstNameVowelCount(val count: Int) : Clue {
    override fun matches(suitor: Suitor): Boolean = Names.vowelCount(suitor.firstName) == count
}

data class SurnameVowelCount(val count: Int) : Clue {
    override fun matches(suitor: Suitor): Boolean = Names.vowelCount(suitor.lastName) == count
}

/** Neither first name nor surname contains the letter (case-insensitive). */
data class NameExcludesLetter(val letter: Char) : Clue {
    init {
        require(letter in 'A'..'Z') { "letter must be an uppercase A–Z, got $letter" }
    }

    override fun matches(suitor: Suitor): Boolean =
        !suitor.firstName.contains(letter, ignoreCase = true) && !suitor.lastName.contains(letter, ignoreCase = true)
}
