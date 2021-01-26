package no.nav.syfo.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.network.sockets.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockkClass
import no.nav.syfo.model.Dialogmelding
import no.nav.syfo.model.ReceivedDialogmelding
import no.nav.syfo.model.Status
import no.nav.syfo.model.ValidationResult
import no.nav.syfo.no.nav.syfo.api.registerStatusPages
import no.nav.syfo.services.RuleService
import org.amshove.kluent.shouldBe
import org.junit.Test
import java.io.IOException
import java.time.LocalDateTime

class StatusPagesTest {

    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

    fun withTestApplicationEnginge(
        testApp: TestApplicationEngine,
        ruleService: RuleService,
        block: TestApplicationEngine.() -> Unit
    ) {
        testApp.start()
        testApp.application.install(ContentNegotiation) {
            jackson {
                registerKotlinModule()
                registerModule(JavaTimeModule())
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            }

            testApp.application.install(StatusPages) {
                registerStatusPages()
            }

            testApp.application.routing {
                registerRuleApi(ruleService)
            }
        }

        return testApp.block()
    }

    @Test
    fun `Ingen feilmelding skal ikke trigge StatusPage`() {
        val ruleService = mockRuleServiceThrowException(null)

        withTestApplicationEnginge(TestApplicationEngine(), ruleService) {
            val receivedDialogmelding = mockReceivedDialogmelding()

            with(handleRequest(HttpMethod.Post, "/v1/rules/validate") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(objectMapper.writeValueAsString(receivedDialogmelding))
            }
            ) {
                response.status() shouldBe HttpStatusCode.OK
            }
        }
    }

    @Test
    fun `IOException skal gi status 503`() {
        val ruleService =
            mockRuleServiceThrowException(IOException("Jeg fikk IOEXception ved kall til en ekstern tjeneste! :("))

        withTestApplicationEnginge(TestApplicationEngine(), ruleService) {
            val receivedDialogmelding = mockReceivedDialogmelding()

            with(handleRequest(HttpMethod.Post, "/v1/rules/validate") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(objectMapper.writeValueAsString(receivedDialogmelding))
            }
            ) {
                response.status() shouldBe HttpStatusCode.ServiceUnavailable
            }
        }
    }

    @Test
    fun `SocketTimeoutException skal gi status 503`() {
        val ruleService =
            mockRuleServiceThrowException(SocketTimeoutException("Jeg fikk SocketTimeoutExcpetion ved kall til en ekstern tjeneste :("))

        withTestApplicationEnginge(TestApplicationEngine(), ruleService) {
            val receivedDialogmelding = mockReceivedDialogmelding()

            with(handleRequest(HttpMethod.Post, "/v1/rules/validate") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(objectMapper.writeValueAsString(receivedDialogmelding))
            }
            ) {
                response.status() shouldBe HttpStatusCode.ServiceUnavailable
            }
        }
    }

    @Test
    fun `RuntimeException skal gi status 500`() {
        val ruleService = mockRuleServiceThrowException(RuntimeException("Jeg fikk RuntimeException! :("))

        withTestApplicationEnginge(TestApplicationEngine(), ruleService) {
            val receivedDialogmelding = mockReceivedDialogmelding()

            with(handleRequest(HttpMethod.Post, "/v1/rules/validate") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(objectMapper.writeValueAsString(receivedDialogmelding))
            }
            ) {
                response.status() shouldBe HttpStatusCode.InternalServerError
            }
        }
    }

    private fun mockRuleServiceThrowException(exception: Throwable?): RuleService {
        val ruleService = mockkClass(RuleService::class)

        if (exception == null) {
            coEvery { ruleService.executeRuleChains(any()) } returns ValidationResult(Status.OK, emptyList())
        } else {
            coEvery { ruleService.executeRuleChains(any()) } throws exception
        }

        return ruleService

    }

    private fun mockReceivedDialogmelding(): ReceivedDialogmelding {
        return ReceivedDialogmelding(
            Dialogmelding(
                "123",
                null,
                null,
                null
            ),
            "12345678901",
            "1234567890123",
            "98765432110",
            "9876543211098",
            "logID",
            "msgID",
            null,
            null,
            null,
            "orgnavn",
            LocalDateTime.now(),
            "fellesformat",
            null
        )
    }
}
