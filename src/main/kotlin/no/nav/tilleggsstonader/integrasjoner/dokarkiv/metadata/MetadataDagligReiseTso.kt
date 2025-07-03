package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import org.springframework.stereotype.Component

@Component
data object DagligReiseTsoFrittståendeBrevMetadata : FrittståendeBrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.DagligReiseTSO,
    dokumenttype = Dokumenttype.DAGLIG_REISE_TSO_FRITTSTÅENDE_BREV,
)

@Component
data object DagligReiseTsoInterntVedtakMetadata : InterntVedtakBrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.DagligReiseTSO,
    dokumenttype = Dokumenttype.DAGLIG_REISE_TSO_INTERNT_VEDTAK,
    tittel = "Internt vedtak daglig reise TSO",
)

@Component
data object DagligReiseTsoVedtaksbrevMetadata : VedtaksbrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.DagligReiseTSO,
    dokumenttype = Dokumenttype.DAGLIG_REISE_TSO_VEDTAKSBREV,
)

@Component
data object DagligReiseTsoKlageInterntVedtak : KlageInterntVedtak(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.DagligReiseTSO,
    dokumenttype = Dokumenttype.DAGLIG_REISE_TSO_KLAGE_INTERNT_VEDTAK,
)

@Component
data object DagligReiseTsoKlageVedtak : KlageVedtak(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.DagligReiseTSO,
    dokumenttype = Dokumenttype.DAGLIG_REISE_TSO_KLAGE_VEDTAKSBREV,
)
