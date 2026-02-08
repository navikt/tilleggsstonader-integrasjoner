package no.nav.tilleggsstonader.integrasjoner.arena

import com.github.tomakehurst.wiremock.client.WireMock.badRequest
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import no.nav.tilleggsstonader.integrasjoner.IntegrationTest
import no.nav.tilleggsstonader.kontrakter.felles.JsonMapperProvider.jsonMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.HttpClientErrorException.BadRequest
import java.time.LocalDate

@TestPropertySource(properties = ["clients.arena.uri=http://localhost:28085"])
class ArenaClientTest : IntegrationTest() {
    @Autowired
    lateinit var arenaClient: ArenaClient

    @Test
    fun `skal svare med tom liste hvis personen ikke finnes i arena`() {
        val response =
            """
            {
              "timestamp": "2024-04-24T12:21:11.734+0200",
              "status": 400,
              "message": "Person med fødselsnummer 28********* finnes ikke i Arena",
              "method": "GET",
              "path": "/api/v1/tilleggsstoenad/aktiviteter",
              "correlationId": "1713954071714-984795294"
            }
            """.trimIndent()
        stubAktiviteter(response)

        val resultat = arenaClient.hentAktiviteter(ident = "1", fom = LocalDate.now(), tom = LocalDate.now())

        assertThat(resultat).isEmpty()
    }

    @Test
    fun `skal kaste videre feilet hvis det er annet feil en att personen ikke finnes`() {
        val response =
            """
            {
              "timestamp": "2024-04-24T12:21:11.734+0200",
              "status": 400,
              "message": "annen feil",
              "method": "GET",
              "path": "/api/v1/tilleggsstoenad/aktiviteter",
              "correlationId": "1713954071714-984795294"
            }
            """.trimIndent()
        stubAktiviteter(response)

        assertThatThrownBy {
            arenaClient.hentAktiviteter("1", LocalDate.now(), LocalDate.now())
        }.isInstanceOf(BadRequest::class.java)
    }

    private fun stubAktiviteter(responseJson: String) {
        val response =
            badRequest()
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withBody(jsonMapper.writeValueAsString(responseJson.trimIndent()))
        stubFor(get(urlMatching("/api/v1/tilleggsstoenad/aktiviteter.*")).willReturn(response))
    }
}
