package no.nav.tilleggsstonader.integrasjoner.journalpost.internal

import no.nav.tilleggsstonader.kontrakter.journalpost.Journalposttype

data class SafRequestVariabler(
    val journalpostId: String,
)

data class SafJournalpostRequest(
    val variables: Any,
    val query: String,
)

data class SafFagsakVariabler(
    val fagsak: SafFagsakInput,
    val antall: Int = 200,
    val journalposttype: List<Journalposttype> = emptyList(),
)

data class SafFagsakInput(
    val fagsakId: String,
    val fagsaksystem: String,
)
