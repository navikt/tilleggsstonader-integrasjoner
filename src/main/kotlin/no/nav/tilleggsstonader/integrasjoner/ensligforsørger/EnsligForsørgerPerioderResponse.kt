package no.nav.tilleggsstonader.integrasjoner.ensligforsørger

import java.time.LocalDate

data class EnsligForsørgerPerioderResponse(
    val perioder: List<Periode>,
)

data class Periode(
    val fomDato: LocalDate,
    val tomDato: LocalDate,
)
