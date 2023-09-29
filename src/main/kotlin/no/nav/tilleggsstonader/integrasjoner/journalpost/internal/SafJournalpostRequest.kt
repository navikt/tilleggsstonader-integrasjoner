package no.nav.tilleggsstonader.integrasjoner.journalpost.internal

import no.nav.tilleggsstonader.kontrakter.felles.Arkivtema
import no.nav.tilleggsstonader.kontrakter.journalpost.Bruker

data class SafRequestVariabler(val journalpostId: String)

data class SafRequest(
    val brukerId: Bruker,
    val tema: List<Arkivtema>?,
    val journalposttype: String?,
    val journalstatus: List<String>?,
    val antall: Int = 200,
)

data class SafJournalpostRequest(
    val variables: Any,
    val query: String,
)

data class JournalposterForVedleggRequest(
    val brukerId: Bruker,
    val tema: List<Arkivtema>?,
    val dokumenttype: String?,
    val journalpostStatus: String?,
    val antall: Int = 200,
) {
    fun tilSafRequest(): SafRequest {
        return SafRequest(
            brukerId = brukerId,
            tema = tema,
            journalposttype = dokumenttype,
            journalstatus = journalpostStatus?.let { listOf(it) },
            antall = antall,
        )
    }
}
