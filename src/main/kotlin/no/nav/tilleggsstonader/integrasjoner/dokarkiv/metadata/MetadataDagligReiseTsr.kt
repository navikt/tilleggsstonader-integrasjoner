package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import org.springframework.stereotype.Component

@Component
data object DagligReiseTsrFrittståendeBrevMetadata : FrittståendeBrevMetadata(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.DagligReiseTSR,
    dokumenttype = Dokumenttype.DAGLIG_REISE_TSR_FRITTSTÅENDE_BREV,
)

@Component
data object DagligReiseTsrInterntVedtakMetadata : InterntVedtakBrevMetadata(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.DagligReiseTSR,
    dokumenttype = Dokumenttype.DAGLIG_REISE_TSR_INTERNT_VEDTAK,
    tittel = "Internt vedtak daglig reise TSR",
)

@Component
data object DagligReiseTsrVedtaksbrevMetadata : VedtaksbrevMetadata(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.DagligReiseTSR,
    dokumenttype = Dokumenttype.DAGLIG_REISE_TSR_VEDTAKSBREV,
)

@Component
data object DagligReiseTsrKlageInterntVedtak : KlageInterntVedtak(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.DagligReiseTSR,
    dokumenttype = Dokumenttype.DAGLIG_REISE_TSR_KLAGE_INTERNT_VEDTAK,
)

@Component
data object DagligReiseTsrKlageVedtak : KlageVedtak(
    tema = Tema.TSR,
    behandlingstema = Behandlingstema.DagligReiseTSR,
    dokumenttype = Dokumenttype.DAGLIG_REISE_TSR_KLAGE_VEDTAKSBREV,
)
