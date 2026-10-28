package com.tfassbender.mrright.puzzle

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SeedsAndConfigTest {

    @Test
    fun `seed hash is stable and uses the first 8 bytes of SHA-256`() {
        // SHA-256("emma") starts with f4aa0655cdb8d4fc – this pins the hashing, changing it changes every puzzle.
        assertEquals(0xf4aa0655cdb8d4fcUL.toLong(), Seeds.toLong("emma"))
        assertNotEquals(Seeds.toLong("emma"), Seeds.toLong("Emma"))
    }

    @Test
    fun `seed validation`() {
        assertTrue(Seeds.isValid("emma"))
        assertTrue(Seeds.isValid("a".repeat(Seeds.MAX_LENGTH)))
        assertFalse(Seeds.isValid(""))
        assertFalse(Seeds.isValid("   "))
        assertFalse(Seeds.isValid(" emma"))
        assertFalse(Seeds.isValid("a".repeat(Seeds.MAX_LENGTH + 1)))
    }

    @Test
    fun `random seeds are valid and differ`() {
        val seeds = (1..100).map { Seeds.random() }

        assertTrue(seeds.all(Seeds::isValid))
        assertEquals(100, seeds.toSet().size)
    }

    @Test
    fun `config is validated against the limits`() {
        PuzzleConfig(100, 1)
        PuzzleConfig(10_000, 5)
        assertThrows<IllegalArgumentException> { PuzzleConfig(99, 2) }
        assertThrows<IllegalArgumentException> { PuzzleConfig(10_001, 2) }
        assertThrows<IllegalArgumentException> { PuzzleConfig(100, 0) }
        assertThrows<IllegalArgumentException> { PuzzleConfig(100, 6) }
    }
}
