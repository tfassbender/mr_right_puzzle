package com.tfassbender.mrright.puzzle

/** Annual income, ordinal: £ .. ££££. */
enum class Income { AT_LEAST_1K, AT_LEAST_2K, AT_LEAST_4K, AT_LEAST_10K }

/** Only the element of the star sign, like in the book. */
enum class Element { AIR, FIRE, WATER, EARTH }

enum class EyeColor { BROWN, BROWN_GREEN, AMBER, GREEN, BLUE }

enum class HairColor { BLOND, BROWN, BLACK, AUBURN }

enum class Temperament { BROODING, CHARMING, RESERVED, AMIABLE, DASHING }

enum class Interest { INTELLECTUAL, MUSICAL, SPORTY, SOCIABLE }

/**
 * The non-ordinal attributes that "is not" and "one of" clues refer to. Income is ordinal and has its own
 * clue ([IncomeAtLeast]). Values are referenced by their enum constant name, so a clue serializes as plain JSON.
 */
enum class Attribute(val valueNames: List<String>, private val valueOf: (Suitor) -> Enum<*>) {
    ELEMENT(Element.entries.map { it.name }, Suitor::element),
    EYE_COLOR(EyeColor.entries.map { it.name }, Suitor::eyeColor),
    HAIR_COLOR(HairColor.entries.map { it.name }, Suitor::hairColor),
    TEMPERAMENT(Temperament.entries.map { it.name }, Suitor::temperament),
    INTEREST(Interest.entries.map { it.name }, Suitor::interest);

    fun valueName(suitor: Suitor): String = valueOf(suitor).name
}
