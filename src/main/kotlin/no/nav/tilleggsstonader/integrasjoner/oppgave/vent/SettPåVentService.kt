package no.nav.tilleggsstonader.integrasjoner.oppgave.vent

import no.nav.tilleggsstonader.integrasjoner.infrastruktur.exception.ApiFeil
import no.nav.tilleggsstonader.integrasjoner.oppgave.OppgaveService
import no.nav.tilleggsstonader.integrasjoner.util.SikkerhetsContext
import no.nav.tilleggsstonader.kontrakter.oppgave.Oppgave
import no.nav.tilleggsstonader.kontrakter.oppgave.OppgaveMappe
import no.nav.tilleggsstonader.kontrakter.oppgave.vent.OppdaterPåVentRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.vent.SettPåVentRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.vent.SettPåVentResponse
import no.nav.tilleggsstonader.kontrakter.oppgave.vent.TaAvVentRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.Optional.of
import java.util.Optional.ofNullable

@Service
class SettPåVentService(
    private val oppgaveService: OppgaveService,
) {
    fun settPåVent(request: SettPåVentRequest): SettPåVentResponse {
        val oppgave = hentOppgave(request.oppgaveId)
        feilHvisIkkeEierAvOppgaven(oppgave, "Kan ikke sette behandling på vent når man ikke er eier av oppgaven.")

        val enhet = oppgave.tildeltEnhetsnr ?: error("Oppgave=${oppgave.id} mangler enhetsnummer")
        val mappe = oppgaveService.finnMappe(enhet, OppgaveMappe.PÅ_VENT)
        val oppdaterOppgave =
            Oppgave(
                id = oppgave.id,
                versjon = oppgave.versjon,
                tilordnetRessurs = if (request.beholdOppgave) SikkerhetsContext.hentSaksbehandler() else "",
                fristFerdigstillelse = request.frist,
                beskrivelse = SettPåVentBeskrivelseUtil.settPåVent(oppgave, request),
                mappeId = of(mappe.id),
            )
        val oppdatertOppgave = oppgaveService.patchOppgave(oppdaterOppgave)
        return SettPåVentResponse(oppgave.id, oppdatertOppgave.versjon)
    }

    fun oppdaterSettPåVent(request: OppdaterPåVentRequest): SettPåVentResponse {
        val oppgave = oppgaveService.hentOppgave(request.oppgaveId)
        feilHvisIkkeEierAvOppgaven(oppgave, "Kan ikke oppdatere behandling på vent når man ikke er eier av oppgaven.")
        val oppdaterOppgave =
            Oppgave(
                id = request.oppgaveId,
                versjon = request.oppgaveVersjon,
                fristFerdigstillelse = request.frist,
                beskrivelse = SettPåVentBeskrivelseUtil.oppdaterSettPåVent(oppgave, request),
                tilordnetRessurs = if (request.beholdOppgave) SikkerhetsContext.hentSaksbehandler() else "",
            )
        val oppdatertOppgave = oppgaveService.patchOppgave(oppdaterOppgave)
        return SettPåVentResponse(oppgave.id, oppdatertOppgave.versjon)
    }

    private fun hentOppgave(oppgaveId: Long): Oppgave = oppgaveService.hentOppgave(oppgaveId)

    fun taAvVent(request: TaAvVentRequest): SettPåVentResponse {
        val oppgave = oppgaveService.hentOppgave(request.oppgaveId)
        feilHvisIkkeEierAvOppgaven(oppgave, "Kan ikke ta behandling av vent når man ikke er eier av oppgaven.")
        val tilordnetRessurs =
            if (SikkerhetsContext.erSaksbehandler() && request.beholdOppgave) {
                SikkerhetsContext.hentSaksbehandler()
            } else {
                ""
            }
        val enhet = oppgave.tildeltEnhetsnr ?: error("Oppgave=${oppgave.id} mangler enhetsnummer")
        val mappeId = oppgaveService.finnMappe(enhet, OppgaveMappe.KLAR).id
        val oppdatertOppgave =
            oppgaveService.patchOppgave(
                Oppgave(
                    id = oppgave.id,
                    versjon = oppgave.versjon,
                    tilordnetRessurs = tilordnetRessurs,
                    fristFerdigstillelse = request.frist,
                    beskrivelse = SettPåVentBeskrivelseUtil.taAvVent(oppgave, request),
                    mappeId = ofNullable(mappeId),
                ),
            )
        return SettPåVentResponse(oppgave.id, oppgaveVersjon = oppdatertOppgave.versjon)
    }

    private fun feilHvisIkkeEierAvOppgaven(
        oppgave: Oppgave,
        feilmelding: String,
    ) {
        if (ikkeEierAvOppgave(oppgave)) {
            throw ApiFeil(feilmelding, HttpStatus.BAD_REQUEST)
        }
    }

    private fun ikkeEierAvOppgave(oppgave: Oppgave) = oppgave.tilordnetRessurs != SikkerhetsContext.hentSaksbehandler()
}
