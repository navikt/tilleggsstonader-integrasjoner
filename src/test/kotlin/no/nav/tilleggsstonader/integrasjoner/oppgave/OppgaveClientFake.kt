package no.nav.tilleggsstonader.integrasjoner.oppgave

import io.mockk.mockk
import no.nav.tilleggsstonader.integrasjoner.oppgave.OppgaveUtil.MAPPE_ID_KLAR
import no.nav.tilleggsstonader.integrasjoner.oppgave.OppgaveUtil.MAPPE_ID_PÅ_VENT
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnMappeRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnMappeResponseDto
import no.nav.tilleggsstonader.kontrakter.oppgave.IdentGruppe
import no.nav.tilleggsstonader.kontrakter.oppgave.MappeDto
import no.nav.tilleggsstonader.kontrakter.oppgave.Oppgave
import no.nav.tilleggsstonader.kontrakter.oppgave.OppgaveIdentV2
import no.nav.tilleggsstonader.kontrakter.oppgave.OppgaveMappe
import no.nav.tilleggsstonader.kontrakter.oppgave.StatusEnum
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional

class OppgaveClientFake : OppgaveClient(URI.create("http://localhost"), mockk()) {
    val oppgavelager = mutableMapOf<Long, Oppgave>()
    var maxOppgaveId = 1L

    override fun finnOppgaveMedId(oppgaveId: Long): Oppgave = oppgavelager.getValue(oppgaveId)

    override fun oppdaterOppgave(patchDto: Oppgave): Oppgave {
        val eksisterendeOppgave = oppgavelager[patchDto.id]!!
        val versjon = patchDto.versjon
        require(versjon == eksisterendeOppgave.versjon) {
            "Oppgaven har endret seg siden du sist hentet oppgaver. versjon=$versjon (${eksisterendeOppgave.versjon}) " +
                "For å kunne gjøre endringer må du hente oppgaver på nytt."
        }
        val oppdatertOppgave =
            eksisterendeOppgave.copy(
                versjon = versjon + 1,
                beskrivelse = patchDto.beskrivelse ?: eksisterendeOppgave.beskrivelse,
                tilordnetRessurs =
                    (patchDto.tilordnetRessurs ?: eksisterendeOppgave.tilordnetRessurs)?.takeIf { it.isNotBlank() },
                mappeId = patchDto.mappeId ?: eksisterendeOppgave.mappeId,
                fristFerdigstillelse = patchDto.fristFerdigstillelse ?: eksisterendeOppgave.fristFerdigstillelse,
            )
        oppgavelager[patchDto.id] = oppdatertOppgave
        return eksisterendeOppgave
    }

    override fun opprettOppgave(request: OpprettOppgaveRequestDto): Long {
        val oppgave =
            Oppgave(
                id = ++maxOppgaveId,
                versjon = 1,
                status = StatusEnum.OPPRETTET,
                identer = listOf(OppgaveIdentV2(request.personident!!, IdentGruppe.FOLKEREGISTERIDENT)),
                tildeltEnhetsnr = request.tildeltEnhetsnr,
                saksreferanse = null,
                journalpostId = request.journalpostId,
                tema = request.tema,
                oppgavetype = request.oppgavetype,
                behandlingstema = request.behandlingstema,
                tilordnetRessurs = request.tilordnetRessurs,
                fristFerdigstillelse = request.fristFerdigstillelse?.let { LocalDate.parse(it) },
                aktivDato = request.aktivDato?.let { LocalDate.parse(it) },
                beskrivelse = request.beskrivelse,
                prioritet = request.prioritet,
                behandlingstype = request.behandlingstype,
                behandlesAvApplikasjon = request.behandlesAvApplikasjon,
                mappeId = request.mappeId?.let { Optional.of(it) },
                opprettetTidspunkt = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
            )
        oppgavelager[oppgave.id] = oppgave
        return oppgave.id
    }

    val mapper =
        listOf(
            MappeDto(MAPPE_ID_PÅ_VENT, OppgaveMappe.PÅ_VENT.navn.first(), "4462"),
            MappeDto(MAPPE_ID_KLAR, OppgaveMappe.KLAR.navn.first(), "4462"),
        )

    override fun finnMapper(finnMappeRequest: FinnMappeRequest): FinnMappeResponseDto = FinnMappeResponseDto(mapper.size, mapper)
}
