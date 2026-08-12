package no.nav.tilleggsstonader.integrasjoner.oppgave

import no.nav.tilleggsstonader.kontrakter.felles.Tema
import no.nav.tilleggsstonader.kontrakter.oppgave.IdentGruppe
import no.nav.tilleggsstonader.kontrakter.oppgave.Oppgave
import no.nav.tilleggsstonader.kontrakter.oppgave.OppgaveIdentV2
import no.nav.tilleggsstonader.kontrakter.oppgave.OppgavePrioritet
import no.nav.tilleggsstonader.kontrakter.oppgave.Oppgavetype
import no.nav.tilleggsstonader.kontrakter.oppgave.OpprettOppgaveRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.PersonIdent
import no.nav.tilleggsstonader.kontrakter.oppgave.StatusEnum
import java.time.LocalDate
import java.util.Optional

private const val DEFAULT_PERSONIDENT = "12345678910"
private const val DEFAULT_AKTOERID = "1234567891011"
private const val DEFAULT_ENHET = "4812"

fun testOppgave(
    id: Long = 1L,
    versjon: Int = 0,
    tildeltEnhetsnr: String = DEFAULT_ENHET,
    tema: Tema = Tema.TSO,
    oppgavetype: String = Oppgavetype.BehandleSak.value,
    prioritet: OppgavePrioritet = OppgavePrioritet.NORM,
    status: StatusEnum = StatusEnum.OPPRETTET,
    aktivDato: LocalDate = LocalDate.now(),
    endretAvEnhetsnr: String? = null,
    journalpostId: String? = null,
    aktoerId: String? = null,
    beskrivelse: String? = null,
    tilordnetRessurs: String? = null,
    fristFerdigstillelse: LocalDate? = null,
    mappeId: Optional<Long>? = null,
): Oppgave =
    Oppgave(
        id = id,
        tildeltEnhetsnr = tildeltEnhetsnr,
        versjon = versjon,
        tema = tema,
        oppgavetype = oppgavetype,
        prioritet = prioritet,
        status = status,
        aktivDato = aktivDato,
        endretAvEnhetsnr = endretAvEnhetsnr,
        journalpostId = journalpostId,
        aktoerId = aktoerId,
        beskrivelse = beskrivelse,
        tilordnetRessurs = tilordnetRessurs,
        fristFerdigstillelse = fristFerdigstillelse,
        mappeId = mappeId,
    )

fun testOpprettOppgaveRequest(
    personident: PersonIdent = PersonIdent(DEFAULT_PERSONIDENT),
    ident: OppgaveIdentV2? = OppgaveIdentV2(ident = DEFAULT_AKTOERID, gruppe = IdentGruppe.AKTOERID),
    tema: Tema = Tema.TSO,
    oppgavetype: Oppgavetype = Oppgavetype.BehandleSak,
    prioritet: OppgavePrioritet = OppgavePrioritet.NORM,
    aktivDato: LocalDate = LocalDate.now(),
    beskrivelse: String? = "Oppgavetekst",
    enhetsnummer: String? = "enhetsnummer",
    journalpostId: String? = null,
    behandlingstema: String? = "behandlingstema",
    tilordnetRessurs: String? = null,
    fristFerdigstillelse: LocalDate = LocalDate.now().plusDays(3),
    behandlingstype: String? = null,
    behandlesAvApplikasjon: String? = null,
    mappeId: Long? = null,
    saksreferanse: String? = null,
): OpprettOppgaveRequest =
    OpprettOppgaveRequest(
        personident = personident,
        ident = ident,
        tema = tema,
        oppgavetype = oppgavetype,
        prioritet = prioritet,
        aktivDato = aktivDato,
        beskrivelse = beskrivelse,
        enhetsnummer = enhetsnummer,
        journalpostId = journalpostId,
        behandlingstema = behandlingstema,
        tilordnetRessurs = tilordnetRessurs,
        fristFerdigstillelse = fristFerdigstillelse,
        behandlingstype = behandlingstype,
        behandlesAvApplikasjon = behandlesAvApplikasjon,
        mappeId = mappeId,
        saksreferanse = saksreferanse,
    )
