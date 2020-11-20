package no.nav.syfo

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.syfo.api.registerRuleApi
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.api.registerNaisApi
import no.nav.syfo.metrics.monitorHttpRequests
import no.nav.syfo.model.Dialogmelding
import no.nav.syfo.model.ReceivedDialogmelding
import no.nav.syfo.services.RuleService
import org.amshove.kluent.`should be less or equal to`
import org.amshove.kluent.shouldBe
import org.junit.Test
import java.time.LocalDateTime

internal class SuperTest {
    val env: Environment = Environment(
        8080,
        "applicationName",
        "http://www.legeSuspensjonEndpointURL.no",
        "http://www.norskHelsenettEndpointURL.no",
        "helsenettproxyId",
        "http://www.aadAccessTokenUrl.no"

    )
    val vaultSecrets: VaultSecrets = VaultSecrets(
        "username",
        "password",
        "clientId",
        "clientSecret"
    ) // Ryan Seacrest

    @Test
    fun `ny test`() {
        val testApp = TestApplicationEngine()
        testApp.start(false)

        val appState = ApplicationState()

        val mockRuleService = mockk<RuleService>()

        coEvery { mockRuleService.executeRuleChains(any()) } throws NullPointerException("dasd")

        testApp.application.install(ContentNegotiation) {
            jackson {
                registerKotlinModule()
                registerModule(JavaTimeModule())
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
        testApp.application.install(StatusPages) {
            exception<Throwable> { cause ->
                call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Unknown error")

                log.error("Caught exception", cause)
//                throw cause
            }
        }
        testApp.application.routing {
            registerNaisApi(appState)
            registerRuleApi(mockRuleService)
        }
        testApp.application.intercept(ApplicationCallPipeline.Monitoring, monitorHttpRequests())

        testApp.doStuff()

        1 `should be less or equal to` 2
    }
}


fun TestApplicationEngine.doStuff() {
    with(
        handleRequest(HttpMethod.Post, "/v1/rules/validate") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())

            val dialogmelding = Dialogmelding(
                id = "id123",
                dialogmeldingNotat = null,
                dialogmeldingSvar = null,
                dialogmeldingForesporsel = null
            )

            val receivedDialogmelding = ReceivedDialogmelding(
                dialogmelding = dialogmelding,
                personNrPasient = "12345678901",
                pasientAktoerId = "123456789012",
                personNrLege = "98765432198",
                legeAktoerId = "987654321987",
                navLogId = "LogId",
                msgId = "msgId",
                legekontorOrgNr = "12345678",
                legekontorHerId = "1234",
                legekontorReshId = "4321",
                legekontorOrgName = "Navn",
                mottattDato = LocalDateTime.now(),
                fellesformat = "asd",
                tssid = "idtss"
            )

            val receivedDialogmeldingJson = objectMapper.writeValueAsString(receivedDialogmelding)

            setBody(receivedDialogmeldingJson)
        }
    ) {
        println(response)
        response.status() shouldBe HttpStatusCode.OK
    }
}

val objectMapper: ObjectMapper = ObjectMapper()
    .registerModule(JavaTimeModule())
    .registerKotlinModule()
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
