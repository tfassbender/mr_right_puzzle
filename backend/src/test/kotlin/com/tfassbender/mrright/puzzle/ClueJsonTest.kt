package com.tfassbender.mrright.puzzle

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/** The clue JSON is what the frontend renders texts from – the format must stay stable. */
class ClueJsonTest {

    private val mapper = jacksonObjectMapper()

    private val allClueTypes: List<Clue> = listOf(
        IncomeAtLeast(Income.AT_LEAST_4K),
        AttributeIsNot(Attribute.HAIR_COLOR, "BLOND"),
        AttributeIsOneOf(Attribute.EYE_COLOR, setOf("GREEN", "BLUE")),
        InitialsDiffer,
        InitialsInRange(LetterRange.A_TO_M),
        SurnameStartAndEndDiffer,
        FirstNameVowelCount(3),
        SurnameVowelCount(2),
        NameExcludesLetter('H'),
    )

    @Test
    fun `clues serialize with a type and their parameters`() {
        assertEquals(
            """{"type":"INCOME_AT_LEAST","level":"AT_LEAST_4K"}""",
            mapper.writeValueAsString(IncomeAtLeast(Income.AT_LEAST_4K)),
        )
        assertEquals(
            """{"type":"ATTRIBUTE_IS_ONE_OF","attribute":"EYE_COLOR","values":["GREEN","BLUE"]}""",
            mapper.writeValueAsString(AttributeIsOneOf(Attribute.EYE_COLOR, setOf("GREEN", "BLUE"))),
        )
        assertEquals("""{"type":"NAME_EXCLUDES_LETTER","letter":"H"}""", mapper.writeValueAsString(NameExcludesLetter('H')))
        assertEquals("""{"type":"INITIALS_DIFFER"}""", mapper.writeValueAsString(InitialsDiffer))
    }

    @Test
    fun `every clue type survives a round trip`() {
        // A bare List<Clue> loses its element type to erasure, so the writer needs it explicitly.
        // In real payloads the clues are a typed field, which works without this.
        val json = mapper.writerFor(jacksonTypeRef<List<Clue>>()).writeValueAsString(allClueTypes)

        assertEquals(allClueTypes, mapper.readValue<List<Clue>>(json))
    }
}
