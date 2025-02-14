package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import org.springframework.stereotype.Component

@Component
data object LæremidlerSøknadVedleggMetadata  : SøknadVedleggMetadata(
    tema = Tema.TSO,
    dokumenttype = Dokumenttype.LÆREMIDLER_SØKNAD_VEDLEGG,
)
