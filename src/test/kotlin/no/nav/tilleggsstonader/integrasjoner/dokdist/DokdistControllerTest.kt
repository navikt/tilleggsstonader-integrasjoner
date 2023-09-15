package no.nav.tilleggsstonader.integrasjoner.dokdist

import com.github.tomakehurst.wiremock.client.WireMock.badRequest
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import no.nav.tilleggsstonader.integrasjoner.IntegrationTest
import no.nav.tilleggsstonader.integrasjoner.util.ProblemDetailUtil.catchProblemDetailException
import no.nav.tilleggsstonader.kontrakter.dokdist.DistribuerJournalpostRequest
import no.nav.tilleggsstonader.kontrakter.dokdist.Distribusjonstidspunkt
import no.nav.tilleggsstonader.kontrakter.dokdist.Distribusjonstype
import no.nav.tilleggsstonader.kontrakter.felles.Fagsystem
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.exchange
import java.nio.charset.StandardCharsets
import java.nio.file.Files

@ActiveProfiles(profiles = ["integrasjonstest", "mock-sts"])
@TestPropertySource(properties = ["clients.dokdist.uri=http://localhost:28085"])
@AutoConfigureWireMock(port = 28085)
class DokdistControllerTest : IntegrationTest() {

    private val request = DistribuerJournalpostRequest(JOURNALPOST_ID, Fagsystem.TILLEGGSSTONADER, "ba-sak", null)

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(onBehalfOfToken())
    }

    @Test
    fun `dokdist returnerer OK uten kjernetid og distribusjonstidspunkt`() {
        mockGodkjentKallMotDokDist()

        val body2 = """
            {
                "journalpostId": "$JOURNALPOST_ID",
                "bestillendeFagsystem": "${Fagsystem.TILLEGGSSTONADER}",
                "dokumentProdApp": "ts-sak"
            }
        """.trimIndent()
        headers.set("Content-Type", "application/json")
        val response: ResponseEntity<String> = restTemplate.exchange(
            localhost(DOKDIST_URL),
            HttpMethod.POST,
            HttpEntity(body2, headers),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("1234567")
    }

    @Test
    fun `dokdist returnerer OK med distribusjonstype`() {
        mockGodkjentKallMotDokDist()

        val body = DistribuerJournalpostRequest(
            JOURNALPOST_ID,
            Fagsystem.TILLEGGSSTONADER,
            "ts-sak",
            Distribusjonstype.VIKTIG,
            Distribusjonstidspunkt.KJERNETID,
        )
        val response: ResponseEntity<String> = restTemplate.exchange(
            localhost(DOKDIST_URL),
            HttpMethod.POST,
            HttpEntity(body, headers),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("1234567")
    }

    @Test
    fun `dokdist returnerer OK`() {
        mockGodkjentKallMotDokDist()

        val response: ResponseEntity<String> = restTemplate.exchange(
            localhost(DOKDIST_URL),
            HttpMethod.POST,
            HttpEntity(request, headers),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("1234567")
    }

    @Test
    fun `dokdist returnerer 400`() {
        stubFor(
            post("/rest/v1/distribuerjournalpost")
                .willReturn(
                    badRequest()
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(badRequestResponse()),
                ),
        )

        val body = request
        val response = catchProblemDetailException {
            restTemplate.exchange<String>(
                localhost(DOKDIST_URL),
                HttpMethod.POST,
                HttpEntity(body, headers),
            )
        }

        assertThat(response.httpStatus).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.detail.detail)
            .contains("validering av distribusjonsforespørsel for journalpostId=453492547 feilet, feilmelding=")
    }

    private fun mockGodkjentKallMotDokDist() {
        stubFor(
            post("/rest/v1/distribuerjournalpost")
                .willReturn(okJson("""{"bestillingsId": "1234567"}""")),
        )
    }

    private fun badRequestResponse(): String {
        return Files.readString(ClassPathResource("dokdist/badrequest.json").file.toPath(), StandardCharsets.UTF_8)
    }

    companion object {

        private const val DOKDIST_URL = "/api/dist/v1"
        private const val JOURNALPOST_ID = "453492547"
    }
}
