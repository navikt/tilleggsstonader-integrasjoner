package no.nav.tilleggsstonader.integrasjoner.journalpost.internal

data class SafRequestVariabler(
    val journalpostId: String,
)

data class SafJournalpostRequest(
    val variables: Any,
    val query: String,
)

data class SafFagsakVariabler(
    val fagsak: SafFagsakInput,
)

data class SafFagsakInput(
    val fagsakId: String,
    val fagsaksystem: String,
)
