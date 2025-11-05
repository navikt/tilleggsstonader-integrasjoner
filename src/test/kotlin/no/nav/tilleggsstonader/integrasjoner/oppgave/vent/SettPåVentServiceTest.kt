package no.nav.tilleggsstonader.integrasjoner.oppgave.vent

import no.nav.tilleggsstonader.integrasjoner.oppgave.OppgaveClientFake
import no.nav.tilleggsstonader.integrasjoner.oppgave.OppgaveService
import no.nav.tilleggsstonader.integrasjoner.oppgave.OppgaveUtil.MAPPE_ID_KLAR
import no.nav.tilleggsstonader.integrasjoner.oppgave.OppgaveUtil.MAPPE_ID_PÅ_VENT
import no.nav.tilleggsstonader.integrasjoner.util.BrukerContextUtil.testWithBrukerContext
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import no.nav.tilleggsstonader.kontrakter.oppgave.IdentGruppe
import no.nav.tilleggsstonader.kontrakter.oppgave.OppgaveIdentV2
import no.nav.tilleggsstonader.kontrakter.oppgave.Oppgavetype
import no.nav.tilleggsstonader.kontrakter.oppgave.OpprettOppgaveRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.vent.OppdaterPåVentRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.vent.SettPåVentRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.vent.TaAvVentRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import java.time.LocalDate
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class SettPåVentServiceTest {
    val oppgaveService = OppgaveService(OppgaveClientFake(), ConcurrentMapCacheManager())
    val settPåVentService = SettPåVentService(oppgaveService = oppgaveService)

    var oppgaveId: Long? = null

    fun settPåVent(oppgaveId: Long) =
        SettPåVentRequest(
            oppgaveId = oppgaveId,
            kommentar = "ny beskrivelse",
            frist = LocalDate.of(2023, 1, 1),
            beholdOppgave = false,
            endretAvEnhetsnr = "4462",
        )

    fun oppdaterSettPåVentDto(
        oppgaveId: Long,
        versjon: Int,
    ) = OppdaterPåVentRequest(
        oppgaveId = oppgaveId,
        oppgaveVersjon = versjon,
        frist = LocalDate.now().plusDays(5),
        kommentar = "oppdatert beskrivelse",
        beholdOppgave = false,
        endretAvEnhetsnr = "4462",
    )

    val dummySaksbehandler = "saksbeh"

    @BeforeEach
    fun setUp() {
        oppgaveId =
            oppgaveService.opprettOppgave(
                OpprettOppgaveRequest(
                    ident = OppgaveIdentV2("", IdentGruppe.FOLKEREGISTERIDENT),
                    enhetsnummer = "4462",
                    tema = Tema.TSO,
                    behandlingstema = Behandlingstema.Læremidler.name,
                    oppgavetype = Oppgavetype.BehandleSak,
                    fristFerdigstillelse = LocalDate.now(),
                    beskrivelse = "en beskrivelse",
                    tilordnetRessurs = dummySaksbehandler,
                ),
            )
    }

    @Nested
    inner class SettPåVent {
        @Test
        fun `skal sette behandling på vent`() {
            testWithBrukerContext(dummySaksbehandler) {
                val request = settPåVent(oppgaveId!!)
                settPåVentService.settPåVent(request)

                with(oppgaveService.hentOppgave(oppgaveId!!)) {
                    assertThat(beskrivelse).contains("ny beskrivelse")
                    assertThat(fristFerdigstillelse).isEqualTo(request.frist)
                    assertThat(tilordnetRessurs).isNull()
                    assertThat(mappeId?.getOrNull()).isEqualTo(MAPPE_ID_PÅ_VENT)
                }
            }
        }

        @Test
        fun `skal sette behandling på vent og fortsette beholde oppgaven`() {
            testWithBrukerContext(dummySaksbehandler) {
                settPåVentService.settPåVent(settPåVent(oppgaveId!!).copy(beholdOppgave = true))

                with(oppgaveService.hentOppgave(oppgaveId!!)) {
                    assertThat(tilordnetRessurs).isEqualTo(dummySaksbehandler)
                }
            }
        }

        @Test
        fun `skal feile hvis man ikke er eier av oppgaven`() {
            testWithBrukerContext {
                assertThatThrownBy {
                    settPåVentService.settPåVent(settPåVent(oppgaveId!!))
                }.hasMessageContaining("Kan ikke sette behandling på vent når man ikke er eier av oppgaven.")
            }
        }
    }

    @Nested
    inner class OppdaterSettPåVent {
        @Test
        fun `skal kunne oppdatere settPåVent`() {
            testWithBrukerContext(dummySaksbehandler) {
                settPåVentService.settPåVent(settPåVent(oppgaveId!!))
                plukkOppgaven()
                val oppdaterRequest = oppdaterSettPåVentDto(oppgaveId!!, versjon = 3)
                settPåVentService.oppdaterSettPåVent(oppdaterRequest)

                with(oppgaveService.hentOppgave(oppgaveId!!)) {
                    assertThat(beskrivelse).contains("oppdatert beskrivelse")
                    assertThat(fristFerdigstillelse).isEqualTo(oppdaterRequest.frist)
                    assertThat(tilordnetRessurs).isNull()
                    assertThat(mappeId?.getOrNull()).isEqualTo(MAPPE_ID_PÅ_VENT)
                }
            }
        }

        @Test
        fun `skal sette behandling på vent og fortsette beholde oppgaven`() {
            testWithBrukerContext(dummySaksbehandler) {
                settPåVentService.settPåVent(settPåVent(oppgaveId!!))
                plukkOppgaven()
                val oppdaterRequest = oppdaterSettPåVentDto(oppgaveId!!, versjon = 3).copy(beholdOppgave = true)
                settPåVentService.oppdaterSettPåVent(oppdaterRequest)

                with(oppgaveService.hentOppgave(oppgaveId!!)) {
                    assertThat(tilordnetRessurs).isEqualTo(dummySaksbehandler)
                }
            }
        }

        @Test
        fun `skal feile hvis man ikke er eier av oppgaven`() {
            testWithBrukerContext(dummySaksbehandler) {
                settPåVentService.settPåVent(settPåVent(oppgaveId!!))
            }
            testWithBrukerContext {
                assertThatThrownBy {
                    settPåVentService.oppdaterSettPåVent(oppdaterSettPåVentDto(oppgaveId!!, versjon = 3))
                }.hasMessageContaining("Kan ikke oppdatere behandling på vent når man ikke er eier av oppgaven.")
            }
        }
    }

    @Nested
    inner class TaAvVent {
        fun taAvVent(oppgaveId: Long) =
            TaAvVentRequest(
                oppgaveId = oppgaveId,
                kommentar = null,
                beholdOppgave = true,
                frist = LocalDate.now(),
                endretAvEnhetsnr = "4462",
            )

        @BeforeEach
        fun setUp() {
            testWithBrukerContext(dummySaksbehandler) {
                settPåVentService.settPåVent(settPåVent(oppgaveId!!).copy(beholdOppgave = true))
            }
        }

        @Test
        fun `skal ta av vent og fortsette behandling`() {
            testWithBrukerContext(dummySaksbehandler) {
                settPåVentService.taAvVent(taAvVent(oppgaveId!!))
            }

            validerOppdatertOppgave(oppgaveId!!, tilordnetRessurs = dummySaksbehandler)
        }

        @Test
        fun `skal ta av vent og fortsette behandling - uten kommentar`() {
            testWithBrukerContext(dummySaksbehandler) {
                settPåVentService.taAvVent(taAvVent(oppgaveId!!).copy(beholdOppgave = true))
            }

            validerOppdatertOppgave(oppgaveId!!, tilordnetRessurs = dummySaksbehandler)
        }

        @Test
        fun `skal ta av vent og fortsette behandling - med kommentar`() {
            testWithBrukerContext(dummySaksbehandler) {
                settPåVentService.taAvVent(
                    taAvVent(oppgaveId!!)
                        .copy(beholdOppgave = true, kommentar = "kommentar"),
                )
            }

            validerOppdatertOppgave(oppgaveId!!, tilordnetRessurs = dummySaksbehandler)
        }

        @Test
        fun `skal ta av vent og markere oppgave som ufordelt - uten kommentar`() {
            testWithBrukerContext(dummySaksbehandler) {
                settPåVentService.taAvVent(taAvVent(oppgaveId!!))
            }

            validerOppdatertOppgave(oppgaveId!!, tilordnetRessurs = dummySaksbehandler)
        }

        @Test
        fun `skal ta av vent og markere oppgave som ufordelt - med kommentar`() {
            testWithBrukerContext(dummySaksbehandler) {
                settPåVentService.taAvVent(taAvVent(oppgaveId!!).copy(kommentar = "kommentar"))
            }

            validerOppdatertOppgave(oppgaveId!!, tilordnetRessurs = dummySaksbehandler)
        }

        @Test
        fun `skal feile hvis man ikke er eier av oppgaven`() {
            testWithBrukerContext {
                assertThatThrownBy {
                    settPåVentService.taAvVent(taAvVent(oppgaveId!!))
                }.hasMessageContaining("Kan ikke ta behandling av vent når man ikke er eier av oppgaven.")
            }
        }

        private fun validerOppdatertOppgave(
            oppgaveId: Long,
            tilordnetRessurs: String?,
        ) {
            with(oppgaveService.hentOppgave(oppgaveId)) {
                assertThat(tilordnetRessurs).isEqualTo(tilordnetRessurs)
                assertThat(beskrivelse).contains("Tatt av vent")
                assertThat(fristFerdigstillelse).isEqualTo(LocalDate.now())
                assertThat(mappeId).isEqualTo(Optional.of(MAPPE_ID_KLAR))
            }
        }
    }

    private fun plukkOppgaven() {
        val oppgave = oppgaveService.hentOppgave(oppgaveId!!)
        oppgaveService.fordelOppgave(oppgaveId!!, dummySaksbehandler, oppgave.versjon, oppgave.endretAvEnhetsnr)
    }
}
