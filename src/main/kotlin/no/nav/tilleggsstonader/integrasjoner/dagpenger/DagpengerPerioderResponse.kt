package no.nav.tilleggsstonader.integrasjoner.dagpenger

import java.time.LocalDate

data class DagpengerPerioderResponse(
    val personIdent: String,
    val perioder: List<Periode>,
)

data class Periode(
    val fraOgMedDato: LocalDate,
    val tilOgMedDato: LocalDate,
    val ytelseType: DagpengerYtelseType,
)

enum class DagpengerYtelseType {
    DAGPENGER_ARBEIDSSOKER_ORDINAER,
    DAGPENGER_PERMITTERING_ORDINAER,
    DAGPENGER_PERMITTERING_FISKEINDUSTRI,
}
