package no.nav.tilleggsstonader.integrasjoner.arena

import no.nav.tilleggsstonader.integrasjoner.util.DatoUtil.min
import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetArenaDto
import no.nav.tilleggsstonader.kontrakter.aktivitet.Kilde
import no.nav.tilleggsstonader.kontrakter.aktivitet.StatusAktivitet
import no.nav.tilleggsstonader.kontrakter.aktivitet.TypeAktivitet
import no.nav.tilleggsstonader.libs.utils.osloDateNow
import java.math.BigDecimal
import java.time.LocalDate

object AktivitetDtoMapper {
    fun map(response: AktivitetArenaResponse): AktivitetArenaDto =
        AktivitetArenaDto(
            id = response.aktivitetId,
            fom = response.periode.fom,
            tom = response.periode.tom,
            type = response.aktivitetstype,
            typeNavn = response.aktivitetsnavn,
            status = mapStatus(response),
            statusArena = response.status?.name,
            antallDagerPerUke = response.antallDagerPerUke?.takeIf { it != 0 },
            prosentDeltakelse = response.prosentAktivitetsdeltakelse?.takeIf { it != BigDecimal.ZERO },
            erStønadsberettiget =
                response.erStoenadsberettigetAktivitet == true || TypeAktivitet.valueOf(response.aktivitetstype).rettTilStønad,
            erUtdanning = response.erUtdanningsaktivitet,
            arrangør = response.arrangoer?.takeIf { it.isNotBlank() },
            kilde = Kilde.ARENA,
        )

    /**
     * Mapping i tiltakspenger som får en annen type status, deltakerstatus
     * https://github.com/navikt/tiltakspenger-tiltak/blob/ed78c33857e20492e8fd0c540093cb11a380b271/src/main/kotlin/no/nav/tiltakspenger/tiltak/services/RouteServiceImpl.kt#L111
     *
     * Mapper til nye verdier for å unngå logikk andre steder koblet til om man venter på oppstart eller deltar på aktivitet
     */
    private fun mapStatus(response: AktivitetArenaResponse): StatusAktivitet? {
        val fom = min(response.periode.fom, response.periode.tom) ?: LocalDate.MAX
        val startDatoErFremITid = fom.isAfter(osloDateNow())

        return when (response.status) {
            StatusAktivitetArena.AKTUL -> StatusAktivitet.AKTUELL
            StatusAktivitetArena.AVBR -> StatusAktivitet.AVBRUTT
            StatusAktivitetArena.BEHOV -> StatusAktivitet.BEHOV
            StatusAktivitetArena.DLTAV -> StatusAktivitet.AVBRUTT
            StatusAktivitetArena.FULLF -> StatusAktivitet.FULLFØRT
            StatusAktivitetArena.GJENN -> if (startDatoErFremITid) StatusAktivitet.VENTER_PA_OPPSTART else StatusAktivitet.DELTAR
            StatusAktivitetArena.GJNAV -> StatusAktivitet.AVBRUTT
            StatusAktivitetArena.JATLB -> if (startDatoErFremITid) StatusAktivitet.VENTER_PA_OPPSTART else StatusAktivitet.DELTAR
            StatusAktivitetArena.OPPHO -> StatusAktivitet.OPPHØRT
            StatusAktivitetArena.OVERF -> StatusAktivitet.OVERFØRT
            StatusAktivitetArena.TILBU -> if (startDatoErFremITid) StatusAktivitet.VENTER_PA_OPPSTART else StatusAktivitet.DELTAR
            StatusAktivitetArena.VENTL -> StatusAktivitet.VENTELISTE
            StatusAktivitetArena.FRAF -> StatusAktivitet.IKKE_AKTUELL
            StatusAktivitetArena.PLAN -> StatusAktivitet.PLANLAGT
            null -> null
        }
    }
}
