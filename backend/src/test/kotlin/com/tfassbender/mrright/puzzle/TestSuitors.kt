package com.tfassbender.mrright.puzzle

/** A suitor with defaults for every field, so a test only states what it is about. */
fun suitor(
    firstName: String = "Edward",
    lastName: String = "Ashby",
    income: Income = Income.AT_LEAST_4K,
    element: Element = Element.AIR,
    eyeColor: EyeColor = EyeColor.GREEN,
    hairColor: HairColor = HairColor.BROWN,
    temperament: Temperament = Temperament.BROODING,
    interest: Interest = Interest.INTELLECTUAL,
): Suitor = Suitor(0, firstName, lastName, income, element, eyeColor, hairColor, temperament, interest)
