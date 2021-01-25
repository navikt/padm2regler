package no.nav.syfo.no.nav.syfo.api

import io.ktor.application.*
import io.ktor.features.StatusPages.*
import io.ktor.http.*
import io.ktor.network.sockets.*
import io.ktor.response.*
import no.nav.syfo.log
import java.io.IOException

fun Configuration.registerStatusPages() {
    exception<IOException> { cause ->
        call.respond(HttpStatusCode.ServiceUnavailable, cause.message ?: "Unknown error")

        log.error("Caught IOException", cause)
    }

    exception<SocketTimeoutException> { cause ->
        call.respond(HttpStatusCode.ServiceUnavailable, cause.message ?: "Unknown error")

        log.error("Caught SocketTimeoutException", cause)
    }

    exception<Throwable> { cause ->
        call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Unknown error")

        log.error("Caught unknown exception", cause)
    }
}
