package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import org.springframework.stereotype.Component

@Component
data object BoutgifterFrittståendeBrevMetadata : FrittståendeBrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.Boutgifter,
    dokumenttype = Dokumenttype.BOUTGIFTER_FRITTSTÅENDE_BREV,
)

@Component
data object BoutgifterInterntVedtakMetadata : InterntVedtakBrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.Boutgifter,
    dokumenttype = Dokumenttype.BOUTGIFTER_INTERNT_VEDTAK,
    tittel = "Internt vedtak bolig/overnatting",
)

@Component
data object BoutgifterVedtaksbrevMetadata : VedtaksbrevMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.Boutgifter,
    dokumenttype = Dokumenttype.BOUTGIFTER_VEDTAKSBREV,
)

@Component
data object BoutgifterKlageInterntVedtak : KlageInterntVedtak(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.Boutgifter,
    dokumenttype = Dokumenttype.BOUTGIFTER_KLAGE_INTERNT_VEDTAK,
)

@Component
data object BoutgifterKlageVedtak : KlageVedtak(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.Boutgifter,
    dokumenttype = Dokumenttype.BOUTGIFTER_KLAGE_VEDTAKSBREV,
)
