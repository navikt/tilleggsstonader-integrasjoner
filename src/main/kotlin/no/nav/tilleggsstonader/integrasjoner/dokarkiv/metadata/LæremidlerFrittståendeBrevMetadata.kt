package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Fagsystem
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import org.springframework.stereotype.Component

@Component
object LæremidlerFrittståendeBrevMetadata : Dokumentmetadata {
    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: Fagsystem = Fagsystem.TILLEGGSSTONADER
    override val tema: Tema = Tema.TSO
    override val behandlingstema: Behandlingstema = Behandlingstema.Læremidler
    override val kanal: String? = null
    override val dokumenttype: Dokumenttype = Dokumenttype.LÆREMIDLER_FRITTSTÅENDE_BREV
    override val tittel: String? = null
    override val brevkode: String = "TSO_BREV_LÆREMIDLER_FRITTSTÅENDE"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.B
}
