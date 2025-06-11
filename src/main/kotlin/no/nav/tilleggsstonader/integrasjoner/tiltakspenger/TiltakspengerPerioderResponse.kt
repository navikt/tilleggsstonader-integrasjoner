package no.nav.tilleggsstonader.integrasjoner.tiltakspenger

import java.time.LocalDate

data class TiltakspengerPerioderResponse(
    val periode: Periode,
)
data class Periode(
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
)
