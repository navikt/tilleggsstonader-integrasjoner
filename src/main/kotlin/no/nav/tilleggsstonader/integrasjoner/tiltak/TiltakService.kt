package no.nav.tilleggsstonader.integrasjoner.tiltak

import no.nav.tilleggsstonader.integrasjoner.tiltak.arbeidsmarkedstiltak.ArbeidsmarkedstiltakClient
import no.nav.tilleggsstonader.integrasjoner.tiltak.arbeidsmarkedstiltak.DeltakerDto
import no.nav.tilleggsstonader.integrasjoner.tiltak.arbeidsmarkedstiltak.DeltakerStatusDto
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class TiltakService(
    private val arbeidsmarkedstiltakClient: ArbeidsmarkedstiltakClient
) {

    fun hentTiltak(ident: String): List<TiltakDto> {
        return arbeidsmarkedstiltakClient.hentDeltakelser(ident).map { tilTiltakDto(it) }
    }

}

private fun tilTiltakDto(deltakelse: DeltakerDto): TiltakDto {
    return TiltakDto(
        id = deltakelse.id.toString(),
        registrertTidspunkt = deltakelse.registrertDato,
        fom = deltakelse.startDato,
        tom = deltakelse.sluttDato,
        status = deltakelse.status.tilStatus(),
        dagerPerUke = deltakelse.dagerPerUke,
        gjennomføring = deltakelse.gjennomforing.let {
            Gjennomføring(
                id = it.id.toString(),
                navn = it.navn,
                typeNavn = it.tiltakstypeNavn,
                arenaKode = mapType(it.type),
                arrangør = Arrangør(
                    navn = it.arrangor.navn,
                    virksomhetsnummer = it.arrangor.virksomhetsnummer
                )
            )
        },
        kilde = KildeTiltak.KOMET,
    )
}

fun mapType(type: String): TiltakArenaType {
    TODO("Not yet implemented")
}

private fun DeltakerStatusDto.tilStatus(): TiltakStatus = when (this) {
    DeltakerStatusDto.AVBRUTT -> TiltakStatus.AVBRUTT
    DeltakerStatusDto.AVBRUTT_UTKAST -> TiltakStatus.AVBRUTT_UTKAST
    DeltakerStatusDto.DELTAR -> TiltakStatus.DELTAR
    DeltakerStatusDto.FEILREGISTRERT -> TiltakStatus.FEILREGISTRERT
    DeltakerStatusDto.FULLFORT -> TiltakStatus.FULLFØRT
    DeltakerStatusDto.IKKE_AKTUELL -> TiltakStatus.IKKE_AKTUELL
    DeltakerStatusDto.HAR_SLUTTET -> TiltakStatus.HAR_SLUTTET
    DeltakerStatusDto.PABEGYNT_REGISTRERING -> TiltakStatus.PÅBEGYNT_REGISTRERING
    DeltakerStatusDto.SOKT_INN -> TiltakStatus.SØKT_INN
    DeltakerStatusDto.VENTELISTE -> TiltakStatus.VENTELISTE
    DeltakerStatusDto.VENTER_PA_OPPSTART -> TiltakStatus.VENTER_PA_OPPSTART
    DeltakerStatusDto.VURDERES -> TiltakStatus.VURDERES
    DeltakerStatusDto.UTKAST_TIL_PAMELDING -> TiltakStatus.UTKAST_TIL_PÅMELDING
}

/**
 * Tiltakspenger har gjort liknende i
 * https://github.com/navikt/tiltakspenger-libs/blob/main/tiltak-dtos/main/no/nav/tiltakspenger/libs/tiltak/TiltakResponsDTO.kt
 */
data class TiltakDto(
    val id: String,
    val registrertTidspunkt: LocalDateTime,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val status: TiltakStatus,
    val dagerPerUke: Float?,
    val gjennomføring: Gjennomføring,
    val kilde: KildeTiltak,
)

data class Gjennomføring(
    val id: String,
    val navn: String,
    val typeNavn: String,
    val arenaKode: TiltakArenaType,
    val arrangør: Arrangør,
)

data class Arrangør(
    val navn: String,
    val virksomhetsnummer: String
)

enum class KildeTiltak {
    KOMET
}

enum class TiltakStatus(val status: String, val rettTilÅSøke: Boolean) {
    VENTER_PA_OPPSTART("Venter på oppstart", true),
    DELTAR("Deltar", true),
    HAR_SLUTTET("Har sluttet", true),
    AVBRUTT("Avbrutt", true),
    FULLFØRT("Fullført", true),

    AVBRUTT_UTKAST("Avbrutt utkast", false), // tiltakspenger hadde ikke denne
    IKKE_AKTUELL("Ikke aktuell", false),
    FEILREGISTRERT("Feilregistrert", false),
    PÅBEGYNT_REGISTRERING("Påbegynt registrering", false),
    SØKT_INN("Søkt inn", false),
    VENTELISTE("Venteliste", false),
    VURDERES("Vurderes", false),
    UTKAST_TIL_PÅMELDING("Utkast til påmelding", false), // tiltakspenger hadde ikke denne
}

enum class TiltakArenaType {

}