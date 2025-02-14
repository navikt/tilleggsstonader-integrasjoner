package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import no.nav.tilleggsstonader.kontrakter.sak.DokumentBrevkode
import org.springframework.stereotype.Component

@Component
data object LæremidlerSøknadMetadata : SøknadMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.Læremidler,
    dokumenttype = Dokumenttype.LÆREMIDLER_SØKNAD,
    tittel = "Søknad om ${Stønadstype.LÆREMIDLER.visningsnavn}",
    brevkode = DokumentBrevkode.LÆREMIDLER.verdi,
)

@Component
data object LæremidlerSøknadVedleggMetadata : SøknadVedleggMetadata(
    tema = Tema.TSO,
    dokumenttype = Dokumenttype.LÆREMIDLER_SØKNAD_VEDLEGG,
)

@Component
data object LæremidlerFrittståendeBrevMetadata : FrittståendeBrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.Læremidler,
    dokumenttype = Dokumenttype.LÆREMIDLER_FRITTSTÅENDE_BREV,
)

@Component
data object LæremidlerInterntVedtakMetadata : InterntVedtakBrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.Læremidler,
    dokumenttype = Dokumenttype.LÆREMIDLER_INTERNT_VEDTAK,
    tittel = "Internt vedtak læremidler",
)

@Component
data object LæremidlerVedtaksbrevMetadata : VedtaksbrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.Læremidler,
    dokumenttype = Dokumenttype.LÆREMIDLER_VEDTAKSBREV,
)

@Component
data object LæremidlerKlageInterntVedtak : KlageInterntVedtak(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.Læremidler,
    dokumenttype = Dokumenttype.LÆREMIDLER_KLAGE_INTERNT_VEDTAK,
)

@Component
data object LæremidlerKlageVedtak : KlageVedtak(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.Læremidler,
    dokumenttype = Dokumenttype.LÆREMIDLER_KLAGE_VEDTAKSBREV,
)
