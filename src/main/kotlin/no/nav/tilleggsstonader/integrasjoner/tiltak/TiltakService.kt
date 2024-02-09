package no.nav.tilleggsstonader.integrasjoner.tiltak

import no.nav.tilleggsstonader.integrasjoner.tiltak.arbeidsmarkedstiltak.ArbeidsmarkedstiltakClient
import no.nav.tilleggsstonader.integrasjoner.tiltak.arbeidsmarkedstiltak.DeltakerDto
import no.nav.tilleggsstonader.integrasjoner.tiltak.arbeidsmarkedstiltak.DeltakerStatusDto
import no.nav.tilleggsstonader.integrasjoner.tiltak.arbeidsmarkedstiltak.GjennomforingDto
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
    DeltakerStatusDto.UTKAST_TIL_PAMELDING -> TODO()
    DeltakerStatusDto.AVBRUTT_UTKAST -> TODO()
    DeltakerStatusDto.VENTER_PA_OPPSTART -> TODO()
    DeltakerStatusDto.DELTAR -> TODO()
    DeltakerStatusDto.HAR_SLUTTET -> TODO()
    DeltakerStatusDto.FULLFORT -> TODO()
    DeltakerStatusDto.IKKE_AKTUELL -> TODO()
    DeltakerStatusDto.FEILREGISTRERT -> TODO()
    DeltakerStatusDto.SOKT_INN -> TODO()
    DeltakerStatusDto.VURDERES -> TODO()
    DeltakerStatusDto.VENTELISTE -> TODO()
    DeltakerStatusDto.AVBRUTT -> TODO()
    DeltakerStatusDto.PABEGYNT_REGISTRERING -> TODO()
}

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

enum class TiltakStatus {

}

enum class TiltakArenaType {

}