package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import no.nav.tilleggsstonader.kontrakter.sak.DokumentBrevkode
import org.springframework.stereotype.Component

@Component
data object FlyttingTsoSøknadMetadata : SøknadMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.FlyttingTSO,
    dokumenttype = Dokumenttype.FLYTTING_TSO_SØKNAD,
    tittel = "Søknad om ${Stønadstype.FLYTTING_TSO.visningsnavn}",
    brevkode = DokumentBrevkode.FLYTTING.verdi,
)

@Component
data object FlyttingTsoSøknadVedleggMetadata : SøknadVedleggMetadata(
    tema = Tema.TSO,
    dokumenttype = Dokumenttype.FLYTTING_TSO_SØKNAD_VEDLEGG,
)

@Component
data object FlyttingTsoFrittståendeBrevMetadata : FrittståendeBrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.FlyttingTSO,
    dokumenttype = Dokumenttype.FLYTTING_TSO_FRITTSTÅENDE_BREV,
)

@Component
data object FlyttingTsoInterntVedtakMetadata : InterntVedtakBrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.FlyttingTSO,
    dokumenttype = Dokumenttype.FLYTTING_TSO_INTERNT_VEDTAK,
    tittel = "Internt vedtak flytting",
)

@Component
data object FlyttingTsoVedtaksbrevMetadata : VedtaksbrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.FlyttingTSO,
    dokumenttype = Dokumenttype.FLYTTING_TSO_VEDTAKSBREV,
)

@Component
data object FlyttingTsoKlageInterntVedtak : KlageInterntVedtak(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.FlyttingTSO,
    dokumenttype = Dokumenttype.FLYTTING_TSO_KLAGE_INTERNT_VEDTAK,
)

@Component
data object FlyttingTsoKlageVedtak : KlageVedtak(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.FlyttingTSO,
    dokumenttype = Dokumenttype.FLYTTING_TSO_KLAGE_VEDTAKSBREV,
)

@Component
data object FlyttingTsrSøknadMetadata : SøknadMetadata(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.FlyttingTSR,
    dokumenttype = Dokumenttype.FLYTTING_TSR_SØKNAD,
    tittel = "Søknad om ${Stønadstype.FLYTTING_TSR.visningsnavn}",
    brevkode = DokumentBrevkode.FLYTTING.verdi,
)

@Component
data object FlyttingTsrSøknadVedleggMetadata : SøknadVedleggMetadata(
    tema = Tema.TSR,
    dokumenttype = Dokumenttype.FLYTTING_TSR_SØKNAD_VEDLEGG,
)

@Component
data object FlyttingTsrFrittståendeBrevMetadata : FrittståendeBrevMetadata(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.FlyttingTSR,
    dokumenttype = Dokumenttype.FLYTTING_TSR_FRITTSTÅENDE_BREV,
)

@Component
data object FlyttingTsrInterntVedtakMetadata : InterntVedtakBrevMetadata(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.FlyttingTSR,
    dokumenttype = Dokumenttype.FLYTTING_TSR_INTERNT_VEDTAK,
    tittel = "Internt vedtak flytting",
)

@Component
data object FlyttingTsrVedtaksbrevMetadata : VedtaksbrevMetadata(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.FlyttingTSR,
    dokumenttype = Dokumenttype.FLYTTING_TSR_VEDTAKSBREV,
)

@Component
data object FlyttingTsrKlageInterntVedtak : KlageInterntVedtak(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.FlyttingTSR,
    dokumenttype = Dokumenttype.FLYTTING_TSR_KLAGE_INTERNT_VEDTAK,
)

@Component
data object FlyttingTsrKlageVedtak : KlageVedtak(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.FlyttingTSR,
    dokumenttype = Dokumenttype.FLYTTING_TSR_KLAGE_VEDTAKSBREV,
)
