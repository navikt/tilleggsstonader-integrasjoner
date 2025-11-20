package no.nav.tilleggsstonader.integrasjoner.tiltakspenger

import java.time.LocalDate
import java.time.OffsetDateTime

data class TiltakspengerPerioderResponseNy(
    val rettighet: RettighetDto,
    val kilde: KildeDto,
    val vedtaksperiode: PeriodeDto,
    val innvilgelsesperioder: List<PeriodeDto>,
    val omgjortAvRammevedtakId: String?, // alltid null i vedtakene fra Arena
    val omgjorRammevedtakId: String?, // alltid null i vedtakene fra Arena
    val vedtakstidspunkt: OffsetDateTime?,
) {
    enum class RettighetDto {
        TILTAKSPENGER,
        TILTAKSPENGER_OG_BARNETILLEGG,
        INGENTING,
    }

    data class PeriodeDto(
        val fraOgMed: LocalDate,
        val tilOgMed: LocalDate,
    )

    enum class KildeDto {
        TPSAK,
        ARENA,
    }
}
