package no.nav.tilleggsstonader.integrasjoner.dokarkiv

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.patch
import com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import no.nav.tilleggsstonader.integrasjoner.IntegrationTest
import no.nav.tilleggsstonader.integrasjoner.util.FileUtil
import no.nav.tilleggsstonader.integrasjoner.util.ProblemDetailUtil.catchProblemDetailException
import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentResponse
import no.nav.tilleggsstonader.kontrakter.dokarkiv.AvsenderMottaker
import no.nav.tilleggsstonader.kontrakter.dokarkiv.DokarkivBruker
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokument
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Filtype
import no.nav.tilleggsstonader.kontrakter.dokarkiv.LogiskVedleggRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.LogiskVedleggResponse
import no.nav.tilleggsstonader.kontrakter.dokarkiv.OppdaterJournalpostRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.OppdaterJournalpostResponse
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Sak
import no.nav.tilleggsstonader.kontrakter.felles.BrukerIdType
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.exchange

@TestPropertySource(properties = ["clients.dokarkiv.uri=http://localhost:28085"])
@AutoConfigureWireMock(port = 28085)
class DokarkivControllerTest : IntegrationTest() {

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(onBehalfOfToken())

        (LoggerFactory.getLogger("secureLogger") as Logger).addAppender(listAppender)
    }

    @Test
    fun `skal midlertidig journalføre dokument`() {
        stubFor(
            post("/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=false")
                .willReturn(okJson(gyldigDokarkivResponse())),
        )
        val body = ArkiverDokumentRequest(
            "FNR",
            false,
            listOf(HOVEDDOKUMENT),
            eksternReferanseId = "id",
            avsenderMottaker = AvsenderMottaker("fnr", BrukerIdType.FNR, "navn"),
        )

        val response: ResponseEntity<ArkiverDokumentResponse> =
            restTemplate.exchange(
                localhost(DOKARKIV_URL),
                HttpMethod.POST,
                HttpEntity(body, headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.journalpostId).isEqualTo("12345678")
        assertThat(response.body?.ferdigstilt!!).isFalse()
    }

    @Test
    fun `skal sende med navIdent fra header til journalpost`() {
        stubFor(
            post("/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=false")
                .willReturn(okJson(gyldigDokarkivResponse())),
        )
        val body = ArkiverDokumentRequest(
            "FNR",
            false,
            listOf(HOVEDDOKUMENT),
            eksternReferanseId = "id",
            avsenderMottaker = AvsenderMottaker("fnr", BrukerIdType.FNR, "navn"),
        )

        val response: ResponseEntity<ArkiverDokumentResponse> =
            restTemplate.exchange(
                localhost(DOKARKIV_URL),
                HttpMethod.POST,
                HttpEntity(body, headersWithNavUserId()),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.journalpostId).isEqualTo("12345678")
        assertThat(response.body?.ferdigstilt!!).isFalse()
        verify(postRequestedFor(anyUrl()).withHeader("Nav-User-Id", equalTo("k123123")))
    }

    @Test
    fun `skal returnere 409 ved 409 response fra dokarkiv`() {
        stubFor(
            post("/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=false")
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withStatus(409)
                        .withBody("Tekst fra body"),
                ),
        )
        val body = ArkiverDokumentRequest(
            "FNR",
            false,
            listOf(HOVEDDOKUMENT),
            eksternReferanseId = "id",
            avsenderMottaker = AvsenderMottaker("fnr", BrukerIdType.FNR, "navn"),
        )

        val response = catchProblemDetailException {
            restTemplate.exchange<ArkiverDokumentResponse>(
                localhost(DOKARKIV_URL),
                HttpMethod.POST,
                HttpEntity(body, headersWithNavUserId()),
            )
        }

        assertThat(response.httpStatus).isEqualTo(HttpStatus.CONFLICT)
        assertThat(response.detail.detail).isEqualTo("[Dokarkiv][Feil ved opprettelse av journalpost ][org.springframework.web.client.HttpClientErrorException\$Conflict]")
    }

    @Test
    fun `skal midlertidig journalføre dokument med vedlegg`() {
        stubFor(
            post("/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=false")
                .willReturn(okJson(gyldigDokarkivResponse())),
        )

        val body = ArkiverDokumentRequest(
            "FNR",
            false,
            listOf(HOVEDDOKUMENT),
            listOf(VEDLEGG),
            eksternReferanseId = "id",
            avsenderMottaker = AvsenderMottaker("fnr", BrukerIdType.FNR, "navn"),
        )

        val response: ResponseEntity<ArkiverDokumentResponse> =
            restTemplate.exchange(
                localhost(DOKARKIV_URL),
                HttpMethod.POST,
                HttpEntity(body, headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.journalpostId).isEqualTo("12345678")
        assertThat(response.body?.ferdigstilt!!).isFalse
    }

    @Test
    fun `dokarkiv returnerer 401`() {
        stubFor(
            post("/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=false")
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withStatus(401)
                        .withBody("Tekst fra body"),
                ),
        )
        val body = ArkiverDokumentRequest(
            "FNR",
            false,
            listOf(HOVEDDOKUMENT),
            eksternReferanseId = "id",
            avsenderMottaker = AvsenderMottaker("fnr", BrukerIdType.FNR, "navn"),
        )

        val exception = catchProblemDetailException {
            restTemplate.exchange<ArkiverDokumentResponse>(
                localhost(DOKARKIV_URL),
                HttpMethod.POST,
                HttpEntity(body, headers),
            )
        }

        assertThat(exception.httpStatus).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(exception.detail.detail).contains("Unauthorized")
    }

    @Test
    fun `oppdaterJournalpost returnerer OK`() {
        val journalpostId = "12345678"
        stubFor(
            put("/rest/journalpostapi/v1/journalpost/$journalpostId")
                .willReturn(okJson(gyldigDokarkivResponse())),
        )

        val body = OppdaterJournalpostRequest(
            bruker = DokarkivBruker(BrukerIdType.FNR, "12345678910"),
            tema = Tema.TSO,
            sak = Sak("11111111", "fagsaksystem"),
        )

        val response: ResponseEntity<OppdaterJournalpostResponse> =
            restTemplate.exchange(
                localhost("$DOKARKIV_URL/12345678"),
                HttpMethod.PUT,
                HttpEntity(body, headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.journalpostId).isEqualTo("12345678")
    }

    @Test
    fun `dokarkiv skal logge detaljert feilmelding til secureLogger ved HttpServerErrorExcetion`() {
        val journalpostId = "12345678"
        stubFor(
            put("/rest/journalpostapi/v1/journalpost/$journalpostId")
                .willReturn(
                    serverError()
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody(gyldigDokarkivResponse(500)),
                ),
        )

        val body = OppdaterJournalpostRequest(
            bruker = DokarkivBruker(BrukerIdType.FNR, "12345678910"),
            tema = Tema.TSO,
            sak = Sak("11111111", "fagsaksystem"),
        )

        val exception = catchProblemDetailException {
            restTemplate.exchange<OppdaterJournalpostResponse>(
                localhost("$DOKARKIV_URL/12345678"),
                HttpMethod.PUT,
                HttpEntity(body, headers),
            )
        }

        assertThat(exception.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(loggingEvents)
            .extracting<String, RuntimeException> { obj: ILoggingEvent -> obj.formattedMessage }
            .anyMatch { message -> message.contains("Fant ikke person med ident: 12345678910") }
    }

    @Test
    fun `ferdigstill returnerer ok`() {
        stubFor(
            patch(urlEqualTo("/rest/journalpostapi/v1/journalpost/123/ferdigstill"))
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody("Journalpost ferdigstilt"),
                ),
        )

        val response: ResponseEntity<Map<String, String>> =
            restTemplate.exchange(
                localhost("$DOKARKIV_URL/123/ferdigstill?journalfoerendeEnhet=9999"),
                HttpMethod.PUT,
                HttpEntity(null, headersWithNavUserId()),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        verify(patchRequestedFor(anyUrl()).withHeader("Nav-User-Id", equalTo("k123123")))
    }

    @Test
    fun `ferdigstill returnerer 400 hvis ikke mulig ferdigstill`() {
        stubFor(
            patch(urlEqualTo("/rest/journalpostapi/v1/journalpost/123/ferdigstill"))
                .willReturn(aResponse().withStatus(400)),
        )

        val exception = catchProblemDetailException {
            restTemplate.exchange<Map<String, String>>(
                localhost("$DOKARKIV_URL/123/ferdigstill?journalfoerendeEnhet=9999"),
                HttpMethod.PUT,
                HttpEntity(null, headers),
            )
        }

        assertThat(exception.httpStatus).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(exception.detail.detail).contains("Kan ikke ferdigstille journalpost 123")
    }

    @Test
    fun `skal opprette logisk vedlegg`() {
        stubFor(
            post("/rest/journalpostapi/v1/dokumentInfo/321/logiskVedlegg/")
                .willReturn(okJson(objectMapper.writeValueAsString(LogiskVedleggResponse(21L)))),
        )

        val response = restTemplate.exchange<LogiskVedleggResponse>(
            localhost("$DOKARKIV_URL/dokument/321/logiskVedlegg"),
            HttpMethod.POST,
            HttpEntity(LogiskVedleggRequest("Ny tittel"), headers),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.logiskVedleggId).isEqualTo(21L)
    }

    @Test
    fun `skal returnere feil hvis man ikke kan opprette logisk vedlegg`() {
        stubFor(
            post("/rest/journalpostapi/v1/dokumentInfo/321/logiskVedlegg/")
                .willReturn(aResponse().withStatus(404).withBody("melding fra klient")),
        )

        val exception = catchProblemDetailException {
            restTemplate.exchange<LogiskVedleggResponse>(
                localhost("$DOKARKIV_URL/dokument/321/logiskVedlegg"),
                HttpMethod.POST,
                HttpEntity(LogiskVedleggRequest("Ny tittel"), headers),
            )
        }

        assertThat(exception.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(exception.detail.detail).contains("melding fra klient")
    }

    @Test
    fun `skal slette logisk vedlegg`() {
        stubFor(
            delete("/rest/journalpostapi/v1/dokumentInfo/321/logiskVedlegg/432")
                .willReturn(ok().withBody("Logiskt vedlegg slettet")),
        )

        val response: ResponseEntity<LogiskVedleggResponse> =
            restTemplate.exchange(
                localhost("$DOKARKIV_URL/dokument/321/logiskVedlegg/432"),
                HttpMethod.DELETE,
                HttpEntity(LogiskVedleggRequest("Ny tittel"), headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.logiskVedleggId).isEqualTo(432L)
    }

    @Test
    fun `skal returnere feil hvis man ikke kan slette logisk vedlegg`() {
        stubFor(
            delete("/rest/journalpostapi/v1/dokumentInfo/321/logiskVedlegg/432")
                .willReturn(aResponse().withStatus(404).withBody("sletting feilet")),
        )

        val exception = catchProblemDetailException {
            restTemplate.exchange<LogiskVedleggResponse>(
                localhost("$DOKARKIV_URL/dokument/321/logiskVedlegg/432"),
                HttpMethod.DELETE,
                HttpEntity(LogiskVedleggRequest("Ny tittel"), headers),
            )
        }

        assertThat(exception.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(exception.detail.detail).contains("sletting feilet")
    }

    private fun gyldigDokarkivResponse(statusKode: Int? = null): String {
        return FileUtil.readFile("dokarkiv/gyldig${statusKode ?: ""}response.json")
    }

    private fun headersWithNavUserId(): HttpHeaders {
        return headers.apply {
            add("Nav-User-Id", NAV_USER_ID_VALUE)
        }
    }

    companion object {

        private const val DOKARKIV_URL = "/api/arkiv"

        private const val NAV_USER_ID_VALUE = "k123123"

        private val HOVEDDOKUMENT =
            Dokument(
                "foo".toByteArray(),
                Filtype.JSON,
                "filnavn",
                null,
                Dokumenttype.BARNETILSYN_SØKNAD,
            )

        private val VEDLEGG =
            Dokument(
                "foo".toByteArray(),
                Filtype.PDFA,
                "filnavn",
                "Vedlegg",
                Dokumenttype.BARNETILSYN_SØKNAD_VEDLEGG,
            )
    }
}
