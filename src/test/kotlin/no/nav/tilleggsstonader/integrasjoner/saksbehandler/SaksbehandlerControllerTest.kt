package no.nav.tilleggsstonader.integrasjoner.saksbehandler

import no.nav.tilleggsstonader.integrasjoner.IntegrationTest
import no.nav.tilleggsstonader.integrasjoner.azure.client.AzureGraphRestClient
import no.nav.tilleggsstonader.integrasjoner.mocks.AzureADClientTestConfig


import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

import java.util.UUID


class SaksbehandlerControllerTest() :IntegrationTest()  {
    @Autowired
    lateinit var azureGraphRestClient: AzureGraphRestClient
    @Autowired
    lateinit var saksbehandlerService: SaksbehandlerService

    @BeforeEach
    fun setUp() {
        AzureADClientTestConfig.resetMock(azureGraphRestClient)
    }


    @Test
    fun `skal kalle korrekt tjeneste for oppslag på id`() {
        val id = UUID.randomUUID()
        val saksbehandler = saksbehandlerService.hentSaksbehandler(id.toString())

        assertThat(saksbehandler.fornavn).isEqualTo("Bob")
        assertThat(saksbehandler.etternavn).isEqualTo("Burger")
        assertThat(saksbehandler.navIdent).isEqualTo("B857496")
        assertThat(saksbehandler.enhet).isEqualTo("4402")
    }

    @Test
    fun `skal kalle korrekt tjeneste for oppslag på navIdent`() {
        val navIdent = "B857496"
        val saksbehandler = saksbehandlerService.hentSaksbehandler(navIdent)

        assertThat(saksbehandler.fornavn).isEqualTo("Bob")
        assertThat(saksbehandler.etternavn).isEqualTo("Burger")
        assertThat(saksbehandler.navIdent).isEqualTo(navIdent)
        assertThat(saksbehandler.enhet).isEqualTo("4402")
    }

    companion object {
        const val BASE_URL = "/api/saksbehandler"
    }
}