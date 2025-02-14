package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Fagsystem
import no.nav.tilleggsstonader.kontrakter.felles.Tema

sealed class KlageInterntVedtak(
    final override val tema: Tema,
    final override val behandlingstema: Behandlingstema,
    override val dokumenttype: Dokumenttype,
) : Dokumentmetadata {
    override val fagsakSystem: Fagsystem = Fagsystem.TILLEGGSSTONADER
    override val journalpostType: JournalpostType = JournalpostType.NOTAT
    override val kanal: String? = null
    override val tittel: String? = null
    override val brevkode: String = "KLAGE_INTERNT_VEDTAK_${behandlingstema.name.uppercase()}"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.VB
}
