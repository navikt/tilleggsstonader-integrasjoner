package no.nav.tilleggsstonader.integrasjoner.mocks


import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.integrasjoner.azure.client.AzureGraphRestClient
import no.nav.tilleggsstonader.integrasjoner.azure.domene.AzureAdBruker
import no.nav.tilleggsstonader.integrasjoner.azure.domene.AzureAdBrukere
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.util.*

@Configuration
@Profile("mock-az-ad")
class AzureADClientTestConfig {
    @Bean
    @Primary
    fun azureadClient(): AzureGraphRestClient {
        val client = mockk<AzureGraphRestClient>()
        resetMock(client)
        return client
    }

    companion object {
        fun resetMock(client: AzureGraphRestClient) {
            clearMocks(client)
            val responseHentSaksbehandler = AzureAdBruker(id = UUID.randomUUID(), onPremisesSamAccountName = "B857496", givenName = "Bob", surname = "Burger", streetAddress = "4402" , userPrincipalName = "Bob Burger"  )
            val responseFinnSaksbehandler = AzureAdBrukere(listOf(responseHentSaksbehandler))
            every { client.hentSaksbehandler(any()) } returns responseHentSaksbehandler
            every { client.finnSaksbehandler(any()) } returns responseFinnSaksbehandler
        }
    }
}

