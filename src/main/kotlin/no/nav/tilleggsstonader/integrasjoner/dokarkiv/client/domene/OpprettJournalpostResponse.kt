package no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene

import no.nav.tilleggsstonader.kontrakter.dokarkiv.DokumentInfo

class OpprettJournalpostResponse(
    val journalpostId: String? = null,
    val melding: String? = null,
    val journalpostferdigstilt: Boolean? = false,
    val dokumenter: List<DokumentInfo>? = null,
)
