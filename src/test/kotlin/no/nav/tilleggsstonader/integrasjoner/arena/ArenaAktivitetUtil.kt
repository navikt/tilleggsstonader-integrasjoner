package no.nav.tilleggsstonader.integrasjoner.arena

import no.nav.tilleggsstonader.kontrakter.aktivitet.TypeAktivitet
import java.math.BigDecimal
import java.time.LocalDate

object ArenaAktivitetUtil {
    fun aktivitetArenaResponse(
        aktivitetId: String = "1",
        aktivitetstype: String = TypeAktivitet.ABOPPF.name,
        aktivitetsnavn: String = "aktivitetnavn",
        periode: PeriodeArena =
            PeriodeArena(
                fom = LocalDate.of(2023, 1, 1),
                tom = LocalDate.of(2023, 1, 31),
            ),
        antallDagerPerUke: Int? = 5,
        prosentAktivitetsdeltakelse: BigDecimal? = 100.toBigDecimal(),
        aktivitetsstatus: String? = StatusAktivitetArena.AKTUL.name,
        aktivitetsstatusnavn: String? = "statusnavn",
        erStoenadsberettigetAktivitet: Boolean? = true,
        erUtdanningsaktivitet: Boolean? = false,
        arrangoer: String? = "arrangør",
    ) = AktivitetArenaResponse(
        aktivitetId = aktivitetId,
        aktivitetstype = aktivitetstype,
        aktivitetsnavn = aktivitetsnavn,
        periode = periode,
        antallDagerPerUke = antallDagerPerUke,
        prosentAktivitetsdeltakelse = prosentAktivitetsdeltakelse,
        aktivitetsstatus = aktivitetsstatus,
        aktivitetsstatusnavn = aktivitetsstatusnavn,
        erStoenadsberettigetAktivitet = erStoenadsberettigetAktivitet,
        erUtdanningsaktivitet = erUtdanningsaktivitet,
        arrangoer = arrangoer,
    )
}
