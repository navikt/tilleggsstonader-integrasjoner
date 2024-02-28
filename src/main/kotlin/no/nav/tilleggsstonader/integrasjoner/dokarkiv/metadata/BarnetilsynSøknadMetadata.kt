package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Fagsystem
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import org.springframework.stereotype.Component

@Component
object BarnetilsynSøknadMetadata : Dokumentmetadata {

    override val journalpostType: JournalpostType = JournalpostType.INNGAAENDE
    override val fagsakSystem: Fagsystem = Fagsystem.TILLEGGSSTONADER
    override val tema: Tema = Tema.TSO
    override val behandlingstema: Behandlingstema = Behandlingstema.TilsynBarn
    override val kanal: String = "NAV_NO"
    override val dokumenttype: Dokumenttype = Dokumenttype.BARNETILSYN_SØKNAD
    override val tittel: String = "Søknad om støtte til tilsyn av barn"
    override val brevkode: String = "NAV 11-12.15"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.SOK
}
