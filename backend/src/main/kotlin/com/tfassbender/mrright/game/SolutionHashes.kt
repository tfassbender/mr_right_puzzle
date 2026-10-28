package com.tfassbender.mrright.game

import java.security.MessageDigest
import java.util.HexFormat
import java.util.UUID

/**
 * Hashes that let the client check the finalists and Mr Right offline without the answer being in the payload.
 * The game id acts as salt. Brute-forcing them is possible and accepted (see doc/puzzle-analysis.md).
 * The frontend computes the same strings – keep both in sync.
 */
object SolutionHashes {

    fun finalistsHash(gameId: UUID, finalistIds: Collection<Int>): String =
        sha256Hex("$gameId:${finalistIds.sorted().joinToString(",")}")

    fun solutionHash(gameId: UUID, solutionId: Int): String = sha256Hex("$gameId:$solutionId")

    private fun sha256Hex(text: String): String =
        HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8)))
}
