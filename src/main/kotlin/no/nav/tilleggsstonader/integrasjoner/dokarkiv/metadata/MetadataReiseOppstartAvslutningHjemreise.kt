package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import no.nav.tilleggsstonader.kontrakter.sak.DokumentBrevkode
import org.springframework.stereotype.Component

@Component
data object ReiseOppstartAvslutningHjemreiseTsoSøknadMetadata : SøknadMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.ReiseOppstartAvslutningHjemreiseTSO,
    dokumenttype = Dokumenttype.REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSO_SØKNAD,
    tittel = "Søknad om ${Stønadstype.REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSO.visningsnavn}",
    brevkode = DokumentBrevkode.REISE_OPPSTART_AVSLUTNING_ELLER_HJEMREISE.verdi,
)

@Component
data object ReiseOppstartAvslutningHjemreiseTsoSøknadVedleggMetadata : SøknadVedleggMetadata(
    tema = Tema.TSO,
    dokumenttype = Dokumenttype.REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSO_SØKNAD_VEDLEGG,
)

@Component
data object ReiseOppstartAvslutningHjemreiseTsoFrittståendeBrevMetadata : FrittståendeBrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.ReiseOppstartAvslutningHjemreiseTSO,
    dokumenttype = Dokumenttype.REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSO_FRITTSTÅENDE_BREV,
) {
    override val brevkode = "FRITTSTÅENDE_BREV_REISE_OPPSTART_TSO"
}

@Component
data object ReiseOppstartAvslutningHjemreiseTsoInterntVedtakMetadata : InterntVedtakBrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.ReiseOppstartAvslutningHjemreiseTSO,
    dokumenttype = Dokumenttype.REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSO_INTERNT_VEDTAK,
    tittel = "Internt vedtak støtte til reise ved oppstart, avslutning og hjemreise",
)

@Component
data object ReiseOppstartAvslutningHjemreiseTsoVedtaksbrevMetadata : VedtaksbrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.ReiseOppstartAvslutningHjemreiseTSO,
    dokumenttype = Dokumenttype.REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSO_VEDTAKSBREV,
) {
    override val brevkode: String = "${tema.name}_BREV_REISE_OPPSTART_VEDTAK"
}

@Component
data object ReiseOppstartAvslutningHjemreiseTsoKlageInterntVedtak : KlageInterntVedtak(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.ReiseOppstartAvslutningHjemreiseTSO,
    dokumenttype = Dokumenttype.REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSO_KLAGE_INTERNT_VEDTAK,
) {
    override val brevkode: String = "KLAGE_INTERNT_VEDTAK_REISE_OPPSTART_TSO"
}

@Component
data object ReiseOppstartAvslutningHjemreiseTsoKlageVedtak : KlageVedtak(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.ReiseOppstartAvslutningHjemreiseTSO,
    dokumenttype = Dokumenttype.REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSO_KLAGE_VEDTAKSBREV,
) {
    override val brevkode: String = "KLAGE_VEDTAKSBREV_REISE_OPPSTART_TSO"
}

@Component
data object ReiseOppstartAvslutningHjemreiseTsrSøknadMetadata : SøknadMetadata(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.ReiseOppstartAvslutningHjemreiseTSR,
    dokumenttype = Dokumenttype.REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSR_SØKNAD,
    tittel = "Søknad om ${Stønadstype.REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSR.visningsnavn}",
    brevkode = DokumentBrevkode.REISE_OPPSTART_AVSLUTNING_ELLER_HJEMREISE.verdi,
)

@Component
data object ReiseOppstartAvslutningHjemreiseTsrSøknadVedleggMetadata : SøknadVedleggMetadata(
    tema = Tema.TSR,
    dokumenttype = Dokumenttype.REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSR_SØKNAD_VEDLEGG,
)

@Component
data object ReiseOppstartAvslutningHjemreiseTsrFrittståendeBrevMetadata : FrittståendeBrevMetadata(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.ReiseOppstartAvslutningHjemreiseTSR,
    dokumenttype = Dokumenttype.REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSR_FRITTSTÅENDE_BREV,
) {
    override val brevkode = "FRITTSTÅENDE_BREV_REISE_OPPSTART_TSR"
}

@Component
data object ReiseOppstartAvslutningHjemreiseTsrInterntVedtakMetadata : InterntVedtakBrevMetadata(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.ReiseOppstartAvslutningHjemreiseTSR,
    dokumenttype = Dokumenttype.REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSR_INTERNT_VEDTAK,
    tittel = "Internt vedtak støtte til reise ved oppstart, avslutning og hjemreise",
)

@Component
data object ReiseOppstartAvslutningHjemreiseTsrVedtaksbrevMetadata : VedtaksbrevMetadata(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.ReiseOppstartAvslutningHjemreiseTSR,
    dokumenttype = Dokumenttype.REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSR_VEDTAKSBREV,
) {
    override val brevkode: String = "${tema.name}_BREV_REISE_OPPSTART_VEDTAK"
}

@Component
data object ReiseOppstartAvslutningHjemreiseTsrKlageInterntVedtak : KlageInterntVedtak(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.ReiseOppstartAvslutningHjemreiseTSR,
    dokumenttype = Dokumenttype.REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSR_KLAGE_INTERNT_VEDTAK,
) {
    override val brevkode: String = "KLAGE_INTERNT_VEDTAK_REISE_OPPSTART_TSR"
}

@Component
data object ReiseOppstartAvslutningHjemreiseTsrKlageVedtak : KlageVedtak(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.ReiseOppstartAvslutningHjemreiseTSR,
    dokumenttype = Dokumenttype.REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSR_KLAGE_VEDTAKSBREV,
) {
    override val brevkode: String = "KLAGE_VEDTAKSBREV_REISE_OPPSTART_TSR"
}
