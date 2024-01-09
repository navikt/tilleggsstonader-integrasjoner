package no.nav.tilleggsstonader.integrasjoner.journalpost.internal

import no.nav.tilleggsstonader.kontrakter.felles.Tema
import no.nav.tilleggsstonader.kontrakter.journalpost.Bruker
import no.nav.tilleggsstonader.kontrakter.journalpost.JournalposterForBrukerRequest
import no.nav.tilleggsstonader.kontrakter.journalpost.Journalposttype
import no.nav.tilleggsstonader.kontrakter.journalpost.Journalstatus

data class SafRequestVariabler(val journalpostId: String)

data class SafRequest(
    val brukerId: Bruker,
    val tema: List<Tema>?,
    val journalposttype: Journalposttype?,
    val journalstatus: List<Journalstatus>?,
    val antall: Int = 200,
)

data class SafJournalpostRequest(
    val variables: Any,
    val query: String,
)

fun JournalposterForBrukerRequest.tilSafRequest(): SafRequest {
    return SafRequest(
        brukerId = brukerId,
        tema = tema,
        journalposttype = journalposttype,
        journalstatus = journalstatus?.let { listOf(it) },
        antall = antall,
    )
}
