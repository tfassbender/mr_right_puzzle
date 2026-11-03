package com.tfassbender.mrright.puzzle

import com.fasterxml.jackson.annotation.JsonIgnore
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

/**
 * A clue on one attribute. The generator builds finalists from the values it [allows] and near misses from the
 * others; the rule set's clue set checks that [referencedValues] exist.
 */
sealed interface AttributeClue : Clue {
    val attribute: String

    fun allows(value: String): Boolean

    fun referencedValues(): Set<String>

    override fun matches(suitor: Suitor): Boolean = allows(suitor.value(attribute))
}

/** Book only: the income is ordinal. The attribute is implied by the type, so it is not part of the JSON. */
data class IncomeAtLeast(val level: Income) : AttributeClue {
    @get:JsonIgnore
    override val attribute: String get() = BookRuleSet.INCOME

    override fun allows(value: String): Boolean = Income.valueOf(value) >= level

    override fun referencedValues(): Set<String> = setOf(level.name)
}

data class AttributeIsNot(override val attribute: String, val value: String) : AttributeClue {
    override fun allows(value: String): Boolean = value != this.value

    override fun referencedValues(): Set<String> = setOf(value)
}

/** "One of" always allows 2 or 3 values – a single value would make the clue too strong. */
data class AttributeIsOneOf(override val attribute: String, val values: Set<String>) : AttributeClue {
    init {
        require(values.size in 2..3) { "one-of clues need 2 or 3 values, got $values" }
    }

    override fun allows(value: String): Boolean = value in values

    override fun referencedValues(): Set<String> = values
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
