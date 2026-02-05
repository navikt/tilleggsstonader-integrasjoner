package no.nav.tilleggsstonader.integrasjoner.journalpost

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import no.nav.tilleggsstonader.integrasjoner.IntegrationTest
import no.nav.tilleggsstonader.integrasjoner.util.FileUtil.readFile
import no.nav.tilleggsstonader.integrasjoner.util.graphqlCompatible
import no.nav.tilleggsstonader.kontrakter.felles.Arkivtema
import no.nav.tilleggsstonader.kontrakter.felles.BrukerIdType
import no.nav.tilleggsstonader.kontrakter.journalpost.Bruker
import no.nav.tilleggsstonader.kontrakter.journalpost.Journalpost
import no.nav.tilleggsstonader.kontrakter.journalpost.JournalposterForBrukerRequest
import no.nav.tilleggsstonader.kontrakter.journalpost.Journalposttype
import no.nav.tilleggsstonader.kontrakter.journalpost.Journalstatus
import no.nav.tilleggsstonader.kontrakter.journalpost.Utsendingsmåte
import no.nav.tilleggsstonader.kontrakter.journalpost.VarselType
import no.nav.tilleggsstonader.libs.test.httpclient.ProblemDetailUtil.catchProblemDetailException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.exchange
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDateTime

@TestPropertySource(properties = ["clients.saf.uri=http://localhost:28085"])
class HentJournalpostControllerTest : IntegrationTest() {
    private val testLogger = LoggerFactory.getLogger(HentJournalpostController::class.java) as Logger

    private lateinit var uriHentSaksnummer: String
    private lateinit var uriHentJournalpost: String
    private lateinit var uriHentDokument: String

    @BeforeEach
    fun setUp() {
        testLogger.addAppender(listAppender)
        headers.setBearerAuth(onBehalfOfToken())
        uriHentSaksnummer =
            UriComponentsBuilder
                .fromUriString(localhost(JOURNALPOST_BASE_URL) + "/sak")
                .queryParam("journalpostId", JOURNALPOST_ID)
                .toUriString()
        uriHentJournalpost =
            UriComponentsBuilder
                .fromUriString(localhost(JOURNALPOST_BASE_URL))
                .queryParam("journalpostId", JOURNALPOST_ID)
                .toUriString()
        uriHentDokument = localhost(JOURNALPOST_BASE_URL) + "/hentdokument/$JOURNALPOST_ID/$DOKUMENTINFO_ID"
    }

    @Test
    fun `hent saksnummer skal returnere saksnummer og status ok`() {
        stubGraphqlEndpoint("saf/gyldigsakresponse.json", expectedRequestBody = gyldigJournalPostIdRequest())

        val response =
            restTemplate.exchange<Map<String, String>>(
                uriHentSaksnummer,
                HttpMethod.GET,
                HttpEntity<String>(headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.get("saksnummer")).isEqualTo(SAKSNUMMER)
    }

    @Test
    fun `hent journalpost skal returnere journalpost og status ok`() {
        stubGraphqlEndpoint("saf/gyldigjournalpostresponse.json", expectedRequestBody = gyldigJournalPostIdRequest())

        val response =
            restTemplate.exchange<Journalpost>(
                uriHentJournalpost,
                HttpMethod.GET,
                HttpEntity<String>(headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.journalposttype).isEqualTo(Journalposttype.I)
        assertThat(response.body?.journalstatus).isEqualTo(Journalstatus.JOURNALFOERT)
        assertThat(response.body?.datoMottatt).isEqualTo(LocalDateTime.of(2020, 3, 26, 1, 0))
    }

    @Test
    fun `hent journalpostForBruker skal returnere journalposter og status ok`() {
        stubGraphqlEndpoint("saf/gyldigJournalposterResponse.json")

        val request =
            JournalposterForBrukerRequest(
                brukerId = Bruker("12345678901", BrukerIdType.FNR),
                antall = 10,
                tema = listOf(Arkivtema.TSO),
                journalposttype = listOf(Journalposttype.I),
                journalstatus = emptyList(),
            )
        val response =
            restTemplate.exchange<List<Journalpost>>(
                uriHentJournalpost,
                HttpMethod.POST,
                HttpEntity(request, headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.first()?.journalposttype).isEqualTo(Journalposttype.I)
        assertThat(response.body?.first()?.journalstatus).isEqualTo(Journalstatus.JOURNALFOERT)
        assertThat(response.body?.first()?.datoMottatt).isEqualTo(LocalDateTime.parse("2020-01-31T08:00:17"))
        val utsendingsinfo =
            response.body?.find { it.utsendingsinfo != null }?.utsendingsinfo
                ?: error("Finner ikke utsendingsinfo på noen journalposter")
        assertThat(utsendingsinfo.utsendingsmåter).hasSize(1)
        assertThat(utsendingsinfo.utsendingsmåter).contains(Utsendingsmåte.DIGITAL_POST)
        assertThat(utsendingsinfo.digitalpostSendt?.adresse).isEqualTo("0000487236")
        assertThat(utsendingsinfo.fysiskpostSendt).isNull()
        assertThat(utsendingsinfo.varselSendt).hasSize(1)
        assertThat(utsendingsinfo.varselSendt.first().type).isEqualTo(VarselType.SMS)
    }

    @Test
    fun `hent dokument skal returnere dokument og status ok`() {
        stubFor(
            get("/rest/hentdokument/$JOURNALPOST_ID/$DOKUMENTINFO_ID/ARKIV")
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/pdf")
                        .withBody("pdf".toByteArray()),
                ),
        )

        val response =
            restTemplate.exchange<ByteArray>(
                uriHentDokument,
                HttpMethod.GET,
                HttpEntity<String>(headers),
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(String(response.body!!)).isEqualTo("pdf")
    }

    @Test
    fun `hent saksnummer skal returnere status 404 hvis sak mangler`() {
        stubGraphqlEndpoint("saf/mangler_sak.json")

        val exception =
            catchProblemDetailException {
                restTemplate.exchange<Map<String, String>>(
                    uriHentSaksnummer,
                    HttpMethod.GET,
                    HttpEntity<String>(headers),
                )
            }

        assertThat(exception.httpStatus).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(exception.detail.detail).isEqualTo("Sak mangler for journalpostId=$JOURNALPOST_ID")
    }

    @Test
    fun `hent saksnummer skal returnere status 404 hvis sak ikke er gsak`() {
        stubGraphqlEndpoint("saf/feil_arkivsaksystem.json")

        val exception =
            catchProblemDetailException {
                restTemplate.exchange<Map<String, String>>(
                    uriHentSaksnummer,
                    HttpMethod.GET,
                    HttpEntity<String>(headers),
                )
            }

        assertThat(exception.httpStatus).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(exception.detail.detail).isEqualTo("Sak mangler for journalpostId=$JOURNALPOST_ID")
    }

    @Test
    fun `hent saksnummer skal returnerer 500 hvis klient returnerer 200 med feilmeldinger`() {
        stubGraphqlEndpoint("saf/error_fra_saf.json")

        val exception =
            catchProblemDetailException {
                restTemplate.exchange<Map<String, String>>(
                    uriHentSaksnummer,
                    HttpMethod.GET,
                    HttpEntity<String>(headers),
                )
            }

        assertThat(exception.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(exception.detail.detail)
            .contains(
                "Feil ved henting av journalpost=12345678 klientfeilmelding=Kan ikke hente journalpost " +
                    "[SafError(message=Feilet ved henting av data (/journalpost) : null, " +
                    "extensions=SafExtension(code=server_error, classification=DataFetchingException))]",
            )
        assertThat(loggingEvents)
            .extracting<Level, RuntimeException> { obj: ILoggingEvent -> obj.level }
            .containsExactly(Level.WARN)
    }

    @Test
    fun `hent saksnummer skal returnere 500 ved ukjent feil`() {
        stubFor(
            post("/graphql")
                .willReturn(serverError().withBody("feilmelding")),
        )

        val exception =
            catchProblemDetailException {
                restTemplate.exchange<Map<String, String>>(
                    uriHentSaksnummer,
                    HttpMethod.GET,
                    HttpEntity<String>(headers),
                )
            }

        assertThat(exception.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(exception.detail.detail).contains("Feil ved henting av journalpost")
        assertThat(loggingEvents)
            .extracting<Level, RuntimeException> { obj: ILoggingEvent -> obj.level }
            .containsExactly(Level.WARN)
    }

    private fun stubGraphqlEndpoint(
        responseFilnavn: String,
        expectedRequestBody: String? = null,
    ) {
        stubFor(
            post("/graphql")
                .apply {
                    if (expectedRequestBody != null) {
                        this.withRequestBody(equalTo(expectedRequestBody))
                    }
                }.willReturn(okJson(readFile(responseFilnavn))),
        )
    }

    private fun gyldigJournalPostIdRequest(): String =
        readFile("saf/gyldigJournalpostIdRequest.json")
            .replace(
                "GRAPHQL-PLACEHOLDER",
                readFile("saf/journalpostForId.graphql").graphqlCompatible(),
            )

    private fun gyldigBrukerRequest(): String =
        readFile("saf/gyldigBrukerRequest.json")
            .replace(
                "GRAPHQL-PLACEHOLDER",
                readFile("saf/journalposterForBruker.graphql").graphqlCompatible(),
            )

    companion object {
        const val JOURNALPOST_ID = "12345678"
        const val DOKUMENTINFO_ID = "123456789"
        const val SAKSNUMMER = "87654321"
        const val JOURNALPOST_BASE_URL = "/api/journalpost"
    }
}
