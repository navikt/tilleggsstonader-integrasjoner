package no.nav.tilleggsstonader.integrasjoner.tiltakspenger

import java.time.LocalDate

data class TiltakspengerDetaljerResponse(
    val fom: LocalDate,
    val tom: LocalDate,
    val rettighet: RettighetResponseJson,
) {
    enum class RettighetResponseJson {
        TILTAKSPENGER,
        TILTAKSPENGER_OG_BARNETILLEGG,
        INGENTING,
    }
}
