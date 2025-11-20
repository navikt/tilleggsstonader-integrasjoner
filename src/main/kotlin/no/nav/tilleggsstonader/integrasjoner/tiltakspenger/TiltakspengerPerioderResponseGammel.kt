package no.nav.tilleggsstonader.integrasjoner.tiltakspenger

import java.time.LocalDate

@Deprecated("Deprekert av team Tiltakspenger", replaceWith = ReplaceWith("TiltakspengerPerioderResponseNy"))
data class TiltakspengerPerioderResponseGammel(
    val periode: Periode,
)

data class Periode(
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
)
