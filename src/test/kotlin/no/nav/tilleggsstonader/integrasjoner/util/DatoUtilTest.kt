package no.nav.tilleggsstonader.integrasjoner.util

import no.nav.tilleggsstonader.integrasjoner.util.DatoUtil.max
import no.nav.tilleggsstonader.integrasjoner.util.DatoUtil.min
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class DatoUtilTest {
    val dato2022: LocalDate = LocalDate.of(2022, 1, 1)
    val dato2023: LocalDate = LocalDate.of(2023, 1, 1)

    @Nested
    inner class Min {
        @Test
        fun `skal finne min-dato`() {
            assertThat(min(null, null)).isNull()
            assertThat(min(null, dato2022)).isEqualTo(dato2022)
            assertThat(min(dato2022, null)).isEqualTo(dato2022)
            assertThat(min(dato2022, dato2022)).isEqualTo(dato2022)
            assertThat(min(dato2022, dato2023)).isEqualTo(dato2022)
            assertThat(min(dato2023, dato2022)).isEqualTo(dato2022)
        }
    }

    @Nested
    inner class Max {
        @Test
        fun `skal finne min-dato`() {
            assertThat(max(null, null)).isNull()
            assertThat(max(null, dato2022)).isEqualTo(dato2022)
            assertThat(max(dato2022, null)).isEqualTo(dato2022)
            assertThat(max(dato2022, dato2022)).isEqualTo(dato2022)
            assertThat(max(dato2022, dato2023)).isEqualTo(dato2023)
            assertThat(max(dato2023, dato2022)).isEqualTo(dato2023)
        }
    }
}
