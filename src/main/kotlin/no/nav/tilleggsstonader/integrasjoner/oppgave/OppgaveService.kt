package no.nav.tilleggsstonader.integrasjoner.oppgave

import DatoFormat
import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.tilleggsstonader.integrasjoner.infrastruktur.exception.OppslagException
import no.nav.tilleggsstonader.integrasjoner.infrastruktur.exception.OppslagException.Level
import no.nav.tilleggsstonader.integrasjoner.util.SikkerhetsContext
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnMappeRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnMappeResponseDto
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnOppgaveRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnOppgaveResponseDto
import no.nav.tilleggsstonader.kontrakter.oppgave.IdentGruppe
import no.nav.tilleggsstonader.kontrakter.oppgave.MappeDto
import no.nav.tilleggsstonader.kontrakter.oppgave.Oppgave
import no.nav.tilleggsstonader.kontrakter.oppgave.OpprettOppgaveRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.StatusEnum
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class OppgaveService(
    private val oppgaveClient: OppgaveClient,
) {

    private val logger = LoggerFactory.getLogger(OppgaveService::class.java)

    fun finnOppgaver(finnOppgaveRequest: FinnOppgaveRequest): FinnOppgaveResponseDto {
        return oppgaveClient.finnOppgaver(finnOppgaveRequest)
    }

    fun hentOppgave(oppgaveId: Long): Oppgave {
        return oppgaveClient.finnOppgaveMedId(oppgaveId)
    }

    fun oppdaterOppgave(request: Oppgave): Long {
        val oppgave: Oppgave = oppgaveClient.finnOppgaveMedId(request.id)
        // Vurdere om man burde logge warn/kaste feil her, finner inget i loggen at dette skulle ha skjedd
        if (oppgave.status === StatusEnum.FERDIGSTILT) {
            logger.info(
                "Ignorerer oppdatering av oppgave som er ferdigstilt for aktørId={} journalpostId={} oppgaveId={}",
                oppgave.aktoerId,
                oppgave.journalpostId,
                oppgave.id,
            )
        } else {
            val patchOppgaveDto = oppgave.copy(
                id = oppgave.id,
                versjon = request.versjon ?: oppgave.versjon,
                beskrivelse = oppgave.beskrivelse + request.beskrivelse,
            )
            oppgaveClient.oppdaterOppgave(patchOppgaveDto)
        }
        return oppgave.id
    }

    fun patchOppgave(patchOppgave: Oppgave): Long {
        return oppgaveClient.oppdaterOppgave(patchOppgave).id
    }

    fun fordelOppgave(oppgaveId: Long, saksbehandler: String?, versjon: Int?): Oppgave {
        val oppgave = oppgaveClient.finnOppgaveMedId(oppgaveId)

        if (oppgave.status === StatusEnum.FERDIGSTILT) {
            throw OppslagException(
                "Kan ikke fordele oppgave=$oppgaveId som allerede er ferdigstilt",
                "Oppgave.fordelOppgave",
                Level.LAV,
                HttpStatus.BAD_REQUEST,
            )
        }

        if (versjon != null && versjon != oppgave.versjon) {
            throw OppslagException(
                "Kan ikke fordele oppgave=$oppgaveId fordi det finnes en nyere versjon av oppgaven.",
                "Oppgave.fordelOppgave",
                Level.LAV,
                HttpStatus.CONFLICT,
            )
        }

        val oppdatertOppgaveDto = oppgave.copy(
            id = oppgave.id,
            versjon = versjon ?: oppgave.versjon,
            tilordnetRessurs = "",
            beskrivelse = lagOppgaveBeskrivelseFordeling(oppgave = oppgave, nySaksbehandlerIdent = saksbehandler),
        )
        return oppgaveClient.oppdaterOppgave(oppdatertOppgaveDto)
    }

    private fun lagOppgaveBeskrivelseFordeling(oppgave: Oppgave, nySaksbehandlerIdent: String? = null): String {
        val innloggetSaksbehandlerIdent = SikkerhetsContext.hentSaksbehandlerEllerSystembruker()
        val saksbehandlerNavn = SikkerhetsContext.hentSaksbehandlerNavn(strict = false)

        val formatertDato = LocalDateTime.now().format(DatoFormat.GOSYS_DATE_TIME)

        val prefix = "--- $formatertDato $saksbehandlerNavn ($innloggetSaksbehandlerIdent) ---\n"
        val endring =
            "Oppgave er flyttet fra ${oppgave.tilordnetRessurs ?: "<ingen>"} til ${nySaksbehandlerIdent ?: "<ingen>"}"

        val nåværendeBeskrivelse = if (oppgave.beskrivelse != null) {
            "\n\n${oppgave.beskrivelse}"
        } else {
            ""
        }

        return prefix + endring + nåværendeBeskrivelse
    }

    fun opprettOppgave(request: OpprettOppgaveRequest): Long {
        val oppgave = OpprettOppgaveRequestDto(
            personident = identHvisGruppe(request, IdentGruppe.FOLKEREGISTERIDENT),
            aktoerId = identHvisGruppe(request, IdentGruppe.AKTOERID),
            orgnr = identHvisGruppe(request, IdentGruppe.ORGNR),
            samhandlernr = identHvisGruppe(request, IdentGruppe.SAMHANDLERNR),
            journalpostId = request.journalpostId,
            prioritet = request.prioritet,
            tema = request.tema,
            tildeltEnhetsnr = request.enhetsnummer,
            behandlingstema = request.behandlingstema,
            fristFerdigstillelse = request.fristFerdigstillelse.format(DateTimeFormatter.ISO_DATE),
            aktivDato = request.aktivFra.format(DateTimeFormatter.ISO_DATE),
            oppgavetype = request.oppgavetype.value,
            beskrivelse = request.beskrivelse,
            behandlingstype = request.behandlingstype,
            tilordnetRessurs = request.tilordnetRessurs?.let { hentNavIdent(it) },
            behandlesAvApplikasjon = request.behandlesAvApplikasjon,
            mappeId = request.mappeId,
        )

        return oppgaveClient.opprettOppgave(oppgave)
    }

    private fun hentNavIdent(id: String): String {
        if (id.length != 7 && id != SikkerhetsContext.SYSTEM_FORKORTELSE) {
            return id
        }
        error("Ident=$id er ugyldig")
    }

    private fun identHvisGruppe(request: OpprettOppgaveRequest, identGruppe: IdentGruppe) =
        if (request.ident?.gruppe == identGruppe) request.ident!!.ident else null

    fun ferdigstill(oppgaveId: Long, versjon: Int?) {
        val oppgave = oppgaveClient.finnOppgaveMedId(oppgaveId)

        validerVersjon(versjon, oppgave)

        when (oppgave.status) {
            StatusEnum.OPPRETTET, StatusEnum.AAPNET, StatusEnum.UNDER_BEHANDLING -> {
                val patchOppgaveDto = oppgave.copy(
                    id = oppgave.id,
                    versjon = versjon ?: oppgave.versjon,
                    status = StatusEnum.FERDIGSTILT,
                )
                oppgaveClient.oppdaterOppgave(patchOppgaveDto)
            }

            StatusEnum.FERDIGSTILT -> logger.info("Oppgave er allerede ferdigstilt. oppgaveId=$oppgaveId")
            StatusEnum.FEILREGISTRERT -> throw OppslagException(
                "Oppgave har status feilregistrert og kan ikke oppdateres. " +
                    "oppgaveId=$oppgaveId",
                "Oppgave.ferdigstill",
                Level.MEDIUM,
                HttpStatus.BAD_REQUEST,
            )

            null -> throw OppslagException(
                "Oppgave har ingen status og kan ikke oppdateres. " +
                    "oppgaveId=$oppgaveId",
                "Oppgave.ferdigstill",
                Level.MEDIUM,
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    private fun validerVersjon(
        versjon: Int?,
        oppgave: Oppgave,
    ) {
        if (versjon != null && versjon != oppgave.versjon) {
            throw OppslagException(
                "Oppgave har har feil versjon og kan ikke ferdigstilles. " +
                    "oppgaveId=${oppgave.id}",
                "Oppgave.ferdigstill",
                Level.LAV,
                HttpStatus.CONFLICT,
            )
        }
    }

    fun finnMapper(finnMappeRequest: FinnMappeRequest): FinnMappeResponseDto {
        val mappeRespons = oppgaveClient.finnMapper(finnMappeRequest)
        if (mappeRespons.antallTreffTotalt > mappeRespons.mapper.size) {
            logger.error(
                "Det finnes flere mapper (${mappeRespons.antallTreffTotalt}) " +
                    "enn vi har hentet ut (${mappeRespons.mapper.size}). Sjekk limit. ",
            )
        }
        return mappeRespons.mapperUtenTema()
    }

    fun finnMapper(enhetNr: String): List<MappeDto> {
        val finnMappeRequest = FinnMappeRequest(enhetsnr = enhetNr, limit = 1000)
        return finnMapper(finnMappeRequest).mapper
    }

    fun tilordneEnhet(oppgaveId: Long, enhet: String, fjernMappeFraOppgave: Boolean, versjon: Int?) {
        val oppgave = oppgaveClient.finnOppgaveMedId(oppgaveId)
        val mappeId = if (fjernMappeFraOppgave) null else oppgave.mappeId
        oppgaveClient.oppdaterEnhet(OppgaveByttEnhet(oppgaveId, enhet, versjon ?: oppgave.versjon!!, mappeId))
    }

    fun fjernBehandlesAvApplikasjon(oppgaveId: Long, versjon: Int) {
        oppgaveClient.fjernBehandlesAvApplikasjon(OppgaveFjernBehandlesAvApplikasjon(oppgaveId, versjon))
    }
}

data class OppgaveByttEnhet(
    val id: Long,
    val tildeltEnhetsnr: String,
    val versjon: Int,
    @JsonInclude(JsonInclude.Include.ALWAYS) val mappeId: Long? = null,
)

data class OppgaveFjernBehandlesAvApplikasjon(
    val id: Long,
    val versjon: Int,
    @JsonInclude(JsonInclude.Include.ALWAYS) val behandlesAvApplikasjon: Long? = null,
)

/**
 * Vil filtrere bort mapper med tema siden disse er spesifikke for andre ytelser enn våre (f.eks Pensjon og Bidrag)
 **/
private fun FinnMappeResponseDto.mapperUtenTema(): FinnMappeResponseDto {
    val mapperUtenTema = this.mapper.filter { it.tema.isNullOrBlank() }
    return this.copy(mapper = mapperUtenTema, antallTreffTotalt = mapperUtenTema.size)
}
