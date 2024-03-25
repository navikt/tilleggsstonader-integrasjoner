package no.nav.tilleggsstonader.integrasjoner.util

import no.nav.tilleggsstonader.integrasjoner.util.DatoUtil.max
import no.nav.tilleggsstonader.integrasjoner.util.DatoUtil.min
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class DatoUtilTest {

    val DATO_2022 = LocalDate.of(2022, 1, 1)
    val DATO_2023 = LocalDate.of(2023, 1, 1)

    @Nested
    inner class Min {

        @Test
        fun `skal finne min-dato`() {
            assertThat(min(null, null)).isNull()
            assertThat(min(null, DATO_2022)).isEqualTo(DATO_2022)
            assertThat(min(DATO_2022, null)).isEqualTo(DATO_2022)
            assertThat(min(DATO_2022, DATO_2022)).isEqualTo(DATO_2022)
            assertThat(min(DATO_2022, DATO_2023)).isEqualTo(DATO_2022)
            assertThat(min(DATO_2023, DATO_2022)).isEqualTo(DATO_2022)
        }
    }

    @Nested
    inner class Max {

        @Test
        fun `skal finne min-dato`() {
            assertThat(max(null, null)).isNull()
            assertThat(max(null, DATO_2022)).isEqualTo(DATO_2022)
            assertThat(max(DATO_2022, null)).isEqualTo(DATO_2022)
            assertThat(max(DATO_2022, DATO_2022)).isEqualTo(DATO_2022)
            assertThat(max(DATO_2022, DATO_2023)).isEqualTo(DATO_2023)
            assertThat(max(DATO_2023, DATO_2022)).isEqualTo(DATO_2023)
        }
    }
}
