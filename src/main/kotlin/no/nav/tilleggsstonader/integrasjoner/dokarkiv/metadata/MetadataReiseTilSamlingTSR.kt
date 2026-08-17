package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import no.nav.tilleggsstonader.kontrakter.sak.DokumentBrevkode
import org.springframework.stereotype.Component

@Component
data object ReiseTilSamlingTsrSøknadMetadata : SøknadMetadata(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.ReiseTilSamlingTSR,
    dokumenttype = Dokumenttype.REISE_TIL_SAMLING_TSR_SØKNAD,
    tittel = "Søknad om ${Stønadstype.REISE_TIL_SAMLING_TSR.visningsnavn}",
    brevkode = DokumentBrevkode.REISE_TIL_SAMLING.verdi,
)

@Component
data object ReiseTilSamlingTsrSøknadVedleggMetadata : SøknadVedleggMetadata(
    tema = Tema.TSR,
    dokumenttype = Dokumenttype.REISE_TIL_SAMLING_TSR_SØKNAD_VEDLEGG,
)

@Component
data object ReiseTilSamlingTsrFrittståendeBrevMetadata : FrittståendeBrevMetadata(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.ReiseTilSamlingTSR,
    dokumenttype = Dokumenttype.REISE_TIL_SAMLING_TSR_FRITTSTÅENDE_BREV,
)

@Component
data object ReiseTilSamlingTsrInterntVedtakMetadata : InterntVedtakBrevMetadata(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.ReiseTilSamlingTSR,
    dokumenttype = Dokumenttype.REISE_TIL_SAMLING_TSR_INTERNT_VEDTAK,
    tittel = "Internt vedtak reise til samling",
)

@Component
data object ReiseTilSamlingTsrVedtaksbrevMetadata : VedtaksbrevMetadata(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.ReiseTilSamlingTSR,
    dokumenttype = Dokumenttype.REISE_TIL_SAMLING_TSR_VEDTAKSBREV,
)

@Component
data object ReiseTilSamlingTsrKlageInterntVedtak : KlageInterntVedtak(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.ReiseTilSamlingTSR,
    dokumenttype = Dokumenttype.REISE_TIL_SAMLING_TSR_KLAGE_INTERNT_VEDTAK,
)

@Component
data object ReiseTilSamlingTsrKlageVedtak : KlageVedtak(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.ReiseTilSamlingTSR,
    dokumenttype = Dokumenttype.REISE_TIL_SAMLING_TSR_KLAGE_VEDTAKSBREV,
)
