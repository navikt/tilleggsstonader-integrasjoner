package no.nav.tilleggsstonader.integrasjoner.ytelse

import no.nav.tilleggsstonader.kontrakter.felles.Periode
import java.time.LocalDate

/**
 * Brukes som nøkkel for cache.
 */
data class HentYtelserCacheData(
    val ident: String,
    override val fom: LocalDate,
    override val tom: LocalDate,
) : Periode<LocalDate> {
    /**
     * Overrider for å unngå at hele identen logges i vanlig logg
     */
    override fun toString(): String {
        val anonymisertIdent = if (ident.length > 6) "${ident.substring(0, 6)}*****" else ident
        return "HentYtelserData(ident='$anonymisertIdent', fom=$fom, tom=$tom)"
    }
}
