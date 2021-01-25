package no.nav.syfo.application

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import no.nav.syfo.Environment
import no.nav.syfo.api.registerRuleApi
import no.nav.syfo.application.api.registerNaisApi
import no.nav.syfo.metrics.monitorHttpRequests
import no.nav.syfo.no.nav.syfo.api.registerStatusPages
import no.nav.syfo.services.RuleService

@KtorExperimentalAPI
fun createApplicationEngine(
    environment: Environment,
    applicationState: ApplicationState,
    ruleService: RuleService
): NettyApplicationEngine {
    return embeddedServer(Netty, environment.applicationPort) {
        install(ContentNegotiation) {
            jackson {
                registerKotlinModule()
                registerModule(JavaTimeModule())
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
        install(StatusPages) {
            registerStatusPages()
        }
        routing {
            registerNaisApi(applicationState)
            registerRuleApi(ruleService)
        }
        intercept(ApplicationCallPipeline.Monitoring, monitorHttpRequests())
    }
}
