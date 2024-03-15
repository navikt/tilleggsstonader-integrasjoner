package no.nav.tilleggsstonader.integrasjoner.arena

import no.nav.tilleggsstonader.integrasjoner.util.DatoUtil.max
import no.nav.tilleggsstonader.integrasjoner.util.DatoUtil.min
import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetDto
import no.nav.tilleggsstonader.kontrakter.aktivitet.Kilde
import no.nav.tilleggsstonader.kontrakter.aktivitet.StatusAktivitet
import no.nav.tilleggsstonader.kontrakter.aktivitet.TypeAktivitet
import java.time.LocalDate

object AktivitetDtoMapper {
    fun map(response: AktivitetArenaResponse): AktivitetDto {
        return AktivitetDto(
            id = response.aktivitetId,
            // TODO Arena skal sjekke opp om de noen ganger snur på fom/tom
            fom = min(response.periode.fom, response.periode.tom),
            tom = max(response.periode.fom, response.periode.tom),
            type = TypeAktivitet.valueOf(response.aktivitetstype),
            status = mapStatus(response),
            antallDagerPerUke = response.antallDagerPerUke,
            prosentDeltakelse = response.prosentAktivitetsdeltakelse,
            arrangør = response.arrangoer,
            kilde = Kilde.ARENA,
        )
    }

    /**
     * Mapping i tiltakspenger som får en annen type status, deltakerstatus
     * https://github.com/navikt/tiltakspenger-tiltak/blob/ed78c33857e20492e8fd0c540093cb11a380b271/src/main/kotlin/no/nav/tiltakspenger/tiltak/services/RouteServiceImpl.kt#L111
     */
    private fun mapStatus(response: AktivitetArenaResponse): StatusAktivitet? {
        val fom = min(response.periode.fom, response.periode.tom) ?: LocalDate.MAX
        val startDatoErFremITid = fom.isAfter(LocalDate.now())

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
