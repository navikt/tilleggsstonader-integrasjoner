package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Tema

sealed class SøknadMetadata(
    final override val tema: Tema,
    final override val behandlingstema: Behandlingstema,
    override val dokumenttype: Dokumenttype,
    override val tittel: String?,
    override val brevkode: String,
) : Dokumentmetadata {
    override val journalpostType: JournalpostType = JournalpostType.INNGAAENDE
    override val kanal: String = "NAV_NO"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.SOK
}
