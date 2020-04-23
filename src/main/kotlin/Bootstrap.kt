package no.nav.syfo

import io.ktor.util.KtorExperimentalAPI
import no.nav.syfo.application.ApplicationServer
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.createApplicationEngine
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("no.nav.syfo.padm2regler")

@KtorExperimentalAPI
fun main() {
    val env = Environment()
    val applicationState = ApplicationState()

    val applicationEngine = createApplicationEngine(env, applicationState)

    ApplicationServer(applicationEngine).start()

    applicationState.ready = true
}
