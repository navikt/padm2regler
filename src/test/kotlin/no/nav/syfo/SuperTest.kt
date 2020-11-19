package no.nav.syfo

import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.createApplicationEngine
import no.nav.syfo.clients.HttpClients
import no.nav.syfo.services.RuleService
import org.junit.Test

internal class SuperTest {


    val env: Environment = Environment(
        8080,
        "applicationName",
        "legeSuspensjonEndpointURL.no",
        "norskHelsenettEndpointURL.no",
        "helsenettproxyId",
        "aadAccessTokenUrl"

    )
    val vaultSecrets: VaultSecrets = VaultSecrets(
        "username",
        "password",
        "clientId",
        "clientSecret") // Ryan Seacrest
    val appState: ApplicationState = ApplicationState()
    val httpClients: HttpClients = HttpClients(env, vaultSecrets)
    val ruleService: RuleService = RuleService(httpClients)


    @Test
    internal fun `Test noe`(){


        val nettyAppEngine = createApplicationEngine(env, appState, ruleService)

        nettyAppEngine.start()

        when(ruleService.executeRuleChains()) then throw Exception // Bruke et mock-bibliotek her?

        `gjør kall til endepunkt`() // Da burde vi få exception inn til StatusPages, tror jeg

        httpClients.throwException()

        nettyAppEngine.stop(0)


    }

    fun HttpClients.throwException(e: Throwable) {
        throw e
    }

}
