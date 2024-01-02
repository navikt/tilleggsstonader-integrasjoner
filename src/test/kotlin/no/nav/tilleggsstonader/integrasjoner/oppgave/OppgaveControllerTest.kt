package no.nav.tilleggsstonader.integrasjoner.oppgave

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.exactly
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.patch
import com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import com.github.tomakehurst.wiremock.client.WireMock.status
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
import no.nav.tilleggsstonader.kontrakter.oppgave.Oppgave
import no.nav.tilleggsstonader.kontrakter.oppgave.OppgaveIdentV2
import no.nav.tilleggsstonader.kontrakter.oppgave.OppgaveResponse
import no.nav.tilleggsstonader.kontrakter.oppgave.Oppgavetype
import no.nav.tilleggsstonader.kontrakter.oppgave.OpprettOppgaveRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.StatusEnum
import no.nav.tilleggsstonader.libs.log.SecureLogger.secureLogger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
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

@TestPropertySource(properties = ["clients.oppgave.uri=http://localhost:28085"])
@AutoConfigureWireMock(port = 28085)
class OppgaveControllerTest : IntegrationTest() {

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
            get(GET_MAPPER_URL)
                .willReturn(okJson(objectMapper.writeValueAsString(FinnMappeResponseDto(3, mapper)))),
        )

        val response: ResponseEntity<FinnMappeResponseDto> =
            restTemplate.exchange(
                localhost("/api/oppgave/mappe/sok?enhetsnr=1234567891011&opprettetFom=dcssdf&limit=50"),
                HttpMethod.GET,
                HttpEntity(null, headers),
            )

        assertThat(response.body?.antallTreffTotalt).isEqualTo(2)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `skal logge stack trace og returnere internal server error ved IllegalStateException`() {
        stubFor(get(GET_OPPGAVE_URL).willReturn(serverError()))

        val oppgave = Oppgave(
            id = OPPGAVE_ID,
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = "test NPE",
            tema = Tema.TSO,
        )

        val response = catchProblemDetailException {
            restTemplate.exchange<Map<String, Long>>(
                localhost(OPPDATER_OPPGAVE_URL),
                HttpMethod.POST,
                HttpEntity(oppgave, headers),
            )
        }

        assertThat(response.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(loggingEvents)
            .extracting<String, RuntimeException> { obj: ILoggingEvent -> obj.formattedMessage }
            .anyMatch { s -> s.contains("Feil ved kall method=GET mot url=http://localhost:28085/api/v1/oppgaver/315488374") }
    }

    @Test
    fun `skal logge og returnere internal server error ved restClientException`() {
        stubFor(get(GET_OPPGAVE_URL).willReturn(status(404)))

        val oppgave = Oppgave(
            id = OPPGAVE_ID,
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = "test RestClientException",
            tema = Tema.TSO,
        )

        val response = catchProblemDetailException {
            restTemplate.exchange<Map<String, Long>>(
                localhost(OPPDATER_OPPGAVE_URL),
                HttpMethod.POST,
                HttpEntity(oppgave, headers),
            )
        }

        assertThat(loggingEvents)
            .extracting<String, RuntimeException> { obj: ILoggingEvent -> obj.formattedMessage }
            .anyMatch { it.contains("Finner ikke oppgave med id") }
        assertThat(response.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.detail.detail).contains("Finner ikke oppgave med id")
    }

    @Test
    fun `skal ignorere oppdatering hvis oppgave er ferdigstilt`() {
        stubFor(get(GET_OPPGAVE_URL).willReturn(okJson(readFile("oppgave/ferdigstilt_oppgave.json"))))

        val oppgave = Oppgave(
            id = OPPGAVE_ID,
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = "test oppgave ikke funnet",
            tema = null,
        )

        val response: ResponseEntity<Map<String, Long>> = restTemplate.exchange(
            localhost(OPPDATER_OPPGAVE_URL),
            HttpMethod.POST,
            HttpEntity(oppgave, headers),
        )

        assertThat(loggingEvents).extracting<String, RuntimeException> { obj: ILoggingEvent -> obj.formattedMessage }
            .anyMatch {
                it.contains(
                    "Ignorerer oppdatering av oppgave som er ferdigstilt for aktørId=1234567891011 " +
                        "journalpostId=123456789 oppgaveId=$OPPGAVE_ID",
                )
            }
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `skal oppdatere oppgave med ekstra beskrivelse, returnere oppgaveid og 200 OK`() {
        stubFor(get(GET_OPPGAVE_URL).willReturn(okJson(readFile("oppgave/oppgave.json"))))

        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .withRequestBody(matchingJsonPath("$.[?(@.beskrivelse == 'Behandle sak$EKSTRA_BESKRIVELSE')]"))
                .willReturn(
                    aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFile("oppgave/ferdigstilt_oppgave.json")),
                ),
        )

        val oppgave = Oppgave(
            id = OPPGAVE_ID,
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = EKSTRA_BESKRIVELSE,
            tema = null,
        )

        val response: ResponseEntity<OppgaveResponse> =
            restTemplate.exchange(
                localhost(OPPDATER_OPPGAVE_URL),
                HttpMethod.POST,
                HttpEntity(oppgave, headers),
            )
        assertThat(response.body?.oppgaveId).isEqualTo(OPPGAVE_ID)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `skal opprette oppgave med mappeId, returnere oppgaveid og 201 Created`() {
        stubFor(post("/api/v1/oppgaver").willReturn(okJson(objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID)))))

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
        val response: ResponseEntity<OppgaveResponse> =
            restTemplate.exchange(
                localhost(OPPRETT_OPPGAVE_URL_V2),
                HttpMethod.POST,
                HttpEntity(opprettOppgave, headers),
            )

        assertThat(response.body?.oppgaveId).isEqualTo(OPPGAVE_ID)
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
    }

    @Test
    fun `skal opprette oppgave uten ident, returnere oppgaveid og 201 Created`() {
        stubFor(post("/api/v1/oppgaver").willReturn(okJson(objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID)))))

        val opprettOppgave = OpprettOppgaveRequest(
            ident = null,
            fristFerdigstillelse = LocalDate.now().plusDays(3),
            behandlingstema = "behandlingstema",
            enhetsnummer = "enhetsnummer",
            tema = Tema.TSO,
            oppgavetype = Oppgavetype.BehandleSak,
            beskrivelse = "Oppgavetekst",
        )
        val response: ResponseEntity<OppgaveResponse> =
            restTemplate.exchange(
                localhost(OPPRETT_OPPGAVE_URL_V2),
                HttpMethod.POST,
                HttpEntity(opprettOppgave, headers),
            )

        assertThat(response.body?.oppgaveId).isEqualTo(OPPGAVE_ID)
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
    }

    @Test
    fun `kall mot oppgave ved opprett feiler med bad request, tjenesten vår returernerer 500 og med info om feil i response `() {
        stubFor(
            post("/api/v1/oppgaver")
                .willReturn(
                    aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("body"),
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
                okJson(
                    objectMapper.writeValueAsString(
                        Oppgave(
                            id = 123,
                            status = StatusEnum.FERDIGSTILT,
                        ),
                    ),
                ),
            ),
        )

        verify(exactly(0), patchRequestedFor(urlEqualTo("/api/v1/oppgaver/123")))

        val response: ResponseEntity<OppgaveResponse> =
            restTemplate.exchange(
                localhost("/api/oppgave/123/ferdigstill"),
                HttpMethod.PATCH,
                HttpEntity(null, headers),
            )

        assertThat(response.body?.oppgaveId).isEqualTo(123)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Ferdigstilling av oppgave som er feilregistrert skal generere en oppslagsfeil`() {
        val oppgave = Oppgave(id = OPPGAVE_ID, status = StatusEnum.FEILREGISTRERT)
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
        val oppgave = Oppgave(id = OPPGAVE_ID, status = StatusEnum.OPPRETTET)
        stubFor(
            get("/api/v1/oppgaver/$OPPGAVE_ID").willReturn(
                okJson(objectMapper.writeValueAsString(oppgave)),
            ),
        )

        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .withRequestBody(matchingJsonPath("$.[?(@.status == 'FERDIGSTILT')]"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            objectMapper.writeValueAsBytes(
                                Oppgave(
                                    id = OPPGAVE_ID,
                                    status = StatusEnum.FERDIGSTILT,
                                ),
                            ),
                        ),
                ),
        )

        val response: ResponseEntity<OppgaveResponse> =
            restTemplate.exchange(
                localhost("/api/oppgave/$OPPGAVE_ID/ferdigstill"),
                HttpMethod.PATCH,
                HttpEntity(null, headers),
            )

        // assertThat(response.body?.melding).contains("ferdigstill OK")
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `fordelOppgave skal tilordne oppgave til saksbehandler når saksbehandler er satt på requesten`() {
        val saksbehandlerId = "Z999999"
        stubFor(get(GET_OPPGAVE_URL).willReturn(okJson(objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID)))))
        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .willReturn(
                    aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFile("oppgave/oppgave.json")),
                ),
        )

        val response: ResponseEntity<Oppgave> = restTemplate.exchange(
            localhost("/api/oppgave/$OPPGAVE_ID/fordel?saksbehandler=$saksbehandlerId"),
            HttpMethod.POST,
            HttpEntity(null, headers),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        // assertThat(response.body?.melding).isEqualTo("Oppgaven ble tildelt saksbehandler $saksbehandlerId")
        assertThat(response.body!!.id).isEqualTo(OPPGAVE_ID)
    }

    @Test
    fun `fordelOppgave skal tilbakestille tilordning på oppgave når saksbehandler ikke er satt på requesten`() {
        stubFor(get(GET_OPPGAVE_URL).willReturn(okJson(objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID)))))
        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .willReturn(
                    aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFile("oppgave/oppgave.json")),
                ),
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
        val oppgave = Oppgave(id = OPPGAVE_ID, status = StatusEnum.FERDIGSTILT)
        stubFor(
            get(GET_OPPGAVE_URL).willReturn(
                okJson(objectMapper.writeValueAsString(oppgave)),
            ),
        )
        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .willReturn(
                    aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFile("oppgave/oppgave.json")),
                ),
        )

        val exception = catchProblemDetailException {
            restTemplate.exchange<Oppgave>(
                localhost("/api/oppgave/$OPPGAVE_ID/fordel?saksbehandler=Z999999"),
                HttpMethod.POST,
                HttpEntity(null, headers),
            )
        }

        assertThat(exception.httpStatus).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(exception.detail.detail)
            .isEqualTo("[Oppgave.fordelOppgave][Kan ikke fordele oppgave=$OPPGAVE_ID som allerede er ferdigstilt]")
    }

    @Test
    fun `Skal hente oppgave basert på id`() {
        stubFor(get(GET_OPPGAVE_URL).willReturn(okJson(objectMapper.writeValueAsString(Oppgave(id = OPPGAVE_ID)))))

        val response: ResponseEntity<Oppgave> =
            restTemplate.exchange(localhost("/api/oppgave/$OPPGAVE_ID"), HttpMethod.GET, HttpEntity(null, headers))

        println(response.body)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.id).isEqualTo(OPPGAVE_ID)
    }

    @Test
    fun `Endre enhet på mappe skal endre enhet og sette mappe til null hvis fjernMappeFraOppgave-flagg satt til true`() {
        stubFor(get("/api/v1/oppgaver/$OPPGAVE_ID").willReturn(okJson(readFile("oppgave/hentOppgave.json"))))

        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .withRequestBody(
                    equalToJson("""{"id":315488374,"tildeltEnhetsnr": "4833","versjon":1,"mappeId":null}"""),
                )
                .willReturn(
                    aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFile("oppgave/ferdigstilt_oppgave.json")),
                ),
        )

        val oppgave = Oppgave(
            id = OPPGAVE_ID,
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = EKSTRA_BESKRIVELSE,
            tema = null,
        )

        val response: ResponseEntity<OppgaveResponse> =
            restTemplate.exchange(
                localhost("$OPPGAVE_URL/$OPPGAVE_ID/enhet/4833?fjernMappeFraOppgave=true"),
                HttpMethod.PATCH,
                HttpEntity(oppgave, headers),
            )
        assertThat(response.body?.oppgaveId).isEqualTo(OPPGAVE_ID)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Endre enhet på mappe skal endre enhet og beholde mappe hvis fjernMappeFraOppgave-flagg satt til false`() {
        stubFor(get("/api/v1/oppgaver/$OPPGAVE_ID").willReturn(okJson(readFile("oppgave/hentOppgave.json"))))

        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .withRequestBody(
                    equalToJson("""{"id":315488374,"tildeltEnhetsnr": "4833","versjon":1,"mappeId":1234}"""),
                )
                .willReturn(
                    aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFile("oppgave/ferdigstilt_oppgave.json")),
                ),
        )

        val oppgave = Oppgave(
            id = OPPGAVE_ID,
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = EKSTRA_BESKRIVELSE,
            tema = null,
        )

        val response: ResponseEntity<OppgaveResponse> =
            restTemplate.exchange(
                localhost("$OPPGAVE_URL/$OPPGAVE_ID/enhet/4833?fjernMappeFraOppgave=false"),
                HttpMethod.PATCH,
                HttpEntity(oppgave, headers),
            )
        assertThat(response.body?.oppgaveId).isEqualTo(OPPGAVE_ID)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Endre enhet på mappe skal feile når oppgave returnerer bad request`() {
        stubFor(get("/api/v1/oppgaver/$OPPGAVE_ID").willReturn(okJson(readFile("oppgave/hentOppgave.json"))))

        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .withRequestBody(
                    equalToJson("""{"id":315488374,"tildeltEnhetsnr": "4833","versjon":1,"mappeId":1234}"""),
                )
                .willReturn(
                    aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(""""{uuid":"123","feilmelding":"Mappe finnes ikke for enhet"} """),
                ),
        )

        val oppgave = Oppgave(
            id = OPPGAVE_ID,
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = EKSTRA_BESKRIVELSE,
            tema = null,
        )

        val exception = catchProblemDetailException {
            restTemplate.exchange<OppgaveResponse>(
                localhost("$OPPGAVE_URL/$OPPGAVE_ID/enhet/4833?fjernMappeFraOppgave=false"),
                HttpMethod.PATCH,
                HttpEntity(oppgave, headers),
            )
        }
        assertThat(exception.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(exception.detail.detail).contains("[Oppgave.byttEnhet][Feil ved bytte av enhet for oppgave for $OPPGAVE_ID")
    }

    @Test
    fun `Skal returnere 409 dersom man oppdaterer oppgave med feil versjon`() {
        stubFor(get("/api/v1/oppgaver/$OPPGAVE_ID").willReturn(okJson(readFile("oppgave/hentOppgave.json"))))
        stubFor(
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .willReturn(
                    aResponse()
                        .withStatus(409)
                        .withHeader("Content-Type", "application/json")
                        .withBody(""""{uuid":"123","feilmelding":"Versjonskonflikt ved forespørsel om endring av oppgave med id"} """),
                ),
        )

        val oppgave = Oppgave(
            id = OPPGAVE_ID,
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
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .withRequestBody(
                    equalToJson("""{"id":315488374, "versjon":1,"behandlesAvApplikasjon":null}"""),
                )
                .willReturn(
                    aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFile("oppgave/ferdigstilt_oppgave.json")),
                ),
        )

        val oppgave = Oppgave(
            id = OPPGAVE_ID,
            aktoerId = "1234567891011",
            journalpostId = "1",
            beskrivelse = EKSTRA_BESKRIVELSE,
            tema = null,
        )

        val response: ResponseEntity<OppgaveResponse> =
            restTemplate.exchange(
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
            patch(urlEqualTo("/api/v1/oppgaver/$OPPGAVE_ID"))
                .withRequestBody(
                    equalToJson("""{"id":315488374, "versjon":1,"behandlesAvApplikasjon":null}"""),
                )
                .willReturn(
                    aResponse()
                        .withStatus(409)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Feil versjonsnummer"),
                ),
        )

        val oppgave = Oppgave(
            id = OPPGAVE_ID,
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

    companion object {

        private const val OPPGAVE_URL = "/api/oppgave"
        private const val OPPRETT_OPPGAVE_URL_V2 = "/api/oppgave/opprett"
        private const val OPPDATER_OPPGAVE_URL = "$OPPGAVE_URL/oppdater"
        private const val OPPGAVE_ID = 315488374L
        private const val GET_OPPGAVER_URL =
            "/api/v1/oppgaver?aktoerId=1234567891011&tema=KON&oppgavetype=BEH_SAK&journalpostId=1&statuskategori=AAPEN"
        private const val GET_MAPPER_URL =
            "/api/v1/mapper?enhetsnr=1234567891011&opprettetFom=dcssdf&limit=50"
        private const val GET_OPPGAVE_URL = "/api/v1/oppgaver/$OPPGAVE_ID"
        private const val EKSTRA_BESKRIVELSE = " Ekstra beskrivelse"
    }
}
