package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import no.nav.tilleggsstonader.kontrakter.sak.DokumentBrevkode
import org.springframework.stereotype.Component

@Component
data object StøtteTilReiseOppstartAvslutningHjemreiseTsoSøknadMetadata : SøknadMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.StøtteTilReiseOppstartAvslutningHjemreiseTSO,
    dokumenttype = Dokumenttype.STØTTE_TIL_REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSO_SØKNAD,
    tittel = "Søknad om ${Stønadstype.STØTTE_TIL_REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSO.visningsnavn}",
    brevkode = DokumentBrevkode.STØTTE_TIL_REISE_OPPSTART_AVSLUTNING_ELLER_HJEMREISE.verdi,
)

@Component
data object StøtteTilReiseOppstartAvslutningHjemreiseTsoSøknadVedleggMetadata : SøknadVedleggMetadata(
    tema = Tema.TSO,
    dokumenttype = Dokumenttype.STØTTE_TIL_REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSO_SØKNAD_VEDLEGG,
)

@Component
data object StøtteTilReiseOppstartAvslutningHjemreiseTsoFrittståendeBrevMetadata : FrittståendeBrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.StøtteTilReiseOppstartAvslutningHjemreiseTSO,
    dokumenttype = Dokumenttype.STØTTE_TIL_REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSO_FRITTSTÅENDE_BREV,
) {
    override val brevkode = "FRITTSTÅENDE_BREV_STØTTE_TIL_REISE_OPPSTART_TSO"
}

@Component
data object StøtteTilReiseOppstartAvslutningHjemreiseTsoInterntVedtakMetadata : InterntVedtakBrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.StøtteTilReiseOppstartAvslutningHjemreiseTSO,
    dokumenttype = Dokumenttype.STØTTE_TIL_REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSO_INTERNT_VEDTAK,
    tittel = "Internt vedtak støtte til reise ved oppstart, avslutning og hjemreise",
)

@Component
data object StøtteTilReiseOppstartAvslutningHjemreiseTsoVedtaksbrevMetadata : VedtaksbrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.StøtteTilReiseOppstartAvslutningHjemreiseTSO,
    dokumenttype = Dokumenttype.STØTTE_TIL_REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSO_VEDTAKSBREV,
) {
    override val brevkode: String = "${tema.name}_BREV_STØTTE_REISE_OPPSTART_VEDTAK"
}

@Component
data object StøtteTilReiseOppstartAvslutningHjemreiseTsoKlageInterntVedtak : KlageInterntVedtak(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.StøtteTilReiseOppstartAvslutningHjemreiseTSO,
    dokumenttype = Dokumenttype.STØTTE_TIL_REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSO_KLAGE_INTERNT_VEDTAK,
) {
    override val brevkode: String = "KLAGE_INTERNT_VEDTAK_STØTTE_TIL_REISE_OPPSTART_TSO"
}

@Component
data object StøtteTilReiseOppstartAvslutningHjemreiseTsoKlageVedtak : KlageVedtak(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.StøtteTilReiseOppstartAvslutningHjemreiseTSO,
    dokumenttype = Dokumenttype.STØTTE_TIL_REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSO_KLAGE_VEDTAKSBREV,
) {
    override val brevkode: String = "KLAGE_VEDTAKSBREV_STØTTE_REISE_OPPSTART_TSO"
}

@Component
data object StøtteTilReiseOppstartAvslutningHjemreiseTsrSøknadMetadata : SøknadMetadata(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.StøtteTilReiseOppstartAvslutningHjemreiseTSR,
    dokumenttype = Dokumenttype.STØTTE_TIL_REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSR_SØKNAD,
    tittel = "Søknad om ${Stønadstype.STØTTE_TIL_REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSR.visningsnavn}",
    brevkode = DokumentBrevkode.STØTTE_TIL_REISE_OPPSTART_AVSLUTNING_ELLER_HJEMREISE.verdi,
)

@Component
data object StøtteTilReiseOppstartAvslutningHjemreiseTsrSøknadVedleggMetadata : SøknadVedleggMetadata(
    tema = Tema.TSR,
    dokumenttype = Dokumenttype.STØTTE_TIL_REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSR_SØKNAD_VEDLEGG,
)

@Component
data object StøtteTilReiseOppstartAvslutningHjemreiseTsrFrittståendeBrevMetadata : FrittståendeBrevMetadata(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.StøtteTilReiseOppstartAvslutningHjemreiseTSR,
    dokumenttype = Dokumenttype.STØTTE_TIL_REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSR_FRITTSTÅENDE_BREV,
) {
    override val brevkode = "FRITTSTÅENDE_BREV_STØTTE_TIL_REISE_OPPSTART_TSR"
}

@Component
data object StøtteTilReiseOppstartAvslutningHjemreiseTsrInterntVedtakMetadata : InterntVedtakBrevMetadata(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.StøtteTilReiseOppstartAvslutningHjemreiseTSR,
    dokumenttype = Dokumenttype.STØTTE_TIL_REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSR_INTERNT_VEDTAK,
    tittel = "Internt vedtak støtte til reise ved oppstart, avslutning og hjemreise",
)

@Component
data object StøtteTilReiseOppstartAvslutningHjemreiseTsrVedtaksbrevMetadata : VedtaksbrevMetadata(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.StøtteTilReiseOppstartAvslutningHjemreiseTSR,
    dokumenttype = Dokumenttype.STØTTE_TIL_REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSR_VEDTAKSBREV,
) {
    override val brevkode: String = "${tema.name}_BREV_STØTTE_REISE_OPPSTART_VEDTAK"
}

@Component
data object StøtteTilReiseOppstartAvslutningHjemreiseTsrKlageInterntVedtak : KlageInterntVedtak(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.StøtteTilReiseOppstartAvslutningHjemreiseTSR,
    dokumenttype = Dokumenttype.STØTTE_TIL_REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSR_KLAGE_INTERNT_VEDTAK,
) {
    override val brevkode: String = "KLAGE_INTERNT_VEDTAK_STØTTE_TIL_REISE_OPPSTART_TSR"
}

@Component
data object StøtteTilReiseOppstartAvslutningHjemreiseTsrKlageVedtak : KlageVedtak(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.StøtteTilReiseOppstartAvslutningHjemreiseTSR,
    dokumenttype = Dokumenttype.STØTTE_TIL_REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSR_KLAGE_VEDTAKSBREV,
) {
    override val brevkode: String = "KLAGE_VEDTAKSBREV_STØTTE_REISE_OPPSTART_TSR"
}
