package no.nav.tilleggsstonader.integrasjoner.tiltak.arbeidsmarkedstiltak

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * https://github.com/navikt/amt-tiltak/blob/main/external-api/src/main/kotlin/no/nav/amt/tiltak/external/api/dto/DeltakerDto.kt
 */
data class DeltakerDto(
    val id: UUID,
    val gjennomforing: GjennomforingDto,
    val startDato: LocalDate?,
    val sluttDato: LocalDate?,
    val status: DeltakerStatusDto,
    val dagerPerUke: Float?,
    val prosentStilling: Float?,
    val registrertDato: LocalDateTime,
)

data class GjennomforingDto(
    val id: UUID,
    val navn: String,
    val type: String, //Arena type
    val tiltakstypeNavn: String,
    val arrangor: ArrangorDto
)

data class ArrangorDto(
    val virksomhetsnummer: String,
    val navn: String
)

/**
 * tiltakspenger har satt status på om man har rett til å søke eller ikke
 * https://github.com/navikt/tiltakspenger-libs/blob/main/tiltak-dtos/main/no/nav/tiltakspenger/libs/tiltak/TiltakResponsDTO.kt#L36
 */
enum class DeltakerStatusDto {
    UTKAST_TIL_PAMELDING,
    AVBRUTT_UTKAST,
    VENTER_PA_OPPSTART,
    DELTAR,
    HAR_SLUTTET,
    FULLFORT,
    IKKE_AKTUELL,
    FEILREGISTRERT,
    SOKT_INN,
    VURDERES,
    VENTELISTE,
    AVBRUTT,
    PABEGYNT_REGISTRERING
}

