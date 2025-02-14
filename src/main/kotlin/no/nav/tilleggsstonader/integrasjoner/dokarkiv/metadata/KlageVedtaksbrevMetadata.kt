package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Fagsystem
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import org.springframework.stereotype.Component

sealed class KlageVedtak(
    final override val tema: Tema,
    final override val behandlingstema: Behandlingstema,
    override val dokumenttype: Dokumenttype,
) : Dokumentmetadata {
    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val kanal: String? = null
    override val tittel: String? = null
    override val brevkode: String = "KLAGE_VEDTAKSBREV_${behandlingstema.name.uppercase()}"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.VB
}
