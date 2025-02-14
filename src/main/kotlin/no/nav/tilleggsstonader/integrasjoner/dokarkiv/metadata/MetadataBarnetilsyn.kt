package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import no.nav.tilleggsstonader.kontrakter.sak.DokumentBrevkode
import org.springframework.stereotype.Component

@Component
data object BarnetilsynSøknadMetadata : SøknadMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.TilsynBarn,
    dokumenttype = Dokumenttype.BARNETILSYN_SØKNAD,
    tittel = "Søknad om ${Stønadstype.BARNETILSYN.visningsnavn}",
    brevkode = DokumentBrevkode.BARNETILSYN.verdi,
)

@Component
data object BarnetilsynSøknadVedleggMetadata : SøknadVedleggMetadata(
    tema = Tema.TSO,
    dokumenttype = Dokumenttype.BARNETILSYN_SØKNAD_VEDLEGG,
)

@Component
data object BarnetilsynFrittståendeBrevMetadata : FrittståendeBrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.TilsynBarn,
    dokumenttype = Dokumenttype.BARNETILSYN_FRITTSTÅENDE_BREV,
)

@Component
data object BarnetilsynInterntVedtakMetadata : InterntVedtakBrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.TilsynBarn,
    dokumenttype = Dokumenttype.BARNETILSYN_INTERNT_VEDTAK,
    tittel = "Internt vedtak tilsyn barn",
)

@Component
data object BarnetilsynVedtaksbrevMetadata : VedtaksbrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.TilsynBarn,
    dokumenttype = Dokumenttype.BARNETILSYN_VEDTAKSBREV,
)

@Component
data object BarnetilsynKlageInterntVedtak : KlageInterntVedtak(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.TilsynBarn,
    dokumenttype = Dokumenttype.BARNETILSYN_KLAGE_INTERNT_VEDTAK,
)

@Component
data object BarnetilsynKlageVedtak : KlageVedtak(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.TilsynBarn,
    dokumenttype = Dokumenttype.BARNETILSYN_KLAGE_VEDTAKSBREV,
)
