package com.tfassbender.mrright.puzzle

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NamesTest {

    @Test
    fun `vowels are a, e, i, o, u in any case`() {
        assertEquals(4, Names.vowelCount("Theodore"))
        assertEquals(2, Names.vowelCount("Oscar"))
    }

    @Test
    fun `y is a consonant`() {
        assertEquals(1, Names.vowelCount("Percy"))
        assertEquals(4, Names.consonantCount("Percy"))
    }

    @Test
    fun `initial is the uppercase first letter`() {
        assertEquals('E', Names.initial("edward"))
    }

    @Test
    fun `full name consonants count first name and surname`() {
        // Hugh: H, g, h = 3 – Wray: W, r, y = 3
        assertEquals(6, Names.fullNameConsonantCount(suitor(firstName = "Hugh", lastName = "Wray")))
    }
}
