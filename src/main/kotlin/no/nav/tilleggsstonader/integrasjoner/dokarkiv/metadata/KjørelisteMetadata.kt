package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import no.nav.tilleggsstonader.kontrakter.sak.DokumentBrevkode
import org.springframework.stereotype.Component

@Component
data object DagligReiseTsoKjørelisteMetadata : Dokumentmetadata {
    override val journalpostType: JournalpostType = JournalpostType.INNGAAENDE
    override val tema: Tema = Tema.TSO
    override val behandlingstema: Behandlingstema? = Behandlingstema.DagligReiseTSO
    override val kanal: String? = "NAV_NO"
    override val dokumenttype: Dokumenttype = Dokumenttype.DAGLIG_REISE_TSO_KJØRELISTE
    override val tittel: String? = "Refusjon av utgifter til daglig reise med bruk av egen bil"
    override val brevkode: String? = DokumentBrevkode.DAGLIG_REISE_KJØRELISTE.verdi
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.TS
}

@Component
data object DagligReiseTsrKjørelisteMetadata : Dokumentmetadata {
    override val journalpostType: JournalpostType = JournalpostType.INNGAAENDE
    override val tema: Tema = Tema.TSR
    override val behandlingstema: Behandlingstema? = Behandlingstema.DagligReiseTSR
    override val kanal: String? = "NAV_NO"
    override val dokumenttype: Dokumenttype = Dokumenttype.DAGLIG_REISE_TSR_KJØRELISTE
    override val tittel: String? = "Refusjon av utgifter til daglig reise med bruk av egen bil"
    override val brevkode: String? = DokumentBrevkode.DAGLIG_REISE_KJØRELISTE.verdi
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.TS
}

sealed class DagligReiseKjørelisteVedleggMetadata(
    final override val tema: Tema,
    override val dokumenttype: Dokumenttype,
) : Dokumentmetadata {
    override val journalpostType: JournalpostType = JournalpostType.INNGAAENDE
    override val behandlingstema: Behandlingstema? = null
    override val kanal: String? = null
    override val tittel: String? = null
    override val brevkode: String? = null
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.IS
}

@Component
data object DagligReiseTsoKjørelisteVedleggMetadata : DagligReiseKjørelisteVedleggMetadata(
    tema = Tema.TSO,
    dokumenttype = Dokumenttype.DAGLIG_REISE_TSO_KJØRELISTE_VEDLEGG,
)

@Component
data object DagligReiseTsrKjørelisteVedlegg : DagligReiseKjørelisteVedleggMetadata(
    tema = Tema.TSR,
    dokumenttype = Dokumenttype.DAGLIG_REISE_TSR_KJØRELISTE_VEDLEGG,
)
