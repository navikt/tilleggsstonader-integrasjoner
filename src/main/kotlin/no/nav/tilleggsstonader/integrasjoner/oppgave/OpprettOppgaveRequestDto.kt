package no.nav.tilleggsstonader.integrasjoner.oppgave

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import no.nav.tilleggsstonader.kontrakter.oppgave.OppgavePrioritet

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OpprettOppgaveRequestDto(
    val tema: Tema,
    val oppgavetype: String,
    val aktivDato: String,
    val prioritet: OppgavePrioritet,
    val tildeltEnhetsnr: String? = null,
    val journalpostId: String? = null,
    val behandlesAvApplikasjon: String? = null,
    val saksreferanse: String? = null,
    val personident: String? = null,
    val tilordnetRessurs: String? = null,
    val beskrivelse: String? = null,
    val behandlingstema: String? = null,
    val behandlingstype: String? = null,
    val mappeId: Long? = null,
    val fristFerdigstillelse: String? = null,
)
