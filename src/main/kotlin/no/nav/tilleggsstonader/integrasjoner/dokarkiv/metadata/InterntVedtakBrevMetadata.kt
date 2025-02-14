package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Tema

sealed class InterntVedtakBrevMetadata(
    final override val tema: Tema,
    final override val behandlingstema: Behandlingstema,
    override val dokumenttype: Dokumenttype,
    override val tittel: String?,
) : Dokumentmetadata {
    override val journalpostType: JournalpostType = JournalpostType.NOTAT
    override val kanal: String? = null
    override val brevkode: String? = null
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.IS
}
