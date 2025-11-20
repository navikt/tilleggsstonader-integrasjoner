package no.nav.tilleggsstonader.integrasjoner.tiltakspenger

import java.time.LocalDate

data class VedtakRequestDto(
    val ident: String,
    val fom: LocalDate,
    val tom: LocalDate,
)
