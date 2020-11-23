package no.nav.syfo.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import no.nav.syfo.VaultSecrets
import no.nav.syfo.log
import java.io.IOException

@KtorExperimentalAPI
class LegeSuspensjonClient(
    private val endpointUrl: String,
    private val secrets: VaultSecrets,
    private val stsClient: StsOidcClient,
    private val httpClient: HttpClient
) {

    suspend fun checkTherapist(therapistId: String, ediloggid: String, oppslagsdato: String): Suspendert {
        val httpStatement = httpClient.get<HttpStatement>("$endpointUrl/api/v1/suspensjon/status") {
            accept(ContentType.Application.Json)
            val oidcToken = stsClient.oidcToken()
            headers {
                append("Nav-Call-Id", ediloggid)
                append("Nav-Consumer-Id", secrets.serviceuserUsername)
                append("Nav-Personident", therapistId)

                append("Authorization", "Bearer ${oidcToken.access_token}")
            }
            parameter("oppslagsdato", oppslagsdato)
        }

        val httpResponse = httpStatement.execute()

        if (httpResponse.status != HttpStatusCode.OK) {
            log.error("Btsys svarte med kode {} for ediloggId {}, {}", httpResponse.status, ediloggid)
            throw IOException("Btsys svarte med uventet kode ${httpResponse.status} for $ediloggid")
        }

        return httpResponse.call.response.receive()
    }
}

data class Suspendert(val suspendert: Boolean)
