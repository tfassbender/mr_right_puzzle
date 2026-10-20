package com.tfassbender.mrright.storage

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.nio.file.Path
import java.time.Clock

/** The configured root directory of all stored files (users/, later games/). */
data class DataDirectory(val path: Path)

/**
 * Beans shared by the file repositories. The feature packages produce their own repositories from
 * [DataDirectory], so `storage` never depends on a feature package.
 */
@ApplicationScoped
class StorageProducers(
    @param:ConfigProperty(name = "mrright.data.dir", defaultValue = "data")
    private val dataDir: String,
) {

    @Produces
    @ApplicationScoped
    fun dataDirectory(): DataDirectory = DataDirectory(Path.of(dataDir))

    @Produces
    @ApplicationScoped
    fun clock(): Clock = Clock.systemUTC()
}
