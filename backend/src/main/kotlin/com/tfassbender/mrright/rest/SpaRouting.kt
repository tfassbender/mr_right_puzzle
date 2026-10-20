package com.tfassbender.mrright.rest

import io.vertx.ext.web.Router
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes

/**
 * Serves the SPA for client-side routes: a deep link like `/verify-email?token=…` or a reload on
 * `/games/123` has no file behind it, so it is rerouted to `/` (the `index.html`) and the client router
 * takes over. API calls and files (paths with a dot, e.g. `/assets/app.js`) pass through unchanged.
 */
@ApplicationScoped
class SpaRouting {

    fun init(@Observes router: Router) {
        router.get("/*").handler { context ->
            val path = context.normalizedPath()
            if (isClientRoute(path)) context.reroute("/") else context.next()
        }
    }

    private fun isClientRoute(path: String): Boolean =
        path != "/" && !path.startsWith("/api/") && !path.startsWith("/q/") && !path.substringAfterLast('/').contains('.')
}
