package no.nav.tilleggsstonader.integrasjoner.oppgave

import no.nav.tilleggsstonader.kontrakter.oppgave.FinnOppgaveRequest
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.min

const val limitMotOppgave = 50L

data class OppgaveRequest(
    val statuskategori: String = "AAPEN",
    val tema: String,
    val sorteringsfelt: String = "OPPRETTET_TIDSPUNKT",
    val sorteringsrekkefolge: String = "DESC",
    val limit: Long = limitMotOppgave,
    val offset: Long = 0,
    val behandlingstema: String?,
    val behandlingstype: String?,
    val oppgavetype: String?,
    val erUtenMappe: Boolean?,
    val tildeltEnhetsnr: String?,
    val tildeltRessurs: Boolean?,
    val tilordnetRessurs: String?,
    val journalpostId: String?,
    val saksreferanse: String?,
    val opprettetFom: LocalDateTime?,
    val opprettetTom: LocalDateTime?,
    val fristFom: LocalDate?,
    val fristTom: LocalDate?,
    val aktivDatoFom: LocalDate?,
    val aktivDatoTom: LocalDate?,
    val mappeId: Long?,
    val aktoerId: String?,
)

fun FinnOppgaveRequest.toDto() =
    OppgaveRequest(
        offset = this.offset ?: 0,
        limit = min(this.limit ?: limitMotOppgave, limitMotOppgave),
        tema = this.tema.name,
        behandlingstema = this.behandlingstema?.value,
        behandlingstype = this.behandlingstype?.value,
        oppgavetype = this.oppgavetype?.value,
        erUtenMappe = this.erUtenMappe,
        tildeltEnhetsnr = this.enhet,
        tildeltRessurs = this.tildeltRessurs,
        tilordnetRessurs = this.tilordnetRessurs ?: this.saksbehandler,
        journalpostId = this.journalpostId,
        opprettetFom = this.opprettetFomTidspunkt,
        opprettetTom = this.opprettetTomTidspunkt,
        fristFom = this.fristFomDato,
        fristTom = this.fristTomDato,
        aktivDatoFom = this.aktivFomDato,
        aktivDatoTom = this.aktivTomDato,
        mappeId = this.mappeId,
        aktoerId = this.aktørId,
        saksreferanse = this.saksreferanse,
    )
