package no.nav.tilleggsstonader.integrasjoner.journalpost.internal

data class SafRequestVariabler(
    val journalpostId: String,
)

data class SafJournalpostRequest(
    val variables: Any,
    val query: String,
)
