package no.nav.tilleggsstonader.integrasjoner.arena

import no.nav.tilleggsstonader.integrasjoner.arena.ArenaAktivitetUtil.aktivitetArenaResponse
import no.nav.tilleggsstonader.kontrakter.aktivitet.StatusAktivitet
import no.nav.tilleggsstonader.kontrakter.aktivitet.TypeAktivitet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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
}
