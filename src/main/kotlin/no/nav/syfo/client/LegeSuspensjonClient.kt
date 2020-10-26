package no.nav.syfo.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import no.nav.syfo.VaultSecrets
import no.nav.syfo.helpers.retry
import no.nav.syfo.log
import java.io.IOException

@KtorExperimentalAPI
class LegeSuspensjonClient(
    private val endpointUrl: String,
    private val secrets: VaultSecrets,
    private val stsClient: StsOidcClient,
    private val httpClient: HttpClient
) {

    suspend fun checkTherapist(therapistId: String, ediloggid: String, oppslagsdato: String): Suspendert =
        retry("lege_suspansjon") {
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
            }.execute()
            when (httpStatement.status) {
                HttpStatusCode.OK -> {
                    httpStatement.call.response.receive<Suspendert>()
                }
                else -> {
                    log.error("Btsys svarte med kode {} for ediloggId {}, {}", httpStatement.status, ediloggid)
                    throw IOException("Btsys svarte med uventet kode ${httpStatement.status} for $ediloggid")
                }
            }
        }
}

data class Suspendert(val suspendert: Boolean)
