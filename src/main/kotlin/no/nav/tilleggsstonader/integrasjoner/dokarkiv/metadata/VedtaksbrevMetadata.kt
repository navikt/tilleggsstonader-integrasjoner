package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Fagsystem
import no.nav.tilleggsstonader.kontrakter.felles.Tema

sealed class VedtaksbrevMetadata(
    final override val tema: Tema,
    override val behandlingstema: Behandlingstema,
    override val dokumenttype: Dokumenttype,
) : Dokumentmetadata {
    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: Fagsystem = Fagsystem.TILLEGGSSTONADER
    override val kanal: String? = null
    override val tittel: String? = null
    override val brevkode: String = "${tema.name}_BREV_${behandlingstema.name.uppercase()}_VEDTAK"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.VB
}