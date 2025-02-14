package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Fagsystem
import no.nav.tilleggsstonader.kontrakter.felles.Tema

sealed interface Dokumentmetadata {
    val journalpostType: JournalpostType
    val fagsakSystem: Fagsystem?
    val tema: Tema
    val behandlingstema: Behandlingstema?
    val kanal: String?
    val dokumenttype: Dokumenttype
    val tittel: String?
    val brevkode: String? // NB: Maks lengde som er støttet i joark er 50 tegn
    val dokumentKategori: Dokumentkategori
}

enum class Dokumentkategori(
    private val beskrivelse: String,
) {
    B("Brev"),
    VB("Vedtaksbrev"),
    IB("Infobrev"),
    ES("Elektronisk skjema"),
    TS("Tolkbart skjema"),
    IS("Ikke tolkbart skjema"),
    KS("Konverterte data fra system"),
    KD("Konvertert fra elektronisk arkiv"),
    SED("SED"),
    PUBL_BLANKETT_EOS("Pb EØS"),
    ELEKTRONISK_DIALOG("Elektronisk dialog"),
    REFERAT("Referat"),
    FORVALTNINGSNOTAT("Forvaltningsnotat"), // DENNE BLIR SYNLIG TIL SLUTTBRUKER!
    SOK("Søknad"),
    KA("Klage eller anke"),
}

fun Dokumenttype.tilMetadata(): Dokumentmetadata =
    when (this) {
        Dokumenttype.BARNETILSYN_SØKNAD -> BarnetilsynSøknadMetadata
        Dokumenttype.BARNETILSYN_SØKNAD_VEDLEGG -> BarnetilsynSøknadVedleggMetadata
        Dokumenttype.BARNETILSYN_VEDTAKSBREV -> BarnetilsynVedtaksbrevMetadata
        Dokumenttype.BARNETILSYN_FRITTSTÅENDE_BREV -> BarnetilsynFrittståendeBrevMetadata
        Dokumenttype.BARNETILSYN_INTERNT_VEDTAK -> BarnetilsynInterntVedtakMetadata
        Dokumenttype.BARNETILSYN_KLAGE_VEDTAKSBREV -> BarnetilsynKlageVedtak
        Dokumenttype.BARNETILSYN_KLAGE_INTERNT_VEDTAK -> BarnetilsynKlageInterntVedtak

        Dokumenttype.LÆREMIDLER_SØKNAD -> LæremidlerSøknadMetadata
        Dokumenttype.LÆREMIDLER_SØKNAD_VEDLEGG -> LæremidlerSøknadVedleggMetadata
        Dokumenttype.LÆREMIDLER_VEDTAKSBREV -> LæremidlerVedtaksbrevMetadata
        Dokumenttype.LÆREMIDLER_FRITTSTÅENDE_BREV -> LæremidlerFrittståendeBrevMetadata
        Dokumenttype.LÆREMIDLER_INTERNT_VEDTAK -> LæremidlerInterntVedtakMetadata
        Dokumenttype.LÆREMIDLER_KLAGE_VEDTAKSBREV -> LæremidlerKlageVedtak
        Dokumenttype.LÆREMIDLER_KLAGE_INTERNT_VEDTAK -> LæremidlerKlageInterntVedtak
    }
