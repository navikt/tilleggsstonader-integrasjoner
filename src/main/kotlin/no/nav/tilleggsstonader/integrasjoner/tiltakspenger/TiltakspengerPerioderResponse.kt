package no.nav.tilleggsstonader.integrasjoner.tiltakspenger

import java.time.LocalDate
data class TiltakspengerPerioderResponse(
    val personIdent: String,
    val perioder: List<Periode>,
)

data class Periode(
    val fraOgMedDato: LocalDate,
    val tilOgMedDato: LocalDate,
)
