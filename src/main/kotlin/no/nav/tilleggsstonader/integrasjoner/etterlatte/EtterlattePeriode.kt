package no.nav.tilleggsstonader.integrasjoner.etterlatte

import java.time.LocalDate

data class Samordningsvedtak(
    val perioder: List<PeriodeEtterlatte>,
)

data class PeriodeEtterlatte(
    val fom: LocalDate,
    val tom: LocalDate?,
)
