package no.nav.tilleggsstonader.integrasjoner.ytelse

import java.time.LocalDate

/**
 * Brukes som nøkkel for cache.
 */
data class HentYtelserCacheData(
    val ident: String,
    val fom: LocalDate,
    val tom: LocalDate,
) {
    /**
     * Overrider for å unngå at hele identen logges i vanlig logg
     */
    override fun toString(): String {
        return "HentYtelserData(ident='${ident.substring(0, 6)}', fom=$fom, tom=$tom)"
    }
}
