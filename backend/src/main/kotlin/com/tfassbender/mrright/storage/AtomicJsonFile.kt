package com.tfassbender.mrright.storage

import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.name

/**
 * Crash-safe file writes: the content goes to a temp file in the target's directory, which is then moved
 * over the target. A reader always sees either the old or the new content, never a half-written file.
 */
object AtomicJsonFile {

    fun write(target: Path, json: String) {
        val dir = target.parent
        Files.createDirectories(dir)
        val tmp = Files.createTempFile(dir, "${target.name}.", ".tmp")
        try {
            Files.writeString(tmp, json)
            move(tmp, target)
        } catch (e: IOException) {
            Files.deleteIfExists(tmp)
            throw UncheckedIOException("Failed to write '$target'", e)
        }
    }

    private fun move(tmp: Path, target: Path) {
        try {
            Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (_: AtomicMoveNotSupportedException) {
            // Rare (e.g. some network filesystems): fall back to a non-atomic replacing move.
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
