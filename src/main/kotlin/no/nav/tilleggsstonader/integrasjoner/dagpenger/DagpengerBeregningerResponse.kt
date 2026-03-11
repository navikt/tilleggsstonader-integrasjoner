package no.nav.tilleggsstonader.integrasjoner.dagpenger

import no.nav.tilleggsstonader.kontrakter.ytelse.GjenståendeDagerFraTelleverk
import java.time.LocalDate

data class DagpengerBeregningerResponse(
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val sats: Int,
    val utbetaltBeløp: Int,
    val gjenståendeDager: Int,
    val kilde: String,
) {
    fun tilDomene() =
        GjenståendeDagerFraTelleverk(
            dato = tilOgMed,
            antallDager = gjenståendeDager,
        )
}
