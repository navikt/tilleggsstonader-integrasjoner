package no.nav.tilleggsstonader.integrasjoner.ensligforsørger

import java.time.LocalDate

data class EnsligForsørgerPerioderResponse(
    val data: EnsligForsørgerPerioder,
)

data class EnsligForsørgerPerioder(
    val personIdent: String,
    val perioder: List<EnsligForsørgerPeriode>,
)

data class EnsligForsørgerPeriode(
    val fomDato: LocalDate,
    val tomDato: LocalDate,
    val stønadstype: EnsligForsørgerStønadstype,
)

enum class EnsligForsørgerStønadstype {
    OVERGANGSSTØNAD,
    SKOLEPENGER,
    BARNETILSYN,
}
