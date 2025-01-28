package no.nav.tilleggsstonader.integrasjoner.journalpost.internal

import no.nav.tilleggsstonader.kontrakter.journalpost.Journalpost

class SafJournalpostBrukerData(
    val dokumentoversiktBruker: Journalpostliste,
)

class Journalpostliste(
    val journalposter: List<Journalpost>,
)
