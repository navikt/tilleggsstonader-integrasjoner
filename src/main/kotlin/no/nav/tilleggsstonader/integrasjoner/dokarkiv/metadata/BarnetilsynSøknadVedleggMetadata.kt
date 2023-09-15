package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Fagsystem
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import org.springframework.stereotype.Component

@Component
object BarnetilsynSøknadVedleggMetadata : Dokumentmetadata {

    override val journalpostType: JournalpostType = JournalpostType.INNGAAENDE
    override val fagsakSystem: Fagsystem = Fagsystem.TILLEGGSSTONADER
    override val tema: Tema = Tema.TSO
    override val behandlingstema: Behandlingstema? = null
    override val kanal: String? = null
    override val dokumenttype: Dokumenttype = Dokumenttype.BARNETILSYN_SØKNAD_VEDLEGG
    override val tittel: String? = null
    override val brevkode: String? = null
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.IS
}
