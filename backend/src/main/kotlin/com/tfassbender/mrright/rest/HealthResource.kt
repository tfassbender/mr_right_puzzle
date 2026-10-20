package com.tfassbender.mrright.rest

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.config.inject.ConfigProperty

/** Public liveness probe, also used by the e2e smoke test. */
@Path("/api/health")
class HealthResource(
    @param:ConfigProperty(name = "mrright.info.version", defaultValue = "0.0.0")
    private val version: String,
) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun health(): HealthStatus = HealthStatus(status = "UP", service = "mr-right", version = version)
}

data class HealthStatus(
    val status: String,
    val service: String,
    val version: String,
)
