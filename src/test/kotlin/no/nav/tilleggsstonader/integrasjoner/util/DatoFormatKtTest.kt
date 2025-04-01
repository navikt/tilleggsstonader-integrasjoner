package no.nav.tilleggsstonader.integrasjoner.util

import norskDatoTekstligMåned
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class DatoFormatKtTest {
    @Test
    fun `skal formattere dato med norsk måned`() {
        assertThat(LocalDate.of(2024, 12, 12).norskDatoTekstligMåned()).isEqualTo("12. desember 2024")
    }
}
