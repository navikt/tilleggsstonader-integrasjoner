package no.nav.tilleggsstonader.integrasjoner.aap

import java.time.LocalDate

data class AAPPerioderResponse(
    val perioder: List<AAPPeriode>,
)

/**
 * Verdier på aktivitetsfaseKode/aktivitetsfaseNavn
 * UA - Under arbeidsavklaring
 * SPE - Sykepengeerstatning
 * IKKE - Ikke spesif. aktivitetsfase
 * UVUP - Vurdering for uføre
 * AU - Arbeidsutprøving
 * FA - Ferdig avklart
 */
data class AAPPeriode(
    val aktivitetsfaseNavn: String,
    val aktivitetsfaseKode: String,
    val periode: Periode,
)

data class Periode(
    val fraOgMedDato: LocalDate,
    val tilOgMedDato: LocalDate,
)
