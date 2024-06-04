package no.nav.tilleggsstonader.integrasjoner.aap

import java.time.LocalDate

data class AAPPerioderResponse(
    val perioder: List<Periode>,
)

data class Periode(
    val fraOgMedDato: LocalDate,
    val tilOgMedDato: LocalDate,
)
