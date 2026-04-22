package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import no.nav.tilleggsstonader.kontrakter.sak.DokumentBrevkode
import org.springframework.stereotype.Component

@Component
data object ReiseTilSamlingTsoSøknadMetadata : SøknadMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.ReiseTilSamlingTSO,
    dokumenttype = Dokumenttype.REISE_TIL_SAMLING_TSO_SØKNAD,
    tittel = "Søknad om ${
        Stønadstype.REISE_TIL_SAMLING_TSO.visningsnavn}",
    brevkode = DokumentBrevkode.REISE_TIL_SAMLING.verdi,
)

@Component
data object ReiseTilSamlingTsoSøknadVedleggMetadata : SøknadVedleggMetadata(
    tema = Tema.TSO,
    dokumenttype = Dokumenttype.REISE_TIL_SAMLING_TSO_SØKNAD_VEDLEGG,
)

@Component
data object ReiseTilSamlingTsoFrittståendeBrevMetadata : FrittståendeBrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.ReiseTilSamlingTSO,
    dokumenttype = Dokumenttype.REISE_TIL_SAMLING_TSO_FRITTSTÅENDE_BREV,
)

@Component
data object ReiseTilSamlingTsoInterntVedtakMetadata : InterntVedtakBrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.ReiseTilSamlingTSO,
    dokumenttype = Dokumenttype.REISE_TIL_SAMLING_TSO_INTERNT_VEDTAK,
    tittel = "Internt vedtak reise til samling",
)

@Component
data object ReiseTilSamlingTsoVedtaksbrevMetadata : VedtaksbrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.ReiseTilSamlingTSO,
    dokumenttype = Dokumenttype.REISE_TIL_SAMLING_TSO_VEDTAKSBREV,
)

@Component
data object ReiseTilSamlingTsoKlageInterntVedtak : KlageInterntVedtak(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.ReiseTilSamlingTSO,
    dokumenttype = Dokumenttype.REISE_TIL_SAMLING_TSO_KLAGE_INTERNT_VEDTAK,
)

@Component
data object ReiseTilSamlingTsoKlageVedtak : KlageVedtak(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.ReiseTilSamlingTSO,
    dokumenttype = Dokumenttype.REISE_TIL_SAMLING_TSO_KLAGE_VEDTAKSBREV,
)
