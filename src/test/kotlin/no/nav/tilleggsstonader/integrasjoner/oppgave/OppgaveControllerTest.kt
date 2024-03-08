package no.nav.tilleggsstonader.integrasjoner.oppgave

import ch.qos.logback.classic.Logger
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.exactly
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.patch
import com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import no.nav.tilleggsstonader.integrasjoner.IntegrationTest
import no.nav.tilleggsstonader.integrasjoner.infrastruktur.exception.ApiExceptionHandler
import no.nav.tilleggsstonader.integrasjoner.util.FileUtil.readFile
import no.nav.tilleggsstonader.integrasjoner.util.ProblemDetailUtil.catchProblemDetailException
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnMappeResponseDto
import no.nav.tilleggsstonader.kontrakter.oppgave.IdentGruppe
import no.nav.tilleggsstonader.kontrakter.oppgave.MappeDto
import no.nav.tilleggsstonader.kontrakter.oppgave.OppdatertOppgaveResponse
import no.nav.tilleggsstonader.kontrakter.oppgave.Oppgave
import no.nav.tilleggsstonader.kontrakter.oppgave.OppgaveIdentV2
import no.nav.tilleggsstonader.kontrakter.oppgave.OppgaveResponse
import no.nav.tilleggsstonader.kontrakter.oppgave.Oppgavetype
import no.nav.tilleggsstonader.kontrakter.oppgave.OpprettOppgaveRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.StatusEnum
import no.nav.tilleggsstonader.libs.log.SecureLogger.secureLogger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.exchange
import java.time.LocalDate
import java.util.Optional

@TestPropertySource(properties = ["clients.oppgave.uri=http://localhost:28085"])
@AutoConfigureWireMock(port = 28085)
class OppgaveControllerTest : IntegrationTest() {

    val oppgave = Oppgave(id = OPPGAVE_ID, versjon = 0)

    private val responseOk = aResponse().withStatus(201).withHeader("Content-Type", "application/json")
        .withBody(readFile("oppgave/oppgave.json"))

    private val responseFerdigstilt = aResponse().withStatus(201).withHeader("Content-Type", "application/json")
        .withBody(readFile("oppgave/ferdigstilt_oppgave.json"))

    @BeforeEach
    fun setup() {
        listOf(OppgaveController::class, OppgaveService::class, ApiExceptionHandler::class).forEach {
            (LoggerFactory.getLogger(it.java) as Logger).addAppender(listAppender)
        }
        (secureLogger as Logger).addAppender(listAppender)
        headers.setBearerAuth(onBehalfOfToken())
    }

    @Test
    fun `finnMapper med gyldig query returnerer mapper uten tema som skal filtreres bort`() {
        val mapper = listOf(
            MappeDto(1, "112", "4489"),
            MappeDto(2, "132", "4489"),
            MappeDto(id = 3, navn = "123", enhetsnr = "4489", tema = "PEN"),
        )
        stubFor(
            get(GET_MAPPER_URL).willReturn(okJson(objectMapper.writeValueAsString(FinnMappeResponseDto(3, mapper)))),
        )

        val response: ResponseEntity<FinnMappeResponseDto> = restTemplate.exchange(
            localhost("/api/oppgave/mappe/sok?enhetsnr=1234567891011&opprettetFom=dcssdf&limit=50"),
            HttpMethod.GET,
            HttpEntity(null, headers),
        )

        assertThat(response.body?.antallTreffTotalt).isEqualTo(2)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `skal patche oppgave med ekstra beskrivelse, returnere oppgaveid og 200 OK`() {
        stubFor(get(GET_OPPGAVE_URL).willReturn(okJson(readFile("oppgave/oppgave.json"))))

        stubFor(patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID")).willReturn(responseFerdigstilt))

        val oppgave = Oppgave(
            id = OPPGAVE_ID,
            versjon = 0,
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = EKSTRA_BESKRIVELSE,
            tema = null,
        )

        val response = patchOppgave(oppgave)
        assertThat(response.body?.oppgaveId).isEqualTo(OPPGAVE_ID)
        assertThat(response.body?.versjon).isEqualTo(1)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `skal opprette oppgave med mappeId, returnere oppgaveid og 201 Created`() {
        stubFor(
            post("/api/v1/oppgaver").willReturn(okJson(objectMapper.writeValueAsString(oppgave))),
        )

        val opprettOppgave = OpprettOppgaveRequest(
            ident = OppgaveIdentV2(ident = "123456789012", gruppe = IdentGruppe.AKTOERID),
            fristFerdigstillelse = LocalDate.now().plusDays(3),
            behandlingstema = "behandlingstema",
            enhetsnummer = "enhetsnummer",
            tema = Tema.TSO,
            oppgavetype = Oppgavetype.BehandleSak,
            mappeId = 1234L,
            beskrivelse = "Oppgavetekst",
        )
        val response: ResponseEntity<OppgaveResponse> = opprettOppgave(opprettOppgave)

        assertThat(response.body?.oppgaveId).isEqualTo(OPPGAVE_ID)
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
    }

    @Test
    fun `skal opprette oppgave uten ident, returnere oppgaveid og 201 Created`() {
        stubFor(post("/api/v1/oppgaver").willReturn(okJson(objectMapper.writeValueAsString(oppgave))))

        val opprettOppgave = OpprettOppgaveRequest(
            ident = null,
            fristFerdigstillelse = LocalDate.now().plusDays(3),
            behandlingstema = "behandlingstema",
            enhetsnummer = "enhetsnummer",
            tema = Tema.TSO,
            oppgavetype = Oppgavetype.BehandleSak,
            beskrivelse = "Oppgavetekst",
        )
        val response: ResponseEntity<OppgaveResponse> = opprettOppgave(opprettOppgave)

        assertThat(response.body?.oppgaveId).isEqualTo(OPPGAVE_ID)
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
    }

    @Test
    fun `kall mot oppgave ved opprett feiler med bad request, tjenesten vår returernerer 500 og med info om feil i response `() {
        stubFor(
            post("/api/v1/oppgaver").willReturn(
                aResponse().withStatus(400).withHeader("Content-Type", "application/json").withBody("body"),
            ),
        )
        val opprettOppgave = OpprettOppgaveRequest(
            ident = OppgaveIdentV2(ident = "123456789012", gruppe = IdentGruppe.AKTOERID),
            fristFerdigstillelse = LocalDate.now().plusDays(3),
            behandlingstema = "behandlingstema",
            enhetsnummer = "enhetsnummer",
            tema = Tema.TSO,
            oppgavetype = Oppgavetype.BehandleSak,
            beskrivelse = "Oppgavetekst",
        )
        val exception = catchProblemDetailException {
            restTemplate.exchange<OppgaveResponse>(
                localhost(OPPRETT_OPPGAVE_URL_V2),
                HttpMethod.POST,
                HttpEntity(opprettOppgave, headers),
            )
        }
        assertThat(exception.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(exception.detail.detail).contains("Feil ved oppretting av oppgave for 123456789012. Response fra oppgave = body")
    }

    @Test
    fun `Ferdigstilling av oppgave som er alt ferdigstillt skal logge og returnerer 200 OK`() {
        stubFor(
            get("/api/v1/oppgaver/123").willReturn(
                okJson(objectMapper.writeValueAsString(oppgave.copy(status = StatusEnum.FERDIGSTILT))),
            ),
        )

        verify(exactly(0), patchRequestedFor(urlEqualTo("/api/v1/oppgaver/123")))

        val response: ResponseEntity<OppgaveResponse> = restTemplate.exchange(
            localhost("/api/oppgave/123/ferdigstill"),
            HttpMethod.PATCH,
            HttpEntity(null, headers),
        )

        assertThat(response.body?.oppgaveId).isEqualTo(123)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Ferdigstilling av oppgave som er feilregistrert skal generere en oppslagsfeil`() {
        val oppgave = oppgave.copy(status = StatusEnum.FEILREGISTRERT)
        stubFor(
            get("/api/v1/oppgaver/$OPPGAVE_ID").willReturn(
                okJson(objectMapper.writeValueAsString(oppgave)),
            ),
        )

        val exception = catchProblemDetailException {
            restTemplate.exchange<OppgaveResponse>(
                localhost("/api/oppgave/$OPPGAVE_ID/ferdigstill"),
                HttpMethod.PATCH,
                HttpEntity(null, headers),
            )
        }

        assertThat(exception.httpStatus).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(exception.detail.detail).contains("Oppgave har status feilregistrert og kan ikke oppdateres")
    }

    @Test
    fun `Ferdigstilling av oppgave som er i status opprettet skal gjøre et patch kall mot oppgave med status FERDIGSTILL`() {
        val oppgave = oppgave.copy(status = StatusEnum.OPPRETTET)
        stubFor(
            get("/api/v1/oppgaver/$OPPGAVE_ID").willReturn(
                okJson(objectMapper.writeValueAsString(oppgave)),
            ),
        )

        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID")).withRequestBody(matchingJsonPath("$.[?(@.status == 'FERDIGSTILT')]"))
                .willReturn(
                    aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(
                        objectMapper.writeValueAsBytes(
                            Oppgave(
                                id = OPPGAVE_ID,
                                versjon = 0,
                                status = StatusEnum.FERDIGSTILT,
                            ),
                        ),
                    ),
                ),
        )

        val response: ResponseEntity<OppgaveResponse> = restTemplate.exchange(
            localhost("/api/oppgave/$OPPGAVE_ID/ferdigstill"),
            HttpMethod.PATCH,
            HttpEntity(null, headers),
        )

        assertThat(response.body?.oppgaveId).isEqualTo(OPPGAVE_ID)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Nested
    inner class OptionalHåndtering {

        @BeforeEach
        fun setUp() {
            stubFor(patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID")).willReturn(responseOk))
        }

        @Test
        fun `skal sende mappeId med verdi hvis den har verdi`() {
            val oppgave = oppgave.copy(mappeId = Optional.of(100))

            val response = patchOppgave(oppgave)
            assertThat(response.body?.oppgaveId).isEqualTo(OPPGAVE_ID)
            assertThat(response.body?.versjon).isEqualTo(1)
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

            val expectedJson = """{"id" : 315488374, "versjon": 0, "mappeId" : 100}"""
            verify(patchRequestedFor(anyUrl()).withRequestBody(equalToJson(expectedJson)))
        }

        @Test
        fun `skal ikke sende mappeId hvis den er null`() {
            val oppgave = oppgave.copy(mappeId = null)

            val response = patchOppgave(oppgave)
            assertThat(response.body?.oppgaveId).isEqualTo(OPPGAVE_ID)
            assertThat(response.body?.versjon).isEqualTo(1)
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

            val expectedJson = """{"id" : 315488374, "versjon": 0}"""
            verify(patchRequestedFor(anyUrl()).withRequestBody(equalToJson(expectedJson)))
        }

        @Test
        fun `skal sende optional mappeId som null hvis den er empty`() {
            val oppgave = oppgave.copy(mappeId = Optional.empty())

            val response = patchOppgave(oppgave)
            assertThat(response.body?.oppgaveId).isEqualTo(OPPGAVE_ID)
            assertThat(response.body?.versjon).isEqualTo(1)
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

            val expectedJson = """{"id" : 315488374, "versjon": 0, "mappeId" : null}"""
            verify(patchRequestedFor(anyUrl()).withRequestBody(equalToJson(expectedJson)))
        }
    }

    @Test
    fun `fordelOppgave skal tilordne oppgave til saksbehandler når saksbehandler er satt på requesten`() {
        val saksbehandlerId = "Z999999"
        stubFor(get(GET_OPPGAVE_URL).willReturn(okJson(objectMapper.writeValueAsString(oppgave))))
        stubFor(patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID")).willReturn(responseOk))

        val response: ResponseEntity<Oppgave> = restTemplate.exchange(
            localhost("/api/oppgave/$OPPGAVE_ID/fordel?saksbehandler=$saksbehandlerId"),
            HttpMethod.POST,
            HttpEntity(null, headers),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.id).isEqualTo(OPPGAVE_ID)
    }

    @Test
    fun `fordelOppgave skal tilbakestille tilordning på oppgave når saksbehandler ikke er satt på requesten`() {
        stubFor(get(GET_OPPGAVE_URL).willReturn(okJson(objectMapper.writeValueAsString(oppgave))))
        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID")).willReturn(responseOk),
        )

        val response: ResponseEntity<Oppgave> = restTemplate.exchange(
            localhost("/api/oppgave/$OPPGAVE_ID/fordel"),
            HttpMethod.POST,
            HttpEntity(null, headers),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.id).isEqualTo(OPPGAVE_ID)
    }

    @Test
    fun `fordelOppgave skal returnere feil når oppgaven er ferdigstilt`() {
        val oppgave = oppgave.copy(status = StatusEnum.FERDIGSTILT)
        stubFor(
            get(GET_OPPGAVE_URL).willReturn(
                okJson(objectMapper.writeValueAsString(oppgave)),
            ),
        )
        stubFor(patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID")).willReturn(responseOk))

        val exception = catchProblemDetailException {
            restTemplate.exchange<Oppgave>(
                localhost("/api/oppgave/$OPPGAVE_ID/fordel?saksbehandler=Z999999"),
                HttpMethod.POST,
                HttpEntity(null, headers),
            )
        }

        assertThat(exception.httpStatus).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(exception.detail.detail).isEqualTo("[Oppgave.fordelOppgave][Kan ikke fordele oppgave=$OPPGAVE_ID som allerede er ferdigstilt]")
    }

    @Test
    fun `Skal hente oppgave basert på id`() {
        stubFor(
            get(GET_OPPGAVE_URL).willReturn(okJson(objectMapper.writeValueAsString(oppgave))),
        )

        val response: ResponseEntity<Oppgave> =
            restTemplate.exchange(localhost("/api/oppgave/$OPPGAVE_ID"), HttpMethod.GET, HttpEntity(null, headers))

        println(response.body)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.id).isEqualTo(OPPGAVE_ID)
    }

    @Test
    fun `Skal returnere 409 dersom man oppdaterer oppgave med feil versjon`() {
        stubFor(get("/api/v1/oppgaver/$OPPGAVE_ID").willReturn(okJson(readFile("oppgave/hentOppgave.json"))))
        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID")).willReturn(
                aResponse().withStatus(409).withHeader("Content-Type", "application/json")
                    .withBody(""""{uuid":"123","feilmelding":"Versjonskonflikt ved forespørsel om endring av oppgave med id"} """),
            ),
        )

        val oppgave = Oppgave(
            id = OPPGAVE_ID,
            versjon = 0,
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = EKSTRA_BESKRIVELSE,
            tema = null,
        )

        val exception = catchProblemDetailException {
            restTemplate.exchange<Oppgave>(
                localhost("$OPPGAVE_URL/$OPPGAVE_ID/fordel?saksbehandler=test123&versjon=1"),
                HttpMethod.POST,
                HttpEntity(oppgave, headers),
            )
        }
        assertThat(exception.httpStatus).isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    fun `Fjern behandlesAvApplikasjon på oppgave`() {
        stubFor(get("/api/v1/oppgaver/$OPPGAVE_ID").willReturn(okJson(readFile("oppgave/hentOppgave.json"))))

        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID")).withRequestBody(
                equalToJson("""{"id":315488374, "versjon":1,"behandlesAvApplikasjon":null}"""),
            ).willReturn(responseFerdigstilt),
        )

        val oppgave = Oppgave(
            id = OPPGAVE_ID,
            versjon = 0,
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = EKSTRA_BESKRIVELSE,
            tema = null,
        )

        val response: ResponseEntity<OppgaveResponse> = restTemplate.exchange(
            localhost("$OPPGAVE_URL/$OPPGAVE_ID/fjern-behandles-av-applikasjon?versjon=1"),
            HttpMethod.PATCH,
            HttpEntity(oppgave, headers),
        )
        assertThat(response.body?.oppgaveId).isEqualTo(OPPGAVE_ID)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Fjern behandlesAvApplikasjon skal returnere httpstatus 409 ved feil versjonsnummer`() {
        stubFor(get("/api/v1/oppgaver/$OPPGAVE_ID").willReturn(okJson(readFile("oppgave/hentOppgave.json"))))

        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID")).withRequestBody(
                equalToJson("""{"id":315488374, "versjon":1,"behandlesAvApplikasjon":null}"""),
            ).willReturn(
                aResponse()
                    .withStatus(409)
                    .withHeader("Content-Type", "application/json")
                    .withBody("Feil versjonsnummer"),
            ),
        )

        val oppgave = Oppgave(
            id = OPPGAVE_ID,
            versjon = 0,
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = EKSTRA_BESKRIVELSE,
            tema = null,
        )

        val exception = catchProblemDetailException {
            restTemplate.exchange<OppgaveResponse>(
                localhost("$OPPGAVE_URL/$OPPGAVE_ID/fjern-behandles-av-applikasjon?versjon=1"),
                HttpMethod.PATCH,
                HttpEntity(oppgave, headers),
            )
        }
        assertThat(exception.httpStatus).isEqualTo(HttpStatus.CONFLICT)
    }

    private fun opprettOppgave(opprettOppgave: OpprettOppgaveRequest): ResponseEntity<OppgaveResponse> {
        return restTemplate.exchange<OppgaveResponse>(
            localhost(OPPRETT_OPPGAVE_URL_V2),
            HttpMethod.POST,
            HttpEntity(opprettOppgave, headers),
        )
    }

    private fun patchOppgave(oppgave: Oppgave): ResponseEntity<OppdatertOppgaveResponse> {
        return restTemplate.exchange<OppdatertOppgaveResponse>(
            localhost(PATCH_OPPGAVE_URL),
            HttpMethod.PATCH,
            HttpEntity(oppgave, headers),
        )
    }

    companion object {

        private const val OPPGAVE_URL = "/api/oppgave"
        private const val OPPRETT_OPPGAVE_URL_V2 = "/api/oppgave/opprett"
        private const val OPPGAVE_ID = 315488374L
        private const val PATCH_OPPGAVE_URL = "$OPPGAVE_URL/$OPPGAVE_ID/oppdater"
        private const val GET_MAPPER_URL = "/api/v1/mapper?enhetsnr=1234567891011&opprettetFom=dcssdf&limit=50"
        private const val GET_OPPGAVE_URL = "/api/v1/oppgaver/$OPPGAVE_ID"
        private const val EKSTRA_BESKRIVELSE = " Ekstra beskrivelse"
    }
}
