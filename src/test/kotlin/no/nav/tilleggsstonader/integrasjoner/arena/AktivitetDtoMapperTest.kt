package no.nav.tilleggsstonader.integrasjoner.arena

import no.nav.tilleggsstonader.kontrakter.aktivitet.StatusAktivitet
import no.nav.tilleggsstonader.kontrakter.aktivitet.TypeAktivitet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class AktivitetDtoMapperTest {
    @Test
    fun `mapping av felter`() {
        val dto = AktivitetDtoMapper.map(aktivitetArenaResponse())
        assertThat(dto.id).isEqualTo("1")
        assertThat(dto.type).isEqualTo("ABOPPF")
        assertThat(dto.typeNavn).isEqualTo("aktivitetnavn")
        assertThat(dto.fom).isEqualTo(LocalDate.of(2023, 1, 1))
        assertThat(dto.tom).isEqualTo(LocalDate.of(2023, 1, 31))
        assertThat(dto.antallDagerPerUke).isEqualTo(5)
        assertThat(dto.prosentDeltakelse).isEqualTo(100.toBigDecimal())
        assertThat(dto.status).isEqualTo(StatusAktivitet.AKTUELL)
        assertThat(dto.statusArena).isEqualTo("AKTUL")
        assertThat(dto.erStønadsberettiget).isTrue
        assertThat(dto.erUtdanning).isFalse
        assertThat(dto.arrangør).isEqualTo("arrangør")
    }

    @Test
    fun `skal mappe 0 aktivitetsdager til null`() {
        val dto = AktivitetDtoMapper.map(aktivitetArenaResponse(antallDagerPerUke = 0))
        assertThat(dto.antallDagerPerUke).isNull()
    }

    @Test
    fun `skal mappe 0 prosent til null`() {
        val dto = AktivitetDtoMapper.map(aktivitetArenaResponse(prosentAktivitetsdeltakelse = 0.toBigDecimal()))
        assertThat(dto.prosentDeltakelse).isNull()
    }

    @Nested
    inner class ErStoenadsberettigetAktivitet {
        /*
         * skal overstyre erStoenadsberettigetAktivitet dersom vi har overstyrt den i kontrakter sånn at den vises i
         * søknad og behandling
         */
        @Test
        fun `skal overstyre erStoenadsberettigetAktivitet dersom vi har overstyrt den i kontrakter`() {
            val dto =
                AktivitetDtoMapper.map(
                    aktivitetArenaResponse(
                        aktivitetstype = TypeAktivitet.FORSFAGGRU.name,
                        erStoenadsberettigetAktivitet = false,
                    ),
                )

            assertThat(dto.erStønadsberettiget).isTrue()
        }
    }

    private fun aktivitetArenaResponse(
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
