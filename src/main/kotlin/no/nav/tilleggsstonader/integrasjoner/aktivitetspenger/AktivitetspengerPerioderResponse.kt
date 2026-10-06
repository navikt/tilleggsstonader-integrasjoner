package no.nav.tilleggsstonader.integrasjoner.aktivitetspenger

import java.time.LocalDate

data class AktivitetspengerPerioderResponse(
    val perioder: List<AktivitetspengerPeriode>,
)

data class AktivitetspengerPeriode(
    val fom: LocalDate,
    val tom: LocalDate,
)
