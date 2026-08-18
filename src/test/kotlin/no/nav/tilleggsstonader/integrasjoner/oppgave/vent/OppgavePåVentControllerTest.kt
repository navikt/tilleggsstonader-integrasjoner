package no.nav.tilleggsstonader.integrasjoner.oppgave.vent

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.patch
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import no.nav.tilleggsstonader.integrasjoner.IntegrationTest
import no.nav.tilleggsstonader.integrasjoner.oppgave.testOppgave
import no.nav.tilleggsstonader.kontrakter.felles.JsonMapperProvider.jsonMapper
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnMappeResponseDto
import no.nav.tilleggsstonader.kontrakter.oppgave.MappeDto
import no.nav.tilleggsstonader.kontrakter.oppgave.OppgaveMappe
import no.nav.tilleggsstonader.kontrakter.oppgave.vent.OppdaterPåVentRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.vent.SettPåVentRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.vent.SettPåVentResponse
import no.nav.tilleggsstonader.kontrakter.oppgave.vent.TaAvVentRequest
import no.nav.tilleggsstonader.libs.test.httpclient.ProblemDetailUtil.catchProblemDetailException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.exchange
import java.time.LocalDate
import java.util.Optional

@TestPropertySource(properties = ["clients.oppgave.uri=http://localhost:28085"])
class OppgavePåVentControllerTest : IntegrationTest() {
    private val oppgave =
        testOppgave(
            id = OPPGAVE_ID,
            versjon = 1,
            tildeltEnhetsnr = ENHET,
            tilordnetRessurs = SAKSBEHANDLER,
        )

    private val mapper =
        listOf(
            MappeDto(MAPPE_ID_PÅ_VENT, OppgaveMappe.PÅ_VENT.navn.first(), ENHET),
            MappeDto(MAPPE_ID_KLAR, OppgaveMappe.KLAR.navn.first(), ENHET),
        )

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(onBehalfOfToken(saksbehandler = SAKSBEHANDLER))
        stubFor(get(GET_MAPPE_URL).willReturn(okJson(jsonMapper.writeValueAsString(FinnMappeResponseDto(2, mapper)))))
        stubFor(
            patch(urlEqualTo(PATCH_OPPGAVE_URL))
                .willReturn(okJson(jsonMapper.writeValueAsString(oppgave.copy(versjon = 2)))),
        )
    }

    @Nested
    inner class SettPåVent {
        @BeforeEach
        fun setUp() {
            stubFor(get(GET_OPPGAVE_URL).willReturn(okJson(jsonMapper.writeValueAsString(oppgave))))
        }

        @Test
        fun `skal sette behandling på vent`() {
            val response =
                settPåVent(SettPåVentRequest(OPPGAVE_ID, "kommentar", LocalDate.now().plusDays(7), false, ENHET))

            assertThat(response.oppgaveId).isEqualTo(OPPGAVE_ID)
            assertThat(response.oppgaveVersjon).isEqualTo(2)
        }

        @Test
        fun `skal feile hvis man ikke er eier av oppgaven`() {
            stubFor(
                get(GET_OPPGAVE_URL).willReturn(
                    okJson(jsonMapper.writeValueAsString(oppgave.copy(tilordnetRessurs = "annenSaksbehandler"))),
                ),
            )

            val exception =
                catchProblemDetailException {
                    settPåVent(SettPåVentRequest(OPPGAVE_ID, null, LocalDate.now().plusDays(7), false, ENHET))
                }

            assertThat(exception.httpStatus).isEqualTo(HttpStatus.BAD_REQUEST)
            assertThat(exception.detail.detail).contains("Kan ikke sette behandling på vent når man ikke er eier av oppgaven.")
        }
    }

    @Nested
    inner class OppdaterSettPåVent {
        @BeforeEach
        fun setUp() {
            stubFor(get(GET_OPPGAVE_URL).willReturn(okJson(jsonMapper.writeValueAsString(oppgave))))
        }

        @Test
        fun `skal oppdatere behandling på vent`() {
            val response =
                oppdaterSettPåVent(
                    OppdaterPåVentRequest(
                        OPPGAVE_ID,
                        1,
                        "ny kommentar",
                        LocalDate.now().plusDays(14),
                        false,
                        ENHET,
                    ),
                )

            assertThat(response.oppgaveId).isEqualTo(OPPGAVE_ID)
            assertThat(response.oppgaveVersjon).isEqualTo(2)
        }

        @Test
        fun `skal feile hvis man ikke er eier av oppgaven`() {
            stubFor(
                get(GET_OPPGAVE_URL).willReturn(
                    okJson(jsonMapper.writeValueAsString(oppgave.copy(tilordnetRessurs = "annenSaksbehandler"))),
                ),
            )

            val exception =
                catchProblemDetailException {
                    oppdaterSettPåVent(
                        OppdaterPåVentRequest(
                            OPPGAVE_ID,
                            1,
                            null,
                            LocalDate.now().plusDays(14),
                            false,
                            ENHET,
                        ),
                    )
                }

            assertThat(exception.httpStatus).isEqualTo(HttpStatus.BAD_REQUEST)
            assertThat(exception.detail.detail).contains("Kan ikke oppdatere behandling på vent når man ikke er eier av oppgaven.")
        }
    }

    @Nested
    inner class TaAvVent {
        @BeforeEach
        fun setUp() {
            stubFor(
                get(GET_OPPGAVE_URL).willReturn(
                    okJson(jsonMapper.writeValueAsString(oppgave.copy(mappeId = Optional.of(MAPPE_ID_PÅ_VENT)))),
                ),
            )
        }

        @Test
        fun `skal ta behandling av vent`() {
            val response = taAvVent(TaAvVentRequest(OPPGAVE_ID, null, true, LocalDate.now(), ENHET))

            assertThat(response.oppgaveId).isEqualTo(OPPGAVE_ID)
            assertThat(response.oppgaveVersjon).isEqualTo(2)
        }

        @Test
        fun `skal feile hvis annen saksbehandler eier oppgaven`() {
            stubFor(
                get(GET_OPPGAVE_URL).willReturn(
                    okJson(
                        jsonMapper.writeValueAsString(
                            oppgave.copy(
                                tilordnetRessurs = "annenSaksbehandler",
                                mappeId = Optional.of(MAPPE_ID_PÅ_VENT),
                            ),
                        ),
                    ),
                ),
            )

            val exception =
                catchProblemDetailException {
                    taAvVent(TaAvVentRequest(OPPGAVE_ID, null, false, LocalDate.now(), ENHET))
                }

            assertThat(exception.httpStatus).isEqualTo(HttpStatus.BAD_REQUEST)
            assertThat(exception.detail.detail).contains("Kan ikke ta behandling av vent når noen andre eier oppgaven")
        }
    }

    private fun settPåVent(request: SettPåVentRequest): SettPåVentResponse =
        restTemplate
            .exchange<SettPåVentResponse>(
                localhost("$VENT_URL/sett-pa-vent"),
                HttpMethod.POST,
                HttpEntity(request, headers),
            ).body!!

    private fun oppdaterSettPåVent(request: OppdaterPåVentRequest): SettPåVentResponse =
        restTemplate
            .exchange<SettPåVentResponse>(
                localhost("$VENT_URL/oppdater-pa-vent"),
                HttpMethod.POST,
                HttpEntity(request, headers),
            ).body!!

    private fun taAvVent(request: TaAvVentRequest): SettPåVentResponse =
        restTemplate
            .exchange<SettPåVentResponse>(
                localhost("$VENT_URL/ta-av-vent"),
                HttpMethod.POST,
                HttpEntity(request, headers),
            ).body!!

    companion object {
        private const val OPPGAVE_ID = 315488374L
        private const val ENHET = "4812"
        private const val SAKSBEHANDLER = "julenissen"
        private val MAPPE_ID_PÅ_VENT = 10L
        private val MAPPE_ID_KLAR = 20L
        private const val VENT_URL = "/api/oppgave/vent"
        private const val GET_OPPGAVE_URL = "/api/v1/oppgaver/$OPPGAVE_ID"
        private const val PATCH_OPPGAVE_URL = "/api/v1/oppgaver/$OPPGAVE_ID"
        private const val GET_MAPPE_URL = "/api/v1/mapper?enhetsnr=$ENHET&limit=1000"
    }
}
