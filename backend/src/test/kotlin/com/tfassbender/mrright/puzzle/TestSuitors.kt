package com.tfassbender.mrright.puzzle

/** A book suitor with defaults for every field, so a test only states what it is about. */
fun suitor(
    firstName: String = "Edward",
    lastName: String = "Ashby",
    income: Income = Income.AT_LEAST_4K,
    element: String = "AIR",
    eyeColor: String = "GREEN",
    hairColor: String = "BROWN",
    temperament: String = "BROODING",
    interest: String = "INTELLECTUAL",
): Suitor = Suitor(
    0, firstName, lastName,
    mapOf(
        BookRuleSet.INCOME to income.name,
        BookRuleSet.ELEMENT to element,
        BookRuleSet.EYE_COLOR to eyeColor,
        BookRuleSet.HAIR_COLOR to hairColor,
        BookRuleSet.TEMPERAMENT to temperament,
        BookRuleSet.INTEREST to interest,
    ),
)
